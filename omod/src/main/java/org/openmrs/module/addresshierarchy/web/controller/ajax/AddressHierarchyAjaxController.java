package org.openmrs.module.addresshierarchy.web.controller.ajax;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
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
					                             @RequestParam(value = "searchString", required = false) String searchString) throws Exception {
		
		AddressHierarchyService ahService = Context.getService(AddressHierarchyService.class);
		
		List<String> childEntryNames = new ArrayList<String>();
	
		// if the search parameter is empty, we just want all items at in the top mapped level
		if (StringUtils.isBlank(searchString)) {
			List<AddressHierarchyLevel> levels = ahService.getOrderedAddressHierarchyLevels(false);
			if (levels != null && levels.size() > 0) {
				for (AddressHierarchyEntry entry : ahService.getAddressHierarchyEntriesByLevel(levels.get(0))) {
					childEntryNames.add(entry.getName());
				}
			}
		}
		else {
			// other, create the appropriate PersonAddress object and then perform the search
			PersonAddress address = new PersonAddress();
			List<AddressHierarchyLevel> levels = ahService.getOrderedAddressHierarchyLevels(false);  // note that we only want the mapped hierarchy levels
			
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
			childEntryNames = ahService.getPossibleAddressValues(address, levels.get(i).getAddressField());
		}
			
    	response.setContentType("application/json");
    	response.setCharacterEncoding("UTF-8");
    	PrintWriter out = response.getWriter();

    	// start the JSON
    	out.print("[");

    	if (childEntryNames != null) {
			Collections.sort(childEntryNames);
    	
			// add the elements: ie, { "name": "Boston" }
			Iterator<String> i = childEntryNames.iterator();			
			while (i.hasNext()) {
				out.print("{ \"name\": \"" + i.next() + "\" }");
				
				// print comma as a delimiter for all but the last option in the list
				if (i.hasNext()) {
					out.print(",");  
				}
				
 			}
    	}
    	
    	// close the JSON
		out.print("]");
	}
	
	/**
	 * Returns a list of full addresses in string format that match the given search string;
	 * Specify a separator if you want the full address strings returned to be delimited by something other than the pipe (|)
	 * (See docs on the underlying getPossibleFullAddresses(String) method for more information
	 */
	@RequestMapping("/module/addresshierarchy/ajax/getPossibleFullAddresses.form") 
	public void getPossibleFullAddressesEntries(ModelMap model, HttpServletRequest request, HttpServletResponse response, 
					                             @RequestParam("searchString") String searchString ,
					                             @RequestParam(value = "separator", required = false) String separator) throws Exception {
		
		List<String> addresses = Context.getService(AddressHierarchyService.class).getPossibleFullAddresses(searchString);
		
		String delimiter = null;		
		if(!StringUtils.equals(separator, "|")){
			delimiter = separator;
		}
					
		// send back the response
		response.setContentType("text/json");
    	response.setCharacterEncoding("UTF-8");
    	PrintWriter out = response.getWriter();
    	
    	out.print("[");
    	
		if (addresses != null && addresses.size() > 0) {
			Iterator<String> i = addresses.iterator();
			if(StringUtils.isNotBlank(delimiter)){
				while (i.hasNext()) {						
					out.print("{ \"address\": \"" + StringUtils.replace(i.next(), "|", delimiter) + "\" }");				
					// print comma between entries for all but the last option in the list
					if (i.hasNext()) {
						out.print(",");  
					}
				}
			}else{
				while (i.hasNext()) {	
					out.print("{ \"address\": \"" + i.next() + "\" }");				
					// print comma between entries for all but the last option in the list
					if (i.hasNext()) {
						out.print(",");  
					}
				}
			}
		}
		
    	out.print("]");
	}
}
