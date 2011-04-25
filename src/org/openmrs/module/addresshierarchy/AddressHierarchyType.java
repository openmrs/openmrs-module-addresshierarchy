package org.openmrs.module.addresshierarchy;

import org.openmrs.BaseOpenmrsMetadata;

/**
 * Represents an Address Hierarchy type (ie., like "Country", or "State", or "City")
 */
public class AddressHierarchyType extends BaseOpenmrsMetadata {
	
	private Integer typeId;
	
	private String name;
	
	private AddressHierarchyType parentType;
	
	private AddressField addressField;
	
	/**
	 * To string
	 */
	public String toString() {
		String name = getName() + " " + getTypeId();
		return name;
	}
	
	public boolean equals(Object obj) {
		if (this.getId() == null)
			return false;
		if (obj instanceof AddressHierarchyType) {
			AddressHierarchyType c = (AddressHierarchyType) obj;
			return (this.getId().equals(c.getId()));
		}
		return false;
	}
	
	/**
	 * Getters and Setters
	 */
	
	public AddressHierarchyType getParentType() {
		return parentType;
	}
	
	public void setParentType(AddressHierarchyType parentType) {
		this.parentType = parentType;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setTypeId(Integer location_attribute_type_id) {
		this.typeId = location_attribute_type_id;
	}
	
	public String getName() {
		return this.name;
	}
	
	public Integer getTypeId() {
		return this.typeId;
	}

	public void setAddressField(AddressField addressField) {
	    this.addressField = addressField;
    }

	public AddressField getAddressField() {
	    return addressField;
    }

    public Integer getId() {
	    return this.typeId;
    }

    public void setId(Integer id) {
	   this.typeId = id;
    }

}
