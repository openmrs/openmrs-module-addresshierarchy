/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.addresshierarchy;

import org.openmrs.BaseOpenmrsObject;
import org.openmrs.PersonAddress;

/**
 * A simple class to provide link between an Address and an Address Hierarchy Entry 
 * for reporting purposes.  This class is not automatically created when an address is
 * saved, but can be set to update changed PersonAddresses on a regular schedule
 */
public class AddressToEntryMap extends BaseOpenmrsObject {
	
	protected Integer personAddressToAddressHierarchyEntryMapId;
	
	private PersonAddress address;
	
	private AddressHierarchyEntry entry;
	
	/**
	 * Constructors
	 */
	public AddressToEntryMap(){
	}

	public AddressToEntryMap(PersonAddress address, AddressHierarchyEntry entry) {
	    this.address = address;
	    this.entry = entry;
    }
	
	/**
	 * Getters and Setters
	 */
    public PersonAddress getAddress() {
    	return address;
    }
	
    public void setAddress(PersonAddress address) {
    	this.address = address;
    }

    public AddressHierarchyEntry getEntry() {
    	return entry;
    }
	
    public void setEntry(AddressHierarchyEntry entry) {
    	this.entry = entry;
    }

	public Integer getAddressToEntryMapId() {
	    return personAddressToAddressHierarchyEntryMapId;
    }

	public void setAddressToEntryMapId(Integer id) {	
		this.personAddressToAddressHierarchyEntryMapId = id;
    }
    
	public Integer getId() {
	    return personAddressToAddressHierarchyEntryMapId;
    }

	public void setId(Integer id) {	
		this.personAddressToAddressHierarchyEntryMapId = id;
    }
	
}
