package org.openmrs.module.addresshierarchy.web.controller;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.openmrs.api.context.Context;
import org.openmrs.layout.web.address.AddressSupport;
import org.openmrs.module.addresshierarchy.AddressField;
import org.openmrs.module.addresshierarchy.AddressHierarchyLevel;
import org.openmrs.module.addresshierarchy.exception.AddressHierarchyModuleException;
import org.openmrs.module.addresshierarchy.propertyeditor.AddressFieldEditor;
import org.openmrs.module.addresshierarchy.propertyeditor.AddressHierarchyLevelEditor;
import org.openmrs.module.addresshierarchy.service.AddressHierarchyService;
import org.openmrs.module.addresshierarchy.validator.AddressHierarchyLevelValidator;
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
public class AddressHierarchyLevelController {

	/** Validator for this controller */
	private AddressHierarchyLevelValidator validator;
	
	@Autowired
	public AddressHierarchyLevelController(AddressHierarchyLevelValidator validator) {
		this.validator = validator;
	}
	
	@InitBinder
	public void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
		// register custom binders
		binder.registerCustomEditor(AddressHierarchyLevel.class, new AddressHierarchyLevelEditor()); 
		binder.registerCustomEditor(AddressField.class, new AddressFieldEditor()); 
	}
	
	@ModelAttribute("addressFields")
	public AddressField [] getAddressFields() {
		return AddressField.values();
	}
	
	@ModelAttribute("nameMappings")
	public Map<String,String> getAddressNameMappings() {
		return AddressSupport.getInstance().getDefaultLayoutTemplate().getNameMappings();
	}
	
	@ModelAttribute("levels")
	public List<AddressHierarchyLevel> getOrderedAddressHierarchyLevels() {
		return Context.getService(AddressHierarchyService.class).getAddressHierarchyLevels();
	}
	
	@ModelAttribute("level")
	public AddressHierarchyLevel getAddressHierarchyLevel(@RequestParam(value = "levelId", required = false) Integer levelId) {
		
		AddressHierarchyLevel level;
    	
    	// fetch the address hierarchy level, or if none specified, create a new one
    	if (levelId != null) {
    		level = Context.getService(AddressHierarchyService.class).getAddressHierarchyLevel(levelId);
    		
    		if (level == null) {
    			throw new AddressHierarchyModuleException("Invalid address hierarchy level id " + levelId);
    		}
    	}
    	else {
    		level = new AddressHierarchyLevel();
    		// set the new type to be the child of the bottom-most type in the hierarchy
    		level.setParent(Context.getService(AddressHierarchyService.class).getBottomAddressHierarchyLevel());
    	}
    	
    	return level;
	}
	
    @RequestMapping("/module/addresshierarchy/admin/listAddressHierarchyLevels.form")
	public ModelAndView listAddressHierarchyLevels() {
		return new ModelAndView("/module/addresshierarchy/admin/listAddressHierarchyLevels");
	}
    
    @RequestMapping("/module/addresshierarchy/admin/editAddressHierarchyLevel.form")
    public ModelAndView viewAddressHierarchyLevel() {
    	return new ModelAndView("/module/addresshierarchy/admin/editAddressHierarchyLevel");
    }
    
    @SuppressWarnings("unchecked")
    @RequestMapping("/module/addresshierarchy/admin/updateAddressHierarchyLevel.form")
    public ModelAndView updateAddressHierarchyLevel(@ModelAttribute("level") AddressHierarchyLevel level, BindingResult result, SessionStatus status,  ModelMap map) {
    
    	// validate form entries
		validator.validate(level, result);
		
		if (result.hasErrors()) {
			map.put("errors", result);
			return new ModelAndView("/module/addresshierarchy/admin/editAddressHierarchyLevel", map);
		}
    	
		// add/update the address hierarchy type
		Context.getService(AddressHierarchyService.class).saveAddressHierarchyLevel(level);
		
		// clears the command object from the session
		status.setComplete();
		
		return new ModelAndView("redirect:/module/addresshierarchy/admin/listAddressHierarchyLevels.form");
    	
    }
    
    @RequestMapping("/module/addresshierarchy/admin/deleteAddressHierarchyLevel.form")
    public ModelAndView deleteAddressHierarchyLevel(@ModelAttribute("level") AddressHierarchyLevel level) {
    	
    	// we are only allowing the deletion of the bottom-most type
    	if (level != Context.getService(AddressHierarchyService.class).getBottomAddressHierarchyLevel()) {
    		throw new AddressHierarchyModuleException("Cannot delete Address Hierarchy Level; not bottom type in the hierarchy");
    	}
    	
    	// deletes the address hierarchy type
    	Context.getService(AddressHierarchyService.class).deleteAddressHierarchyLevel(level);
    	
    	return new ModelAndView("redirect:/module/addresshierarchy/admin/listAddressHierarchyLevels.form");
    }
    
}

