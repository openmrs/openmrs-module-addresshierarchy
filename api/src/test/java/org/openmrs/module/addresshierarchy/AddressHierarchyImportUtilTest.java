/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.addresshierarchy;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.api.context.Context;
import org.openmrs.module.addresshierarchy.service.AddressHierarchyService;
import org.openmrs.module.addresshierarchy.util.AddressHierarchyImportUtil;
import org.openmrs.test.jupiter.BaseModuleContextSensitiveTest;
import org.openmrs.test.SkipBaseSetup;
import org.openmrs.test.Verifies;
import org.springframework.test.annotation.DirtiesContext;

import java.io.InputStream;
import java.util.List;

@DirtiesContext
@SkipBaseSetup
public class AddressHierarchyImportUtilTest extends BaseModuleContextSensitiveTest {
	
	protected static final String CSV_FILE_TO_IMPORT = "org/openmrs/module/addresshierarchy/include/addressHierarchyUtilTest-sampleFile.csv";
	
	protected static final String CSV_FILE__WITH_USER_GENERATED_IDS_TO_IMPORT = "org/openmrs/module/addresshierarchy/include/addressHierarchyUtilTest-sampleFileWithUserGeneratedIds.csv";
	
	protected static final String CSV_LARGE_FILE_TO_IMPORT = "org/openmrs/module/addresshierarchy/include/addressHierarchyUtilTest-sampleLargeFile.csv";

	protected static final String CSV_FILE_ADDITIONS_TO_IMPORT = "org/openmrs/module/addresshierarchy/include/addressHierarchyUtilTest-sampleFileAdditions.csv";

	protected static final String CSV_FILE_WITH_UPDATED_USER_GENERATED_IDS_TO_IMPORT = "org/openmrs/module/addresshierarchy/include/addressHierarchyUtilTest-sampleFileWithUpdatedUserGeneratedIds.csv";
	
	@BeforeEach
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
		Assertions.assertEquals(Integer.valueOf(39), ahService.getAddressHierarchyEntryCount());
		
		// verify that a few data points exist	
		List<AddressHierarchyLevel> levels = ahService.getOrderedAddressHierarchyLevels();
		Assertions.assertEquals("BOTHA-BOTHE",
		    ahService.getAddressHierarchyEntriesByLevelAndName(levels.get(0), "BOTHA-BOTHE").get(0).getName());
		Assertions.assertEquals("LITHABANENG",
		    ahService.getAddressHierarchyEntriesByLevelAndName(levels.get(1), "LITHABANENG").get(0).getName());
		Assertions.assertEquals("Maseru Municipality",
		    ahService.getAddressHierarchyEntriesByLevelAndName(levels.get(2), "Maseru Municipality").get(0).getName());
		Assertions.assertEquals("Thaba-Kholo",
		    ahService.getAddressHierarchyEntriesByLevelAndName(levels.get(3), "Thaba-Kholo").get(0).getName());
		
		// make sure that an entry hasn't been created twice just because it of case-sensitive issues
		Assertions.assertEquals(1, ahService.getAddressHierarchyEntriesByLevelAndName(levels.get(0), "BOTHA-BOTHE").size());
		
		// make sure that both samples with the same name have been created
		List<AddressHierarchyEntry> duplicateSample = ahService.getAddressHierarchyEntriesByLevelAndName(levels.get(3),
		    "Sample Dup");
		Assertions.assertEquals(2, duplicateSample.size());
		Assertions.assertTrue(duplicateSample.get(0).getParent().getName().equals("First Sample")
		        || duplicateSample.get(0).getParent().getName().equals("Second Sample"));
		Assertions.assertTrue(duplicateSample.get(1).getParent().getName().equals("First Sample")
		        || duplicateSample.get(1).getParent().getName().equals("Second Sample"));
		Assertions.assertTrue(
		    !duplicateSample.get(0).getParent().getName().equals(duplicateSample.get(1).getParent().getName()));
		
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
		Assertions.assertEquals("BOTHA-BOTHE",
		    ahService.getAddressHierarchyEntriesByLevelAndName(levels.get(0), "BOTHA-BOTHE").get(0).getName());
		Assertions.assertEquals("Liqobong Council",
		    ahService.getAddressHierarchyEntriesByLevelAndName(levels.get(2), "Liqobong Council").get(0).getName());
		
