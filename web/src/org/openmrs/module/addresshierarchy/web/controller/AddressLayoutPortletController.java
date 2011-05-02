package org.openmrs.module.addresshierarchy.web.controller;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openmrs.api.context.Context;
import org.openmrs.module.addresshierarchy.service.AddressHierarchyService;
import org.springframework.web.servlet.ModelAndView;

public class AddressLayoutPortletController extends org.openmrs.web.controller.layout.AddressLayoutPortletController {
	
	/** Overrides the handle request to use the custom address hierarchy jsp if in edit mode */
	
	@SuppressWarnings("unchecked")
    @Override
	public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException,
	                                                                                           IOException {
		ModelAndView mav = super.handleRequest(request, response);
		
		// we only want to override with our custom page if in edit mode
		Map map = (Map) mav.getModelMap().get("model");	
		if (map.containsKey("layoutMode") && ((String) map.get("layoutMode")).equals("edit")) {
			
			// add the hierarchy to the map
			mav.getModelMap().addAttribute("hierarchyLevels", Context.getService(AddressHierarchyService.class).getOrderedAddressHierarchyLevels());
			
			// set the path to the custom page
			String portletPath = "/module/addresshierarchy/portlets/addressLayout";
			mav.setViewName(portletPath);
		}
		
		return mav;
	}
}
