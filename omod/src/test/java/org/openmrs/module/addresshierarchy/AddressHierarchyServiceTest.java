package org.openmrs.module.addresshierarchy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.PersonAddress;
import org.openmrs.api.context.Context;
import org.openmrs.module.addresshierarchy.service.AddressHierarchyService;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.openmrs.test.Verifies;

public class AddressHierarchyServiceTest extends BaseModuleContextSensitiveTest {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	protected static final String XML_DATASET_PACKAGE_PATH = "org/openmrs/module/addresshierarchy/include/addressHierarchy-dataset.xml";
	
	@Before
	public void setupDatabase() throws Exception {
		initializeInMemoryDatabase();
		authenticate();
		executeDataSet(XML_DATASET_PACKAGE_PATH);
	}
	
	@Test
	@Verifies(value = "should get hierarchy level by id", method = "getAddressHierarchyLevel(int id)")
	public void getAddressHierarchyLevel_shouldGetAddressHierarchyLevelById() throws Exception {
		AddressHierarchyLevel level = Context.getService(AddressHierarchyService.class).getAddressHierarchyLevel(1);
		
		Assert.assertEquals("Country", level.getName());
		Assert.assertEquals("country", level.getAddressField().getName());
		
	}
	
	@Test
	@Verifies(value = "should get top level hierarchy", method = "getTopAddressHierarchyLevel(int id)")
	public void getTopAddressHierarchyLevel_shouldGetTopAddressHierarchyLevel() throws Exception {
		AddressHierarchyLevel level = Context.getService(AddressHierarchyService.class).getTopAddressHierarchyLevel();
		
		Assert.assertEquals("Country", level.getName());
		Assert.assertEquals("country", level.getAddressField().getName());
		
	}
	
	@Test
	@Verifies(value = "should get bottom level hierarchy", method = "getBottomAddressHierarchyLevel(int id)")
	public void getBottomAddressHierarchyLevel_shouldGetBottomAddressHierarchyLevel() throws Exception {
		AddressHierarchyLevel level = Context.getService(AddressHierarchyService.class).getBottomAddressHierarchyLevel();
		
		Assert.assertEquals("Street", level.getName());
		Assert.assertEquals(null, level.getAddressField());
		
	}
	
	@Test
	@Verifies(value = "should get all address hierarchy levels", method = "getAddressHierarchyLevels()")
	public void getAddressHierarchyLevels_shouldGetAllAddressHierarchyLevels() throws Exception {
		
		AddressHierarchyService ahService = Context.getService(AddressHierarchyService.class);
		
		List<AddressHierarchyLevel> levels = ahService.getAddressHierarchyLevels();
		
		Assert.assertEquals(7, levels.size());
		
		// make sure that the list returned contains all the levels
		Assert.assertTrue(levels.contains(ahService.getAddressHierarchyLevel(1)));
		Assert.assertTrue(levels.contains(ahService.getAddressHierarchyLevel(4)));
		Assert.assertTrue(levels.contains(ahService.getAddressHierarchyLevel(2)));
		Assert.assertTrue(levels.contains(ahService.getAddressHierarchyLevel(5)));
		Assert.assertTrue(levels.contains(ahService.getAddressHierarchyLevel(3)));
		Assert.assertTrue(levels.contains(ahService.getAddressHierarchyLevel(6)));
		Assert.assertTrue(levels.contains(ahService.getAddressHierarchyLevel(7)));
	}
	
