package org.openmrs.module.addresshierarchy;

import org.openmrs.BaseOpenmrsMetadata;

/**
 * The Class AddressHierarchy is linked to the table address_hierarchy table mapped in
 * AddressHierarchy.hbm.xml.
 */
public class AddressHierarchy extends BaseOpenmrsMetadata {
	
	private Integer addressHierarchyId;
	
	private String locationName;
	
	private AddressHierarchyType hierarchyType;
	
	private AddressHierarchy parent;
	
	private String userGeneratedId;
	
	private Double latitude;
	
	private Double longitude;
	
	private Double elevation;
	
	/**
	 * To string
	 */
	public String toString() {
		return locationName;
	}
	
	/**
	 * Getters and Setters
	 */
	
	public String getLocationName() {
		return locationName;
	}
	
	public void setLocationName(String locationName) {
		this.locationName = locationName;
	}
	
	public Double getLatitude() {
		return latitude;
	}
	
	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}
	
	public Double getLongitude() {
		return longitude;
	}
	
	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}
	
	public Double getElevation() {
		return elevation;
	}
	
	public void setElevation(Double elevation) {
		this.elevation = elevation;
	}
	
	public AddressHierarchyType getHierarchyType() {
		return hierarchyType;
	}
	
	public void setHierarchyType(AddressHierarchyType hierarchyType) {
		this.hierarchyType = hierarchyType;
	}
	
	public String getUserGeneratedId() {
		return userGeneratedId;
	}
	
	public void setUserGeneratedId(String userGeneratedId) {
		this.userGeneratedId = userGeneratedId;
	}
	
	public Integer getAddressHierarchyId() {
		return addressHierarchyId;
	}
	
	public void setAddressHierarchyId(Integer addressHierarchyId) {
		this.addressHierarchyId = addressHierarchyId;
	}
	
	public AddressHierarchy getParent() {
		return parent;
	}
	
	public void setParent(AddressHierarchy parent) {
		this.parent = parent;
	}

    public Integer getId() {
    	return this.addressHierarchyId;
    }
    
    public void setId(Integer id) {
	    this.addressHierarchyId = id;	    
    }
	
}
