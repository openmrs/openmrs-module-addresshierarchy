package org.openmrs.module.addresshierarchy.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.addresshierarchy.AddressHierarchyEntry;
import org.openmrs.module.addresshierarchy.AddressHierarchyLevel;
import org.openmrs.module.addresshierarchy.exception.AddressHierarchyModuleException;
import org.openmrs.module.addresshierarchy.service.AddressHierarchyService;


public class AddressHierarchyImportUtil {
	
	  protected static final Log log = LogFactory.getLog(AddressHierarchyImportUtil.class);
	
	/**
	 * Takes a file of delimited addresses and creates and address hierarchy out of it
	 * Starting level determines what level of the hierarchy to start at when doing the input
	 */
	public static final void importAddressHierarchyFile(InputStream stream, String delimiter, AddressHierarchyLevel startingLevel) {
		
		AddressHierarchyService ahService = Context.getService(AddressHierarchyService.class);
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(stream, Charset.forName("UTF-8")));
		
		String line;
		
		// get an ordered list of the address hierarchy levels
		List<AddressHierarchyLevel> levels = ahService.getOrderedAddressHierarchyLevels();
		
		// if we aren't starting at the top level of the hierarchy, remove all the levels before the one we wish to start at
		if (startingLevel != null) {
			Iterator<AddressHierarchyLevel> i = levels.iterator();
			while (i.next() != startingLevel) {
				i.remove();
			}
		}
		
		try {
			// step through the file line by line
	        while ((line = reader.readLine()) != null) {
	        	// now split the line up by the delimiter
	        	String [] locations = line.split(delimiter);
	        	
	        	if (locations != null) {
		        	// now cycle through all the locations on this line
		        	for (int i = 0; i < locations.length; i++) {
		   
		        		// we only need to create a new entry if the location doesn't exist for the specific level
		        		if (ahService.searchHierarchy(locations[i], levels.get(i).getId(), true).size() == 0) {
		        			
		        			// create the new entry and set its name and location
		        			AddressHierarchyEntry entry = new AddressHierarchyEntry();
		        			entry.setName(locations[i]);
		        			entry.setLevel(levels.get(i));
		        			
		        			// link to parent if we aren't at the first level
		        			if (i > 0) {
		        				entry.setParent(ahService.searchHierarchy(locations[i-1], levels.get(i-1).getId(), true).get(0));
		        			}
		        			
		        			// save the new entry
		        			ahService.saveAddressHierarchyEntry(entry);
		        		}
		        	}
	        	}
	        }
        }
        catch (IOException e) { 
	        throw new AddressHierarchyModuleException("Error accessing address hierarchy import stream ", e);
        }
        catch (IndexOutOfBoundsException e) {
        	throw new AddressHierarchyModuleException("Error importing address hierarchy entries. Have you defined your address hierarchy levels?",e);
        }
	}
	
	public static final void importAddressHierarchyFile(InputStream stream, String delimiter) {
		importAddressHierarchyFile(stream, delimiter, null);
	}
	
	/**
	 * Takes a CSV file of addresses and creates and address hierarchy out of it
	 */
	public static final void importCsvFile(InputStream stream, AddressHierarchyLevel startingLevel) {
		importAddressHierarchyFile(stream, ",", startingLevel);
	}
	
	public static final void importCsvFile(InputStream stream) {
		importCsvFile(stream, null);
	}
	
	
	
}
