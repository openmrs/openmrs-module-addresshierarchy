package org.openmrs.module.addresshierarchy;

/**
 * The Class AddressHierarchyType is linked to the table address_hierarchy_type table mapped in AddressHierarchyType.hbm.xml.
 */
public class AddressHierarchyType {
	private Integer typeId;
	private String name;
	
	/**
	 * To string.
	 * 
	 * @return the string
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString(){
		String name = getName()+" "+getTypeId();
		return name;
	}
	
	/**
	 * Sets the name.
	 * 
	 * @param name the new name
	 */
	public void setName(String name){
		this.name = name;
	}
	
	/**
	 * Sets the type id.
	 * 
	 * @param location_attribute_type_id the new type id
	 */
	public void setTypeId(Integer location_attribute_type_id){
		this.typeId = location_attribute_type_id;
	}
	
	/**
	 * Gets the name.
	 * 
	 * @return the name
	 */
	public String getName(){
		return this.name;
	}
	
	/**
	 * Gets the type id.
	 * 
	 * @return the type id
	 */
	public Integer getTypeId(){
		return this.typeId;
	}
	
	
}
