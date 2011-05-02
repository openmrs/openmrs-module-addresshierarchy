package org.openmrs.module.addresshierarchy.service;

import java.util.List;

import org.openmrs.annotation.Authorized;
import org.openmrs.module.addresshierarchy.AddressHierarchyConstants;
import org.openmrs.module.addresshierarchy.AddressHierarchyEntry;
import org.openmrs.module.addresshierarchy.AddressHierarchyLevel;
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
	
	public List<AddressHierarchyEntry> getChildAddressHierarchyEntries(AddressHierarchyEntry entry);
	
	public List<AddressHierarchyEntry> getChildAddressHierarchyEntries(Integer entryId);
	
	@Authorized( { AddressHierarchyConstants.PRIV_MANAGE_ADDRESS_HIERARCHY })
	public void saveAddressHierarchyEntry(AddressHierarchyEntry entry);
	
	/**
	 * Removes all address hierarchy entries--use with care!
	 */
	@Authorized( { AddressHierarchyConstants.PRIV_MANAGE_ADDRESS_HIERARCHY })
	public void deleteAllAddressHierarchyEntries();
	
	/**
	 * Gets all AddressHierarchyLevels, ordered from the top of hierarchy to the bottom
	 */
	public List<AddressHierarchyLevel> getOrderedAddressHierarchyLevels();
	
	/**
	 * Gets all AddressHierarchyLevels
	 */
	public List<AddressHierarchyLevel> getAddressHierarchyLevels();
	
	/**
	 * Gets the AddressHierarchyLevel that represents the top level of the hierarchy
	 */
	public AddressHierarchyLevel getTopAddressHierarchyLevel();
	
	/**
	 * Gets the AddressHierarchyLevel that represents the lowest level of the hierarchy
	 */
	public AddressHierarchyLevel getBottomAddressHierarchyLevel();
	
	/**
	 * Gets an AddressHierarchyLevel by id
	 */
	public AddressHierarchyLevel getAddressHierarchyLevel(int levelId);
	
	/**
	 * Finds the child AddressHierarchyLevel of the given AddressHierarchyLevel
	 */
	public AddressHierarchyLevel getChildAddressHierarchyLevel(AddressHierarchyLevel level);
	
	/**
	 * Saves an AddressHierarchyLevel
	 */
	@Authorized( { AddressHierarchyConstants.PRIV_MANAGE_ADDRESS_HIERARCHY })
	public void saveAddressHierarchyLevel(AddressHierarchyLevel level);
	
	/**
	 * Deletes an AddressHierarchyLevel
	 */
	@Authorized( { AddressHierarchyConstants.PRIV_MANAGE_ADDRESS_HIERARCHY })
	public void deleteAddressHierarchyLevel(AddressHierarchyLevel level);
	
	
	// TODO: figure out if I need to rename any of these
	
	public List<AddressHierarchyEntry> getLeafNodes(AddressHierarchyEntry ah);
	
	public List<AddressHierarchyEntry> searchHierarchy(String searchString, int levelId);
	
	public List<AddressHierarchyEntry> searchHierarchy(String searchString, int levelId, Boolean exact);
	
	public void associateCoordinates(AddressHierarchyEntry ah, double latitude, double longitude);
	
	@Authorized( { AddressHierarchyConstants.PRIV_MANAGE_ADDRESS_HIERARCHY })
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
	@Authorized( { AddressHierarchyConstants.PRIV_MANAGE_ADDRESS_HIERARCHY })
	public AddressHierarchyEntry addAddressHierarchyEntry(int parentId, String name, int levelId);
	
	@Deprecated
	@Authorized( { AddressHierarchyConstants.PRIV_MANAGE_ADDRESS_HIERARCHY })
	public AddressHierarchyEntry editAddressHierarchyEntryName(Integer parentLocationId, String newName);
	
	/**
	 * I've renamed the following methods to make them a little more clear, but kept the old methods
	 * for backwards compatibility
	 */
	
	@Deprecated
	public int getAddressHierarchyCount();
	
	@Deprecated
	public List<AddressHierarchyEntry> getNextComponent(Integer locationId);
	
	@Deprecated
	public AddressHierarchyEntry getAddressHierarchy(int addressHierarchyId);
	
	@Deprecated
	@Authorized( { AddressHierarchyConstants.PRIV_MANAGE_ADDRESS_HIERARCHY })
	public void saveAddressHierarchy(AddressHierarchyEntry ah);
	
	@Deprecated
	@Authorized( { AddressHierarchyConstants.PRIV_MANAGE_ADDRESS_HIERARCHY })
	public AddressHierarchyEntry addLocation(int parentId, String name, int levelId);
	
	@Deprecated
	@Authorized( { AddressHierarchyConstants.PRIV_MANAGE_ADDRESS_HIERARCHY })
	public AddressHierarchyEntry editLocationName(Integer locationId, String newName);
	
	@Deprecated
	public AddressHierarchyEntry getLocationFromUserGenId(String userGeneratedId);
	
	@Deprecated
	public AddressHierarchyLevel getHierarchyType(int levelId);;
	
}