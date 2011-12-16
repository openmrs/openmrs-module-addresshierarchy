package org.openmrs.module.addresshierarchy.service;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.PersonAddress;
import org.openmrs.api.context.Context;
import org.openmrs.module.ModuleFactory;
import org.openmrs.module.addresshierarchy.AddressField;
import org.openmrs.module.addresshierarchy.AddressHierarchyConstants;
import org.openmrs.module.addresshierarchy.AddressHierarchyEntry;
import org.openmrs.module.addresshierarchy.AddressHierarchyLevel;
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
	
	private Map<String,String> fullAddressCache;
	
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
			possibleEntries.add(getChildAddressHierarchyEntryByName(null, topLevelValue));
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
		
		// do a union of the results from the higher and lower level tests
		if (results.retainAll(possibleEntries));
		
		return results;
	}
	
	@Transactional(readOnly = true)
	public List<String> getPossibleFullAddresses(String searchString) {
		
		// if search string is empty or null, just return empty list
		if (StringUtils.isBlank(searchString)) {
			return new ArrayList<String>();
		}
		
		// initialize the cache (if necessary)
		initializeFullAddressCache();
		
		// first determine if we are going to do phonetic processing
		String phoneticProcessor = fetchPhoneticProcessor();
		Method encodeStringMethod = fetchEncodeStringMethod();
		
		List<String> matchingKeys = new ArrayList<String>();
		
		// first remove all characters that are not alphanumerics or whitespace
		// (more specifically, this pattern matches sets of 1 or more characters that are both non-word (\W) and non-whitespace (\S))
		searchString = Pattern.compile("[\\W&&\\S]+").matcher(searchString).replaceAll("");
				
		// split the search string into words
		String [] words = searchString.split(" ");
		
		// another sanity check; return an empty string if nothing to search on
		if (words.length == 0 || StringUtils.isBlank(words[0])) {
			return new ArrayList<String>();
		}
		
		// find all addresses in the full address cache that contain the first word in the search string
		Pattern p = Pattern.compile(Pattern.quote(encodeString(encodeStringMethod, words[0], phoneticProcessor)), Pattern.CASE_INSENSITIVE);
		for (String address : this.fullAddressCache.keySet()) {
			if (p.matcher(address).find()) {
				matchingKeys.add(address);
			}
		}
		
		// now go through and remove from the results list any addresses that don't contain the other words in the search string
		if (words.length > 1) {
			for (String word : Arrays.copyOfRange(words, 1, words.length)) {
				Iterator<String> i = matchingKeys.iterator();
				
				p = Pattern.compile(Pattern.quote(encodeString(encodeStringMethod, word, phoneticProcessor)), Pattern.CASE_INSENSITIVE);
				while (i.hasNext()) {
					if (!p.matcher(i.next()).find()) {
						i.remove();
					}
				}
			}
		}
 		
		List<String> results = new ArrayList<String>();
		
		// the results are the values for the matching keys
		for (String key : matchingKeys) {
			results.add(fullAddressCache.get(key));
		}
		
		return results;
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
		return dao.getAddressHierarchyEntriesByLevel(level);
	}
	
	@Transactional(readOnly = true)
	public List<AddressHierarchyEntry> getAddressHierarchyEntriesByLevelAndName(AddressHierarchyLevel level, String name) {
		return dao.getAddressHierarchyEntriesByLevelAndName(level, name);
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
		if (this.fullAddressCache == null || this.fullAddressCache.size() == 0) {
				
			this.fullAddressCache = new HashMap<String,String>();
		 
			// first determine if we are going to do phonetic processing
			String phoneticProcessor = fetchPhoneticProcessor();
			Method encodeStringMethod = fetchEncodeStringMethod();
			 
			List<AddressHierarchyLevel> levels = getOrderedAddressHierarchyLevels(true,false);
			AddressHierarchyLevel bottomLevel = levels.get(levels.size() - 1);
			
			// go through all the entries at the bottom level of the hierarchy
			for (AddressHierarchyEntry bottomLevelEntry : getAddressHierarchyEntriesByLevel(bottomLevel)) {	
				StringBuilder key = new StringBuilder();
				StringBuilder value = new StringBuilder();
				
				// set the key to the encoded name of the entry, and the value to the actual name
				key.append(encodeString(encodeStringMethod, bottomLevelEntry.getName(), phoneticProcessor));
				value.append(bottomLevelEntry.getName());
				
				AddressHierarchyEntry entry = bottomLevelEntry;
				
				// follow back up the tree to the top level and concatenate the names to create the full address string
				while (entry.getParent() != null) {
					entry = entry.getParent();		
					key.insert(0, encodeString(encodeStringMethod, entry.getName(), phoneticProcessor) + "|");		
					value.insert(0, entry.getName() + "|");	
				}
				
				this.fullAddressCache.put(key.toString(), value.toString());
			}
		}	
	}
	

	// utility method used to retrieve name of soundex processor from a global property and then determine if it is valid or not
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
	 
	// utility method that retrieves the encodeString method from the Name phonetics module
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
	 
	// utility method to call the encodeString method via reflection
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
