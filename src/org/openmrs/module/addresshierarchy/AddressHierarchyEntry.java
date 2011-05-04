package org.openmrs.module.addresshierarchy;

import org.openmrs.BaseOpenmrsMetadata;

/**
 * Represents a single entry in the Address Hierarchy (ie., like "United States", or "Massachusetts", or "Boston")
 * 
 * Must be unique on combined name and parent
 */
public class AddressHierarchyEntry extends BaseOpenmrsMetadata implements Comparable<AddressHierarchyEntry> {
	
	private Integer addressHierarchyEntryId;
	
	private String name;
	
	private AddressHierarchyLevel level;
	
	private AddressHierarchyEntry parent;
	
	private String userGeneratedId;
	
	private Double latitude;
	
	private Double longitude;
	
	private Double elevation;
	
	/**
	 * To string
	 */
	public String toString() {
		return name;
	}
	
	/**
	 * Comparator
	 */

    public int compareTo(AddressHierarchyEntry other) {
	    return this.name.compareTo(other.getName());
    }
	
	/**
	 * Equals
	 */
	public boolean equals(Object obj) {
		if (this.getId() == null)
			return false;
		if (obj instanceof AddressHierarchyEntry) {
			AddressHierarchyEntry c = (AddressHierarchyEntry) obj;
			return (this.getId().equals(c.getId()));
		}
		return false;
	}
	
	/**
	 * Getters and Setters
	 */
	
	public String getLocationName() {
		return name;
	}
	
	public void setLocationName(String locationName) {
		this.name = locationName;
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
	
	public AddressHierarchyLevel getLevel() {
		return level;
	}
	
	public void setLevel(AddressHierarchyLevel level) {
		this.level = level;
	}
	
	public String getUserGeneratedId() {
		return userGeneratedId;
	}
	
	public void setUserGeneratedId(String userGeneratedId) {
		this.userGeneratedId = userGeneratedId;
	}
	
	public Integer getAddressHierarchyEntryId() {
		return addressHierarchyEntryId;
	}
	
	public void setAddressHierarchyEntryId(Integer addressHierarchyEntryId) {
		this.addressHierarchyEntryId = addressHierarchyEntryId;
	}
	
	public AddressHierarchyEntry getParent() {
		return parent;
	}
	
	public void setParent(AddressHierarchyEntry parent) {
		this.parent = parent;
	}

	/**
	 * Getters and Setters to map fields to alternative names
	 */
    public Integer getId() {
    	return this.addressHierarchyEntryId;
    }
    
    public void setId(Integer id) {
	    this.addressHierarchyEntryId = id;	    
    }
	
    public String getName() {
    	return this.name;
    }
    
    public void setName(String name) {
    	this.name = name;
    }
    
	public AddressHierarchyLevel getAddressHierarchyLevel() {
		return level;
	}
	
	public void setAddressHierarchyLevel(AddressHierarchyLevel level) {
		this.level = level;
	}
}
