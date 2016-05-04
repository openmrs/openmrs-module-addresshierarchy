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
	 * Sub-classes should override this method instead of the run method to implement their logic
	 * The run method takes care of exception handling and authentication to the Context for you
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
			log.error("Error authenticating user. Please ensure you scheduler username and password are configured correctly in your global properties", e);
		}
	}
}
