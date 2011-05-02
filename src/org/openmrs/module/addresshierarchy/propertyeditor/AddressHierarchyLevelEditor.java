package org.openmrs.module.addresshierarchy.propertyeditor;

import java.beans.PropertyEditorSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.addresshierarchy.AddressHierarchyLevel;
import org.openmrs.module.addresshierarchy.service.AddressHierarchyService;
import org.springframework.util.StringUtils;


public class AddressHierarchyLevelEditor extends PropertyEditorSupport {

	private Log log = LogFactory.getLog(this.getClass());
	
	public AddressHierarchyLevelEditor() {
	}
	
	public void setAsText(String text) throws IllegalArgumentException {
		if (StringUtils.hasText(text)) {
			try {
				setValue(Context.getService(AddressHierarchyService.class).getAddressHierarchyLevel(Integer.valueOf(text)));
			}
			catch (Exception ex) {
				log.error("Error setting text" + text, ex);
				throw new IllegalArgumentException("Concept not found: " + ex.getMessage());
			}
		} else {
			setValue(null);
		}
	}
	
	public String getAsText() {
		AddressHierarchyLevel level = (AddressHierarchyLevel) getValue();
		if (level == null) {
			return "";
		} else {
			return level.getId().toString();
		}
	}
	
}
