package org.openmrs.module.addresshierarchy.service;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.PersonAddress;
import org.openmrs.api.context.Context;
import org.openmrs.module.ModuleFactory;
import org.openmrs.module.addresshierarchy.AddressField;
import org.openmrs.module.addresshierarchy.AddressHierarchyConstants;
import org.openmrs.module.addresshierarchy.AddressHierarchyEntry;
import org.openmrs.module.addresshierarchy.AddressHierarchyLevel;
import org.openmrs.module.addresshierarchy.AddressToEntryMap;
import org.openmrs.module.addresshierarchy.db.AddressHierarchyDAO;
import org.openmrs.module.addresshierarchy.exception.AddressHierarchyModuleException;
import org.openmrs.module.addresshierarchy.util.AddressHierarchyUtil;
import org.springframework.transaction.annotation.Transactional;

/**
 * The Class AddressHierarchyServiceImpl default implementation of AddressHierarchyService.
 */
public class AddressHierarchyServiceImpl implements AddressHierarchyService {
	
	protected static final Log log = LogFactory.getLog(AddressHierarchyServiceImpl.class);
	
	private AddressHierarchyDAO dao;
	
	private Map<String,List<String>> fullAddressCache;
	private Boolean fullAddressCacheInitialized = false;
	
	public void setAddressHierarchyDAO(AddressHierarchyDAO dao) {
		this.dao = dao;
	}
	
	@Transactional(readOnly = true)
	public List<String> getPossibleAddressValues(PersonAddress address, String fieldName) {	
		
		AddressField field = AddressField.getByName(fieldName);
	
		if (field == null) {
			throw new AddressHierarchyModuleException(fieldName + " is not the name of a valid address field");
		}
		
		return getPossibleAddressValues(address, field);
	}
	
	@Transactional(readOnly = true)
	public List<String> getPossibleAddressValues(Map<String,String> addressMap, String fieldName) {		
		return getPossibleAddressValues(AddressHierarchyUtil.convertAddressMapToPersonAddress(addressMap),fieldName);
	}
	
	@Transactional(readOnly = true)
	public List<String> getPossibleAddressValues(PersonAddress address, AddressField field) {	
		
		Map<String,String> possibleAddressValues = new HashMap<String,String>();
		AddressHierarchyLevel targetLevel = null;
		
		// iterate through the ordered levels until we reach the level associated with the specified fieldName
		for (AddressHierarchyLevel level : getOrderedAddressHierarchyLevels(false)) {
			if (level.getAddressField() != null && level.getAddressField().equals(field)) {
				targetLevel = level;
				break;
			}
		}
		
		if (targetLevel == null) {
			log.error("Address field " + field + " is not mapped to address hierarchy level.");
			return null;
		}
		
		// calls getPossibleAddressHierarchyEntries(PersonAddress, AddressHierarchLevel) to perform the actual search
		List<AddressHierarchyEntry> entries = getPossibleAddressHierarchyEntries(address, targetLevel);
		
		// note that by convention entries should not be null, so we don't test for null here
		
		// take the entries returns and convert them into a list of *unique* names (using case-insensitive comparison)
		// we use a map here to make this process more efficient
		for (AddressHierarchyEntry entry : entries) {
			// see if there is already key for this entry name (converted to lower-case)
			if(!possibleAddressValues.containsKey(entry.getName().toLowerCase())) {
				// if not, add the key/value pair for this entry name, where the value equals the entry name,
				// and the key is the entry name converted to lower-case
				possibleAddressValues.put(entry.getName().toLowerCase(), entry.getName());
			}
		}
		
		List<String> results = new ArrayList<String>();
		results.addAll(possibleAddressValues.values());
		return results;
	}
	
