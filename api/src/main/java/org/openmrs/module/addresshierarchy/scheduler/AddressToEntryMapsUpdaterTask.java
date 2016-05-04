package org.openmrs.module.addresshierarchy.scheduler;

import java.text.DateFormat;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.GlobalProperty;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.module.addresshierarchy.AddressHierarchyConstants;
import org.openmrs.module.addresshierarchy.util.AddressHierarchyUtil;
import org.openmrs.scheduler.tasks.AbstractTask;


public class AddressToEntryMapsUpdaterTask extends AbstractTask {

	private static Log log = LogFactory.getLog(AddressToEntryMapsUpdaterTask.class);
	
	// TODO: are these tasks thread safe?
	
	private GlobalProperty lastStartTimeGlobalProp = null;
	
	private Date lastStartTime = null;
	
	@Override
    public void execute() {
		Context.openSession();
		log.info("Starting update of AddressToEntryMaps... ");
		try {
			
			// get the last time this task started (which we are storing in a global property)
			lastStartTimeGlobalProp = Context.getAdministrationService().getGlobalPropertyObject(AddressHierarchyConstants.GLOBAL_PROP_ADDRESS_TO_ENTRY_MAP_UPDATER_LAST_START_TIME);
			
			if (StringUtils.isNotBlank(lastStartTimeGlobalProp.getPropertyValue())) {
				lastStartTime = DateFormat.getInstance().parse(lastStartTimeGlobalProp.getPropertyValue());
			}
			
			// now set the new last start time as the current time
			lastStartTimeGlobalProp.setPropertyValue(DateFormat.getInstance().format(new Date()));
			Context.getAdministrationService().saveGlobalProperty(lastStartTimeGlobalProp);
			
			// update all the patients that have changed since last execution of this task (lastStartTime will be null on first execution, so it will check all records to date)
			AddressHierarchyUtil.updateAddressToEntryMapsForPatientsWithDateChangedAfter(lastStartTime);
		}
		catch (Throwable t) {
			// set the last start time back to the original one since this task didn't complete properly
			if (lastStartTime == null) {
				lastStartTimeGlobalProp.setPropertyValue(null);
			}
			else {
				lastStartTimeGlobalProp.setPropertyValue(DateFormat.getInstance().format(lastStartTime));
			}
			Context.getAdministrationService().saveGlobalProperty(lastStartTimeGlobalProp);
			
			
			log.error("Error while updating AddressToEntryMaps", t);
			throw new APIException(t);
		}
		finally {
			Context.closeSession();
			lastStartTime = null;
			lastStartTimeGlobalProp = null;
		}
    }
	
}
