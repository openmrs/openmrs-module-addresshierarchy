package org.openmrs.module.addresshierarchy.scheduler;

import org.openmrs.api.context.Context;
import org.openmrs.module.addresshierarchy.service.AddressHierarchyService;

/** 
 * Calls the service method to initialize the full address cache (which only initialized cache if necessary)
 * (note that this method is scheduled via the Spring scheduler, not the OpenMRS scheduler) 
 */
public class InitializeFullAddressCacheTask extends AbstractAddressHierarchyTask {
	
	
	@Override
	public Runnable getRunnableTask() {
		return new RunnableTask();
	}
	
	private class RunnableTask implements Runnable {
		
		@Override
		public void run() {
			Context.getService(AddressHierarchyService.class).initializeFullAddressCache();
		}
	}
}