	@Transactional(readOnly = true)
	public List<AddressHierarchyEntry> getPossibleAddressHierarchyEntries(PersonAddress address, AddressHierarchyLevel targetLevel) {
		
		// split the levels into levels before and after the level associated with the field name
		Boolean reachedFieldLevel = false;
		List<AddressHierarchyLevel> higherLevels = new ArrayList<AddressHierarchyLevel>();
		List<AddressHierarchyLevel> lowerLevels = new ArrayList<AddressHierarchyLevel>();
		
		for (AddressHierarchyLevel level : getOrderedAddressHierarchyLevels()) {
			if (reachedFieldLevel) {
				lowerLevels.add(level);
			}
			else {
				higherLevels.add(level);
			}
			if (level.equals(targetLevel)) {
				lowerLevels.add(level);  // we want the target level in both the higher and lower level lists
				reachedFieldLevel = true;
			}
		}
		
		if (!reachedFieldLevel) {
			// if we haven't found an address hierarchy level associated with this field, then we certainly aren't going to be
			// able to find a list of possible values
			// return an empty set here, not null, because null is the default method in core if not overridden
			return new ArrayList<AddressHierarchyEntry>();
		}
		
		List<AddressHierarchyEntry> possibleEntries = new ArrayList<AddressHierarchyEntry>();
		
		// first handle the top level
		AddressHierarchyLevel topLevel= higherLevels.remove(0);
		String topLevelValue = topLevel.getAddressField() != null ? AddressHierarchyUtil.getAddressFieldValue(address, topLevel.getAddressField()) : null;
		
		// if we have a top level value, find the top-level entry that matches that value
		if (StringUtils.isNotBlank(topLevelValue)) {
			AddressHierarchyEntry entry = getChildAddressHierarchyEntryByName(null, topLevelValue);	
			if (entry != null) {
				possibleEntries.add(entry);
			}
		}
		// otherwise, get all the entries at the top level
		else {
			possibleEntries.addAll(getAddressHierarchyEntriesAtTopLevel());
		}
		
		// now go through all the other levels above the level we are looking for
		for (AddressHierarchyLevel level : higherLevels) {
			List<AddressHierarchyEntry> possibleEntriesAtNextLevel = new ArrayList<AddressHierarchyEntry>();
			
			// find the value of the address field at the level we are dealing with
			String levelValue = level.getAddressField() != null ? AddressHierarchyUtil.getAddressFieldValue(address, level.getAddressField()) : null;
			
			// loop through all the possible entries
			for (AddressHierarchyEntry entry : possibleEntries) {
				// if a value has been specified, we only want child entries that match that value
				if(StringUtils.isNotBlank(levelValue)) {
					AddressHierarchyEntry childEntry = getChildAddressHierarchyEntryByName(entry, levelValue);
					if (childEntry != null) {
						possibleEntriesAtNextLevel.add(childEntry);
					}
				}
				// otherwise, we need to add all children of the possible entry
				else {
					possibleEntriesAtNextLevel.addAll(getChildAddressHierarchyEntries(entry));
				}
			}
			// now continue the loop and move on to the next level
			possibleEntries = possibleEntriesAtNextLevel;
		}
		
		// assign the possible entry to results array
		List<AddressHierarchyEntry> results = possibleEntries;
	
		// now we need to handle the any person address field values that may have been specified *below* the field
		// we are looking for in the hierarchy
		
		// we need to loop through the results in reverse and find any fields that have values
		Collections.reverse(lowerLevels);
		Iterator<AddressHierarchyLevel> i = lowerLevels.iterator();
		
		possibleEntries = null;
		
		while (i.hasNext()) {
			AddressHierarchyLevel level = i.next();
			
			String levelValue = level.getAddressField() != null ? AddressHierarchyUtil.getAddressFieldValue(address, level.getAddressField()) : null;
			
			// we are looking for the first level with a value
			if (StringUtils.isNotBlank(levelValue)) {
				possibleEntries = getAddressHierarchyEntriesByLevelAndName(level, levelValue);		
				break;
			}
		}
		
		// if we haven't found any possible lower level entries, then we can just return the possibleEntries we calculated by higher level
		if (possibleEntries == null) {
			return results;
		}
		
		// now that we've go something to start with, we need to work our way back up the tree 
		while (i.hasNext()) {
			
			AddressHierarchyLevel level = i.next();
			
			String levelValue = level.getAddressField() != null ? AddressHierarchyUtil.getAddressFieldValue(address, level.getAddressField()) : null;
			
			List<AddressHierarchyEntry> possibleEntriesAtNextLevel = new ArrayList<AddressHierarchyEntry>();
			
			for (AddressHierarchyEntry entry : possibleEntries) {
				AddressHierarchyEntry parentEntry = entry.getParent();
				
				if (parentEntry != null) {
					possibleEntriesAtNextLevel.add(parentEntry);
				}	
			}
			
			// if we have a value restriction here, remove any entries that don't fit the restriction
			if (StringUtils.isNotBlank(levelValue)) {
				Iterator<AddressHierarchyEntry> j = possibleEntriesAtNextLevel.iterator();
				while (j.hasNext()) {
					AddressHierarchyEntry entry = j.next();
					if (!entry.getName().equalsIgnoreCase(levelValue)) {
						j.remove();
					}
				}
			}
			
			possibleEntries = possibleEntriesAtNextLevel;
		}
		
		// do an intersection of the results from the higher and lower level tests
		if (results.retainAll(possibleEntries));
		
		return results;
	}
	
	@Transactional(readOnly = true)
	public List<String> getPossibleFullAddresses(AddressHierarchyEntry entry) {
		// if entry is null, just return empty list
		if (entry == null) {
			return new ArrayList<String>();
		}
		
		// use the helper method recursively to create the list of possible addresses
		List<String> addresses = new  ArrayList<String>();
		generatePossibleFullAddressesHelper(entry, addresses);
		return addresses;
    }
	
