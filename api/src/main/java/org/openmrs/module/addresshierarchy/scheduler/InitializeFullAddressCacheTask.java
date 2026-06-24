/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.addresshierarchy.scheduler;

import org.openmrs.api.context.Context;
import org.openmrs.module.addresshierarchy.service.AddressHierarchyService;

/**
 * Calls the service method to initialize the full address cache (which only initialized cache if
 * necessary) (note that this method is scheduled via the Spring scheduler, not the OpenMRS
 * scheduler)
 */
public class InitializeFullAddressCacheTask extends AbstractAddressHierarchyTask {
	
	@Override
	public void execute() {
		try {
			Context.addProxyPrivilege("Get Global Properties");
			Context.getService(AddressHierarchyService.class).initializeFullAddressCache();
		}
		finally {
			Context.removeProxyPrivilege("Get Global Properties");
		}
	}
}