	@Test
	@Verifies(value = "should get all address hierarchy levels in order", method = "getOrderedAddressHierarchyLevels()")
	public void getOrderedAddressHierarchyLevels_shouldGetAllAddressHierarchyLevelsInOrder() throws Exception {
		
		AddressHierarchyService ahService = Context.getService(AddressHierarchyService.class);
		
		List<AddressHierarchyLevel> levels = ahService.getOrderedAddressHierarchyLevels();
		
		Assert.assertEquals(7, levels.size());
		
		// make sure that the list returns the levels in the proper order
		Assert.assertTrue(levels.get(0) == (ahService.getAddressHierarchyLevel(1)));
		Assert.assertTrue(levels.get(1) == (ahService.getAddressHierarchyLevel(7)));
		Assert.assertTrue(levels.get(2) == (ahService.getAddressHierarchyLevel(4)));
		Assert.assertTrue(levels.get(3) == (ahService.getAddressHierarchyLevel(2)));
		Assert.assertTrue(levels.get(4) == (ahService.getAddressHierarchyLevel(5)));
		Assert.assertTrue(levels.get(5) == (ahService.getAddressHierarchyLevel(3)));
		Assert.assertTrue(levels.get(6) == (ahService.getAddressHierarchyLevel(6)));
		
	}
	
	@Test
	@Verifies(value = "should get all address hierarchy levels in order ignoring unmapped levels", method = "getOrderedAddressHierarchyLevels(Boolean)")
	public void getOrderedAddressHierarchyLevels_shouldGetAllAddressHierarchyLevelsInOrderIgnoringUnmappedLevels()
	                                                                                                              throws Exception {
		
		AddressHierarchyService ahService = Context.getService(AddressHierarchyService.class);
		
		List<AddressHierarchyLevel> levels = ahService.getOrderedAddressHierarchyLevels(false);
		
		Assert.assertEquals(5, levels.size());
		
		// make sure that the list returns the levels in the proper order
		Assert.assertTrue(levels.get(0) == (ahService.getAddressHierarchyLevel(1)));
		Assert.assertTrue(levels.get(1) == (ahService.getAddressHierarchyLevel(4)));
		Assert.assertTrue(levels.get(2) == (ahService.getAddressHierarchyLevel(2)));
		Assert.assertTrue(levels.get(3) == (ahService.getAddressHierarchyLevel(5)));
		Assert.assertTrue(levels.get(4) == (ahService.getAddressHierarchyLevel(3)));
		
	}
	
	@Test
	@Verifies(value = "should get all address hierarchy levels in order ignoring empty levels", method = "getOrderedAddressHierarchyLevels(Boolean,Boolean)")
	public void getOrderedAddressHierarchyLevels_shouldGetAllAddressHierarchyLevelsInOrderIgnoringEmptyLevels()
	                                                                                                              throws Exception {
		
		AddressHierarchyService ahService = Context.getService(AddressHierarchyService.class);
		
		List<AddressHierarchyLevel> levels = ahService.getOrderedAddressHierarchyLevels(true, false);
		
		Assert.assertEquals(6, levels.size());
		
		// make sure that the list returns the levels in the proper order
		Assert.assertTrue(levels.get(0) == (ahService.getAddressHierarchyLevel(1)));
		Assert.assertTrue(levels.get(1) == (ahService.getAddressHierarchyLevel(7)));
		Assert.assertTrue(levels.get(2) == (ahService.getAddressHierarchyLevel(4)));
		Assert.assertTrue(levels.get(3) == (ahService.getAddressHierarchyLevel(2)));
		Assert.assertTrue(levels.get(4) == (ahService.getAddressHierarchyLevel(5)));
		Assert.assertTrue(levels.get(5) == (ahService.getAddressHierarchyLevel(3)));
		
	}
	
	@Test
	@Verifies(value = "should get all address hierarchy levels in order ignoring unmapped levels and empty levels", method = "getOrderedAddressHierarchyLevels(Boolean,Boolean)")
	public void getOrderedAddressHierarchyLevels_shouldGetAllAddressHierarchyLevelsInOrderIgnoringUnmappedLevelsAndEmptyLevels()
	                                                                                                              throws Exception {
		
		AddressHierarchyService ahService = Context.getService(AddressHierarchyService.class);
		
		List<AddressHierarchyLevel> levels = ahService.getOrderedAddressHierarchyLevels(false, false);
		
		Assert.assertEquals(5, levels.size());
		
		// make sure that the list returns the levels in the proper order
		Assert.assertTrue(levels.get(0) == (ahService.getAddressHierarchyLevel(1)));
		Assert.assertTrue(levels.get(1) == (ahService.getAddressHierarchyLevel(4)));
		Assert.assertTrue(levels.get(2) == (ahService.getAddressHierarchyLevel(2)));
		Assert.assertTrue(levels.get(3) == (ahService.getAddressHierarchyLevel(5)));
		Assert.assertTrue(levels.get(4) == (ahService.getAddressHierarchyLevel(3)));
			
	}
	
