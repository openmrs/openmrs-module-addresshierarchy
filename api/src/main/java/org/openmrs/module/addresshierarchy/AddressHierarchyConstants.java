package org.openmrs.module.addresshierarchy;


public class AddressHierarchyConstants {

	/*  Address Hierarchy Module Privileges */
	public static final String PRIV_MANAGE_ADDRESS_HIERARCHY = "Manage Address Hierarchy";

	/* Address Hierarchy Global Properties */
	public static final String GLOBAL_PROP_SOUNDEX_PROCESSER = "addresshierarchy.soundexProcessor";

	public static final String GLOBAL_PROP_ADDRESS_TO_ENTRY_MAP_UPDATER_LAST_START_TIME = "addresshierarchy.addressToEntryMapUpdaterLastStartTime";

	/* Name of the task that updates the address toe entry map */
	public static final String TASK_NAME_ADDRESS_TO_ENTRY_MAP_UPDATER = "Address To Entry Map Updater";
	public static final String TASK_CLASS_ADDRESS_TO_ENTRY_MAP_UPDATER = "org.openmrs.module.addresshierarchy.scheduler.AddressToEntryMapsUpdaterTask";
	public static final Long TASK_PARAMETER_ADDRESS_ENTRY_MAP_DEFAULT_REPEAT_INTERVAL = new Long(86400);   // default repeat interval is once every 24 hours (24 hours = 86400 seconds)
	
	// TODO: add some other global property that are currently directly referenced within the code
}
