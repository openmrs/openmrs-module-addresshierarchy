package org.openmrs.module.addresshierarchy.util;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.PersonAddress;
import org.openmrs.api.context.Context;
import org.openmrs.module.addresshierarchy.AddressField;
import org.openmrs.module.addresshierarchy.AddressHierarchyEntry;
import org.openmrs.module.addresshierarchy.exception.AddressHierarchyModuleException;
import org.openmrs.module.addresshierarchy.service.AddressHierarchyService;


public class AddressHierarchyUtil {

	protected static final Log log = LogFactory.getLog(AddressHierarchyUtil.class);
	
	/**
	 * Fetches a global property and converts it to a Boolean value
	 * 
	 * @param globalPropertyName
	 * @return Boolean value of global property
	 */
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
	
	
	/**
	 * Converts a map of address field name to address value pairs to an actualy person address
	 * Auto generated method comment
	 * 
	 * @param addressMap
	 * @return
	 */
	public static final PersonAddress convertAddressMapToPersonAddress(Map<String,String> addressMap) {
		PersonAddress address = new PersonAddress();

		for (String addressFieldName : addressMap.keySet()) {
			setAddressFieldValue(address, AddressField.getByName(addressFieldName), addressMap.get(addressFieldName));
		}
		
		return address;
	}
	
	/**
	 * Tests whether the first addresses hierarchy entry is the descendant of the second--i.e., can you reach the second entry by travelling
	 * up the tree from the first hierarchy entry  
	 */
	public static final boolean isDescendantOf(AddressHierarchyEntry descendant, AddressHierarchyEntry ancestor) {
		// handle null case
		if (descendant == null || ancestor == null) {
			return false;
		}
		
		AddressHierarchyEntry parent = descendant.getParent();
		
		// cycle up the tree to until we either find a match or reach the top
		while (parent != null) {
			if (parent.equals(ancestor)) {
				return true;
			}
			
			parent = parent.getParent();
		}
		
		return false;
	}
	
	/**
	 * Given a Java Date, this method finds all the non-voided Patients that have
	 * a date changed after the specified date, and the updates the AddressToEntryMaps
	 * for all PersonAddresses associated with this patient
	 * 
	 * (Pulled this out the AddressHierarchyService so that the entire operation doesn't
	 * happen as part of a single transaction)
	 */
	public static final void updateAddressToEntryMapsForPatientsWithDateChangedAfter(Date date) {
		
		AddressHierarchyService ahService = Context.getService(AddressHierarchyService.class);
		
	    List<Patient> patients;
	    
	    log.info("Updating AddressToEntryMaps for all patients with date changed after " + date);
	    
	    if (date != null) {
	    	patients = ahService.findAllPatientsWithDateChangedAfter(date);
	    }
	    else {
	    	patients = Context.getPatientService().getAllPatients();
	    }
	    	    
	    if (patients != null && patients.size() > 0) {
	    	for (Patient patient : patients) {
	    		ahService.updateAddressToEntryMapsForPerson(patient);
	    	}
	    }
    }
}


