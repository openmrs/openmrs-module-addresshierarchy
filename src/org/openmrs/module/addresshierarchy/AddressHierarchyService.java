package org.openmrs.module.addresshierarchy;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

/**
 * The Interface AddressHierarchyService has the service methods for AddressHierarchy module.
 */
@Transactional
public interface AddressHierarchyService {
	
	// TODO: remove or deprecate unused methods
	
	public int getAddressHierarchyCount();
	
	public AddressHierarchy getAddressHierarchy(int addressHierarchyId);
	
	public void saveAddressHierarchy(AddressHierarchy ah);
	
	public AddressHierarchy addLocation(int parentId, String name, int typeId);
	
	public AddressHierarchy editLocationName(Integer parentLocationId, String newName);
	
	public AddressHierarchy getLocation(int addressHierarchyId);
	
	public AddressHierarchy getLocationFromUserGenId(String userGeneratedId);
	
	/**
	 * Gets all address hierarchy types, ordered from the top of hierarchy to the bottom
	 */
	public List<AddressHierarchyType> getAddressHierarchyTypes();
	
	/**
	 * Gets the address hierarchy type that represents the top level of the hierarchy
	 */
	public AddressHierarchyType getTopLevelAddressHierarchyType();
	
	public AddressHierarchyType getHierarchyType(int typeId);
	
	public List<AddressHierarchy> getLeafNodes(AddressHierarchy ah);
	
	public List<AddressHierarchy> getNextComponent(Integer locationId);
	
	public List<AddressHierarchy> searchHierarchy(String searchString, int locationTypeId);
	
	public List<AddressHierarchy> searchHierarchy(String searchString, int locationTypeId, Boolean exact);
	
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