	@Test
	@Verifies(value = "should add a address hierarchy level", method = "addAddressHierarchyLevel()")
	public void addAddressHierarchyLevel_shouldAddAddressHierarchyLevel() throws Exception {
		
		AddressHierarchyService ahService = Context.getService(AddressHierarchyService.class);
		
		AddressHierarchyLevel newLevel = ahService.addAddressHierarchyLevel();
		
		List<AddressHierarchyLevel> levels = ahService.getOrderedAddressHierarchyLevels();
		
		Assert.assertEquals(8, levels.size());
		
		// make sure that the list returns the levels in the proper order
		Assert.assertTrue(levels.get(0) == (ahService.getAddressHierarchyLevel(1)));
		Assert.assertTrue(levels.get(1) == (ahService.getAddressHierarchyLevel(7)));
		Assert.assertTrue(levels.get(2) == (ahService.getAddressHierarchyLevel(4)));
		Assert.assertTrue(levels.get(3) == (ahService.getAddressHierarchyLevel(2)));
		Assert.assertTrue(levels.get(4) == (ahService.getAddressHierarchyLevel(5)));
		Assert.assertTrue(levels.get(5) == (ahService.getAddressHierarchyLevel(3)));
		Assert.assertTrue(levels.get(6) == (ahService.getAddressHierarchyLevel(6)));
		Assert.assertTrue(levels.get(7) == (newLevel));
		
	}
	
	@Test
	@Verifies(value = "should count address hierarchy entries at level", method = "getAddressHierarchyEntryCountByLevel(AddressHierarchyLevel)")
	public void getAddressHierarchyEntryCountByLevel_shouldGetCountOfAddressHierarchyEntries() throws Exception {
		AddressHierarchyService ahService = Context.getService(AddressHierarchyService.class);
		
		Assert.assertEquals(new Integer(2), ahService.getAddressHierarchyEntryCountByLevel(ahService.getAddressHierarchyLevel(1)));
		Assert.assertEquals(new Integer(3), ahService.getAddressHierarchyEntryCountByLevel(ahService.getAddressHierarchyLevel(2)));
		Assert.assertEquals(new Integer(2), ahService.getAddressHierarchyEntryCountByLevel(ahService.getAddressHierarchyLevel(3)));
		Assert.assertEquals(new Integer(2), ahService.getAddressHierarchyEntryCountByLevel(ahService.getAddressHierarchyLevel(4)));
		Assert.assertEquals(new Integer(7), ahService.getAddressHierarchyEntryCountByLevel(ahService.getAddressHierarchyLevel(5)));
		Assert.assertEquals(new Integer(0), ahService.getAddressHierarchyEntryCountByLevel(ahService.getAddressHierarchyLevel(6)));
		Assert.assertEquals(new Integer(1), ahService.getAddressHierarchyEntryCountByLevel(ahService.getAddressHierarchyLevel(7)));
	}
	