	// helper method for getPossibleFullAddresses(AddressHierarchyEntry entry)
	private void generatePossibleFullAddressesHelper(AddressHierarchyEntry entry, List<String> addresses)  {
		
		List<AddressHierarchyEntry> entries = getChildAddressHierarchyEntries(entry);
		
		// if this is leaf node, then create the full address and add it to the list of addresses to return
		if (entries == null || entries.isEmpty()) {
			StringBuilder address = new StringBuilder();
			address.append(entry.getName());
			
			AddressHierarchyEntry tempEntry = entry;
			// follow back up the tree to the top level and concatenate the names to create the full address string
			while (tempEntry.getParent() != null) {
				tempEntry = tempEntry.getParent();		
				address.insert(0, tempEntry.getName() + "|");	
			}
			
			// add the string to the results
			addresses.add(address.toString());
		}
		// if not a leaf node, process it's children recursively
		else {
			for (AddressHierarchyEntry currentEntry : entries) {
				generatePossibleFullAddressesHelper(currentEntry, addresses);
			}
		}
	}
	
	@Transactional(readOnly = true)
	public Set<String> searchAddresses(String searchString) {
		return searchAddresses(searchString, null);
	}
	
	@Transactional(readOnly = true)
	public Set<String> searchAddresses(String searchString, AddressHierarchyLevel level) {
		
		// if search string is empty or null, just return empty list
		if (StringUtils.isBlank(searchString)) {
			return new HashSet<String>();
		}
		
		// initialize the cache (if necessary)
		initializeFullAddressCache();
		
		// first determine if we are going to do phonetic processing
		String phoneticProcessor = fetchPhoneticProcessor();
		Method encodeStringMethod = fetchEncodeStringMethod();
		
		// if we have been passed an address hierarchy level, figure out the index of that
		// level so that we can split the string appropriately for the search
		Integer levelIndex = null;
		List<AddressHierarchyLevel> levels = getOrderedAddressHierarchyLevels(true);
		if (level != null) {
			for (int i = 0; i < levels.size() ; i++) {
				if (levels.get(i).equals(level)) {
					levelIndex = i;
					break;
				}
			}
		}
		
		// remove all characters that are not alphanumerics or whitespace
		// (more specifically, this pattern matches sets of 1 or more characters that are both non-word (\W) and non-whitespace (\S))
		searchString = Pattern.compile("[\\W&&\\S]+").matcher(searchString).replaceAll("");
				
		// split the search string into words
		String [] words = searchString.split(" ");
		
		// another sanity check; return an empty string if nothing to search on
		if (words.length == 0 || StringUtils.isBlank(words[0])) {
			return new HashSet<String>();
		}
		
		List<String> matchingKeys = new ArrayList<String>();
		
		// find all addresses in the full address cache that contain the first word in the search string
		// (optionally restricting the search to a single address level in the address cache)
		Pattern p = Pattern.compile(Pattern.quote(encodeString(encodeStringMethod, words[0], phoneticProcessor)), Pattern.CASE_INSENSITIVE);
		for (String address : this.fullAddressCache.keySet()) {
			if (p.matcher(retrieveSpecifiedLevel(address, levelIndex)).find()) {
				matchingKeys.add(address);
			}
		}
		
		// now go through and remove from the results list any addresses that don't contain the other words in the search string
		if (words.length > 1) {
			for (String word : Arrays.copyOfRange(words, 1, words.length)) {
				Iterator<String> i = matchingKeys.iterator();
				
				p = Pattern.compile(Pattern.quote(encodeString(encodeStringMethod, word, phoneticProcessor)), Pattern.CASE_INSENSITIVE);
				while (i.hasNext()) {
					String address = i.next();
					if (!p.matcher(retrieveSpecifiedLevel(address, levelIndex)).find()) {
						i.remove();
					}
				}
			}
		}
 		
		Set<String> results = new HashSet<String>();
		
		// the results are the values for the matching keys
		for (String key : matchingKeys) {
			for (String address : fullAddressCache.get(key)) {
				results.add(retrieveSpecifiedLevel(address, levelIndex));
			}
		}
		
		return results;
	}
	
	// utility method used by searchAddress(String, AddressHierarchyLevel)
	private String retrieveSpecifiedLevel(String address, Integer levelIndex) {
		
		// if no level is specified, return the entire address
		if (levelIndex == null) {
			return address;
		}
		
		String subAddresses[] = address.split("\\|");
		
		// if this level isn't in this full address, return blank string
		if (levelIndex >= subAddresses.length) {
			return ""; 
		}
 		
		return subAddresses[levelIndex];
	}
	
	@Transactional(readOnly = true)
	public Integer getAddressHierarchyEntryCount() {
		return dao.getAddressHierarchyEntryCount();
	}
	
	@Transactional(readOnly = true)
	public Integer getAddressHierarchyEntryCountByLevel(AddressHierarchyLevel level) {
		return dao.getAddressHierarchyEntryCountByLevel(level);
	}
	
