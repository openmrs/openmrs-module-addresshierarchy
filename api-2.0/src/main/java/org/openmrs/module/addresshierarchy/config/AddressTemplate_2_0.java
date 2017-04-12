package org.openmrs.module.addresshierarchy.config;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.annotation.OpenmrsProfile;
import org.openmrs.api.context.Context;
import org.openmrs.layout.address.AddressTemplate;
import org.openmrs.module.addresshierarchy.AddressHierarchyConstants;
import org.openmrs.serialization.SerializationException;
import org.springframework.stereotype.Component;

@Component(AddressHierarchyConstants.COMPONENT_ADDRESS_TEMPLATE)
@OpenmrsProfile(openmrsPlatformVersion = "2.*")
public class AddressTemplate_2_0 implements AddressTemplateCompatibility {

	protected Log log = LogFactory.getLog(getClass());
	
	protected AddressTemplate template = new AddressTemplate("");
	
	@Override
	public void setNameMappings(Map<String, String> nameMappings) {
		template.setNameMappings(nameMappings);
	}

	@Override
	public void setSizeMappings(Map<String, String> sizeMappings) {
		template.setSizeMappings(sizeMappings);
	}

	@Override
	public void setElementDefaults(Map<String, String> elementDefaults) {
		template.setElementDefaults(elementDefaults);
	}
	
	@Override
	public void setLineByLineFormat(List<String> lineByLineFormat) {
		template.setLineByLineFormat(lineByLineFormat);
	}

	@Override
	public String asXml() throws SerializationException {
		return Context.getSerializationService().getDefaultSerializer().serialize(template);
	}
}