	@Test
	@Verifies(value = "should fetch children address hierarchy entries", method = "getChildAddressHierarchyEntries(AddressHierarchyEntry entry)")
	public void getChildAddressHierarchyEntries_shouldGetChildAddressHierarchyEntries() throws Exception {
		AddressHierarchyService ahService = Context.getService(AddressHierarchyService.class);
		
		// fetch the children of "United States"
		List<AddressHierarchyEntry> entries = ahService.getChildAddressHierarchyEntries(ahService
		        .getAddressHierarchyEntry(1));
		
		// make sure the result set has 1 entry, New England
		Assert.assertEquals(1, entries.size());
		Assert.assertTrue(entries.contains(ahService.getAddressHierarchyEntry(17)));
		
		// fetch the children of "Plymouth (County)"
		entries = ahService.getChildAddressHierarchyEntries(ahService.getAddressHierarchyEntry(4));
		
		// make sure there are 4 entries, Scituate, Cohasseet, Hingham and Plymouth
		Assert.assertEquals(4, entries.size());
		Assert.assertTrue(entries.contains(ahService.getAddressHierarchyEntry(6)));
		Assert.assertTrue(entries.contains(ahService.getAddressHierarchyEntry(7)));
		Assert.assertTrue(entries.contains(ahService.getAddressHierarchyEntry(8)));
		Assert.assertTrue(entries.contains(ahService.getAddressHierarchyEntry(9)));
		
	}
	
	@Test
	@Verifies(value = "should fetch children address hierarchy entries by id", method = "getChildAddressHierarchyEntries(AddressHierarchyEngy entry)")
	public void getChildAddressHierarchyEntries_shouldGetChildAddressHierarchyEntriesById() throws Exception {
		AddressHierarchyService ahService = Context.getService(AddressHierarchyService.class);
		
		// fetch the children of "United States"
		List<AddressHierarchyEntry> entries = ahService.getChildAddressHierarchyEntries(ahService
		        .getAddressHierarchyEntry(1).getId());
		
		// make sure the result set has 1 entry, New England
		Assert.assertEquals(1, entries.size());
		Assert.assertTrue(entries.contains(ahService.getAddressHierarchyEntry(17)));
		
		// fetch the children of "Plymouth (County)"
		entries = ahService.getChildAddressHierarchyEntries(ahService.getAddressHierarchyEntry(4).getId());
		
		// make sure there are 4 entries, Scituate, Cohasseet, Hingham and Plymouth
		Assert.assertEquals(4, entries.size());
		Assert.assertTrue(entries.contains(ahService.getAddressHierarchyEntry(6)));
		Assert.assertTrue(entries.contains(ahService.getAddressHierarchyEntry(7)));
		Assert.assertTrue(entries.contains(ahService.getAddressHierarchyEntry(8)));
		Assert.assertTrue(entries.contains(ahService.getAddressHierarchyEntry(9)));
		
	}
	
	@Test
	@Verifies(value = "should fetch child address hierarchy entry referenced by name", method = "getChildAddressHierarchyEntryByName(AddressHierarchyEntry)")
	public void getChildAddressHierarchyEntryByName_shouldGetChildAddressHierarchyEntryByName() throws Exception {
		AddressHierarchyService ahService = Context.getService(AddressHierarchyService.class);
		
		AddressHierarchyEntry scituateMa = ahService.getAddressHierarchyEntry(7);
		AddressHierarchyEntry plymouthCounty = ahService.getAddressHierarchyEntry(4);
		
		Assert.assertEquals(scituateMa, ahService.getChildAddressHierarchyEntryByName(plymouthCounty, "Scituate"));
		
		// test to make sure the case-insensitive
		Assert.assertEquals(scituateMa, ahService.getChildAddressHierarchyEntryByName(plymouthCounty, "sCiTuAtE"));
		
	}
	
	@Test
	@Verifies(value = "should find all address hierarchy entries by level", method = "getAddressHierarchyEntriesByLevel(AddressHierarchyLevel)")
	public void getAddressHierarchyEntriesByLevel_shouldFindAllAddressHierarchyEntriesByLevel() throws Exception {
		AddressHierarchyService ahService = Context.getService(AddressHierarchyService.class);
		
		List<AddressHierarchyEntry> entries = ahService.getAddressHierarchyEntriesByLevel(ahService
		        .getAddressHierarchyLevel(5));
		
		Assert.assertEquals(7, entries.size());
		Assert.assertTrue(entries.contains(ahService.getAddressHierarchyEntry(6)));
		Assert.assertTrue(entries.contains(ahService.getAddressHierarchyEntry(7)));
		Assert.assertTrue(entries.contains(ahService.getAddressHierarchyEntry(8)));
		Assert.assertTrue(entries.contains(ahService.getAddressHierarchyEntry(9)));
		Assert.assertTrue(entries.contains(ahService.getAddressHierarchyEntry(10)));
		Assert.assertTrue(entries.contains(ahService.getAddressHierarchyEntry(11)));
		Assert.assertTrue(entries.contains(ahService.getAddressHierarchyEntry(15)));
	}
	
