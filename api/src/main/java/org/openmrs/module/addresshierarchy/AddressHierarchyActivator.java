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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.BaseModuleActivator;
import org.openmrs.module.ModuleActivator;
import org.openmrs.module.addresshierarchy.config.AddressConfigurationLoader;
import org.openmrs.module.addresshierarchy.service.AddressHierarchyService;

/**
 * This class contains the logic that is run every time this module
 * is either started or shutdown
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
