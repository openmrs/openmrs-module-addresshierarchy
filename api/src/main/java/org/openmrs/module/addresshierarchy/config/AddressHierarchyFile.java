/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.addresshierarchy.config;

import org.openmrs.util.OpenmrsUtil;

/**
 * Represents the configuration of file containing address hierarchy entries to load
 */
public class AddressHierarchyFile {
	
	private String filename;
	
	private String entryDelimiter = "|";
	
	private String identifierDelimiter = "^";
	
	public AddressHierarchyFile() {
	}
	
	public String getFilename() {
		return filename;
	}
	
	public void setFilename(String filename) {
		this.filename = filename;
	}
	
	public String getEntryDelimiter() {
		return entryDelimiter;
	}
	
	public void setEntryDelimiter(String entryDelimiter) {
		this.entryDelimiter = entryDelimiter;
	}
	
	public String getIdentifierDelimiter() {
		return identifierDelimiter;
	}
	
	public void setIdentifierDelimiter(String identifierDelimiter) {
		this.identifierDelimiter = identifierDelimiter;
	}
	
	@Override
	public int hashCode() {
		int ret = 17;
		ret = 31 * ret + (filename == null ? 0 : filename.hashCode());
		ret = 31 * ret + (entryDelimiter == null ? 0 : entryDelimiter.hashCode());
		ret = 31 * ret + (identifierDelimiter == null ? 0 : identifierDelimiter.hashCode());
		return ret;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof AddressHierarchyFile)) {
			return false;
		}
		AddressHierarchyFile that = (AddressHierarchyFile) obj;
		boolean ret = true;
		ret = ret && OpenmrsUtil.nullSafeEquals(this.getFilename(), that.getFilename());
		ret = ret && OpenmrsUtil.nullSafeEquals(this.getEntryDelimiter(), that.getEntryDelimiter());
		ret = ret && OpenmrsUtil.nullSafeEquals(this.getIdentifierDelimiter(), that.getIdentifierDelimiter());
		return ret;
	}
}