		// verify that the codes have been created
		Assertions.assertEquals("12",
		    ahService.getAddressHierarchyEntriesByLevelAndName(levels.get(0), "BOTHA-BOTHE").get(0).getUserGeneratedId());
		Assertions.assertEquals("34",
		    ahService.getAddressHierarchyEntriesByLevelAndName(levels.get(1), "MECHECHANE").get(0).getUserGeneratedId());
		Assertions.assertEquals("56", ahService.getAddressHierarchyEntriesByLevelAndName(levels.get(2), "Makhunoane Council")
		        .get(0).getUserGeneratedId());
		Assertions.assertEquals("78",
		    ahService.getAddressHierarchyEntriesByLevelAndName(levels.get(3), "Ha Ntereke").get(0).getUserGeneratedId());
		Assertions.assertEquals("654",
		    ahService.getAddressHierarchyEntriesByLevelAndName(levels.get(3), "Ha Sefako").get(0).getUserGeneratedId());
		Assertions.assertEquals("212", ahService.getAddressHierarchyEntriesByLevelAndName(levels.get(2), "Liqobong Council")
		        .get(0).getUserGeneratedId());
	}
	
	@Test
	@Verifies(value = "should not duplicate entries when the same file is imported twice", method = "importAddressHierarchyFile()")
	public void importCsvFile_shouldNotDuplicateEntriesWhenImportedTwice() throws Exception {

		AddressHierarchyService ahService = Context.getService(AddressHierarchyService.class);

		AddressHierarchyImportUtil.importAddressHierarchyFile(
		    getClass().getClassLoader().getResourceAsStream(CSV_FILE_TO_IMPORT), "\\|");
		Assertions.assertEquals(Integer.valueOf(39), ahService.getAddressHierarchyEntryCount());

		AddressHierarchyImportUtil.importAddressHierarchyFile(
		    getClass().getClassLoader().getResourceAsStream(CSV_FILE_TO_IMPORT), "\\|");

		// every entry in the file already exists, so the second import should add nothing
		Assertions.assertEquals(Integer.valueOf(39), ahService.getAddressHierarchyEntryCount());

		List<AddressHierarchyLevel> levels = ahService.getOrderedAddressHierarchyLevels();
		Assertions.assertEquals(1, ahService.getAddressHierarchyEntriesByLevelAndName(levels.get(0), "BOTHA-BOTHE").size());
		Assertions.assertEquals(1,
		    ahService.getAddressHierarchyEntriesByLevelAndName(levels.get(2), "Makhunoane Council").size());
	}

	@Test
	@Verifies(value = "should attach new entries to existing parents when importing over an existing hierarchy", method = "importAddressHierarchyFile()")
	public void importCsvFile_shouldAttachNewEntriesToExistingParents() throws Exception {

		AddressHierarchyService ahService = Context.getService(AddressHierarchyService.class);

		AddressHierarchyImportUtil.importAddressHierarchyFile(
		    getClass().getClassLoader().getResourceAsStream(CSV_FILE_TO_IMPORT), "\\|");

		List<AddressHierarchyLevel> levels = ahService.getOrderedAddressHierarchyLevels();
		AddressHierarchyEntry existingParent = ahService
		        .getAddressHierarchyEntriesByLevelAndName(levels.get(2), "Makhunoane Council").get(0);

		AddressHierarchyImportUtil.importAddressHierarchyFile(
		    getClass().getClassLoader().getResourceAsStream(CSV_FILE_ADDITIONS_TO_IMPORT), "\\|");

		// the first line of the additions file already exists in its entirety, the second adds a single leaf
		// under an existing parent, and the third adds a whole new branch below an existing top-level entry
		Assertions.assertEquals(Integer.valueOf(43), ahService.getAddressHierarchyEntryCount());

		// the new leaf must hang off the entry that was already there rather than off a duplicate, even though
		// the additions file spells its top-level ancestor in a different case
		List<AddressHierarchyEntry> newVillage = ahService.getAddressHierarchyEntriesByLevelAndName(levels.get(3),
		    "Brand New Village");
		Assertions.assertEquals(1, newVillage.size());
		Assertions.assertEquals(existingParent.getId(), newVillage.get(0).getParent().getId());
		Assertions.assertEquals(1,
		    ahService.getAddressHierarchyEntriesByLevelAndName(levels.get(2), "Makhunoane Council").size());
		Assertions.assertEquals(1, ahService.getAddressHierarchyEntriesByLevelAndName(levels.get(0), "BOTHA-BOTHE").size());

		// the wholly new branch should have been created and rooted under the existing top-level entry
		AddressHierarchyEntry newDistrict = ahService
		        .getAddressHierarchyEntriesByLevelAndName(levels.get(1), "BRAND NEW DISTRICT").get(0);
		Assertions.assertEquals("BOTHA-BOTHE", newDistrict.getParent().getName());
	}

	@Test
	@Verifies(value = "should update the user generated id of an existing entry", method = "importAddressHierarchyFile()")
	public void importCsvFile_shouldUpdateUserGeneratedIdOfExistingEntry() throws Exception {

		AddressHierarchyService ahService = Context.getService(AddressHierarchyService.class);

		AddressHierarchyImportUtil.importAddressHierarchyFile(
		    getClass().getClassLoader().getResourceAsStream(CSV_FILE__WITH_USER_GENERATED_IDS_TO_IMPORT), "\\|", "%");

		List<AddressHierarchyLevel> levels = ahService.getOrderedAddressHierarchyLevels();
		Assertions.assertEquals("12",
		    ahService.getAddressHierarchyEntriesByLevelAndName(levels.get(0), "BOTHA-BOTHE").get(0).getUserGeneratedId());

		int countBefore = ahService.getAddressHierarchyEntryCount();
		AddressHierarchyImportUtil.importAddressHierarchyFile(
		    getClass().getClassLoader().getResourceAsStream(CSV_FILE_WITH_UPDATED_USER_GENERATED_IDS_TO_IMPORT), "\\|",
		    "%");

		// re-importing an entry that already exists must update its user generated id in place, not add a row
		Assertions.assertEquals(Integer.valueOf(countBefore), ahService.getAddressHierarchyEntryCount());
		Assertions.assertEquals("99",
		    ahService.getAddressHierarchyEntriesByLevelAndName(levels.get(0), "BOTHA-BOTHE").get(0).getUserGeneratedId());
	}

	@Test
	@Verifies(value = "should import large csv file", method = "importAddressHierarchyFile()")
	public void importCsvFile_shouldImportLargeCsvFile() throws Exception {
		
		AddressHierarchyService ahService = Context.getService(AddressHierarchyService.class);
		
		InputStream file = getClass().getClassLoader().getResourceAsStream(CSV_LARGE_FILE_TO_IMPORT);
		AddressHierarchyImportUtil.importAddressHierarchyFile(file, ",");
		
		// confirm that all 17902 entries have been added
		Assertions.assertEquals(Integer.valueOf(17902), ahService.getAddressHierarchyEntryCount());
		
		// verify that a few data points exist	
		List<AddressHierarchyLevel> levels = ahService.getOrderedAddressHierarchyLevels();
		Assertions.assertEquals("Haiti",
		    ahService.getAddressHierarchyEntriesByLevelAndName(levels.get(0), "Haiti").get(0).getName());
		Assertions.assertEquals("Sud-Est",
		    ahService.getAddressHierarchyEntriesByLevelAndName(levels.get(1), "Sud-Est").get(0).getName());
		Assertions.assertEquals("Thiotte",
		    ahService.getAddressHierarchyEntriesByLevelAndName(levels.get(2), "Thiotte").get(0).getName());
		Assertions.assertEquals("Tou Roche",
		    ahService.getAddressHierarchyEntriesByLevelAndName(levels.get(4), "Tou Roche").get(0).getName());
		
	}
	
}
