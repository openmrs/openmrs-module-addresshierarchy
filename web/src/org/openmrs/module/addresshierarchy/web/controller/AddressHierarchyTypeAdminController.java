package org.openmrs.module.addresshierarchy.web.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.openmrs.api.context.Context;
import org.openmrs.module.addresshierarchy.AddressField;
import org.openmrs.module.addresshierarchy.AddressHierarchyType;
import org.openmrs.module.addresshierarchy.exception.AddressHierarchyModuleException;
import org.openmrs.module.addresshierarchy.propertyeditor.AddressFieldEditor;
import org.openmrs.module.addresshierarchy.propertyeditor.AddressHierarchyTypeEditor;
import org.openmrs.module.addresshierarchy.service.AddressHierarchyService;
import org.openmrs.module.addresshierarchy.validator.AddressHierarchyTypeValidator;
import org.springframework.beans.factory.annotation.Autowired;
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

	/** Validator for this controller */
	private AddressHierarchyTypeValidator validator;
	
	@Autowired
	public AddressHierarchyTypeAdminController(AddressHierarchyTypeValidator validator) {
		this.validator = validator;
	}
	
	@InitBinder
	public void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
		// register custom binders
		binder.registerCustomEditor(AddressHierarchyType.class, new AddressHierarchyTypeEditor()); 
		binder.registerCustomEditor(AddressField.class, new AddressFieldEditor()); 
	}
	
	@ModelAttribute("types")
	public List<AddressHierarchyType> getOrderedAddressHierarchyTypes() {
		return Context.getService(AddressHierarchyService.class).getAddressHierarchyTypes();
	}
	
	@ModelAttribute("type")
	public AddressHierarchyType getAddressHierarchyType(@RequestParam(value = "typeId", required = false) Integer typeId) {
		
		AddressHierarchyType type;
    	
    	// fetch the address hierarchy type, or if none specified, create a new one
    	if (typeId != null) {
    		type = Context.getService(AddressHierarchyService.class).getAddressHierarchyType(typeId);
    		
    		System.out.println("got here: " + type);
    		
    		if (type == null) {
    			throw new AddressHierarchyModuleException("Invalid address hierarchy type id " + typeId);
    		}
    	}
    	else {
    		type = new AddressHierarchyType();
    		// set the new type to be the child of the bottom-most type in the hierarchy
    		type.setParentType(Context.getService(AddressHierarchyService.class).getBottomLevelAddressHierarchyType());
    	}
    	
    	return type;
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
    public ModelAndView viewAddressHierarchyType() {
    	return new ModelAndView("/module/addresshierarchy/admin/editAddressHierarchyType");
    }
    
    @SuppressWarnings("unchecked")
    @RequestMapping("/module/addresshierarchy/admin/updateAddressHierarchyType.form")
    public ModelAndView updateAddressHierarchyType(@ModelAttribute("type") AddressHierarchyType type, BindingResult result, SessionStatus status,  ModelMap map) {
    
    	// validate form entries
		validator.validate(type, result);
		
		if (result.hasErrors()) {
			map.put("errors", result);
			return new ModelAndView("/module/addresshierarchy/admin/editAddressHierarchyType", map);
		}
    	
		// add/update the address hierarchy type
		Context.getService(AddressHierarchyService.class).saveAddressHierarchyType(type);
		
		// clears the command object from the session
		status.setComplete();
		
		return new ModelAndView("redirect:/module/addresshierarchy/admin/listAddressHierarchyTypes.form");
    	
    }
    
    @RequestMapping("/module/addresshierarchy/admin/deleteAddressHierarchyType.form")
    public ModelAndView deleteAddressHierarchyType(@ModelAttribute("type") AddressHierarchyType type) {
    	
    	// we are only allowing the deletion of the bottom-most type
    	if (type != Context.getService(AddressHierarchyService.class).getBottomLevelAddressHierarchyType()) {
    		throw new AddressHierarchyModuleException("Cannot delete Address HierarchyType; not bottom type in the hierarchy");
    	}
    	
    	// deletes the address hierarchy type
    	Context.getService(AddressHierarchyService.class).deleteAddressHierarchyType(type);
    	
    	return new ModelAndView("redirect:/module/addresshierarchy/admin/listAddressHierarchyTypes.form");
    }
    
}

