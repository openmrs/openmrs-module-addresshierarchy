/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.addresshierarchy;

import org.openmrs.BaseOpenmrsObject;
import org.openmrs.PersonAddress;

/**
 * A simple class to provide link between an Address and an Address Hierarchy Entry for reporting
 * purposes. This class is not automatically created when an address is saved, but can be set to
 * update changed PersonAddresses on a regular schedule
 */
public class AddressToEntryMap extends BaseOpenmrsObject {
	
	protected Integer personAddressToAddressHierarchyEntryMapId;
	
	private PersonAddress address;
	
	private AddressHierarchyEntry entry;
	
	/**
	 * Constructors
	 */
	public AddressToEntryMap() {
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
