package org.openmrs.module.addresshierarchy;

import java.util.regex.Pattern;


public class AddressHierarchyConstants {

	/*  Address Hierarchy Module Privileges */
	public static final String PRIV_MANAGE_ADDRESS_HIERARCHY = "Manage Address Hierarchy";

	/* Address Hierarchy Global Properties */
	public static final String GLOBAL_PROP_SOUNDEX_PROCESSER = "addresshierarchy.soundexProcessor";
	
	public static final String GLOBAL_PROP_ADDRESS_TO_ENTRY_MAP_UPDATER_LAST_START_TIME = "addresshierarchy.addressToEntryMapUpdaterLastStartTime";

    public static final String GLOBAL_PROP_INITIALIZE_ADDRESS_HIERARCHY_CACHE_ON_STARTUP = "addresshierarchy.initializeAddressHierarchyCacheOnStartup";

	/* Name of the task that updates the address toe entry map */
	public static final String TASK_NAME_ADDRESS_TO_ENTRY_MAP_UPDATER = "Address To Entry Map Updater";
	public static final String TASK_CLASS_ADDRESS_TO_ENTRY_MAP_UPDATER = "org.openmrs.module.addresshierarchy.scheduler.AddressToEntryMapsUpdaterTask";
	public static final Long TASK_PARAMETER_ADDRESS_ENTRY_MAP_DEFAULT_REPEAT_INTERVAL = new Long(86400);   // default repeat interval is once every 24 hours (24 hours = 86400 seconds)
	
	/* Precompile some standard Patterns that we are using */
	public static final Pattern PATTERN_NON_WORD_AND_NON_WHITESPACE = Pattern.compile("[\\/\\\\\"'`~@#$%^&*()+{}\\[\\]<>,.!?-]+");   // matches sets of 1 or more of the characters: /\"'`~@#$%^&*()+{}[]<>,.!?-
	public static final Pattern PATTERN_ANY_DIGIT = Pattern.compile("[\\d]+");  // matches one or more digits (0-9)
	
	// TODO: add some other global property that are currently directly referenced within the code
}