package org.openmrs.module.addresshierarchy.db;

import java.util.List;

import org.openmrs.module.addresshierarchy.AddressHierarchy;
import org.openmrs.module.addresshierarchy.AddressHierarchyType;

/**
 * The Interface AddressHierarchyDAO which is implemented in HibernateAddressHierarchyDAO which
 * links to the tables address_hierarchy, address_hierarchy_type and person_address. This class does
 * the functions of storing and retrieving addresses.
 */
public interface AddressHierarchyDAO {
	
	// TODO: remove or deprecate unused methods
	
	public int getAddressHierarchyEntryCount();
	
	public AddressHierarchy getAddressHierarchyEntry(int addressHierarchyId);
	
	public void saveAddressHierarchyEntry(AddressHierarchy ah);
	
	public AddressHierarchy addAddressHierarchyEntry(int parentId, String name, int typeId);
	
	public AddressHierarchy editAddressHierarchyEntryName(Integer parentLocationId, String newName);
	
	public AddressHierarchy getAddressHierarchyEntryByUserGenId(String userGeneratedId);
	
	public List<AddressHierarchyType> getAddressHierarchyTypes();
	
	public AddressHierarchyType getTopLevelAddressHierarchyType();
	
	public AddressHierarchyType getAddressHierarchyType(int typeId);
	
	public AddressHierarchyType getAddressHierarchyTypeByParent(AddressHierarchyType parentType);
	
	public void saveAddressHierarchyType(AddressHierarchyType type);
	
	public List<AddressHierarchy> getLeafNodes(AddressHierarchy ah);
	
	public List<AddressHierarchy> getNextComponent(Integer locationId);
	
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
	public int getUnstructuredCount(int page);
	
	@Deprecated
	public List<Object[]> findUnstructuredAddresses(int page, int locationId);
	
	@Deprecated
	public List<Object[]> getLocationAddressBreakdown(int locationId);
	
	@Deprecated
	public List<Object[]> getAllAddresses(int page);
	
}
