package org.openmrs.module.addresshierarchy.db;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.openmrs.Patient;
import org.openmrs.PersonAddress;
import org.openmrs.module.addresshierarchy.AddressHierarchyEntry;
import org.openmrs.module.addresshierarchy.AddressHierarchyLevel;
import org.openmrs.module.addresshierarchy.AddressToEntryMap;

/**
 * The Interface AddressHierarchyDAO which is implemented in HibernateAddressHierarchyDAO which
 * links to the tables address_hierarchy_entry, address_hierarchy_level and person_address. This class does
 * the functions of storing and retrieving addresses.
 */
public interface AddressHierarchyDAO {

	/**
	 * Returns the number of address hierarchy entries
	 */
	public int getAddressHierarchyEntryCount();
	
	/**
	 * Returns the number of address hierarchy entries at the given level
	 */
	public int getAddressHierarchyEntryCountByLevel(AddressHierarchyLevel level);
	
	/**
	 * Get an address hierarchy entry, reference by id
	 */
	public AddressHierarchyEntry getAddressHierarchyEntry(int addressHierarchyId);
	
	/**
	 * Get an address hierarchy entry, referenced by the userGeneratedId property
	 */
	public AddressHierarchyEntry getAddressHierarchyEntryByUserGenId(String userGeneratedId);
	
	/**
	 * Gets all address hierarchy entries associated with a certain level
	 */
	public List<AddressHierarchyEntry> getAddressHierarchyEntriesByLevel(AddressHierarchyLevel level);
	
	/**
	 * Gets all address hierarchy entries associated with the a certain level that have the specified name
	 */
	public List<AddressHierarchyEntry> getAddressHierarchyEntriesByLevelAndName(AddressHierarchyLevel level, String name);
	
	/**
	 * Gets all address hierarchy entries associated with the a certain level that have the specified name and the specified parent
	 */
	public List<AddressHierarchyEntry> getAddressHierarchyEntriesByLevelAndNameAndParent(AddressHierarchyLevel level, String name, AddressHierarchyEntry parent);

	public List<AddressHierarchyEntry> getAddressHierarchyEntriesByLevelAndLikeNameAndParent(AddressHierarchyLevel level, String name, AddressHierarchyEntry parent);

	/**
	 * Gets all the address hierarchy entries that are children of the specified entry
	 */
	public List<AddressHierarchyEntry> getChildAddressHierarchyEntries(AddressHierarchyEntry entry);
	
	/**
	 * Gets the address hierarchy entry which the specified name that is a child of the given entry
	 * (Will throw exception if there are multiple matches, as there should never be two entries with the same name AND parent)
	 */
	public AddressHierarchyEntry getChildAddressHierarchyEntryByName(AddressHierarchyEntry entry, String childName);
	
	/**
	 * Saves the specified address hierarchy entry
	 */
	public void saveAddressHierarchyEntry(AddressHierarchyEntry ah);
	
	/**
	 * Deletes all the address hierarchy entries (use with care!)
	 */
	public void deleteAllAddressHierarchyEntries();
	
	/**
	 * Returns a list of all address hierarchy levels
	 */
	public List<AddressHierarchyLevel> getAddressHierarchyLevels();
	
	/**
	 * Returns the top level in the hierarchy (i.e., the highest level, with no parent)
	 * (Will throw an exception if the multiple levels with no parents)
	 */
	public AddressHierarchyLevel getTopAddressHierarchyLevel();
	
	/**
	 * Gets an address hierarchy level by Id
	 */
	public AddressHierarchyLevel getAddressHierarchyLevel(int levelId);
	
	/**
	 * Gets the address hierarchy level that is the child of the specified level
	 * (Will throw an exception if there are multiple children--a level should only have one child)
	 */
	public AddressHierarchyLevel getAddressHierarchyLevelByParent(AddressHierarchyLevel parent);
	
	/**
	 * Saves the specified address hierarchy level
	 */
	public void saveAddressHierarchyLevel(AddressHierarchyLevel level);
	
	/**
	 * Deletes the specified address hierarchy level
	 */
	public void deleteAddressHierarchyLevel(AddressHierarchyLevel level);
	
	/**
	 * Gets a AddressToEntryMap by Id
	 */
	public AddressToEntryMap getAddressToEntryMap(int id);
	
	/**
	 * Gets all the AddressToEntryMap objects for a given PersonAddress
	 */
	public List<AddressToEntryMap> getAddressToEntryMapByPersonAddress(PersonAddress address);
	
	/**
	 * Saves the specified AddressToEntryMap
	 */
	public void saveAddressToEntryMap(AddressToEntryMap addressToEntry);
	
	/**
	 * Deletes the specified AddressToEntryMap
	 */
	public void deleteAddressToEntryMap(AddressToEntryMap addressToEntryMap);
	
	/**
	 * Finds all patients which have dateChanged after or equal to the specified date
	 */
	public List<Patient> findAllPatientsWithDateChangedAfter(Date date);
	
	/**
	 * The following methods are deprecated and just exist to provide backwards compatibility to
	 * Rwanda Address Hierarchy module--most are Rwanda-specific
	 */
	@Deprecated
	public void associateCoordinates(AddressHierarchyEntry ah, double latitude, double longitude);
	
	@Deprecated
	public List<AddressHierarchyEntry> getLeafNodes(AddressHierarchyEntry ah);
	
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

    List<AddressHierarchyEntry> getAddressHierarchyEntriesByLevelAndLikeName(AddressHierarchyLevel level, String name, int limit);
}
