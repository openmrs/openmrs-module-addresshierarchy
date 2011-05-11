package org.openmrs.module.addresshierarchy.util;

import org.openmrs.api.context.Context;
import org.openmrs.module.addresshierarchy.exception.AddressHierarchyModuleException;


public class AddressHierarchyUtil {

	public static final Boolean getGlobalPropertyAsBoolean(String globalPropertyName) {
		
		String globalPropertyValue = Context.getAdministrationService().getGlobalProperty(globalPropertyName, "true");
		
		if (globalPropertyValue.equalsIgnoreCase("true")) {
			return true;
		}
		
		if (globalPropertyValue.equalsIgnoreCase("false")) {
			return false;
		}

		throw new AddressHierarchyModuleException("Global property " + globalPropertyName + " must be set to either 'true' or 'false'.");
	}
	
}
