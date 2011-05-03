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
import org.openmrs.module.addresshierarchy.exception.AddressHierarchyModuleException;
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
					                             @RequestParam(value = "parentEntry", required = false) String parentEntry,
					                             @RequestParam("addressHierarchyLevel") Integer levelId) throws Exception {
		
		AddressHierarchyService ahService = Context.getService(AddressHierarchyService.class);
		
		List<AddressHierarchyEntry> childEntries = null;
		
		// if parent entry is null, this means we want all the entries at the specified level
		if (StringUtils.isBlank(parentEntry)) {
			childEntries = ahService.getAddressHierarchyEntriesByLevel(levelId);
		}
		// otherwise, find the parent entry and then its children
		else {			
			List<AddressHierarchyEntry> entries = ahService.searchHierarchy(parentEntry, levelId, true);
			
			// this should be an exact match, so throw an exception if we get multiple matches
			if (entries.size() > 1) {
				throw new AddressHierarchyModuleException("Parent entry string '" + parentEntry + "' has duplicate Address Hierarchy Entry matches");
			}
			
			// fetch the child entries of this entries
			if (entries.size() == 1) {
				childEntries = ahService.getChildAddressHierarchyEntries(entries.get(0));
			}
		}
				
		Collections.sort(childEntries);
		
    	response.setContentType("application/json");
    	response.setCharacterEncoding("UTF-8");
    	PrintWriter out = response.getWriter();

    	// start the JSON
    	out.println("[");

		// add the elements: ie, { "name": "Boston" }
		for (AddressHierarchyEntry e : childEntries) {
			out.println("{ \"name\": \"" + e.getName() + "\" },");
		}
    	
    	// close the JSON
		out.println("]");
    	
	}
}
