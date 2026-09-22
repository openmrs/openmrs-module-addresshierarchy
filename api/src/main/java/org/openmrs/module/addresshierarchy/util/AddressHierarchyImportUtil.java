/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.addresshierarchy.util;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.io.UnicodeInputStream;
import org.openmrs.api.context.Context;
import org.openmrs.module.addresshierarchy.AddressHierarchyEntry;
import org.openmrs.module.addresshierarchy.AddressHierarchyLevel;
import org.openmrs.module.addresshierarchy.exception.AddressHierarchyModuleException;
import org.openmrs.module.addresshierarchy.service.AddressHierarchyService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class AddressHierarchyImportUtil {
	
	protected static final Log log = LogFactory.getLog(AddressHierarchyImportUtil.class);
	
	/**
	 * Takes a file of delimited addresses and creates and address hierarchy out of it Starting level
	 * determines what level of the hierarchy to start at when doing the input
	 */
	public static final void importAddressHierarchyFile(InputStream stream, String delimiter,
	        String userGeneratedIdDelimiter, AddressHierarchyLevel startingLevel) {
		
		AddressHierarchyService ahService = Context.getService(AddressHierarchyService.class);
		
		String line;
		
		// a cache we use to speed up performance
		Map<AddressHierarchyEntry, Map<String, AddressHierarchyEntry>> entryCache = new HashMap<AddressHierarchyEntry, Map<String, AddressHierarchyEntry>>();

		// the entries the file describes that do not exist yet, parents always ahead of their children
		List<AddressHierarchyEntry> entries = new ArrayList<AddressHierarchyEntry>();

		// user generated ids the file assigns to entries that already exist. Those entries are detached, so the
		// change has to be collected here and written as an update rather than riding along on an insert.
		Map<Integer, String> userGeneratedIdUpdates = new HashMap<Integer, String>();

		// get an ordered list of the address hierarchy levels
		List<AddressHierarchyLevel> levels = ahService.getOrderedAddressHierarchyLevels();

		// resolving each entry against the database as the file was read made importing over an existing
		// hierarchy quadratic, so the hierarchy is pulled into memory once up front instead
		for (AddressHierarchyEntry entry : ahService.getDetachedAddressHierarchyEntries()) {
			addToCache(entryCache, entry.getParent(), entry);
		}

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
			BufferedReader reader = new BufferedReader(
			        new InputStreamReader(new UnicodeInputStream(stream), Charset.forName("UTF-8")));
			
			// step through the file line by line
			while ((line = reader.readLine()) != null) {
				
				if (StringUtils.isNotBlank(line)) {
					// now split the line up by the delimiter
					String[] locations = line.split(delimiter);
					
					if (locations != null) {
						
						Stack<AddressHierarchyEntry> entryStack = new Stack<AddressHierarchyEntry>();
						
						// now cycle through all the locations on this line
						for (int i = 0; i < locations.length; i++) {
							
							// create a new level if we need it
							if (levels.size() == i) {
								levels.add(ahService.addAddressHierarchyLevel());
							}
							
							String[] entryNameAndIdPair = splitIntoNameAndUserGeneratedId(StringUtils.trim(locations[i]),
							    userGeneratedIdDelimiter);
							
							AddressHierarchyEntry entry = null;
							AddressHierarchyEntry parent = entryStack.isEmpty() ? null : entryStack.peek();
							
							// the cache holds the pre-existing hierarchy as well as everything created by this
							// import, so a miss here means the entry does not exist yet and must be created
							Map<String, AddressHierarchyEntry> siblings = entryCache.get(parent);
							if (siblings != null) {
								entry = siblings.get(entryNameAndIdPair[0].toLowerCase());
							}
							
							// if we still haven't found the entry, we need to create it
							if (entry == null) {
								// create the new entry and set its name, location and parent
								entry = new AddressHierarchyEntry();
								entry.setName(entryNameAndIdPair[0]);
								entry.setLevel(levels.get(i));
								entry.setParent(parent);
								
								// add the entry to the list to add, and add it to the cache
								entries.add(entry);
								addToCache(entryCache, parent, entry);
							}
							
							// update/set the user defined id if one has been specified. A file is allowed to name
							// the same entry more than once with different ids, and the last one read wins, so
							// an entry that already exists records the change for the update pass each time.
							if (entryNameAndIdPair.length > 1) {
								if (entry.getId() != null
								        && !entryNameAndIdPair[1].equals(entry.getUserGeneratedId())) {
									userGeneratedIdUpdates.put(entry.getId(), entryNameAndIdPair[1]);
								}
								entry.setUserGeneratedId(entryNameAndIdPair[1]);
							}
							
							// push this entry onto the stack
							entryStack.push(entry);
						}
					}
				}
			}
		}
		catch (IOException e) {
			throw new AddressHierarchyModuleException("Error accessing address hierarchy import stream ", e);
		}
		
		log.info(entries.size() + " address hierarchy entries to save");

		// one call, so the whole hierarchy lands in a single transaction and a failure part way through cannot
		// leave the hierarchy half loaded
		ahService.bulkSaveAddressHierarchyEntries(entries, userGeneratedIdUpdates);
	}
	
	public static final void importAddressHierarchyFile(InputStream stream, String delimiter,
	        String userGeneratedIdDelimiter) {
		importAddressHierarchyFile(stream, delimiter, userGeneratedIdDelimiter, null);
	}
	
	public static final void importAddressHierarchyFile(InputStream stream, String delimiter) {
		importAddressHierarchyFile(stream, delimiter, null);
	}
	
	/**
	 * Splits one cell of the file into the entry name and, where the file carries them, the user generated id.
	 *
	 * @return an array holding just the name, or the name followed by the user generated id. A length greater
	 *         than one therefore means the file really did specify an id for this entry.
	 */
	private static final String[] splitIntoNameAndUserGeneratedId(String location, String userGeneratedIdDelimiter) {

		// only need to split out into name and id if we have a user generated id delimiter
		if (StringUtils.isNotBlank(userGeneratedIdDelimiter)) {
			String[] entryNameAndIdPair = location.split(userGeneratedIdDelimiter);
			entryNameAndIdPair[0] = StringUtils.strip(entryNameAndIdPair[0]);

			if (entryNameAndIdPair.length > 1) {
				entryNameAndIdPair[1] = StringUtils.strip(entryNameAndIdPair[1]);
			}

			return entryNameAndIdPair;
		}

		return new String[] { StringUtils.strip(location) };
	}
	
	private static final void addToCache(Map<AddressHierarchyEntry, Map<String, AddressHierarchyEntry>> entryCache,
	        AddressHierarchyEntry parent, AddressHierarchyEntry entry) {
		entryCache.computeIfAbsent(parent, k -> new HashMap<String, AddressHierarchyEntry>())
		        .put(entry.getName().toLowerCase(), entry);
	}
	
}
