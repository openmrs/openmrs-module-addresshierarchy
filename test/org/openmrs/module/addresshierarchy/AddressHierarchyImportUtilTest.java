package org.openmrs.module.addresshierarchy;

import java.io.InputStream;

import org.junit.Assert;
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
	@Verifies(value = "should import csv file", method = "importAddressHierarchyFile()")
	public void importCsvFile_shouldImportCsvFile() throws Exception {
		
		AddressHierarchyService ahService = Context.getService(AddressHierarchyService.class);
		
		InputStream file = getClass().getClassLoader().getResourceAsStream(CSV_FILE_TO_IMPORT);
		AddressHierarchyImportUtil.importAddressHierarchyFile(file, "\\|");
		
		// verify that a few data points exist	
		Assert.assertEquals(ahService.searchAddressHierarchy("BOTHA-BOTHE").getName(),"BOTHA-BOTHE");
		Assert.assertEquals(ahService.searchAddressHierarchy("BOTHA-BOTHE").getLevel().getName(),"District");

		Assert.assertEquals(ahService.searchAddressHierarchy("MASERU|LITHABANENG").getName(),"LITHABANENG");
		Assert.assertEquals(ahService.searchAddressHierarchy("MASERU|LITHABANENG").getLevel().getName(),"Constituency");

		Assert.assertEquals(ahService.searchAddressHierarchy("MASERU|LITHABANENG|Maseru Municipality").getName(),"Maseru Municipality");
		Assert.assertEquals(ahService.searchAddressHierarchy("MASERU|LITHABANENG|Maseru Municipality").getLevel().getName(),"Community Council");
		
		Assert.assertEquals(ahService.searchAddressHierarchy("BOTHA-BOTHE|HOLOLO|Likila Council|Thaba-Kholo").getName(),"Thaba-Kholo");
		Assert.assertEquals(ahService.searchAddressHierarchy("BOTHA-BOTHE|HOLOLO|Likila Council|Thaba-Kholo").getLevel().getName(),"Village");
		
		// make sure that both samples with the same name have been created
		AddressHierarchyEntry entry = ahService.searchAddressHierarchy("BOTHA-BOTHE|SAMPLE DUP");
		Assert.assertEquals("First Sample", ahService.getChildAddressHierarchyEntries(entry).get(0).getName());
		entry = ahService.searchAddressHierarchy("MASERU|SAMPLE DUP");
		Assert.assertEquals("Second Sample", ahService.getChildAddressHierarchyEntries(entry).get(0).getName());
	}
}
