package org.openmrs.module.addresshierarchy.web.controller.ajax;

import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.addresshierarchy.AddressHierarchyEntry;
import org.openmrs.module.addresshierarchy.service.AddressHierarchyService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Handles all the AJAX requests for this module
 */
@Controller
public class AddressHierarchyAjaxController {

	protected final Log log = LogFactory.getLog(getClass());
	
	
	/**
	 * Returns a list of Address Hierarchy Entry names in JSON format
	 * 
	 * If a parent entry is specified, it returns the names of all the children of the specified entry
	 * If no parent is specified, it returns the names for of all entries for the specified address hierarchy level
	 * 
	 * Note that a levelId is mandatory in either case specified in either case, since Address Hierarchy Entries may have the same name as long as they
	 * are on different levels of the hierarchy
	 */
	@RequestMapping("/module/addresshierarchy/ajax/getChildAddressHierarchyEntries.form")
	 public void getChildAddressHierarchyEntries(ModelMap model, HttpServletRequest request, HttpServletResponse response, 
					                             @RequestParam(value = "searchString", required = false) String searchString) throws Exception {
		
		AddressHierarchyService ahService = Context.getService(AddressHierarchyService.class);
		
		List<AddressHierarchyEntry> childEntries = null;
	
		if (StringUtils.isNotBlank(searchString)) {
			// if we have a search string, find the entry referenced
			AddressHierarchyEntry entry = ahService.searchAddressHierarchy(searchString);
			// now find all its children
			if (entry != null) {
				childEntries = ahService.getChildAddressHierarchyEntries(entry);
			}
		}
		else {
			// otherwise, if the search parameter is empty, we just want all the top level items
			childEntries = ahService.getAddressHierarchyEntriesAtTopLevel();
		}
			
    	response.setContentType("application/json");
    	response.setCharacterEncoding("UTF-8");
    	PrintWriter out = response.getWriter();

    	// start the JSON
    	out.print("[");

    	if (childEntries != null) {
			Collections.sort(childEntries);
    	
			// add the elements: ie, { "name": "Boston" }
			for (AddressHierarchyEntry e : childEntries) {
				out.print("{ \"name\": \"" + e.getName() + "\" },");
			}
    	}
    	
    	// close the JSON
		out.print("]");
	}
	
}
