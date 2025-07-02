package org.openmrs.module.addresshierarchy.config;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import java.io.IOException;
import java.nio.file.Files;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.GlobalProperty;
import org.openmrs.api.AdministrationService;
import org.openmrs.module.addresshierarchy.AddressField;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.openmrs.util.OpenmrsClassLoader;
import org.springframework.beans.factory.annotation.Autowired;

public class AddressConfigurationLoaderTest extends BaseContextSensitiveTest {

	protected final Log log = LogFactory.getLog(getClass());

	public static final String CONFIG_RESOURCE = "org/openmrs/module/addresshierarchy/include/addressConfiguration.json";

	@Autowired
    private AdministrationService adminService;

	@BeforeEach
	public void setup() throws IOException {
		System.setProperty("user.home", Files.createTempDirectory(null).toString()); // see OpenmrsUtil.getApplicationDataDirectory()
		adminService.saveGlobalProperty(
            new GlobalProperty("addressHierarchy.configuration.serializer.whitelist.types",
                "org.openmrs.module.addresshierarchy.**"));
		adminService.saveGlobalProperty(
            new GlobalProperty("layout.address.format",
                "<org.openmrs.layout.address.AddressTemplate>     <nameMappings class=\"properties\">       <property name=\"postalCode\" value=\"Location.postalCode\"/>       <property name=\"address2\" value=\"Location.address2\"/>       <property name=\"address1\" value=\"Location.address1\"/>       <property name=\"country\" value=\"Location.country\"/>       <property name=\"stateProvince\" value=\"Location.stateProvince\"/>       <property name=\"cityVillage\" value=\"Location.cityVillage\"/>     </nameMappings>     <sizeMappings class=\"properties\">       <property name=\"postalCode\" value=\"10\"/>       <property name=\"address2\" value=\"40\"/>       <property name=\"address1\" value=\"40\"/>       <property name=\"country\" value=\"10\"/>       <property name=\"stateProvince\" value=\"10\"/>       <property name=\"cityVillage\" value=\"10\"/>     </sizeMappings>     <lineByLineFormat>       <string>address1</string>       <string>address2</string>       <string>cityVillage stateProvince country postalCode</string>     </lineByLineFormat>    <requiredElements> </requiredElements> </org.openmrs.layout.address.AddressTemplate>"));
	}

	@Test
	public void should_writeToString() throws Exception {
		AddressConfiguration config = getAddressConfiguration();
		String expectedJson = IOUtils.toString(OpenmrsClassLoader.getInstance().getResourceAsStream(CONFIG_RESOURCE), "UTF-8");
		String actualJson = AddressConfigurationLoader.writeToString(config);
		assertEquals(StringUtils.deleteWhitespace(expectedJson), StringUtils.deleteWhitespace(actualJson));
	}

	@Test
	public void should_readFromString() throws Exception {
		AddressConfiguration expectedConfig = getAddressConfiguration();
		
		String serialized = IOUtils.toString(OpenmrsClassLoader.getInstance().getResourceAsStream(CONFIG_RESOURCE), "UTF-8");
		AddressConfiguration actualConfig = AddressConfigurationLoader.readFromString(serialized);
		assertTrue(expectedConfig.equals(actualConfig));
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
