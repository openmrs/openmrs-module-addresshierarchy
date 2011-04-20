package org.openmrs.module.addresshierarchy;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

/**
 * The Interface AddressHierarchyService has the service methods for AddressHierarchy module.
 */
@Transactional
public interface AddressHierarchyService {
	
	public int getAddressHierarchyCount();
	
	public AddressHierarchy getAddressHierarchy(int addressHierarchyId);
	
	public void saveAddressHierarchy(AddressHierarchy ah);
	
	public AddressHierarchy addLocation(int parentId, String name, int typeId);
	
	public AddressHierarchy editLocationName(Integer parentLocationId, String newName);
	
	public AddressHierarchy getLocation(int addressHierarchyId);
	
	public AddressHierarchy getLocationFromUserGenId(String userGeneratedId);
	
	public List<AddressHierarchyType> getAddressHierarchyTypes();
	
	public AddressHierarchyType getHierarchyType(int typeId);
	
	public List<AddressHierarchy> getLeafNodes(AddressHierarchy ah);
	
	public List<AddressHierarchy> getNextComponent(Integer locationId);
	
	public List<AddressHierarchy> searchHierarchy(String searchString, int locationTypeId);
	
	public void associateCoordinates(AddressHierarchy ah, double latitude, double longitude);
	
	public void truncateHierarchyTables();
	
	public List<AddressHierarchy> getTopOfHierarchyList();
	
	/**
	 * The following methods are deprecated and just exist to provide backwards compatibility to
	 * Rwanda Address Hierarchy module
	 */
	@Deprecated
	public void initializeRwandaHierarchyTables();
	
	@Deprecated
	public List<Object[]> findUnstructuredAddresses(int page, int locationId);
	
	@Deprecated
	public List<Object[]> getLocationAddressBreakdown(int locationId);
	
	@Deprecated
	public List<Object[]> getAllAddresses(int page);
	
}
