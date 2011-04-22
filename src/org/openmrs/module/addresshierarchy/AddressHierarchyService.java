package org.openmrs.module.addresshierarchy;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

/**
 * The Interface AddressHierarchyService has the service methods for AddressHierarchy module.
 */
@Transactional
public interface AddressHierarchyService {
	
	// TODO: remove or deprecate unused methods
	
	public int getAddressHierarchyEntryCount();
	
	public AddressHierarchy getAddressHierarchyEntry(int addressHierarchyEntryId);
	
	public AddressHierarchy getAddressHierarchyEntryByUserGenId(String userGeneratedId);
	
	public void saveAddressHierarchyEntry(AddressHierarchy entry);
	
	public AddressHierarchy addAddressHierarchyEntry(int parentId, String name, int typeId);
	
	public AddressHierarchy editAddressHierarchyEntryName(Integer parentLocationId, String newName);
	
	/**
	 * Gets all AddressHierarchyTypes, ordered from the top of hierarchy to the bottom
	 */
	public List<AddressHierarchyType> getAddressHierarchyTypes();
	
	/**
	 * Gets the AddressHierarchyType that represents the top level of the hierarchy
	 */
	public AddressHierarchyType getTopLevelAddressHierarchyType();
	
	/**
	 * Gets an Address Hierarchy Type by id
	 */
	public AddressHierarchyType getAddressHierarchyType(int typeId);
	
	/**
	 * Finds the child AddressHierarchyType of the given AddressHierarchyType
	 */
	public AddressHierarchyType getChildAddressHierarchyType(AddressHierarchyType type);
	
	/**
	 * Saves an AddressHierarchyType
	 */
	public void saveAddressHierarchyType(AddressHierarchyType type);
	
	
	
	// TODO: figure out if I need to rename any of these
	
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
	
	/**
	 * I've renamed the following methods to make them a little more clear, but kept the old methods
	 * for backwards compatibility
	 */
	
	@Deprecated
	public int getAddressHierarchyCount();
	
	@Deprecated
	public AddressHierarchy getAddressHierarchy(int addressHierarchyId);
	
	@Deprecated
	public void saveAddressHierarchy(AddressHierarchy ah);
	
	@Deprecated
	public AddressHierarchy addLocation(int parentId, String name, int typeId);
	
	@Deprecated
	public AddressHierarchy editLocationName(Integer parentLocationId, String newName);
	
	@Deprecated
	public AddressHierarchy getLocationFromUserGenId(String userGeneratedId);
	
	@Deprecated
	public AddressHierarchyType getHierarchyType(int typeId);;
	
}
