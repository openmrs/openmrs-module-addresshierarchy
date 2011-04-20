package org.openmrs.module.addresshierarchy;

import java.util.List;

import org.openmrs.module.addresshierarchy.db.AddressHierarchyDAO;
import org.springframework.transaction.annotation.Transactional;

/**
 * The Interface AddressHierarchyService has the service methods for AddressHierarchy module.
 */
@Transactional
public interface AddressHierarchyService {
    
    /**
     * Sets the address hierarchy object dao.
     * 
     * @param dao the new address hierarchy dao
     */
    public void setAddressHierarchyDAO(AddressHierarchyDAO dao);

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
    @Transactional(readOnly=true)
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
    @Transactional(readOnly=true)
    public Integer locationCount();

    /**
     * Method used to get the locations from the top hierarchy.
     * 
     * @return the top hierarchy component locations list
     * 
     * @see org.openmrs.module.addresshierarchy.db.AddressHierarchyDAO#getCountryList()
     */
    @Transactional(readOnly=true)
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
    @Transactional(readOnly=true)
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
    @Transactional(readOnly=true)
    @SuppressWarnings("unchecked")
	public List locationsLoader(Integer id,String str);

    /**
     * Method which sends the necessary data to servlet to feed the tree.
     * 
     * @return the complete locations
     * 
     * @see org.openmrs.module.addresshierarchy.db.AddressHierarchyDAO#getCompleteLocations()
     */
    @Transactional(readOnly=true)
    public String getCompleteLocations();
    
    /**
     * Gets the location type component names.
     * 
     * @return the location type component names in an array
     * 
     * @see org.openmrs.module.addresshierarchy.db.AddressHierarchyDAO#getLocationType()
     */
    @Transactional(readOnly=true)
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
    @Transactional(readOnly=true)
    public String[] getAddressHierarchyTypeList(Integer typeid);

    /**
     * Gets the size of person table.
     * 
     * @return the size of person table
     * 
     * @see org.openmrs.module.addresshierarchy.db.AddressHierarchyDAO#getSizeOfPersonTable()
     */
    @Transactional(readOnly=true)
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
    @Transactional(readOnly=true)
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
