package org.openmrs.module.addresshierarchy.scheduler;

import java.util.TimerTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Daemon;
import org.openmrs.module.DaemonToken;

/**
 * Used as a base class for tasks we configure via build-in Spring scheduling
 */
public abstract class AbstractAddressHierarchyTask extends TimerTask {

	private static Log log = LogFactory.getLog(AbstractAddressHierarchyTask.class);
	
	private static DaemonToken daemonToken;
	
	private static boolean enabled = false;
	
	/**
	 * Sub-classes should override this method instead of the run method to implement their logic
	 * The run method takes care of exception handling and authentication to the Context for you
	 */
	
	private Class<? extends AbstractAddressHierarchyTask> taskClass;
	
	/**
	 * @see TimerTask#run()
	 */
	@Override
	public final void run() {
		if (daemonToken != null && enabled) {
			createAndRunTask();
		} else {
			log.warn("Not running scheduled task. DaemonToken = " + daemonToken + "; enabled = " + enabled);
		}
	}
	
	public synchronized void createAndRunTask() {
		try {
			log.info("Running AddressHierarchy Sheduler task: " + getClass().getSimpleName());;
			Daemon.runInDaemonThread(getRunnableTask(), daemonToken);
		} catch (Exception e) {
			log.error("An error occurred while running scheduled Address Hierarchy task", e);
		}
	}
	
	public abstract Runnable getRunnableTask();
	
	/**
	 * Sets the daemon token
	 */
	public static void setDaemonToken(DaemonToken token) {
		daemonToken = token;
	}
	
	public static boolean isEnabled() {
		return enabled;
	}
	
	public static void setEnabled(boolean enabled) {
		AbstractAddressHierarchyTask.enabled = enabled;
	}
	
}
