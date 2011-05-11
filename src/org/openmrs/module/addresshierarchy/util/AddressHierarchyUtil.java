package org.openmrs.module.addresshierarchy.util;

import java.lang.reflect.Method;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.PersonAddress;
import org.openmrs.api.context.Context;
import org.openmrs.module.addresshierarchy.exception.AddressHierarchyModuleException;


public class AddressHierarchyUtil {

	protected static final Log log = LogFactory.getLog(AddressHierarchyUtil.class);
	
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

	/**
	 * Given a person address object, this method uses reflection fetch the value of the field
	 * specified by the string field name
	 */
	public static final String getAddressFieldValue(PersonAddress address, String fieldName) {		
		try {
	        Method getter = PersonAddress.class.getMethod("get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1));
	        return (String) getter.invoke(address);
	        
        }
        catch (Exception e) {
	        throw new AddressHierarchyModuleException("Unable to get address field " + fieldName + " off of PersonAddress", e);
        }
	}
}


