package org.openmrs.module.addresshierarchy.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.PersonAddress;
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
	
	public void setAddressHierarchyDAO(AddressHierarchyDAO dao) {
		this.dao = dao;
	}
	
	@Transactional(readOnly = true)
	public List<String> getPossibleAddressValues(PersonAddress address, String fieldName) {	
		
		List<String> possibleAddressValues = new ArrayList<String>();
		
		List<AddressHierarchyEntry> entries = getPossibleAddressHierarchyEntries(address, fieldName);
		
		if (entries == null) {
			return null;
		}
		
		for (AddressHierarchyEntry entry : entries) {
			possibleAddressValues.add(entry.getName());
		}
		
		return possibleAddressValues;
	}
	
	public List<AddressHierarchyEntry> getPossibleAddressHierarchyEntries(PersonAddress address, String fieldName) {
		
		// TODO: what about cases where there is already a value for fieldname?  Right now we just ignore it
		
		// first off, split the levels into levels before and after the level associated with the field name
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
			if (level.getAddressField().getName().equals(fieldName)) {
				lowerLevels.add(level);  // we want the target level in both the higher and lower level lists
				reachedFieldLevel = true;
			}
		}
		
		if (!reachedFieldLevel) {
			// if we haven't found an address hierarchy level associated with this field, then we certainly aren't going to be
			// able to find a list of possible values
			return null;
		}
		
		List<AddressHierarchyEntry> possibleEntries = new ArrayList<AddressHierarchyEntry>();
		
		// first handle the top level
		AddressHierarchyLevel topLevel= higherLevels.remove(0);
		String topLevelValue = topLevel.getAddressField() != null ? AddressHierarchyUtil.getAddressFieldValue(address, topLevel.getAddressField().getName()) : null;
		
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
			String levelValue = level.getAddressField() != null ? AddressHierarchyUtil.getAddressFieldValue(address, level.getAddressField().getName()) : null;
			
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
			
			String levelValue = level.getAddressField() != null ? AddressHierarchyUtil.getAddressFieldValue(address, level.getAddressField().getName()) : null;
			
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
			
			String levelValue = level.getAddressField() != null ? AddressHierarchyUtil.getAddressFieldValue(address, level.getAddressField().getName()) : null;
			
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
					if (!entry.getName().equals(levelValue)) {
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
	public int getAddressHierarchyEntryCount() {
		return dao.getAddressHierarchyEntryCount();
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
		return getAddressHierarchyEntriesByLevel(level.getId());
	}
	
	@Transactional(readOnly = true)
	public List<AddressHierarchyEntry> getAddressHierarchyEntriesByLevel(Integer levelId) {
		return dao.getAddressHierarchyEntriesByLevel(levelId);
	}
	
	@Transactional(readOnly = true)
	public List<AddressHierarchyEntry> getAddressHierarchyEntriesByLevelAndName(AddressHierarchyLevel level, String name) {
		List<AddressHierarchyEntry> entries = getAddressHierarchyEntriesByLevel(level);
		List<AddressHierarchyEntry> results = new ArrayList<AddressHierarchyEntry>();
		
		if (entries != null) {
			for (AddressHierarchyEntry entry : entries) {
				if (entry.getName().equals(name)) {
					results.add(entry);
				}
			}
		}
		
		return results;
	}
	
	@Transactional(readOnly = true)
	public List<AddressHierarchyEntry> getAddressHierarchyEntriesAtTopLevel() {
		return getAddressHierarchyEntriesByLevel(getTopAddressHierarchyLevel());
	}
	
	@Transactional(readOnly = true)
	public List<AddressHierarchyEntry> getChildAddressHierarchyEntries(AddressHierarchyEntry entry) {
		return dao.getChildAddressHierarchyEntries(entry);
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
	public AddressHierarchyEntry getChildAddressHierarchyEntryByName(AddressHierarchyEntry entry, String name) {
		List<AddressHierarchyEntry> entries;
		
		if (entry != null) {
			entries = getChildAddressHierarchyEntries(entry);
		}
		else {
			// by definition, if no address hierarchy entry specified, operate on the top level
			entries = getAddressHierarchyEntriesByLevel(getTopAddressHierarchyLevel());
		}
		
		if (entries != null) {
			for (AddressHierarchyEntry e : entries) {
				if (e.getName().equals(name)) {
					return e;
				}
			}
		}
		return null;
	} 
	
	@Transactional
	public void saveAddressHierarchyEntry(AddressHierarchyEntry entry) {
		dao.saveAddressHierarchyEntry(entry);
	}
	
	@Transactional
	public void deleteAllAddressHierarchyEntries() {
		dao.deleteAllAddressHierarchyEntries();
	}
	
	@Transactional(readOnly = true)
	public AddressHierarchyEntry searchAddressHierarchy(String searchString) {
		
		// TODO: do i need move some of this to the DAO to speed up performance
		
		AddressHierarchyEntry entry = null;
		
		// iterate through all the names in the search string
		for (String name : searchString.split("\\|")) {
			
			entry = getChildAddressHierarchyEntryByName(entry, name);
				
			if (entry == null) {
				return null;
			}
		}
		
		return entry;
	}
	
	@Transactional(readOnly = true)
	public List<AddressHierarchyLevel> getOrderedAddressHierarchyLevels() {
		
		// TODO: integrate this with the new method we may create to make sure that the hierarchy is well-formed?
		
		List<AddressHierarchyLevel> levels = new ArrayList<AddressHierarchyLevel>();
		
		// first, get the top level level
		AddressHierarchyLevel topLevel = getTopAddressHierarchyLevel();
		
		if (topLevel != null) {
			// add the top level to this list
			levels.add(topLevel);
			
			// now fetch the children in order
			while (getChildAddressHierarchyLevel(levels.get(levels.size() - 1)) != null) {
				levels.add(getChildAddressHierarchyLevel(levels.get(levels.size() - 1)));
			}
		}
		
		// make sure we've reached all the levels this way
		if (levels.size() < getAddressHierarchyLevels().size()) {
			log.warn("Address Hierarchy Levels are not in strict hierarchical format. There may be orphaned or widowed levels.");
		}
		
		return levels;
	}
	
	@Transactional(readOnly = true)
	public List<AddressHierarchyLevel> getAddressHierarchyLevels() {
		return dao.getAddressHierarchyLevels();
	}
	
	@Transactional(readOnly = true)
	public int getAddressHierarchyLevelsCount() {
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
	public AddressHierarchyLevel getAddressHierarchyLevel(int levelId) {
		return dao.getAddressHierarchyLevel(levelId);
	}
	
	@Transactional(readOnly = true)
    public AddressHierarchyLevel getChildAddressHierarchyLevel(AddressHierarchyLevel level) {
	    return dao.getAddressHierarchyLevelByParent(level);
    }
	
	@Transactional
	public void saveAddressHierarchyLevel(AddressHierarchyLevel level) {
		dao.saveAddressHierarchyLevel(level);
	}
	
	@Transactional
    public void deleteAddressHierarchyLevel(AddressHierarchyLevel level) {
    	dao.deleteAddressHierarchyLevel(level);  
    }
	
	@Transactional(readOnly = true)
	public List<AddressHierarchyEntry> getLeafNodes(AddressHierarchyEntry ah) {
		return dao.getLeafNodes(ah);
	}
	
	@Transactional
	public void associateCoordinates(AddressHierarchyEntry ah, double latitude, double longitude) {
		dao.associateCoordinates(ah, latitude, longitude);
	}
	
	@Transactional
	public void truncateHierarchyTables() {
		dao.truncateHierarchyTables();
	}
	
	@Transactional(readOnly = true)
	public List<AddressHierarchyEntry> getTopOfHierarchyList() {
		return dao.getTopOfHierarchyList();
	}

	/**
	 * The following methods are deprecated and just exist to provide backwards compatibility to
	 * Rwanda Address Hierarchy module
	 */
	
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
	@Transactional
	public AddressHierarchyEntry addLocation(int parentId, String name, int levelId) {
		return addAddressHierarchyEntry(parentId, name, levelId);
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
	
	@Deprecated
	@Transactional(readOnly = true)
	public AddressHierarchyEntry editLocationName(Integer parentLocationId, String newName) {
		return editAddressHierarchyEntryName(parentLocationId, newName);
	}
	
	@Deprecated
	@Transactional
	public AddressHierarchyEntry addAddressHierarchyEntry(int parentId, String name, int levelId) {
		
		AddressHierarchyEntry parent = getAddressHierarchyEntry(parentId);
		
		if (parent == null) {
			throw new AddressHierarchyModuleException("Invalid entry id for parent entry");
		}
		
		AddressHierarchyLevel level = getAddressHierarchyLevel(levelId);
		
		if (level == null) {
			// if no level has been specified, use the level of the parent
			level = parent.getLevel();
		}
		
		AddressHierarchyEntry entry = new AddressHierarchyEntry();
		entry.setName(name);
		entry.setParent(parent);
		entry.setLevel(level);
		
		dao.saveAddressHierarchyEntry(entry);
		
		return entry;
	}
	
	@Deprecated
	@Transactional
	public AddressHierarchyEntry editAddressHierarchyEntryName(Integer locationId, String newName) {
		
		AddressHierarchyEntry entry = getAddressHierarchyEntry(locationId);
		
		if (entry == null) {
			throw new AddressHierarchyModuleException("Invalid address entry id");
		}
		
		entry.setName(newName);
		
		saveAddressHierarchyEntry(entry);
		
		return entry;
	}

}
