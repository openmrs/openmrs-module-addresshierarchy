package org.openmrs.module.addresshierarchy;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.api.context.Context;
import org.openmrs.module.addresshierarchy.service.AddressHierarchyService;
import org.openmrs.module.addresshierarchy.util.AddressHierarchyImportUtil;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.openmrs.test.Verifies;

import java.io.InputStream;
import java.util.List;


public class AddressHierarchyImportUtilTest extends BaseModuleContextSensitiveTest {
	
	protected static final String CSV_FILE_TO_IMPORT = "org/openmrs/module/addresshierarchy/include/addressHierarchyUtilTest-sampleFile.csv";

    protected static final String CSV_FILE__WITH_USER_GENERATED_IDS_TO_IMPORT = "org/openmrs/module/addresshierarchy/include/addressHierarchyUtilTest-sampleFileWithUserGeneratedIds.csv";

	protected static final String CSV_LARGE_FILE_TO_IMPORT = "org/openmrs/module/addresshierarchy/include/addressHierarchyUtilTest-sampleLargeFile.csv";
	
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

        // make sure the right number of entries have been imported (particularly to double-check that no blank entries have been created)
        Assert.assertEquals(new Integer(39), ahService.getAddressHierarchyEntryCount());

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

    @Test
    @Verifies(value = "should import csv file with user generated ids", method = "importAddressHierarchyFile()")
    public void importCsvFile_shouldImportCsvFileWithUserGeneratedIds() throws Exception {

        AddressHierarchyService ahService = Context.getService(AddressHierarchyService.class);

        InputStream file = getClass().getClassLoader().getResourceAsStream(CSV_FILE__WITH_USER_GENERATED_IDS_TO_IMPORT);
        AddressHierarchyImportUtil.importAddressHierarchyFile(file, "\\|", "%");

        // make sure that the codes

        // verify that a few data points exist
        List<AddressHierarchyLevel> levels = ahService.getOrderedAddressHierarchyLevels();
        Assert.assertEquals("BOTHA-BOTHE", ahService.getAddressHierarchyEntriesByLevelAndName(levels.get(0), "BOTHA-BOTHE").get(0).getName());
        Assert.assertEquals("Liqobong Council", ahService.getAddressHierarchyEntriesByLevelAndName(levels.get(2), "Liqobong Council").get(0).getName());

        // verify that the codes have been created
        Assert.assertEquals("12", ahService.getAddressHierarchyEntriesByLevelAndName(levels.get(0), "BOTHA-BOTHE").get(0).getUserGeneratedId());
        Assert.assertEquals("34", ahService.getAddressHierarchyEntriesByLevelAndName(levels.get(1), "MECHECHANE").get(0).getUserGeneratedId());
        Assert.assertEquals("56", ahService.getAddressHierarchyEntriesByLevelAndName(levels.get(2), "Makhunoane Council").get(0).getUserGeneratedId());
        Assert.assertEquals("78", ahService.getAddressHierarchyEntriesByLevelAndName(levels.get(3), "Ha Ntereke").get(0).getUserGeneratedId());
        Assert.assertEquals("654", ahService.getAddressHierarchyEntriesByLevelAndName(levels.get(3), "Ha Sefako").get(0).getUserGeneratedId());
        Assert.assertEquals("212", ahService.getAddressHierarchyEntriesByLevelAndName(levels.get(2), "Liqobong Council").get(0).getUserGeneratedId());
    }

	@Test
	@Verifies(value = "should import large csv file", method = "importAddressHierarchyFile()")
	public void importCsvFile_shouldImportLargeCsvFile() throws Exception {
		
		AddressHierarchyService ahService = Context.getService(AddressHierarchyService.class);
		
		InputStream file = getClass().getClassLoader().getResourceAsStream(CSV_LARGE_FILE_TO_IMPORT);
		AddressHierarchyImportUtil.importAddressHierarchyFile(file, ",");
		
		// confirm that all 17902 entries have been added
		Assert.assertEquals(new Integer(17902), ahService.getAddressHierarchyEntryCount());
		
		// verify that a few data points exist	
		List<AddressHierarchyLevel> levels = ahService.getOrderedAddressHierarchyLevels();
		Assert.assertEquals("Haiti", ahService.getAddressHierarchyEntriesByLevelAndName(levels.get(0), "Haiti").get(0).getName());
		Assert.assertEquals("Sud-Est", ahService.getAddressHierarchyEntriesByLevelAndName(levels.get(1), "Sud-Est").get(0).getName());
		Assert.assertEquals("Thiotte", ahService.getAddressHierarchyEntriesByLevelAndName(levels.get(2), "Thiotte").get(0).getName());
		Assert.assertEquals("Tou Roche", ahService.getAddressHierarchyEntriesByLevelAndName(levels.get(4), "Tou Roche").get(0).getName());
		
	}

}
