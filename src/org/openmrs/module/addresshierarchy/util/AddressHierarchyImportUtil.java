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
import org.openmrs.module.addresshierarchy.AddressHierarchy;
import org.openmrs.module.addresshierarchy.AddressHierarchyService;
import org.openmrs.module.addresshierarchy.AddressHierarchyType;
import org.openmrs.module.addresshierarchy.exception.AddressHierarchyModuleException;


public class AddressHierarchyImportUtil {
	
	  protected static final Log log = LogFactory.getLog(AddressHierarchyImportUtil.class);
	
	/**
	 * Takes a file of delimited addresses and creates and address hierarchy out of it
	 * Starting level determines what level of the hierarchy to start at when doing the input
	 */
	public static final void importAddressHierarchyFile(InputStream stream, String delimiter, AddressHierarchyType startingLevel) {
		
		AddressHierarchyService ahService = Context.getService(AddressHierarchyService.class);
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(stream, Charset.forName("UTF-8")));
		
		String line;
		
		// get an ordered list of the address hierarchy types
		List<AddressHierarchyType> types = ahService.getAddressHierarchyTypes();
		
		// if we aren't starting at the top level of the hierarchy, remove all the levels before the one we wish to start at
		if (startingLevel != null) {
			Iterator<AddressHierarchyType> i = types.iterator();
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
		   
		        		// we only need to create a new entry if the location doesn't exist for the specific type
		        		if (ahService.searchHierarchy(locations[i], types.get(i).getId(), true).size() == 0) {
		        			
		        			// create the new entry and set its name and location
		        			AddressHierarchy entry = new AddressHierarchy();
		        			entry.setName(locations[i]);
		        			entry.setHierarchyType(types.get(i));
		        			
		        			// link to parent if we aren't at the first level
		        			if (i > 0) {
		        				entry.setParent(ahService.searchHierarchy(locations[i-1], types.get(i-1).getId(), true).get(0));
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
	}
	
	public static final void importAddressHierarchyFile(InputStream stream, String delimiter) {
		importAddressHierarchyFile(stream, delimiter, null);
	}
	
	/**
	 * Takes a CSV file of addresses and creates and address hierarchy out of it
	 */
	public static final void importCsvFile(InputStream stream, AddressHierarchyType startingLevel) {
		importAddressHierarchyFile(stream, ",", startingLevel);
	}
	
	public static final void importCsvFile(InputStream stream) {
		importCsvFile(stream, null);
	}
	
	
	
}