	@Transactional(readOnly = true)
	public AddressHierarchyEntry getAddressHierarchyEntry(int addressHierarchyId) {
		return dao.getAddressHierarchyEntry(addressHierarchyId);
	}
	
	@Transactional(readOnly = true)
	public AddressHierarchyEntry getAddressHierarchyEntryByUserGenId(String userGeneratedId) {
		return dao.getAddressHierarchyEntryByUserGenId(userGeneratedId);
	}
	
	@Transactional(readOnly = true)
	public List<AddressHierarchyEntry> getAddressHierarchyEntriesByLevel(AddressHierarchyLevel level) {
		if (level == null) {
			return null;
		}
		
		return dao.getAddressHierarchyEntriesByLevel(level);
	}
	
	@Transactional(readOnly = true)
	public List<AddressHierarchyEntry> getAddressHierarchyEntriesByLevelAndName(AddressHierarchyLevel level, String name) {
		if (level == null || StringUtils.isBlank(name)) {
			return null;
		}
		
		return dao.getAddressHierarchyEntriesByLevelAndName(level, name);
	}
	
	@Transactional(readOnly = true)
	public List<AddressHierarchyEntry> getAddressHierarchyEntriesByLevelAndNameAndParent(AddressHierarchyLevel level, String name, AddressHierarchyEntry parent) {
		if (level == null || StringUtils.isBlank(name) || parent == null) {
			return null;
		}
		
	    return dao.getAddressHierarchyEntriesByLevelAndNameAndParent(level, name, parent);
    }
	
	
	@Transactional(readOnly = true)
	public List<AddressHierarchyEntry> getAddressHierarchyEntriesAtTopLevel() {
		return getAddressHierarchyEntriesByLevel(getTopAddressHierarchyLevel());
	}
	
	@Transactional(readOnly = true)
	public List<AddressHierarchyEntry> getChildAddressHierarchyEntries(AddressHierarchyEntry entry) {
		if (entry != null) {
			return dao.getChildAddressHierarchyEntries(entry);
		}
		else {
			return getAddressHierarchyEntriesAtTopLevel();
		}
	}
	
	@Transactional(readOnly = true)
	public List<AddressHierarchyEntry> getChildAddressHierarchyEntries(Integer entryId) {
		AddressHierarchyEntry entry = getAddressHierarchyEntry(entryId);
		
		if (entry == null) {
			throw new AddressHierarchyModuleException("Invalid Address Hierarchy Entry Id " + entryId);
		}
		
		return getChildAddressHierarchyEntries(entry);
	}
	
	@Transactional(readOnly = true)
	public AddressHierarchyEntry getChildAddressHierarchyEntryByName(AddressHierarchyEntry entry, String childName) {
		if (entry != null) {
			return dao.getChildAddressHierarchyEntryByName(entry, childName);
		}
		else {
			List<AddressHierarchyEntry> entries = dao.getAddressHierarchyEntriesByLevelAndName(getTopAddressHierarchyLevel(), childName);
			if (entries.size() == 0) {
				return null;
			}
			else if (entries.size() == 1) {
				return entries.get(0);
			}
			else {
				throw new AddressHierarchyModuleException("Two or more top-level entries have the same name");
			}
		}
	} 
	
	@Transactional
	public void saveAddressHierarchyEntry(AddressHierarchyEntry entry) {
		dao.saveAddressHierarchyEntry(entry);
		resetFullAddressCache();
	}
	
	@Transactional
	public void saveAddressHierarchyEntries(List<AddressHierarchyEntry> entries) {
		for (AddressHierarchyEntry entry : entries) {
			dao.saveAddressHierarchyEntry(entry);
		}
		resetFullAddressCache();
	}
	
	@Transactional
	public void deleteAllAddressHierarchyEntries() {
		dao.deleteAllAddressHierarchyEntries();
		resetFullAddressCache();
	}
	
	@Transactional(readOnly = true)
	public List<AddressHierarchyLevel> getOrderedAddressHierarchyLevels() {
		return getOrderedAddressHierarchyLevels(true, true);
	}
	
	@Transactional(readOnly = true)
	public List<AddressHierarchyLevel> getOrderedAddressHierarchyLevels(Boolean includeUnmapped) {	
		return getOrderedAddressHierarchyLevels(includeUnmapped, true);
	}
	
