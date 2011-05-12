package org.openmrs.module.addresshierarchy.web.controller.ajax;

import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.PersonAddress;
import org.openmrs.api.context.Context;
import org.openmrs.module.addresshierarchy.AddressHierarchyEntry;
import org.openmrs.module.addresshierarchy.AddressHierarchyLevel;
import org.openmrs.module.addresshierarchy.exception.AddressHierarchyModuleException;
import org.openmrs.module.addresshierarchy.service.AddressHierarchyService;
import org.openmrs.module.addresshierarchy.util.AddressHierarchyUtil;
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
	 * Returns a list of child address hierarchy entries in JSON format
	 * 
	 * The parent entry is specified by a string in the format "UNITED STATES|MASSACHUSETTES|PLYMOUTH COUNTY"
	 */
	@RequestMapping("/module/addresshierarchy/ajax/getChildAddressHierarchyEntries.form")
	 public void getChildAddressHierarchyEntries(ModelMap model, HttpServletRequest request, HttpServletResponse response, 
					                             @RequestParam(value = "searchString", required = false) String searchString,
					                             @RequestParam(value = "includeUnmapped", required = false) Boolean includeUnmapped) throws Exception {
		
		AddressHierarchyService ahService = Context.getService(AddressHierarchyService.class);
		if (includeUnmapped == null) {
			includeUnmapped = false;
		}
		
		List<AddressHierarchyEntry> childEntries = null;
	
		// if the search parameter is empty, we just want all items at in the top mapped level
		if (StringUtils.isBlank(searchString)) {
			List<AddressHierarchyLevel> levels = ahService.getOrderedAddressHierarchyLevels(false);
			if (levels != null && levels.size() > 0) {
				childEntries = ahService.getAddressHierarchyEntriesByLevel(levels.get(0));
			}
		}
		else {
			// other, create the appropriate PersonAddress object and then perform the search
			PersonAddress address = new PersonAddress();
			List<AddressHierarchyLevel> levels = ahService.getOrderedAddressHierarchyLevels(includeUnmapped);
			
			int i = 0;
			// iterate through all the names in the search string to form the PersonAddress object
			for (String name : searchString.split("\\|")) {
				if (StringUtils.isNotBlank(name)) {
					if (levels.size() <= i-1) {  // make sure we haven't reached the bottom level, because this would make no sense
						throw new AddressHierarchyModuleException("Address hierarchy levels have not been properly defined.");
					}
					else {
						AddressHierarchyUtil.setAddressFieldValue(address, levels.get(i).getAddressField(), name);
					}
				}
				i++;
			}			
			
			// now do the actual search
			childEntries = ahService.getPossibleAddressHierarchyEntries(address, levels.get(i));
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
