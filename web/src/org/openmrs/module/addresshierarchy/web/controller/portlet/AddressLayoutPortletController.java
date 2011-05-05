package org.openmrs.module.addresshierarchy.web.controller.portlet;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openmrs.api.context.Context;
import org.openmrs.module.addresshierarchy.AddressHierarchyLevel;
import org.openmrs.module.addresshierarchy.service.AddressHierarchyService;
import org.springframework.web.servlet.ModelAndView;

public class AddressLayoutPortletController extends org.openmrs.web.controller.layout.AddressLayoutPortletController {
	
	/** Overrides the handle request to use the custom address hierarchy jsp if in edit mode */
	
	@SuppressWarnings("unchecked")
    @Override
	public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException,
	                                                                                           IOException {
		ModelAndView mav = super.handleRequest(request, response);
		
		AddressHierarchyService ahService = Context.getService(AddressHierarchyService.class);
		
		// we only want to override with our custom page if size="full", we aren't in view mode, 
		// and if we have defined address hierarchy levels
		Map map = (Map) mav.getModelMap().get("model");	
		if (map.containsKey("size") && ((String) map.get("size")).equals("full") &&
				(!map.containsKey("layoutMode") || ((String) map.get("layoutMode")).equals("edit")) &&
				ahService.getAddressHierarchyLevelsCount() > 0) {
			
			// set the path to the custom page
			String portletPath = "/module/addresshierarchy/portlets/addressLayout";
			mav.setViewName(portletPath);
			
			// get the ordered address hierarchy levels and add them to the map
			List<AddressHierarchyLevel> levels = ahService.getOrderedAddressHierarchyLevels();
			mav.getModelMap().addAttribute("hierarchyLevels", levels);
			
			
			System.out.println("model key set = " + map.keySet());
			
			/**
			// add any address options we need to preload based on any address fields stored
			Map<String,List<AddressHierarchyEntry>> addressFieldOptions = new HashMap<String,List<AddressHierarchyEntry>>();
			
			// first, add the top level options (we can assume we have a top level because we've already done this test)
			ListIterator<AddressHierarchyLevel> i = levels.listIterator();
			AddressHierarchyLevel level = i.next();
			addressFieldOptions.put(level.getAddressField().getName(), ahService.getAddressHierarchyEntriesAtTopLevel());
			
			// now add the other options as needed
			StringBuffer searchString = new StringBuffer();
			Person person = (Person) map.get("person");
	
			System.out.println("main key set = " + mav.getModelMap().keySet());
			System.out.println("model key set = " + map.keySet());
			
			System.out.println("got here!");
			
			System.out.println(person);
			
			
			if (person != null && person.getPersonAddress() != null) {
				while (i.hasNext()) {
					// using reflection, see if we have a value for the address field associated with the current level
					String fieldName = level.getAddressField().getName();
					String fieldGetterName = "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
					try {
		                Method fieldGetter = PersonAddress.class.getMethod(fieldGetterName);
		                String fieldValue = (String) fieldGetter.invoke(person.getPersonAddress());
		                
		                // if we've got a value here, we need to the prepopulate the options in the *next* dropdowns based on this value
		                if (StringUtils.isNotBlank(fieldValue)) {
		                	searchString.append(fieldValue);
		                	AddressHierarchyEntry entry = ahService.searchAddressHierarchy(searchString.toString());
		                	
		                	if (entry != null) {
		                		level = i.next();
		                		
		                		
		                		System.out.println("setting entries for " + entry.getName());
		                		
		                		
			                	addressFieldOptions.put(level.getAddressField().getName(), ahService.getChildAddressHierarchyEntries(entry));
			                	searchString.append('|');
		                	}
		                	else {
		                		// once we reach a level where we don't find any entries, stop searching further
		                		break;
		                	}
		                }
		                else {
		                	// once we reach a level in the hierarchy where we don't have a match, stop searching further
		                	break;
		                }
	                }
	                catch (Exception e) {
		               throw new RuntimeException("Problem fetching method " + fieldGetterName + " on PersonAddress");
	                }
				}
			}
		
			
			mav.getModelMap().addAttribute("addressFieldOptions", addressFieldOptions);
			*/
			
		}
		
		return mav;
	}
}