	@Test
	@Verifies(value = "should find all address hierarchy entries at top level", method = "getAddressHierarchyEntriesAtTopLevel()")
	public void getAddressHierarchyEntriesAtTopLevel_shouldGetAddressHierarchyEntriesAtTopLevel() throws Exception {
		AddressHierarchyService ahService = Context.getService(AddressHierarchyService.class);
		
		List<AddressHierarchyEntry> entries = ahService.getAddressHierarchyEntriesAtTopLevel();
		
		Assert.assertEquals(2, entries.size());
		Assert.assertTrue(entries.contains(ahService.getAddressHierarchyEntry(1)));
		Assert.assertTrue(entries.contains(ahService.getAddressHierarchyEntry(16)));
		
	}
	
	@Test
	@Verifies(value = "should find address hierarchy entry by id", method = "getAddressHierarchyEntry(int)")
	public void getAddressHierarchyEntry_shouldFindAddressHierarchyEntryById() throws Exception {
		AddressHierarchyService ahService = Context.getService(AddressHierarchyService.class);
		
		Assert.assertTrue(ahService.getAddressHierarchyEntry(3).getName().equals("Rhode Island"));
		Assert.assertTrue(ahService.getAddressHierarchyEntry(5).getName().equals("Suffolk County"));
		
	}
	
	@Test
	@Verifies(value = "should find address hierarchy entry by level and name", method = "getAddressHierarchyEntryByLevelAndName(AddressHierarchyLevel,String)")
	public void getAddressHierarchyEntryByLevelAndName_shouldFindAddressHierarchyEntryByLevelAndName() throws Exception {
		AddressHierarchyService ahService = Context.getService(AddressHierarchyService.class);
		
		List<AddressHierarchyEntry> entries = ahService.getAddressHierarchyEntriesByLevelAndName(ahService
		        .getAddressHierarchyLevel(5), "Plymouth");
		Assert.assertEquals(1, entries.size());
		Assert.assertTrue(entries.contains(ahService.getAddressHierarchyEntry(6)));
		
		// test case insensitive
		entries = ahService.getAddressHierarchyEntriesByLevelAndName(ahService
	        .getAddressHierarchyLevel(5), "pLyMoUtH");
		Assert.assertEquals(1, entries.size());
		Assert.assertTrue(entries.contains(ahService.getAddressHierarchyEntry(6)));
		
		entries = ahService.getAddressHierarchyEntriesByLevelAndName(ahService.getAddressHierarchyLevel(5), "Scituate");
		Assert.assertEquals(2, entries.size());
		Assert.assertTrue(entries.contains(ahService.getAddressHierarchyEntry(7)));
		Assert.assertTrue(entries.contains(ahService.getAddressHierarchyEntry(15)));
		
		entries = ahService.getAddressHierarchyEntriesByLevelAndName(ahService.getAddressHierarchyLevel(5), "Blah");
		Assert.assertEquals(0, entries.size());
	}
	