	@Transactional(readOnly = true)
	public List<AddressHierarchyLevel> getOrderedAddressHierarchyLevels(Boolean includeUnmapped, Boolean includeEmptyLevels) {
		List<AddressHierarchyLevel> levels = new ArrayList<AddressHierarchyLevel>();
		
		// first, get the top level level
		AddressHierarchyLevel level = getTopAddressHierarchyLevel();
		
		if (level != null) {
			// add the top level to this list
			if ((includeUnmapped == true || level.getAddressField() != null) 
					&& (includeEmptyLevels == true ||  getAddressHierarchyEntryCountByLevel(level) > 0)) {
				levels.add(level);
			}
				
			// now fetch the children in order
			while (getChildAddressHierarchyLevel(level) != null) {
				level = getChildAddressHierarchyLevel(level);
				if ((includeUnmapped == true || level.getAddressField() != null) 
						&& (includeEmptyLevels == true ||  getAddressHierarchyEntryCountByLevel(level) > 0)) {	
					levels.add(level);
				}
			}
		}
		
		return levels;
	}
	
	@Transactional(readOnly = true)
	public List<AddressHierarchyLevel> getAddressHierarchyLevels() {
		return dao.getAddressHierarchyLevels();
	}
	
	@Transactional(readOnly = true)
	public Integer getAddressHierarchyLevelsCount() {
		List<AddressHierarchyLevel> levels = getAddressHierarchyLevels();
		return levels != null ? levels.size() : 0;
	}
	
	@Transactional(readOnly = true)
	public AddressHierarchyLevel getTopAddressHierarchyLevel() {
		return dao.getTopAddressHierarchyLevel();
	}
	
	@Transactional(readOnly = true)
    public AddressHierarchyLevel getBottomAddressHierarchyLevel() {
		
		// get the ordered list
		List<AddressHierarchyLevel> levels = getOrderedAddressHierarchyLevels();
		
		// return the last member in the list
		if (levels != null && levels.size() > 0) {
			return levels.get(levels.size() - 1);
		}
		else {
			return null;
		}
    }
	
	@Transactional(readOnly = true)
	public AddressHierarchyLevel getAddressHierarchyLevel(Integer levelId) {
		return dao.getAddressHierarchyLevel(levelId);
	}
	
	@Transactional(readOnly = true)
	public AddressHierarchyLevel getAddressHierarchyLevelByAddressField(AddressField addressField) {
		if (addressField == null) {
			return null;
		}
		
		for (AddressHierarchyLevel level : getAddressHierarchyLevels()) {
			if (level.getAddressField() != null && level.getAddressField().equals(addressField)) {
				return level;
			}
		}
		// if we have gotten here, no match, return null
		return null;
	}
	
	@Transactional(readOnly = true)
    public AddressHierarchyLevel getChildAddressHierarchyLevel(AddressHierarchyLevel level) {
	    return dao.getAddressHierarchyLevelByParent(level);
    }
	
	@Transactional
	public AddressHierarchyLevel addAddressHierarchyLevel() {
		AddressHierarchyLevel newLevel = new AddressHierarchyLevel();
		newLevel.setParent(getBottomAddressHierarchyLevel());
		// need to call the service method through the context to take care of AOP
		Context.getService(AddressHierarchyService.class).saveAddressHierarchyLevel(newLevel);
		return newLevel;
	}
	
	@Transactional
	public void saveAddressHierarchyLevel(AddressHierarchyLevel level) {
		dao.saveAddressHierarchyLevel(level);
	}
	
	@Transactional
    public void deleteAddressHierarchyLevel(AddressHierarchyLevel level) {
    	dao.deleteAddressHierarchyLevel(level);  
    }
	
	@Transactional
	public void setAddressHierarchyLevelParents() {
		// iterate through the levels
		for (AddressHierarchyLevel level : getAddressHierarchyLevels()) {
			
			if (getAddressHierarchyEntryCountByLevel(level) > 0) {
				// get an entry for this level
				AddressHierarchyEntry entry = getAddressHierarchyEntriesByLevel(level).get(0);
				// if needed, set the parent for this level based on the level of the parent of this entry
				if (entry.getParent() != null && entry.getParent().getLevel() != level.getParent()) {
					level.setParent(entry.getParent().getLevel());
					// need to call the service method through the context to take care of AOP
					Context.getService(AddressHierarchyService.class).saveAddressHierarchyLevel(level);
				}
			}
			// if is level has no entries and no parents, delete it
			else if (level.getParent() == null){
				// need to call the service method through the context to take care of AOP
				Context.getService(AddressHierarchyService.class).deleteAddressHierarchyLevel(level);
			}
		}
	}

	@Transactional(readOnly = true)
	public void resetFullAddressCache() {
		this.fullAddressCache = null;
		this.fullAddressCacheInitialized = false;
	}
	
	@Transactional(readOnly = true)
	public AddressToEntryMap getAddressToEntryMap(Integer id) {
		if (id == null) {
			return null;
		}
		
	    return dao.getAddressToEntryMap(id);
    }

	@Transactional(readOnly = true)
	public List<AddressToEntryMap> getAddressToEntryMapsByPersonAddress(PersonAddress address) {
		if (address == null) {
			return null;
		}
		
	    return dao.getAddressToEntryMapByPersonAddress(address);
    }
	
