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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.BaseModuleActivator;
import org.openmrs.module.ModuleActivator;
import org.openmrs.module.addresshierarchy.config.AddressConfigurationLoader;
import org.openmrs.module.addresshierarchy.service.AddressHierarchyService;

/**
 * This class contains the logic that is run every time this module is either started or shutdown
 */
public class AddressHierarchyActivator extends BaseModuleActivator implements ModuleActivator {
	
	private Log log = LogFactory.getLog(this.getClass());
	
	@Override
	public void started() {
		log.info("AddressHierarchy Module Started");
		AddressConfigurationLoader.loadAddressConfiguration();
	}
	
	@Override
	public void stopped() {
		log.info("AddressHierarchy Module Stopped");
	}
	
	@Override
	public void contextRefreshed() {
		// initialize the caches on module startup
		Context.getService(AddressHierarchyService.class).initializeFullAddressCache();
		Context.getService(AddressHierarchyService.class).initI18nCache();
	}
}
