package org.openmrs.module.addresshierarchy.web.controller;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.addresshierarchy.service.AddressHierarchyService;
import org.openmrs.module.addresshierarchy.util.AddressHierarchyImportUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/module/addresshierarchy/admin/uploadAddressHierarchy.form")
public class UploadAddressHierarchyController {
	
	protected static final Log log = LogFactory.getLog(UploadAddressHierarchyController.class);
	
	@RequestMapping(method = RequestMethod.GET)
	public ModelAndView showAddressHierarchyUploadForm(ModelMap map) {
		return new ModelAndView("/module/addresshierarchy/admin/uploadAddressHierarchy", map);
	}
	
	@RequestMapping(method = RequestMethod.POST)
	public ModelAndView processAddressHierarchyUploadForm(@RequestParam("file") MultipartFile file,
	                                                      @RequestParam("delimiter") String delimiter, 
	                                                      @RequestParam(value = "overwrite", required = false) Boolean overwrite,
	                                                      ModelMap map) {
		
		List<String> messages = new ArrayList<String>();
				
		// handle validation
		if (delimiter == null || delimiter.isEmpty()) {
			messages.add("addresshierarchy.admin.validation.noDelimiter");
		}
		if (file == null || file.isEmpty()) {
			messages.add("addresshierarchy.admin.validation.noFile");
		}
		if (messages.size() > 0) {
			map.addAttribute("messages", messages);
			map.addAttribute("delimiter", delimiter);
			map.addAttribute("overwrite", overwrite);
			return new ModelAndView("/module/addresshierarchy/admin/uploadAddressHierarchy", map);
		}
		
		// do the actual update
		try {
			// delete old records if overwrite has been selected
			if (overwrite != null && overwrite == true) {
				Context.getService(AddressHierarchyService.class).deleteAllAddressHierarchyEntries();
			}
			
			// do the actual import
	        AddressHierarchyImportUtil.importAddressHierarchyFile(file.getInputStream(), delimiter);
        }
        catch (Exception e) {
	        log.error("Unable to import address hierarchy file", e);
	        messages.add("addresshierarchy.admin.uploadFailure");
	        map.addAttribute("messages", messages);
			map.addAttribute("delimiter", delimiter);
			map.addAttribute("overwrite", overwrite);
			return new ModelAndView("/module/addresshierarchy/admin/uploadAddressHierarchy", map);
        }
        
        // add a success message
        messages.add("addresshierarchy.admin.uploadSuccess");
		map.addAttribute("messages", messages);
        
		return new ModelAndView("/module/addresshierarchy/admin/uploadAddressHierarchy", map);
	}
	
}
