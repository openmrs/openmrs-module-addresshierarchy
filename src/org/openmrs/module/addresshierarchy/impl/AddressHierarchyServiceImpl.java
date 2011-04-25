package org.openmrs.module.addresshierarchy.impl;

import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.addresshierarchy.AddressHierarchyEntry;
import org.openmrs.module.addresshierarchy.AddressHierarchyService;
import org.openmrs.module.addresshierarchy.AddressHierarchyType;
import org.openmrs.module.addresshierarchy.db.AddressHierarchyDAO;

/**
 * The Class AddressHierarchyServiceImpl default implementation of AddressHierarchyService.
 */
public class AddressHierarchyServiceImpl implements AddressHierarchyService {
	
	private AddressHierarchyDAO dao;
	
	public void setAddressHierarchyDAO(AddressHierarchyDAO dao) {
		this.dao = dao;
	}
	
	public int getAddressHierarchyEntryCount() {
		return dao.getAddressHierarchyEntryCount();
	}
	
	public AddressHierarchyEntry getAddressHierarchyEntry(int addressHierarchyId) {
		return dao.getAddressHierarchyEntry(addressHierarchyId);
	}
	
	public AddressHierarchyEntry getAddressHierarchyEntryByUserGenId(String userGeneratedId) {
		return dao.getAddressHierarchyEntryByUserGenId(userGeneratedId);
	}
	
	public void saveAddressHierarchyEntry(AddressHierarchyEntry entry) {
		dao.saveAddressHierarchyEntry(entry);
	}
	
	public AddressHierarchyEntry addAddressHierarchyEntry(int parentId, String name, int typeId) {
		return dao.addAddressHierarchyEntry(parentId, name, typeId);
	}
	
	public AddressHierarchyEntry editAddressHierarchyEntryName(Integer parentLocationId, String newName) {
		return dao.editAddressHierarchyEntryName(parentLocationId, newName);
	}
	
	public List<AddressHierarchyType> getAddressHierarchyTypes() {
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
		return types;
	}
	
	public AddressHierarchyType getTopLevelAddressHierarchyType() {
		return dao.getTopLevelAddressHierarchyType();
	}
	
	public AddressHierarchyType getAddressHierarchyType(int typeId) {
		return dao.getAddressHierarchyType(typeId);
	}
	
    public AddressHierarchyType getChildAddressHierarchyType(AddressHierarchyType type) {
	    return dao.getAddressHierarchyTypeByParent(type);
    }
	
	public void saveAddressHierarchyType(AddressHierarchyType type) {
		dao.saveAddressHierarchyType(type);
	}
	
	public List<AddressHierarchyEntry> getLeafNodes(AddressHierarchyEntry ah) {
		return dao.getLeafNodes(ah);
	}
	
	public List<AddressHierarchyEntry> getNextComponent(Integer locationId) {
		return dao.getNextComponent(locationId);
	}
	
	public List<AddressHierarchyEntry> searchHierarchy(String searchString, int locationTypeId) {
		return searchHierarchy(searchString, locationTypeId, false);
	}
	
	public List<AddressHierarchyEntry> searchHierarchy(String searchString, int locationTypeId, Boolean exact) {
		return dao.searchHierarchy(searchString, locationTypeId, exact);
	}
	
	public void associateCoordinates(AddressHierarchyEntry ah, double latitude, double longitude) {
		dao.associateCoordinates(ah, latitude, longitude);
	}
	
	public void truncateHierarchyTables() {
		dao.truncateHierarchyTables();
	}
	
	public List<AddressHierarchyEntry> getTopOfHierarchyList() {
		return dao.getTopOfHierarchyList();
	}
	
	/**
	 * The following methods are deprecated and just exist to provide backwards compatibility to
	 * Rwanda Address Hierarchy module
	 */
	
	@Deprecated
	public List<Object[]> getLocationAddressBreakdown(int locationId) {
		return dao.getLocationAddressBreakdown(locationId);
	}
	
	@Deprecated
	public List<Object[]> findUnstructuredAddresses(int page, int locationId) {
		return dao.findUnstructuredAddresses(page, locationId);
	}
	
	@Deprecated
	public List<Object[]> getAllAddresses(int page) {
		return dao.getAllAddresses(page);
	}
	
	@Deprecated
	public void initializeRwandaHierarchyTables() {
		dao.initializeRwandaHierarchyTables();
	}
	
	/**
	 * I've renamed the following methods to make them a little more clear, but kept the old method
	 * names for backwards compatibility
	 */
	
	@Deprecated
	public int getAddressHierarchyCount() {
		return getAddressHierarchyEntryCount();
	}
	
	@Deprecated
	public void saveAddressHierarchy(AddressHierarchyEntry ah) {
		saveAddressHierarchyEntry(ah);
	}
	
	@Deprecated
	public AddressHierarchyEntry getLocationFromUserGenId(String userGeneratedId) {
		return getAddressHierarchyEntryByUserGenId(userGeneratedId);
	}
	
	@Deprecated
	public AddressHierarchyEntry addLocation(int parentId, String name, int typeId) {
		return addAddressHierarchyEntry(parentId, name, typeId);
	}
	
	@Deprecated
	public AddressHierarchyEntry getAddressHierarchy(int addressHierarchyId) {
		return getAddressHierarchyEntry(addressHierarchyId);
	}
	
	@Deprecated
	public AddressHierarchyType getHierarchyType(int typeId) {
		return getAddressHierarchyType(typeId);
	}
	
	@Deprecated
	public AddressHierarchyEntry editLocationName(Integer parentLocationId, String newName) {
		return editAddressHierarchyEntryName(parentLocationId, newName);
	}
}
