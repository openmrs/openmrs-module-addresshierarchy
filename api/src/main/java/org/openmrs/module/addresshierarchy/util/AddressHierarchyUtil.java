package org.openmrs.module.addresshierarchy.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.PersonAddress;
import org.openmrs.api.context.Context;
import org.openmrs.module.addresshierarchy.AddressField;
import org.openmrs.module.addresshierarchy.AddressHierarchyEntry;
import org.openmrs.module.addresshierarchy.exception.AddressHierarchyModuleException;
import org.openmrs.module.addresshierarchy.service.AddressHierarchyService;

import java.lang.reflect.Method;
import java.text.Normalizer;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;


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
	    	    
	    if (patients != null && !patients.isEmpty()) {
	    	for (Patient patient : patients) {
	    		ahService.updateAddressToEntryMapsForPerson(patient);
	    	}
	    }
    }

    /**
     * (This method is copied from org.apache.commons.lang3.StringUtils -- unfortunately, the lang3 library
     *  is only included in OpenMRS 1.9; I didn't want to import the class into address hierarchy module
     *  directly for fear of having classloading issues when running on OpenMRS 1.9; once Address Hierarchy
     *  switches to only support OpenMRS 1.9+, we can import lang3 and delete this method)
     *
     * <p>Removes diacritics (~= accents) from a string. The case will not be altered.</p>
     * <p>For instance, '&agrave;' will be replaced by 'a'.</p>
     * <p>Note that ligatures will be left as is.</p>
     *
     * <pre>
     * StringUtils.stripAccents(null)                = null
     * StringUtils.stripAccents("")                  = ""
     * StringUtils.stripAccents("control")           = "control"
     * StringUtils.stripAccents("&eacute;clair")     = "eclair"
     * </pre>
     *
     * @param input String to be stripped
     * @return input text with diacritics removed
     *
     * @since 3.0
     */
    // See also Lucene's ASCIIFoldingFilter (Lucene 2.9) that replaces accented characters by their unaccented equivalent (and uncommitted bug fix: https://issues.apache.org/jira/browse/LUCENE-1343?focusedCommentId=12858907&page=com.atlassian.jira.plugin.system.issuetabpanels%3Acomment-tabpanel#action_12858907).
    public static String stripAccents(String input) {
        if(input == null) {
            return null;
        }
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");//$NON-NLS-1$
        String decomposed = Normalizer.normalize(input, Normalizer.Form.NFD);
        // Note that this doesn't correctly remove ligatures...
        return pattern.matcher(decomposed).replaceAll("");//$NON-NLS-1$
    }

}


