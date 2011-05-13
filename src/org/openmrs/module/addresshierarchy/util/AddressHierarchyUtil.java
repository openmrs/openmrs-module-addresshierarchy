package org.openmrs.module.addresshierarchy.util;

import java.lang.reflect.Method;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.PersonAddress;
import org.openmrs.api.context.Context;
import org.openmrs.module.addresshierarchy.AddressField;
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
	 * Given a person address object, this method uses reflection fetch the value of the specified field
	 */
	public static final String getAddressFieldValue(PersonAddress address, AddressField field) {		
		try {
	        Method getter = PersonAddress.class.getMethod("get" + field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1));
	        return (String) getter.invoke(address);
	        
        }
        catch (Exception e) {
	        throw new AddressHierarchyModuleException("Unable to get address field " + field.getName() + " off of PersonAddress", e);
        }
	}
	
	/**
	 * Given a person address object, this method uses reflection to set the value of the specified field
	 */
	public static final  void setAddressFieldValue(PersonAddress address, AddressField field, String value) {
		try {
			Method setter = PersonAddress.class.getMethod("set" + field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1), String.class);
			setter.invoke(address, value);
		}
		catch (Exception e) {
	        throw new AddressHierarchyModuleException("Unable to set address field " + field.getName() + " on PersonAddress", e);
        }
	}
}