	@Transactional
	public void saveAddressToEntryMap(AddressToEntryMap addressToEntry) {
		if (addressToEntry == null) {
			return;
		}
		
	    dao.saveAddressToEntryMap(addressToEntry);
    }

	@Transactional
	public void deleteAddressToEntryMap(AddressToEntryMap addressToEntryMap) {
		if (addressToEntryMap == null) {
			return;
		}
		
		dao.deleteAddressToEntryMap(addressToEntryMap);
	}

	@Transactional
	public void deleteAddressToEntryMapsByPersonAddress(PersonAddress address) {
		if (address == null) {
			return;
		}
		
	   	List<AddressToEntryMap> maps = getAddressToEntryMapsByPersonAddress(address);
	   	
	   	if (maps != null && maps.size() > 0) {
	   		for (AddressToEntryMap map : maps) {
	   			dao.deleteAddressToEntryMap(map);
	   		}
	   	}
    }

	@Transactional
	public void updateAddressToEntryMapsForPersonAddress(PersonAddress address) {
		log.info("Updating AddressToEntryMaps for PersonAddress " + address);
		if (address == null) {
			return;
		}
		
	  	// first delete any existing maps for this person address
		List<AddressToEntryMap> maps = getAddressToEntryMapsByPersonAddress(address);
		if (maps != null && !maps.isEmpty()) {
			for (AddressToEntryMap map : maps) {
				deleteAddressToEntryMap(map);
			}
		}
		
		// now create and save the new maps if the Person Address has not been voided
    	if (!address.isVoided()) {
	    	maps = createAddressToEntryMapsForPersonAddress(address);
	    	if (maps != null && !maps.isEmpty()) {
	    		for (AddressToEntryMap map : maps) {
	    			Context.getService(AddressHierarchyService.class).saveAddressToEntryMap(map);
	    		}
	    	}
    	}
    }

	@Transactional
	public void updateAddressToEntryMapsForPerson(Person person) {
		log.info("Updating AddressToEntryMaps for person " + person);
		if (person == null) {
			return;
		}
		
		Set<PersonAddress> addresses = person.getAddresses();
		if (addresses != null && !addresses.isEmpty()) {
			for (PersonAddress address : addresses) {
				if (address != null && !address.isVoided()) {
					updateAddressToEntryMapsForPersonAddress(address);
				}
			}
		}
	    
    }

	@Transactional
	public List<Patient> findAllPatientsWithDateChangedAfter(Date date) {
		if (date == null) {
			return null;
		}
		
		return dao.findAllPatientsWithDateChangedAfter(date);
	}
	
	/**
	 * Utility methods
	 */
	
	/**
	 * Builds a key/value map of pipe-delimited strings that represents all the possible full addresses,
	 * and stores this in a local cache for use by the getPossibleFullAddresses(String) method
	 * 
	 * The map values are full addresses represented as a pipe-delimited string of address hierarchy entry names,
	 * ordered from the entry at the highest level to the entry at the lowest level in the tree.
	 * For example, the full address for the Beacon Hill neighborhood in the city of Boston might be:
	 * "United States|Massachusetts|Suffolk County|Boston|Beacon Hill"
	 * 
	 * In the standard implemention, the keys are the same as the values.  However, if the Name Phonetics module
	 * has been installed, and the addresshierarchy.soundexProcessor global property has been configured, the keys
	 * will be the same pipe-delimited string, but with each entry name transformed via the specified soundex processor
	 * 
	 * The getPossibleFullAddresses method compares the input string against the keys, and returns the values of any matches
	 * 
	 * Need to make sure we synchronize to avoid having multiple threads
	 * trying to initialize it at the same time, or one using it before it is initialized
	 * (Note that the one thing this won't prevent against is it being re-initialized while another
	 * thread is accessing it)
	 *  
	 */
	
	 synchronized private void initializeFullAddressCache() {
		
		// only initialize if necessary
		if (this.fullAddressCacheInitialized == false || this.fullAddressCache == null && this.fullAddressCache.isEmpty()) {
				
			this.fullAddressCache = new HashMap<String,List<String>>();
		 			 
			for (AddressHierarchyEntry entry : getAddressHierarchyEntriesByLevel(getTopAddressHierarchyLevel())) {	
				initializeFullAddressCacheHelper(entry);
			}
			
			this.fullAddressCacheInitialized = true;
		}	
	}
	
