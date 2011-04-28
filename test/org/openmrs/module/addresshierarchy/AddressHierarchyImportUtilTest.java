package org.openmrs.module.addresshierarchy;

import java.io.InputStream;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.api.context.Context;
import org.openmrs.module.addresshierarchy.service.AddressHierarchyService;
import org.openmrs.module.addresshierarchy.util.AddressHierarchyImportUtil;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.openmrs.test.Verifies;


public class AddressHierarchyImportUtilTest extends BaseModuleContextSensitiveTest {
	
	protected static final String XML_DATASET_PACKAGE_PATH = "org/openmrs/module/addresshierarchy/include/addressHierarchyUtilTest-dataset.xml";
	
	protected static final String CSV_FILE_TO_IMPORT = "org/openmrs/module/addresshierarchy/include/addressHierarchyUtilTest-sampleFile.csv";
	
	@Before
	public void setupDatabase() throws Exception {
		initializeInMemoryDatabase();
		authenticate();
		executeDataSet(XML_DATASET_PACKAGE_PATH);
	}
	
	@Test
	@Verifies(value = "should get hierarchy type by id", method = "getHierarchyType(int id)")
	public void importCsvFile_shouldImportCsvFile() throws Exception {
		
		AddressHierarchyService ahService = Context.getService(AddressHierarchyService.class);
		
		AddressHierarchyType district = ahService.getAddressHierarchyType(2);
		
		InputStream file = getClass().getClassLoader().getResourceAsStream(CSV_FILE_TO_IMPORT);
		AddressHierarchyImportUtil.importAddressHierarchyFile(file, "\\|", district);
		
		// verify that a few data points exist	
		Assert.assertEquals(ahService.searchHierarchy("BOTHA-BOTHE", -1).get(0).getName(),"BOTHA-BOTHE");
		Assert.assertEquals(ahService.searchHierarchy("BOTHA-BOTHE", -1).get(0).getType().getName(),"District");

		Assert.assertEquals(ahService.searchHierarchy("LITHABANENG", -1).get(0).getName(),"LITHABANENG");
		Assert.assertEquals(ahService.searchHierarchy("LITHABANENG", -1).get(0).getType().getName(),"Constituency");

		Assert.assertEquals(ahService.searchHierarchy("Maseru Municipality", -1).get(0).getName(),"Maseru Municipality");
		Assert.assertEquals(ahService.searchHierarchy("Maseru Municipality", -1).get(0).getType().getName(),"Community Council");
		
		Assert.assertEquals(ahService.searchHierarchy("Thaba-Kholo", -1).get(0).getName(),"Thaba-Kholo");
		Assert.assertEquals(ahService.searchHierarchy("Thaba-Kholo", -1).get(0).getType().getName(),"Village");

		
	}
}
