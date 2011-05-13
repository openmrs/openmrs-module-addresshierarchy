package org.openmrs.module.addresshierarchy.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.io.UnicodeInputStream;
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
		
		// TODO: enforce that top level is unique, but others do not need to be?
		AddressHierarchyService ahService = Context.getService(AddressHierarchyService.class);
		
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
			// Note that we are using UnicodeInputStream to work around this Java bug: http://bugs.sun.com/view_bug.do?bug_id=4508058 
			BufferedReader reader = new BufferedReader(new InputStreamReader(new UnicodeInputStream(stream), Charset.forName("UTF-8")));
			
			// step through the file line by line
	        while ((line = reader.readLine()) != null) {
	        	
	        	// now split the line up by the delimiter
	        	String [] locations = line.split(delimiter);
	        	
	        	if (locations != null) {
	        	
	        		Stack<AddressHierarchyEntry> entryStack = new Stack<AddressHierarchyEntry>();
	        		
		        	// now cycle through all the locations on this line
		        	for (int i = 0; i < locations.length; i++) {
		        	
		        		// create a new level if we need it
	        			if (levels.size() == i) {
	        				levels.add(ahService.addAddressHierarchyLevel());
	        			}
		        		
	        			String trimmedLocation = StringUtils.trim(locations[i]);
	        			
		        		// fetch the entry associated with this location
		        		AddressHierarchyEntry entry = ahService.getChildAddressHierarchyEntryByName(entryStack.isEmpty() ? null : entryStack.peek(), trimmedLocation);		
		        		
		        		// create this entry if need be
		        		if (entry == null) {
		        			
		        			// create the new entry and set its name, location and parent
		        			entry = new AddressHierarchyEntry();
		        			entry.setName(trimmedLocation);
		        			entry.setLevel(levels.get(i));
		        			entry.setParent(entryStack.isEmpty() ? null : entryStack.peek());
		        			
		        			// save the new entry
		        			ahService.saveAddressHierarchyEntry(entry);
		        		}
		        		
		        		// push this entry onto the stack on the stack
	        			entryStack.push(entry);
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
	public static final void importCsvFile(InputStream stream, AddressHierarchyLevel startingLevel) {
		importAddressHierarchyFile(stream, ",", startingLevel);
	}
	
	public static final void importCsvFile(InputStream stream) {
		importCsvFile(stream, null);
	}
	
	
	
}
