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

import org.openmrs.GlobalProperty;
import org.openmrs.api.GlobalPropertyListener;
import org.openmrs.api.context.Context;
import org.openmrs.module.addresshierarchy.service.AddressHierarchyService;

/**
 * Listens for all events that should trigger a reset of the Address Cache (currently this is only
 * when the addresshierarchy.soundexProcessor global property is changed)
 */
public class AddressCacheResetSupport implements GlobalPropertyListener {
	
	public void globalPropertyChanged(GlobalProperty globalProperty) {
		if (AddressHierarchyConstants.GLOBAL_PROP_SOUNDEX_PROCESSER.equalsIgnoreCase(globalProperty.getProperty())) {
			Context.getService(AddressHierarchyService.class).resetFullAddressCache();
		}
	}
	
	public void globalPropertyDeleted(String propertyName) {
		if (AddressHierarchyConstants.GLOBAL_PROP_SOUNDEX_PROCESSER.equalsIgnoreCase(propertyName)) {
			Context.getService(AddressHierarchyService.class).resetFullAddressCache();
		}
	}
	
	public boolean supportsPropertyName(String propertyName) {
		return AddressHierarchyConstants.GLOBAL_PROP_SOUNDEX_PROCESSER.equalsIgnoreCase(propertyName);
	}
}
