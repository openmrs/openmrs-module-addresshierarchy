package org.openmrs.module.addresshierarchy.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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
	  
	  // number of entries to save at one time
	  // we want to save in batches to improve performance, but if try to save ALL at once we can run into memory issues
	  protected static final int ENTRY_BATCH_SIZE = 10;
	
	/**
	 * Takes a file of delimited addresses and creates and address hierarchy out of it
	 * Starting level determines what level of the hierarchy to start at when doing the input
	 */
	public static final void importAddressHierarchyFile(InputStream stream, String delimiter, AddressHierarchyLevel startingLevel) {
		
		AddressHierarchyService ahService = Context.getService(AddressHierarchyService.class);
		
		String line;
		
		// to let us know if we even need to query the database (to speed up performance)
		Boolean hasExistingEntries = ahService.getAddressHierarchyEntryCount() > 0 ? true : false;
		
		 // a cache we use to speed up performance
		Map<AddressHierarchyEntry,Map<String,AddressHierarchyEntry>> entryCache = new HashMap<AddressHierarchyEntry,Map<String,AddressHierarchyEntry>>(); 
		
		// the list of all address hierarchy entries
		List<AddressHierarchyEntry> entries = new LinkedList<AddressHierarchyEntry>();
		
		// get an ordered list of the address hierarchy levels
		List<AddressHierarchyLevel> levels = ahService.getOrderedAddressHierarchyLevels();
		
		// if we aren't starting at the top level of the hierarchy, remove all the levels before the one we wish to start at
		if (startingLevel != null) {
			Iterator<AddressHierarchyLevel> i = levels.iterator();
			while (i.next() != startingLevel) {
				i.remove();
			}
		}
		
		// process the file
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
		        		
	        			String strippedLocation = StringUtils.strip(StringUtils.trim(locations[i]));
	        			
	        			AddressHierarchyEntry entry = null;
	        			AddressHierarchyEntry parent = entryStack.isEmpty() ? null : entryStack.peek();
	        			
	        			// first see if this entry already exists in the cache
	        			if (entryCache.containsKey(parent) && entryCache.get(parent).containsKey(strippedLocation.toLowerCase())) {
	        				entry = entryCache.get(parent).get(strippedLocation.toLowerCase());
	        			}
	        			// if it is not in the cache, see if it is in the database if there are existing entries
	        			else if (hasExistingEntries) {
	            			entry = ahService.getChildAddressHierarchyEntryByName(parent, strippedLocation);
	            			// if we have found an entry, add it to the cache
	            			if (entry != null) {
	            				addToCache(entryCache, parent, entry);
	            			}
	        			}
	        			    		
		        		// if we still haven't found the entry, we need to create it
		        		if (entry == null) {
		        			
		        			// create the new entry and set its name, location and parent
		        			entry = new AddressHierarchyEntry();
		        			entry.setName(strippedLocation);
		        			entry.setLevel(levels.get(i));
		        			entry.setParent(parent);
		        			
		        			// add the entry to the list to add, and add it to the cache
		        			entries.add(entry);
		        			addToCache(entryCache, parent, entry);
		        			
		        		}
		        		// push this entry onto the stack
	        			entryStack.push(entry);
		        	}
	        	}
	        }  
        }
        catch (IOException e) { 
	        throw new AddressHierarchyModuleException("Error accessing address hierarchy import stream ", e);
        }
        
		log.info(entries.size() + " address hierarchy entries to save");
		
        // now do the actual save, broken up into batches
		int batchStart = 0;
		int batchEnd = ENTRY_BATCH_SIZE; 
	
		while (batchEnd <= entries.size()) {
			ahService.saveAddressHierarchyEntries(entries.subList(batchStart, batchEnd));
			batchStart = batchEnd;
			batchEnd = batchEnd + ENTRY_BATCH_SIZE;
		}
		
		if (batchStart < entries.size()) {
			ahService.saveAddressHierarchyEntries(entries.subList(batchStart, entries.size()));
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
	
	
	/**
	 * Utility methods
	 */
	public static final void addToCache(Map<AddressHierarchyEntry,Map<String,AddressHierarchyEntry>> entryCache, 
	                                    AddressHierarchyEntry parent, AddressHierarchyEntry entry) {
		if (!entryCache.containsKey(parent)) {
			entryCache.put(parent, new HashMap<String,AddressHierarchyEntry>());
		}
		entryCache.get(parent).put(entry.getName().toLowerCase(), entry);
	}
}