	private void initializeFullAddressCacheHelper(AddressHierarchyEntry entry) {
		
		List<AddressHierarchyEntry> entries = getChildAddressHierarchyEntries(entry);
		
		// if this is leaf node, then create the full address and add it to the list of addresses to return
		if (entries == null || entries.isEmpty()) {
		
			// first determine if we are going to do phonetic processing
			String phoneticProcessor = fetchPhoneticProcessor();
			Method encodeStringMethod = fetchEncodeStringMethod();
			
			StringBuilder key = new StringBuilder();
			StringBuilder value = new StringBuilder();
			
			// set the key to the encoded name of the entry, and the value to the actual name
			key.append(encodeString(encodeStringMethod, entry.getName(), phoneticProcessor));
			value.append(entry.getName());
			
			AddressHierarchyEntry tempEntry = entry;
			
			// follow back up the tree to the top level and concatenate the names to create the full address string
			while (tempEntry.getParent() != null) {
				tempEntry = tempEntry.getParent();		
				key.insert(0, encodeString(encodeStringMethod, tempEntry.getName(), phoneticProcessor) + "|");		
				value.insert(0, tempEntry.getName() + "|");	
			}
			
			// add it to the cache
			if (!this.fullAddressCache.containsKey(key.toString())) {
				this.fullAddressCache.put(key.toString(), new ArrayList<String>());
			}
			this.fullAddressCache.get(key.toString()).add(value.toString());
		}
		// if not a leaf node, process it's children recursively
		else {
			for (AddressHierarchyEntry currentEntry : entries) {
				initializeFullAddressCacheHelper(currentEntry);
			}
		}
	}
	 

	/**
	 * Utility method used to retrieve name of soundex processor from a global property and then determine if it is valid or not
	 */
	private String fetchPhoneticProcessor() {
		String phoneticProcessor = null;
		
		// only relevant if we have the name phonetics module running
		if (ModuleFactory.getStartedModulesMap().containsKey("namephonetics")) {
			phoneticProcessor = Context.getAdministrationService().getGlobalProperty(AddressHierarchyConstants.GLOBAL_PROP_SOUNDEX_PROCESSER);
			
			// if a soundex algorithm has been specified, make sure it is valid
			if (StringUtils.isNotBlank(phoneticProcessor)) {
				
				// call the name phonetics service by reflection and see if the processor has been registered
				try {
					Class<?> namePhoneticsServiceClass = Context.loadClass("org.openmrs.module.namephonetics.NamePhoneticsService");
					Object namePhonetics = Context.getService(namePhoneticsServiceClass);
			        Method getProcessorClassName = namePhoneticsServiceClass.getMethod("getProcessorClassName", String.class);
			        
			        // log an error and clear out the processor name if we don't find it
			        if (getProcessorClassName.invoke(namePhonetics, phoneticProcessor) == null) {
			        	log.error("No soundex processor found with name " + phoneticProcessor+" - will do standard string comparison");
			        	phoneticProcessor = null;
			        }
				}
				catch (Exception e) {
					log.error("Unable to access Name Phonetics service for address search.  Is the Name Phonetics module installed and up-to-date?", e);
					phoneticProcessor = null;
				} 
			}
		}
		return phoneticProcessor;
	}
	 
	/**
	 * Utility method that retrieves the encodeString method from the Name phonetics module
	 */
	private Method fetchEncodeStringMethod() {
		Method encodeStringMethod = null;
		
		if (ModuleFactory.getStartedModulesMap().containsKey("namephonetics")) {
			try {
				Class<?> namePhoneticsUtilClass = Context.loadClass("org.openmrs.module.namephonetics.NamePhoneticsUtil");
		        encodeStringMethod = namePhoneticsUtilClass.getMethod("encodeString", String.class, String.class);
			}
			catch (Exception e) {
				log.error("Unable to access Name Phonetics service for address search.  Is the Name Phonetics module installed and up-to-date?", e);
			} 
		}
		return encodeStringMethod;
	}
	 
	/**
	 * Utility method to call the encodeString method via reflection
	 */
	private String encodeString(Method encodeStringMethod, String stringToEncode, String phoneticProcessor) {
		
		// if the phonetics processor hasn't been set, or the string to encode is blank, just return original string
		if (StringUtils.isBlank(stringToEncode) || StringUtils.isBlank(phoneticProcessor) || encodeStringMethod == null) {
			return stringToEncode;
		}
		
		StringBuilder codedString = new StringBuilder();	

		// first remove all characters that are not alphanumerics or whitespace
		// (more specifically, this pattern matches sets of 1 or more characters that are both non-word (\W) and non-whitespace (\S))
		stringToEncode = Pattern.compile("[\\W&&\\S]+").matcher(stringToEncode).replaceAll("");
		
		// break the string to encode into words
		String [] words = stringToEncode.split(" ");
		
		// cycle through each word in the string, encode it, and then add it to the new coded string
		for (String word : words) {
			try {
	            codedString.append((String) encodeStringMethod.invoke(null, word, phoneticProcessor) + " ");
	        }
	        catch (Exception e) {
	        	// hopefully we will never get here, because problems will be caught earlier
	        	throw new AddressHierarchyModuleException("Unable to encode string", e);
	        }
		}
		
		// remove the trailing space
		codedString.deleteCharAt(codedString.length() - 1);
		
		return codedString.toString();
	}
	
