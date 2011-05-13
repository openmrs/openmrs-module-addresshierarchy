package org.openmrs.module.addresshierarchy;

import java.io.InputStream;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.api.context.Context;
import org.openmrs.module.addresshierarchy.service.AddressHierarchyService;
import org.openmrs.module.addresshierarchy.util.AddressHierarchyImportUtil;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.openmrs.test.Verifies;


public class AddressHierarchyImportUtilTest extends BaseModuleContextSensitiveTest {
	
	protected static final String CSV_FILE_TO_IMPORT = "org/openmrs/module/addresshierarchy/include/addressHierarchyUtilTest-sampleFile.csv";
	
	@Before
	public void setupDatabase() throws Exception {
		initializeInMemoryDatabase();
		authenticate();
	}
	
	@Test
	@Verifies(value = "should import csv file", method = "importAddressHierarchyFile()")
	public void importCsvFile_shouldImportCsvFile() throws Exception {
		
		AddressHierarchyService ahService = Context.getService(AddressHierarchyService.class);
		
		InputStream file = getClass().getClassLoader().getResourceAsStream(CSV_FILE_TO_IMPORT);
		AddressHierarchyImportUtil.importAddressHierarchyFile(file, "\\|");
		
		// verify that a few data points exist	
		List<AddressHierarchyLevel> levels = ahService.getOrderedAddressHierarchyLevels();
		Assert.assertEquals("BOTHA-BOTHE", ahService.getAddressHierarchyEntriesByLevelAndName(levels.get(0), "BOTHA-BOTHE").get(0).getName());
		Assert.assertEquals("LITHABANENG", ahService.getAddressHierarchyEntriesByLevelAndName(levels.get(1), "LITHABANENG").get(0).getName());
		Assert.assertEquals("Maseru Municipality", ahService.getAddressHierarchyEntriesByLevelAndName(levels.get(2), "Maseru Municipality").get(0).getName());
		Assert.assertEquals("Thaba-Kholo", ahService.getAddressHierarchyEntriesByLevelAndName(levels.get(3), "Thaba-Kholo").get(0).getName());
		
		// make sure that an entry hasn't been created twice just because it of case-sensitive issues
		Assert.assertEquals(1, ahService.getAddressHierarchyEntriesByLevelAndName(levels.get(0), "BOTHA-BOTHE").size());
		
		// make sure that both samples with the same name have been created
		List<AddressHierarchyEntry> duplicateSample = ahService.getAddressHierarchyEntriesByLevelAndName(levels.get(3), "Sample Dup");
		Assert.assertEquals(2, duplicateSample.size());
		Assert.assertTrue(duplicateSample.get(0).getParent().getName().equals("First Sample") || duplicateSample.get(0).getParent().getName().equals("Second Sample"));
		Assert.assertTrue(duplicateSample.get(1).getParent().getName().equals("First Sample") || duplicateSample.get(1).getParent().getName().equals("Second Sample"));
		Assert.assertTrue(!duplicateSample.get(0).getParent().getName().equals(duplicateSample.get(1).getParent().getName()));
		
	}
}
