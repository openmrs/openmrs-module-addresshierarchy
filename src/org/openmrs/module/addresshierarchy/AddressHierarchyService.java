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
	
	public AddressHierarchyEntry getAddressHierarchyEntry(int addressHierarchyEntryId);
	
	public AddressHierarchyEntry getAddressHierarchyEntryByUserGenId(String userGeneratedId);
	
	public void saveAddressHierarchyEntry(AddressHierarchyEntry entry);
	
	/**
	 * Removes all address hierarchy entries--use with care!
	 */
	public void deleteAllAddressHierarchyEntries();
	
	/**
	 * Gets all AddressHierarchyTypes, ordered from the top of hierarchy to the bottom
	 */
	public List<AddressHierarchyType> getOrderedAddressHierarchyTypes();
	
	/**
	 * Gets all AddressHierarchyTypes
	 */
	public List<AddressHierarchyType> getAddressHierarchyTypes();
	
	/**
	 * Gets the AddressHierarchyType that represents the top level of the hierarchy
	 */
	public AddressHierarchyType getTopLevelAddressHierarchyType();
	
	/**
	 * Gets the AddressHierarchyType that represents the lowest level of the hierarchy
	 */
	public AddressHierarchyType getBottomLevelAddressHierarchyType();
	
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
	
	/**
	 * Deletes an AddressHierarchy Type
	 */
	public void deleteAddressHierarchyType(AddressHierarchyType type);
	
	
	// TODO: figure out if I need to rename any of these
	
	public List<AddressHierarchyEntry> getLeafNodes(AddressHierarchyEntry ah);
	
	public List<AddressHierarchyEntry> getNextComponent(Integer locationId);
	
	public List<AddressHierarchyEntry> searchHierarchy(String searchString, int locationTypeId);
	
	public List<AddressHierarchyEntry> searchHierarchy(String searchString, int locationTypeId, Boolean exact);
	
	public void associateCoordinates(AddressHierarchyEntry ah, double latitude, double longitude);
	
	public void truncateHierarchyTables();
	
	public List<AddressHierarchyEntry> getTopOfHierarchyList();
	
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
	
	@Deprecated
	public AddressHierarchyEntry addAddressHierarchyEntry(int parentId, String name, int typeId);
	
	@Deprecated
	public AddressHierarchyEntry editAddressHierarchyEntryName(Integer parentLocationId, String newName);
	
	/**
	 * I've renamed the following methods to make them a little more clear, but kept the old methods
	 * for backwards compatibility
	 */
	
	@Deprecated
	public int getAddressHierarchyCount();
	
	@Deprecated
	public AddressHierarchyEntry getAddressHierarchy(int addressHierarchyId);
	
	@Deprecated
	public void saveAddressHierarchy(AddressHierarchyEntry ah);
	
	@Deprecated
	public AddressHierarchyEntry addLocation(int parentId, String name, int typeId);
	
	@Deprecated
	public AddressHierarchyEntry editLocationName(Integer locationId, String newName);
	
	@Deprecated
	public AddressHierarchyEntry getLocationFromUserGenId(String userGeneratedId);
	
	@Deprecated
	public AddressHierarchyType getHierarchyType(int typeId);;
	
}
