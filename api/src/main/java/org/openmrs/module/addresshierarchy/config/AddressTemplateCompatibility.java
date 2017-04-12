package org.openmrs.module.addresshierarchy.config;

import java.util.List;
import java.util.Map;

import org.openmrs.serialization.SerializationException;

public interface AddressTemplateCompatibility {
	
	void setNameMappings(Map<String, String> nameMappings);
 
	void setSizeMappings(Map<String, String> sizeMappings);
    
	void setElementDefaults(Map<String, String> elementDefaults);
    
	void setLineByLineFormat(List<String> lineByLineFormat);
	
	String asXml() throws SerializationException;
}