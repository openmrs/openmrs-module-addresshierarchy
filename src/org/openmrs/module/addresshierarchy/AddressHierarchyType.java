package org.openmrs.module.addresshierarchy;

import org.openmrs.BaseOpenmrsMetadata;

/**
 * The Class AddressHierarchyType is linked to the table address_hierarchy_type table mapped in
 * AddressHierarchyType.hbm.xml.
 */
public class AddressHierarchyType extends BaseOpenmrsMetadata {
	
	private Integer typeId;
	
	private String name;
	
	private AddressHierarchyType parentType;
	
	private AddressHierarchyType childType;
	
	private AddressField addressField;
	
	/**
	 * To string
	 */
	public String toString() {
		String name = getName() + " " + getTypeId();
		return name;
	}
	
	/**
	 * Getters and Setters
	 */
	
	public AddressHierarchyType getChildType() {
		return childType;
	}
	
	public void setChildType(AddressHierarchyType childType) {
		this.childType = childType;
	}
	
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
