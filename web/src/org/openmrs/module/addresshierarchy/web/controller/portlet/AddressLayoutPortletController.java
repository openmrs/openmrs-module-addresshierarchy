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
			
		}
		
		return mav;
	}
}
