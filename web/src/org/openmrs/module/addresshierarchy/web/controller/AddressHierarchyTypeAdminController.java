package org.openmrs.module.addresshierarchy.web.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.openmrs.api.context.Context;
import org.openmrs.module.addresshierarchy.AddressField;
import org.openmrs.module.addresshierarchy.AddressHierarchyService;
import org.openmrs.module.addresshierarchy.AddressHierarchyType;
import org.openmrs.module.addresshierarchy.exception.AddressHierarchyModuleException;
import org.openmrs.module.addresshierarchy.propertyeditor.AddressFieldEditor;
import org.openmrs.module.addresshierarchy.propertyeditor.AddressHierarchyTypeEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.ModelAndView;


@Controller
public class AddressHierarchyTypeAdminController {

	@InitBinder
	public void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
		// register custom binders
		binder.registerCustomEditor(AddressHierarchyType.class, new AddressHierarchyTypeEditor()); 
		binder.registerCustomEditor(AddressField.class, new AddressFieldEditor()); 
	}
	
	@ModelAttribute("types")
	public List<AddressHierarchyType> getAddressHierarchyTypes() {
		return Context.getService(AddressHierarchyService.class).getAddressHierarchyTypes();
	}
	
	@ModelAttribute("addressFields")
	public AddressField [] getAddressFields() {
		return AddressField.values();
	}
	
    @RequestMapping("/module/addresshierarchy/admin/listAddressHierarchyTypes.form")
	public ModelAndView listAddressHierarchyTypes() {
		return new ModelAndView("/module/addresshierarchy/admin/listAddressHierarchyTypes");
	}
    
    @RequestMapping("/module/addresshierarchy/admin/editAddressHierarchyType.form")
    public ModelAndView viewAddressHierarchyType(@RequestParam(value = "typeId", required = false) Integer typeId) {
    	
    	AddressHierarchyType type;
    	
    	// fetch the address hierarchy type, or if none specified, create a new one
    	if (typeId != null) {
    		type = Context.getService(AddressHierarchyService.class).getAddressHierarchyType(typeId);
    		if (type == null) {
    			throw new AddressHierarchyModuleException("Invalid address hierarchy type id " + typeId);
    		}
    	}
    	else {
    		type = new AddressHierarchyType();
    	}
    	
    	ModelMap map = new ModelMap();
    	map.addAttribute("type", type);
    	return new ModelAndView("/module/addresshierarchy/admin/editAddressHierarchyType");
    }
    
    @RequestMapping("/module/addresshierarchy/admin/updateAddressHierarchyType.form")
    public ModelAndView updateAddressHierarchyType(@ModelAttribute("type") AddressHierarchyType type, BindingResult result, SessionStatus status) {
    
    	// TODO: add validation
    	
		// add/update the flag
		Context.getService(AddressHierarchyService.class).saveAddressHierarchyType(type);
		
		// clears the command object from the session
		status.setComplete();
		
		// just display the edit page again
		return new ModelAndView("redirect:/module/addresshierarchy/admin/listAddressHierarchyTypes.form");
    	
    }
    
}

