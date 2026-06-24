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

import java.util.TimerTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.ContextAuthenticationException;

/**
 * Used as a base class for tasks we configure via build-in Spring scheduling
 */
public abstract class AbstractAddressHierarchyTask extends TimerTask {
	
	private static Log log = LogFactory.getLog(AbstractAddressHierarchyTask.class);
	
	/**
	 * Sub-classes should override this method instead of the run method to implement their logic The
	 * run method takes care of exception handling and authentication to the Context for you
	 */
	public abstract void execute();
	
	/**
	 * @see TimerTask#run()
	 */
	@Override
	public final void run() {
		try {
			Context.openSession();
			execute();
		}
		catch (Exception e) {
			log.error("An error occurred while running scheduled address hierarchy task", e);
		}
		finally {
			if (Context.isSessionOpen()) {
				Context.closeSession();
			}
		}
	}
	
	/**
	 * Authenticate the context so the task can call service layer.
	 */
	protected void authenticate() {
		try {
			AdministrationService adminService = Context.getAdministrationService();
			String userName = adminService.getGlobalProperty("scheduler.username");
			String password = adminService.getGlobalProperty("scheduler.password");
			Context.authenticate(userName, password);
		}
		catch (ContextAuthenticationException e) {
			log.error(
			    "Error authenticating user. Please ensure you scheduler username and password are configured correctly in your global properties",
			    e);
		}
	}
}
