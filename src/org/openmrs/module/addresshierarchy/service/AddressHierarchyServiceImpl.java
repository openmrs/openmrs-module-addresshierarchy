package org.openmrs.module.addresshierarchy.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.addresshierarchy.AddressHierarchyEntry;
import org.openmrs.module.addresshierarchy.AddressHierarchyLevel;
import org.openmrs.module.addresshierarchy.db.AddressHierarchyDAO;
import org.openmrs.module.addresshierarchy.exception.AddressHierarchyModuleException;
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
	
	@Transactional
	public void saveAddressHierarchyEntry(AddressHierarchyEntry entry) {
		dao.saveAddressHierarchyEntry(entry);
	}
	
	@Transactional
	public void deleteAllAddressHierarchyEntries() {
		dao.deleteAllAddressHierarchyEntries();
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
	
	@Transactional(readOnly = true)
	public List<AddressHierarchyEntry> searchHierarchy(String searchString, int levelId) {
		return searchHierarchy(searchString, levelId, false);
	}
	
	@Transactional(readOnly = true)
	public List<AddressHierarchyEntry> searchHierarchy(String searchString, int levelId, Boolean exact) {
		return dao.searchHierarchy(searchString, levelId, exact);
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
