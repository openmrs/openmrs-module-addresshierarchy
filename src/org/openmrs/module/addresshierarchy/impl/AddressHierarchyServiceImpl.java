package org.openmrs.module.addresshierarchy.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.addresshierarchy.AddressHierarchyEntry;
import org.openmrs.module.addresshierarchy.AddressHierarchyService;
import org.openmrs.module.addresshierarchy.AddressHierarchyType;
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
	
	@Transactional
	public void saveAddressHierarchyEntry(AddressHierarchyEntry entry) {
		dao.saveAddressHierarchyEntry(entry);
	}
	
	@Transactional
	public void deleteAllAddressHierarchyEntries() {
		dao.deleteAllAddressHierarchyEntries();
	}
	
	@Transactional(readOnly = true)
	public List<AddressHierarchyType> getOrderedAddressHierarchyTypes() {
		
		// TODO: integrate this with the new method we may create to make sure that the hierarchy is well-formed?
		
		List<AddressHierarchyType> types = new ArrayList<AddressHierarchyType>();
		
		// first, get the top level type
		AddressHierarchyType topLevel = getTopLevelAddressHierarchyType();
		
		if (topLevel != null) {
			// add the top level to this list
			types.add(topLevel);
			
			// now fetch the children in order
			while (getChildAddressHierarchyType(types.get(types.size() - 1)) != null) {
				types.add(getChildAddressHierarchyType(types.get(types.size() - 1)));
			}
		}
		
		// make sure we've reached all the types this way
		if (types.size() < getAddressHierarchyTypes().size()) {
			log.warn("Address Hierarchy Types are not in strict hierarchical format. There may be orphaned or widowed types.");
		}
		
		return types;
	}
	
	@Transactional(readOnly = true)
	public List<AddressHierarchyType> getAddressHierarchyTypes() {
		return dao.getAddressHierarchyTypes();
	}
	
	@Transactional(readOnly = true)
	public AddressHierarchyType getTopLevelAddressHierarchyType() {
		return dao.getTopLevelAddressHierarchyType();
	}
	
	@Transactional(readOnly = true)
    public AddressHierarchyType getBottomLevelAddressHierarchyType() {
		
		// get the ordered list
		List<AddressHierarchyType> types = getOrderedAddressHierarchyTypes();
		
		// return the last member in the list
		if (types != null && types.size() > 0) {
			return types.get(types.size() - 1);
		}
		else {
			return null;
		}
    }
	
	@Transactional(readOnly = true)
	public AddressHierarchyType getAddressHierarchyType(int typeId) {
		return dao.getAddressHierarchyType(typeId);
	}
	
	@Transactional(readOnly = true)
    public AddressHierarchyType getChildAddressHierarchyType(AddressHierarchyType type) {
	    return dao.getAddressHierarchyTypeByParent(type);
    }
	
	@Transactional
	public void saveAddressHierarchyType(AddressHierarchyType type) {
		dao.saveAddressHierarchyType(type);
	}
	
	@Transactional
    public void deleteAddressHierarchyType(AddressHierarchyType type) {
    	dao.deleteAddressHierarchyType(type);  
    }
	
	@Transactional(readOnly = true)
	public List<AddressHierarchyEntry> getLeafNodes(AddressHierarchyEntry ah) {
		return dao.getLeafNodes(ah);
	}
	
	@Transactional(readOnly = true)
	public List<AddressHierarchyEntry> getNextComponent(Integer locationId) {
		return dao.getNextComponent(locationId);
	}
	
	@Transactional(readOnly = true)
	public List<AddressHierarchyEntry> searchHierarchy(String searchString, int locationTypeId) {
		return searchHierarchy(searchString, locationTypeId, false);
	}
	
	@Transactional(readOnly = true)
	public List<AddressHierarchyEntry> searchHierarchy(String searchString, int locationTypeId, Boolean exact) {
		return dao.searchHierarchy(searchString, locationTypeId, exact);
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
	public AddressHierarchyEntry addLocation(int parentId, String name, int typeId) {
		return addAddressHierarchyEntry(parentId, name, typeId);
	}
	
	@Deprecated
	@Transactional(readOnly = true)
	public AddressHierarchyEntry getAddressHierarchy(int addressHierarchyId) {
		return getAddressHierarchyEntry(addressHierarchyId);
	}
	
	@Deprecated
	@Transactional(readOnly = true)
	public AddressHierarchyType getHierarchyType(int typeId) {
		return getAddressHierarchyType(typeId);
	}
	
	@Deprecated
	@Transactional(readOnly = true)
	public AddressHierarchyEntry editLocationName(Integer parentLocationId, String newName) {
		return editAddressHierarchyEntryName(parentLocationId, newName);
	}
	
	@Deprecated
	@Transactional
	public AddressHierarchyEntry addAddressHierarchyEntry(int parentId, String name, int typeId) {
		
		AddressHierarchyEntry parent = getAddressHierarchyEntry(parentId);
		
		if (parent == null) {
			throw new AddressHierarchyModuleException("Invalid entry id for parent entry");
		}
		
		AddressHierarchyType type = getAddressHierarchyType(typeId);
		
		if (type == null) {
			// if no type has been specified, use the type of the parent
			type = parent.getType();
		}
		
		AddressHierarchyEntry entry = new AddressHierarchyEntry();
		entry.setName(name);
		entry.setParent(parent);
		entry.setType(type);
		
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