	/**
	@Test
	@Verifies(value = "should find appropriate address hierarchy entries", method = "searchHierarchy(String, int)")
	public void searchHierarchy_shouldFindAppropriateHierarchyEntries() throws Exception {
		AddressHierarchyService ahService = Context.getService(AddressHierarchyService.class);
		
		// first try a basic search
		Assert.assertTrue(ahService.searchAddressHierarchy("United States|New England|Massachusetts|Suffolk County|Boston")
		        .getId() == 10);
		
		// make sure it can distinguish between the two Scituates
		Assert.assertTrue(ahService.searchAddressHierarchy(
		    "United States|New England|Massachusetts|Plymouth County|Scituate").getId() == 7);
		Assert.assertTrue(ahService.searchAddressHierarchy(
		    "United States|New England|Rhode Island|Providence County|Scituate").getId() == 15);
		
	}
	*/
	
	@Test
	@Verifies(value = "should find possible matching address hiearchy values", method = "getPossibleAddressValues(PersonAddress,String)")
	public void getPossibleAddressValues_shouldFindPossibleAddressValues() throws Exception {
		
		AddressHierarchyService ahService = Context.getService(AddressHierarchyService.class);
		
		// lets start with a simple one
		PersonAddress address = new PersonAddress();
		address.setCountry("United States");
		List<String> results = ahService.getPossibleAddressValues(address, "stateProvince");
		Assert.assertEquals(2, results.size());
		Assert.assertTrue(results.contains("Rhode Island"));
		Assert.assertTrue(results.contains("Massachusetts"));
		
		// test that the search is case insensitive
		address = new PersonAddress();
		address.setCountry("uNiTeD sTaTes");
		results = ahService.getPossibleAddressValues(address, "stateProvince");
		Assert.assertEquals(2, results.size());
		Assert.assertTrue(results.contains("Rhode Island"));
		Assert.assertTrue(results.contains("Massachusetts"));
		
		
		// how about the "null" case?
		address = new PersonAddress();
		results = ahService.getPossibleAddressValues(address, "country");
		Assert.assertEquals(2, results.size());
		Assert.assertTrue(results.contains("United States"));
		Assert.assertTrue(results.contains("China"));
		
		// how about an unmapped address field?
		address = new PersonAddress();
		address.setCountry("United States");
		results = ahService.getPossibleAddressValues(address, "address1");
		Assert.assertEquals(null, results);
		
		// now try a two-level search
		address = new PersonAddress();
		address.setCountry("United States");
		address.setStateProvince("Massachusetts");
		results = ahService.getPossibleAddressValues(address, "countyDistrict");
		Assert.assertEquals(2, results.size());
		Assert.assertTrue(results.contains("Plymouth County"));
		Assert.assertTrue(results.contains("Suffolk County"));
		
		// now a three-level search
		address = new PersonAddress();
		address.setCountry("United States");
		address.setStateProvince("Massachusetts");
		address.setCountyDistrict("Suffolk County");
		results = ahService.getPossibleAddressValues(address, "cityVillage");
		Assert.assertEquals(2, results.size());
		Assert.assertTrue(results.contains("Boston"));
		Assert.assertTrue(results.contains("Newton"));
		
		// now one that searches the entire hierarchy
		address = new PersonAddress();
		address.setCountry("United States");
		address.setStateProvince("Massachusetts");
		address.setCountyDistrict("Suffolk County");
		address.setCityVillage("Boston");
		results = ahService.getPossibleAddressValues(address, "neighborhoodCell");
		Assert.assertEquals(2, results.size());
		Assert.assertTrue(results.contains("Jamaica Plain"));
		Assert.assertTrue(results.contains("Beacon Hill"));
		
		// now try a search the doesn't start at the top level
		address = new PersonAddress();
		address.setStateProvince("Massachusetts");
		address.setCountyDistrict("Suffolk County");
		results = ahService.getPossibleAddressValues(address, "cityVillage");
		Assert.assertEquals(2, results.size());
		Assert.assertTrue(results.contains("Boston"));
		Assert.assertTrue(results.contains("Newton"));
		
		// now try a search that skips a level
		address = new PersonAddress();
		address.setCountry("United States");
		address.setCountyDistrict("Suffolk County");
		results = ahService.getPossibleAddressValues(address, "cityVillage");
		Assert.assertEquals(2, results.size());
		Assert.assertTrue(results.contains("Boston"));
		Assert.assertTrue(results.contains("Newton"));
		
		// now try a search where there is not a value specified for the field immediately preceding the one we are searching for
		address = new PersonAddress();
		address.setCountry("United States");
		address.setStateProvince("Massachusetts");
		results = ahService.getPossibleAddressValues(address, "cityVillage");
		Assert.assertEquals(6, results.size());
		Assert.assertTrue(results.contains("Plymouth"));
		Assert.assertTrue(results.contains("Cohasset"));
		Assert.assertTrue(results.contains("Boston"));
		Assert.assertTrue(results.contains("Newton"));
		Assert.assertTrue(results.contains("Hingham"));
		Assert.assertTrue(results.contains("Scituate"));
		
		// try another tricky one
		address = new PersonAddress();
		address.setCountry("United States");
		results = ahService.getPossibleAddressValues(address, "neighborhoodCell");
		Assert.assertEquals(2, results.size());
		Assert.assertTrue(results.contains("Jamaica Plain"));
		Assert.assertTrue(results.contains("Beacon Hill"));
		
		// now try a couple that are invalid (and so should return no results)
		address = new PersonAddress();
		address.setCountry("China");
		address.setStateProvince("Massachusetts");
		results = ahService.getPossibleAddressValues(address, "cityVillage");
		Assert.assertEquals(0, results.size());
		
		// now try a couple that are invalid (and so should return no results)
		address = new PersonAddress();
		address.setStateProvince("Massachusetts");
		address.setCityVillage("Providence");
		results = ahService.getPossibleAddressValues(address, "cityVillage");
		Assert.assertEquals(0, results.size());
		
		// now try the inverse case, where we specify values *below* the field we are looking for in the hierarchy
		address = new PersonAddress();
		address.setCountyDistrict("Plymouth County");
		results = ahService.getPossibleAddressValues(address, "stateProvince");
		Assert.assertEquals(1, results.size());
		Assert.assertTrue(results.contains("Massachusetts"));
		
		// try a more specified hierarchy
		address = new PersonAddress();
		address.setCountyDistrict("Plymouth County");
		address.setStateProvince("Massachusetts");
		results = ahService.getPossibleAddressValues(address, "country");
		Assert.assertEquals(1, results.size());
		Assert.assertTrue(results.contains("United States"));
		
		// now try one with multiple options
		address = new PersonAddress();
		address.setCityVillage("Scituate");
		results = ahService.getPossibleAddressValues(address, "countyDistrict");
		Assert.assertEquals(2, results.size());
		Assert.assertTrue(results.contains("Plymouth County"));
		Assert.assertTrue(results.contains("Providence County"));
		
		// now try one with multiple options
		address = new PersonAddress();
		address.setCityVillage("Scituate");
		results = ahService.getPossibleAddressValues(address, "stateProvince");
		Assert.assertEquals(2, results.size());
		Assert.assertTrue(results.contains("Massachusetts"));
		Assert.assertTrue(results.contains("Rhode Island"));
		
		// now try a bogus one
		address = new PersonAddress();
		address.setCityVillage("Scituate");
		address.setCountyDistrict("Suffolk County");
		results = ahService.getPossibleAddressValues(address, "stateProvince");
		Assert.assertEquals(0, results.size());
		
		// now try a mix of higher and lower entries
		address = new PersonAddress();
		address.setCountry("United States");
		address.setStateProvince("Massachusetts");
		address.setCityVillage("Scituate");
		results = ahService.getPossibleAddressValues(address, "countyDistrict");
		Assert.assertEquals(1, results.size());
		Assert.assertTrue(results.contains("Plymouth County"));
		
		// now try a mix of higher and lower that is bogus
		address = new PersonAddress();
		address.setCountry("United States");
		address.setStateProvince("Rhode Island");
		address.setCityVillage("Hingham");
		results = ahService.getPossibleAddressValues(address, "countyDistrict");
		Assert.assertEquals(0, results.size());
	}
	