	/**
	 * Utility method that tests an existing Person Address against the Address Hierarchy Entries for matches, and returns a list of AddressToEntryMaps
	 * for any matches it finds; in essence, this method returns a list of records that map the passed person address to all address hierarchy
	 * entries it matches
	 */
	private List<AddressToEntryMap> createAddressToEntryMapsForPersonAddress(PersonAddress address) {
	
		List<AddressToEntryMap> results = new ArrayList<AddressToEntryMap>();
		
		// iterate through all the address hierarchy levels, and see if we can find a match at each level
		AddressHierarchyEntry parent = null;
			
		for (AddressHierarchyLevel level : getOrderedAddressHierarchyLevels(false, false)) {
			List<AddressHierarchyEntry> entries;
			
			// get all the entries at that level that are possible matches for the given name
			entries = getAddressHierarchyEntriesByLevelAndName(level, AddressHierarchyUtil.getAddressFieldValue(address, level.getAddressField()));

			if (entries != null && entries.size() > 0) {
				
				// make sure we remove any results that aren't descendants of the previous level matched
				if (parent != null) {
					Iterator<AddressHierarchyEntry> i = entries.iterator();
					while (i.hasNext()) {
						if (!(AddressHierarchyUtil.isDescendantOf(i.next(),parent))) {
							i.remove();
						}
					}
				}
				
				// we only want to create a new record if we have one and only one matches
				if (entries.size() == 1) {
					// create the new AddressToEntry record and add it to the results list
					results.add(new AddressToEntryMap(address, entries.get(0)));
					// set this entry as the parent for the next level search
					parent = entries.get(0);
				}
			}
		}
		
		return results;
	}
	
	/**
	 * Deprecated methods
	 */
	
	@Deprecated
	@Transactional(readOnly = true)
	public List<String> getPossibleFullAddresses(String searchString) {
	    return new ArrayList<String>(searchAddresses(searchString, null));  
    }
	
	/**
	 * The following methods are deprecated and just exist to provide backwards compatibility to
	 * Rwanda Address Hierarchy module
	 */
	
	@Deprecated
	@Transactional(readOnly = true)
	public List<AddressHierarchyEntry> getLeafNodes(AddressHierarchyEntry ah) {
		return dao.getLeafNodes(ah);
	}
	
	@Deprecated
	@Transactional
	public void associateCoordinates(AddressHierarchyEntry ah, double latitude, double longitude) {
		dao.associateCoordinates(ah, latitude, longitude);
	}
	
	@Deprecated
	@Transactional(readOnly = true)
	public List<AddressHierarchyEntry> getTopOfHierarchyList() {
		return getAddressHierarchyEntriesAtTopLevel();
	}

	
	@Deprecated
	@Transactional(readOnly = true)
	public List<Object[]> getLocationAddressBreakdown(int locationId) {
		return dao.getLocationAddressBreakdown(locationId);
	}
	
	@Deprecated
	@Transactional(readOnly = true)
	public List<Object[]> findUnstructuredAddresses(int page, int locationId) {
		return dao.findUnstructuredAddresses(page, locationId);
	}
	
	@Deprecated
	@Transactional(readOnly = true)
	public List<Object[]> getAllAddresses(int page) {
		return dao.getAllAddresses(page);
	}
	
	@Deprecated
	@Transactional
	public void initializeRwandaHierarchyTables() {
		dao.initializeRwandaHierarchyTables();
	}
	
	/**
	 * I've renamed the following methods to make them a little more clear, but kept the old method
	 * names for backwards compatibility
	 */
	
	@Deprecated
	@Transactional(readOnly = true)
	public int getAddressHierarchyCount() {
		return getAddressHierarchyEntryCount();
	}
	
	@Deprecated
	@Transactional(readOnly = true)
	public List<AddressHierarchyEntry> getNextComponent(Integer locationId) {
		return getChildAddressHierarchyEntries(locationId);
	}
	
	@Deprecated
	@Transactional
	public void saveAddressHierarchy(AddressHierarchyEntry ah) {
		saveAddressHierarchyEntry(ah);
	}
	
	@Deprecated
	@Transactional(readOnly = true)
	public AddressHierarchyEntry getLocationFromUserGenId(String userGeneratedId) {
		return getAddressHierarchyEntryByUserGenId(userGeneratedId);
	}
	
	@Deprecated
	@Transactional(readOnly = true)
	public AddressHierarchyEntry getAddressHierarchy(int addressHierarchyId) {
		return getAddressHierarchyEntry(addressHierarchyId);
	}
	
	@Deprecated
	@Transactional(readOnly = true)
	public AddressHierarchyLevel getHierarchyType(int levelId) {
		return getAddressHierarchyLevel(levelId);
	}

}
