package org.openmrs.module.addresshierarchy;


/**
 * The Class AddressHierarchy is linked to the table address_hierarchy table mapped in AddressHierarchy.hbm.xml.
 */
public class AddressHierarchy {

    private Integer locationId;
    private String locationName;
    private Integer typeId;
    private Integer parentId;
    
    
    /**
     * To string.
     * 
     * @return the string
     * 
     * @see java.lang.Object#toString()
     */
    public String toString(){
        return locationName+" "+locationId+" "+typeId+" "+parentId;
    }
    
    /**
     * Gets the location id.
     * 
     * @return the location id
     */
    public Integer getLocationId(){
        return locationId;
    }
    
    /**
     * Sets the location id.
     * 
     * @param locationId the new location id
     */
    public void setLocationId(Integer locationId){
        this.locationId = locationId;
    }
    
    /**
     * Gets the location name.
     * 
     * @return the location name
     */
    public String getLocationName(){
        return locationName;
    }
    
    /**
     * Sets the location name.
     * 
     * @param locationName the new location name
     */
    public void setLocationName(String locationName){
        this.locationName = locationName;
    }
    
    /**
     * Gets the type id.
     * 
     * @return the type id
     */
    public Integer getTypeId(){
        return typeId;
    }
    
    /**
     * Sets the type id.
     * 
     * @param typeId the new type id
     */
    public void setTypeId(Integer typeId){
        this.typeId = typeId;
    }
    
    /**
     * Sets the parent id.
     * 
     * @param parentId the new parent id
     */
    public void setParentId(Integer parentId){
        this.parentId = parentId;
    }
    
    /**
     * Gets the parent id.
     * 
     * @return the parent id
     */
    public Integer getParentId(){
        return parentId;
    }
    
    
}
