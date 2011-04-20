package org.openmrs.module.addresshierarchy.db;

import java.util.List;

import net.sf.json.JSONArray;

import org.openmrs.PersonAddress;
import org.openmrs.module.addresshierarchy.AddressHierarchy;
import org.openmrs.module.addresshierarchy.AddressHierarchyType;

/**
 * The Interface AddressHierarchyDAO which is implemented in HibernateAddressHierarchyDAO which links to the tables address_hierarchy, address_hierarchy_type and person_address. This class does the functions of storing and retrieving addresses.
 */
public interface AddressHierarchyDAO{

    /**
     * Method used to get the child locations.
     * 
     * @param type_Id the type_ id
     * @param location_Name the location_ name
     * @param parent_Id the parent_ id
     * 
     * @return the next component in an array
     * 
     * @see org.openmrs.module.addresshierarchy.db.AddressHierarchyDAO#getNextComponent(java.lang.Integer, java.lang.String, java.lang.Integer)
     */
    public String[] getNextComponent(Integer type_Id,String location_Name,Integer parent_Id);

    /**
     * Method used to add a location to the address_hierarchy table when an AddressHierarchy object is sent.
     * 
     * @param ahs the AddressHierarchy Object
     * 
     * @return the Location Id
     * 
     * @see org.openmrs.module.addresshierarchy.db.AddressHierarchyDAO#setNextComponent(org.openmrs.module.addresshierarchy.AddressHierarchy)
     */
    public Integer setNextComponent(AddressHierarchy ahs);

    /**
     * Method gives out the total number of locations in the address_hierarchy table.
     * 
     * @return the location count
     * 
     * @see org.openmrs.module.addresshierarchy.db.AddressHierarchyDAO#locationCount()
     */
    public Integer locationCount();

    /**
     * Method used to get the locations from the top hierarchy.
     * 
     * @return the top hierarchy component locations list
     * 
     * @see org.openmrs.module.addresshierarchy.db.AddressHierarchyDAO#getCountryList()
     */
    public String[] getCountryList();

    /**
     * Method used to get the location id of a location from the address_hierarchy table.
     * 
     * @param parent_type_Id the parent_type_ id
     * @param location_Name the location_ name
     * @param parent_Id the parent_ id
     * 
     * @return the parent location id
     * 
     * @see org.openmrs.module.addresshierarchy.db.AddressHierarchyDAO#getLocationId(java.lang.Integer, java.lang.String, java.lang.Integer)
     */
    public Integer getLocationId(Integer parent_type_Id,String location_Name,Integer parent_Id);

    /**
     * Method used to edit a location name.
     * 
     * @param parentLocationId the parent location id
     * @param oldName the old name
     * @param newName the new name
     * 
     * @see org.openmrs.module.addresshierarchy.db.AddressHierarchyDAO#editLocation(java.lang.Integer, java.lang.String, java.lang.String)
     */
    public void editLocation(Integer parentLocationId,String oldName,String newName);

    /**
     * Method used to delete a location.
     * 
     * @param parentLocationId the parent location id
     * @param name the location name
     * 
     * @see org.openmrs.module.addresshierarchy.db.AddressHierarchyDAO#deleteLocation(java.lang.Integer, java.lang.String)
     */
    public void deleteLocation(Integer parentLocationId,String name);
    /**
     * Method used to list out the locations with similar names.
     * 
     * @param id the type id
     * @param str the string typed at autocomplete box
     * 
     * @return the list
     * 
     * @see org.openmrs.module.addresshierarchy.db.AddressHierarchyDAO#locationsLoader(java.lang.Integer, java.lang.String)
     */
    @SuppressWarnings("unchecked")
	public List locationsLoader(Integer id,String str);

    /**
     * Method which sends the necessary data to servlet to feed the tree.
     * 
     * @return the complete locations
     * 
     * @see org.openmrs.module.addresshierarchy.db.AddressHierarchyDAO#getCompleteLocations()
     */
    public String getCompleteLocations();
    /**
     * Gets the location type component names.
     * 
     * @return the location type component names in an array
     * 
     * @see org.openmrs.module.addresshierarchy.db.AddressHierarchyDAO#getLocationType()
     */
    public String[] getLocationType();
    /**
     * Update location type table.
     * 
     * @param aht the aht
     * 
     * @see org.openmrs.module.addresshierarchy.db.AddressHierarchyDAO#updateLocationTypeTable(org.openmrs.module.addresshierarchy.AddressHierarchyType)
     */
    public void updateLocationTypeTable(AddressHierarchyType aht);

    /**
     * Gets the address hierarchy type list.
     * 
     * @param typeid the typeid
     * 
     * @return the address hierarchy type list in an array
     * 
     * @see org.openmrs.module.addresshierarchy.db.AddressHierarchyDAO#getAddressHierarchyTypeList(java.lang.Integer)
     */
    public String[] getAddressHierarchyTypeList(Integer typeid);

    /**
     * Gets the size of person table.
     * 
     * @return the size of person table
     * 
     * @see org.openmrs.module.addresshierarchy.db.AddressHierarchyDAO#getSizeOfPersonTable()
     */
    public Integer getSizeOfPersonTable();

    /**
     * Gets the location lists when the type id of the previous component is provided.
     * 
     * @param id the type id
     * 
     * @return the location lists in an array
     * 
     * @see org.openmrs.module.addresshierarchy.db.AddressHierarchyDAO#getLocation(java.lang.Integer)
     */
    public String[] getLocation(Integer id);
    /**
     * Execute an update query when a string query is passed.
     * 
     * @param query the update query
     * 
     * @see org.openmrs.module.addresshierarchy.db.AddressHierarchyDAO#execQuery(java.lang.String)
     */
    public void execQuery(String query);
    
}
