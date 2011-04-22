package org.openmrs.module.addresshierarchy.propertyeditor;

import java.beans.PropertyEditorSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.addresshierarchy.AddressField;
import org.springframework.util.StringUtils;


public class AddressFieldEditor extends PropertyEditorSupport  {
	
	private Log log = LogFactory.getLog(this.getClass());
	
	public AddressFieldEditor() {
	}
	
	public void setAsText(String text) throws IllegalArgumentException {
		if (StringUtils.hasText(text)) {
			try {
				setValue(AddressField.getByName(text));
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
		AddressField field = (AddressField) getValue();
		if (field == null) {
			return "";
		} else {
			return field.getName();
		}
	}
}
