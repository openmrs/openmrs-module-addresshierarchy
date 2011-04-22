package org.openmrs.module.addresshierarchy.impl;

import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.addresshierarchy.AddressHierarchy;
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
	
	public AddressHierarchy getAddressHierarchyEntry(int addressHierarchyId) {
		return dao.getAddressHierarchyEntry(addressHierarchyId);
	}
	
	public AddressHierarchy getAddressHierarchyEntryByUserGenId(String userGeneratedId) {
		return dao.getAddressHierarchyEntryByUserGenId(userGeneratedId);
	}
	
	public void saveAddressHierarchyEntry(AddressHierarchy entry) {
		dao.saveAddressHierarchyEntry(entry);
	}
	
	public AddressHierarchy addAddressHierarchyEntry(int parentId, String name, int typeId) {
		return dao.addAddressHierarchyEntry(parentId, name, typeId);
	}
	
	public AddressHierarchy editAddressHierarchyEntryName(Integer parentLocationId, String newName) {
		return dao.editAddressHierarchyEntryName(parentLocationId, newName);
	}
	
	public List<AddressHierarchyType> getAddressHierarchyTypes() {
		List<AddressHierarchyType> types = new ArrayList<AddressHierarchyType>();
		
		// first, get the top level type
		types.add(getTopLevelAddressHierarchyType());
		
		// now fetch the children in order
		while (types.get(types.size() - 1).getChildType() != null) {
			types.add(types.get(types.size() - 1).getChildType());
		}
		
		return types;
	}
	
	public AddressHierarchyType getTopLevelAddressHierarchyType() {
		return dao.getTopLevelAddressHierarchyType();
	}
	
	public AddressHierarchyType getAddressHierarchyType(int typeId) {
		return dao.getAddressHierarchyType(typeId);
	}
	
	public List<AddressHierarchy> getLeafNodes(AddressHierarchy ah) {
		return dao.getLeafNodes(ah);
	}
	
	public List<AddressHierarchy> getNextComponent(Integer locationId) {
		return dao.getNextComponent(locationId);
	}
	
	public List<AddressHierarchy> searchHierarchy(String searchString, int locationTypeId) {
		return searchHierarchy(searchString, locationTypeId, false);
	}
	
	public List<AddressHierarchy> searchHierarchy(String searchString, int locationTypeId, Boolean exact) {
		return dao.searchHierarchy(searchString, locationTypeId, exact);
	}
	
	public void associateCoordinates(AddressHierarchy ah, double latitude, double longitude) {
		dao.associateCoordinates(ah, latitude, longitude);
	}
	
	public void truncateHierarchyTables() {
		dao.truncateHierarchyTables();
	}
	
	public List<AddressHierarchy> getTopOfHierarchyList() {
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
	public void saveAddressHierarchy(AddressHierarchy ah) {
		saveAddressHierarchyEntry(ah);
	}
	
	@Deprecated
	public AddressHierarchy getLocationFromUserGenId(String userGeneratedId) {
		return getAddressHierarchyEntryByUserGenId(userGeneratedId);
	}
	
	@Deprecated
	public AddressHierarchy addLocation(int parentId, String name, int typeId) {
		return addAddressHierarchyEntry(parentId, name, typeId);
	}
	
	@Deprecated
	public AddressHierarchy getAddressHierarchy(int addressHierarchyId) {
		return getAddressHierarchyEntry(addressHierarchyId);
	}
	
	@Deprecated
	public AddressHierarchyType getHierarchyType(int typeId) {
		return getAddressHierarchyType(typeId);
	}
	
	@Deprecated
	public AddressHierarchy editLocationName(Integer parentLocationId, String newName) {
		return editAddressHierarchyEntryName(parentLocationId, newName);
	}
	
}