	@Test
	@Verifies(value = "should find possible matching address hierarchy values", method = "getPossibleAddressValues(Map<String,String>,String)")
	public void getPossibleAddressValuesMap_shouldFindPossibleAddressValues() throws Exception {
	
			AddressHierarchyService ahService = Context.getService(AddressHierarchyService.class);
	
			// lets start with a simple one
			Map<String,String> addressMap = new HashMap<String,String>();
			addressMap.put("country", "United States");
			List<String> results = ahService.getPossibleAddressValues(addressMap, "stateProvince");
			Assert.assertEquals(2, results.size());
			Assert.assertTrue(results.contains("Rhode Island"));
			Assert.assertTrue(results.contains("Massachusetts"));
			
			// now try a two-level search
			addressMap = new HashMap<String,String>();
			addressMap.put("country", "United States");
			addressMap.put("stateProvince", "Massachusetts");
			results = ahService.getPossibleAddressValues(addressMap, "countyDistrict");
			Assert.assertEquals(2, results.size());
			Assert.assertTrue(results.contains("Plymouth County"));
			Assert.assertTrue(results.contains("Suffolk County"));
	}
	
	@Test
	@Verifies(value = "should find possible full addresses that match search string", method = "getPossibleFullAddresses(String)")
	public void getPossibleFullAddresses_shouldFindPossibleFullAddressesThatMatchSearchString() throws Exception {
		
		AddressHierarchyService ahService = Context.getService(AddressHierarchyService.class);
	
		// try a single word that is an exact match for an entry
		List<String> results = ahService.getPossibleFullAddresses("boston");
		Assert.assertEquals(2,results.size());
		Assert.assertTrue(results.contains("United States|New England|Massachusetts|Suffolk County|Boston|Jamaica Plain"));
		Assert.assertTrue(results.contains("United States|New England|Massachusetts|Suffolk County|Boston|Beacon Hill"));
		
		// try a partial word
		results = ahService.getPossibleFullAddresses("bos");
		Assert.assertEquals(2,results.size());
		Assert.assertTrue(results.contains("United States|New England|Massachusetts|Suffolk County|Boston|Jamaica Plain"));
		Assert.assertTrue(results.contains("United States|New England|Massachusetts|Suffolk County|Boston|Beacon Hill"));
		
		// test case-sensitive
		results = ahService.getPossibleFullAddresses("bOsToN");
		Assert.assertEquals(2,results.size());
		Assert.assertTrue(results.contains("United States|New England|Massachusetts|Suffolk County|Boston|Jamaica Plain"));
		Assert.assertTrue(results.contains("United States|New England|Massachusetts|Suffolk County|Boston|Beacon Hill"));
		
		// test multiple words
		results = ahService.getPossibleFullAddresses("jamaica boston");
		Assert.assertEquals(1,results.size());
		Assert.assertTrue(results.contains("United States|New England|Massachusetts|Suffolk County|Boston|Jamaica Plain"));
		
		// test multiple words
		results = ahService.getPossibleFullAddresses("boston new england beacon hill");
		Assert.assertEquals(1,results.size());
		Assert.assertTrue(results.contains("United States|New England|Massachusetts|Suffolk County|Boston|Beacon Hill"));
		
		// test with multiple, partial words
		results = ahService.getPossibleFullAddresses("bos hil");
		Assert.assertEquals(1,results.size());
		Assert.assertTrue(results.contains("United States|New England|Massachusetts|Suffolk County|Boston|Beacon Hill"));
		
		// test a string with commas (or other non-word characters) in it (which should be ignored)
		results = ahService.getPossibleFullAddresses("boston, beacon hill");
		Assert.assertEquals(1,results.size());
		Assert.assertTrue(results.contains("United States|New England|Massachusetts|Suffolk County|Boston|Beacon Hill"));
		
		
		// test case with no results
		results = ahService.getPossibleFullAddresses("boston new england beacon hill plymouth");
		Assert.assertEquals(0,results.size());
	
		
	}
}
