package org.openmrs.module.addresshierarchy;

import org.openmrs.GlobalProperty;
import org.openmrs.api.GlobalPropertyListener;
import org.openmrs.api.context.Context;
import org.openmrs.module.addresshierarchy.service.AddressHierarchyService;


/**
 * Listens for all events that should trigger a reset of the Address Cache
 * (currently this is only when the addresshierarchy.soundexProcessor global property
 * is changed)
 */
public class AddressCacheResetSupport implements GlobalPropertyListener {

	public void globalPropertyChanged(GlobalProperty globalProperty) {
		if (AddressHierarchyConstants.GLOBAL_PROP_SOUNDEX_PROCESSER.equalsIgnoreCase(globalProperty.getProperty())) {
	    	Context.getService(AddressHierarchyService.class).resetFullAddressCache();
	    }
    }

	public void globalPropertyDeleted(String propertyName) {
	    if (AddressHierarchyConstants.GLOBAL_PROP_SOUNDEX_PROCESSER.equalsIgnoreCase(propertyName)) {
	    	Context.getService(AddressHierarchyService.class).resetFullAddressCache();
	    }
    }

	public boolean supportsPropertyName(String propertyName) {
	  return AddressHierarchyConstants.GLOBAL_PROP_SOUNDEX_PROCESSER.equalsIgnoreCase(propertyName);
    }
	
}
