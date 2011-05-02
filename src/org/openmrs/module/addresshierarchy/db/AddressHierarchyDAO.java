package org.openmrs.module.addresshierarchy.db;

import java.util.List;

import org.openmrs.module.addresshierarchy.AddressHierarchyEntry;
import org.openmrs.module.addresshierarchy.AddressHierarchyLevel;

/**
 * The Interface AddressHierarchyDAO which is implemented in HibernateAddressHierarchyDAO which
 * links to the tables address_hierarchy_entry, address_hierarchy_level and person_address. This class does
 * the functions of storing and retrieving addresses.
 */
public interface AddressHierarchyDAO {
	
	// TODO: remove or deprecate unused methods
	
	public int getAddressHierarchyEntryCount();
	
	public AddressHierarchyEntry getAddressHierarchyEntry(int addressHierarchyId);
	
	public AddressHierarchyEntry getAddressHierarchyEntryByUserGenId(String userGeneratedId);
	
	public List<AddressHierarchyEntry> getChildAddressHierarchyEntries(AddressHierarchyEntry entry);
	
	public void saveAddressHierarchyEntry(AddressHierarchyEntry ah);
	
	public void deleteAllAddressHierarchyEntries();
	
	public List<AddressHierarchyLevel> getAddressHierarchyLevels();
	
	public AddressHierarchyLevel getTopAddressHierarchyLevel();
	
	public AddressHierarchyLevel getAddressHierarchyLevel(int levelId);
	
	public AddressHierarchyLevel getAddressHierarchyLevelByParent(AddressHierarchyLevel parent);
	
	public void saveAddressHierarchyLevel(AddressHierarchyLevel level);
	
	public void deleteAddressHierarchyLevel(AddressHierarchyLevel level);
	
	public List<AddressHierarchyEntry> getLeafNodes(AddressHierarchyEntry ah);
	
	public List<AddressHierarchyEntry> searchHierarchy(String searchString, int levelId, Boolean exact);
	
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
	public int getUnstructuredCount(int page);
	
	@Deprecated
	public List<Object[]> findUnstructuredAddresses(int page, int locationId);
	
	@Deprecated
	public List<Object[]> getLocationAddressBreakdown(int locationId);
	
	@Deprecated
	public List<Object[]> getAllAddresses(int page);
	
}
