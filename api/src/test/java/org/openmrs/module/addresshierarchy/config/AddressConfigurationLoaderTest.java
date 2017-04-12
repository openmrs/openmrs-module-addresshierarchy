package org.openmrs.module.addresshierarchy.config;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Test;
import org.openmrs.module.addresshierarchy.AddressField;
import org.openmrs.util.OpenmrsClassLoader;

public class AddressConfigurationLoaderTest {

	protected final Log log = LogFactory.getLog(getClass());

	public static final String CONFIG_RESOURCE = "org/openmrs/module/addresshierarchy/include/addressConfiguration.xml";

	@Test
	public void should_writeToString() throws Exception {
		AddressConfiguration config = getAddressConfiguration();
		String expected = IOUtils.toString(OpenmrsClassLoader.getInstance().getResourceAsStream(CONFIG_RESOURCE), "UTF-8");
		String actual = AddressConfigurationLoader.writeToString(config);
		Assert.assertEquals(StringUtils.deleteWhitespace(expected), StringUtils.deleteWhitespace(actual));
	}

	@Test
	public void should_readFromString() throws Exception {
		AddressConfiguration expectedConfig = getAddressConfiguration();
		
		String serialized = IOUtils.toString(OpenmrsClassLoader.getInstance().getResourceAsStream(CONFIG_RESOURCE), "UTF-8");
		AddressConfiguration actualConfig = AddressConfigurationLoader.readFromString(serialized);
		Assert.assertEquals(expectedConfig, actualConfig);
	}

	protected AddressConfiguration getAddressConfiguration() {
		AddressConfiguration configuration = new AddressConfiguration();
		configuration.addAddressComponent(new AddressComponent(AddressField.COUNTRY, "Country", 40, "Sierra Leone", true));
		configuration.addAddressComponent(new AddressComponent(AddressField.STATE_PROVINCE, "Province/Area", 40, null, true));
		configuration.addAddressComponent(new AddressComponent(AddressField.COUNTY_DISTRICT, "District", 40, null, false));
		configuration.addAddressComponent(new AddressComponent(AddressField.CITY_VILLAGE, "Chiefdom", 40, null, false));
		configuration.addAddressComponent(new AddressComponent(AddressField.ADDRESS_1, "Address", 80, null, false));
		configuration.addLineByLineFormat("address1");
		configuration.addLineByLineFormat("cityVillage");
		configuration.addLineByLineFormat("countyDistrict, stateProvince");
		configuration.addLineByLineFormat("country");
		AddressHierarchyFile file = new AddressHierarchyFile();
		file.setFilename("address-hierarchy-entries.csv");
		file.setEntryDelimiter("|");
		file.setIdentifierDelimiter("^");
		configuration.setAddressHierarchyFile(file);
		return configuration;
	}
}
