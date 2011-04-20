package org.openmrs.module.addresshierarchy.impl;

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
	
	public int getAddressHierarchyCount() {
		return dao.getAddressHierarchyCount();
	}
	
	public void saveAddressHierarchy(AddressHierarchy ah) {
		dao.saveAddressHierarchy(ah);
	}
	
	public AddressHierarchy addLocation(int parentId, String name, int typeId) {
		return dao.addLocation(parentId, name, typeId);
	}
	
	public AddressHierarchy getAddressHierarchy(int addressHierarchyId) {
		return dao.getLocation(addressHierarchyId);
	}
	
	public AddressHierarchy editLocationName(Integer parentLocationId, String newName) {
		return dao.editLocationName(parentLocationId, newName);
	}
	
	public AddressHierarchy getLocation(int location) {
		return getAddressHierarchy(location);
	}
	
	public AddressHierarchy getLocationFromUserGenId(String userGeneratedId) {
		return dao.getLocationFromUserGenId(userGeneratedId);
	}
	
	public List<AddressHierarchyType> getAddressHierarchyTypes() {
		return dao.getAddressHierarchyTypes();
	}
	
	public AddressHierarchyType getHierarchyType(int typeId) {
		return dao.getHierarchyType(typeId);
	}
	
	public List<AddressHierarchy> getLeafNodes(AddressHierarchy ah) {
		return dao.getLeafNodes(ah);
	}
	
	public List<AddressHierarchy> getNextComponent(Integer locationId) {
		return dao.getNextComponent(locationId);
	}
	
	public List<AddressHierarchy> searchHierarchy(String searchString, int locationTypeId) {
		return dao.searchHierarchy(searchString, locationTypeId);
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
}
