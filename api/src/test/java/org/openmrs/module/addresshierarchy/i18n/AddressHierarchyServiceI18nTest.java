package org.openmrs.module.addresshierarchy.i18n;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Test;
import org.openmrs.Patient;
import org.openmrs.PersonAddress;
import org.openmrs.api.context.Context;
import org.openmrs.module.addresshierarchy.AddressHierarchyEntry;
import org.openmrs.module.addresshierarchy.AddressHierarchyLevel;
import org.openmrs.module.addresshierarchy.AddressToEntryMap;
import org.openmrs.module.addresshierarchy.service.AddressHierarchyService;
import org.openmrs.module.addresshierarchy.util.AddressHierarchyUtil;
import org.openmrs.test.SkipBaseSetup;
import org.openmrs.test.Verifies;
import org.springframework.test.annotation.DirtiesContext;

@DirtiesContext
@SkipBaseSetup
public class AddressHierarchyServiceI18nTest extends I18nModuleContextSensitiveTest {

	protected final Log log = LogFactory.getLog(getClass());

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
	@Verifies(value = "should find address hierarchy entry by id", method = "getAddressHierarchyEntry(int)")
	public void getAddressHierarchyEntry_shouldFindAddressHierarchyEntryById() throws Exception {
		AddressHierarchyService ahService = Context.getService(AddressHierarchyService.class);

		Assert.assertEquals("Rhode Island", ahService.getAddressHierarchyEntry(3).getLocalizedName());
		Assert.assertEquals("Suffolk County", ahService.getAddressHierarchyEntry(5).getLocalizedName());
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

	@Test
	@Verifies(value = "should find address hierarchy entry by level and name and parent", method = "getAddressHierarchyEntryByLevelAndNameAndParent(AddressHierarchyLevel,String)")
	public void getAddressHierarchyEntryByLevelAndNameAndParent_shouldFindAddressHierarchyEntryByLevelAndNameAndParent() throws Exception {
		AddressHierarchyService ahService = Context.getService(AddressHierarchyService.class);

		// do a basic test
		List<AddressHierarchyEntry> entries = ahService.getAddressHierarchyEntriesByLevelAndNameAndParent(ahService
				.getAddressHierarchyLevel(5), "Plymouth", ahService.getAddressHierarchyEntry(4));
		Assert.assertEquals(1, entries.size());
		Assert.assertTrue(entries.contains(ahService.getAddressHierarchyEntry(6)));

		// now make sure that Plymouth is NOT found if the parent is set to Rhode Island instead of Massachusetts
		entries = ahService.getAddressHierarchyEntriesByLevelAndNameAndParent(ahService
				.getAddressHierarchyLevel(5), "Plymouth", ahService.getAddressHierarchyEntry(5));
		Assert.assertEquals(0, entries.size());
	}

	@Test
	@Verifies(value = "should find possible matching address hiearchy values", method = "getPossibleAddressValues(PersonAddress,String)")
	public void getPossibleAddressValues_shouldFindPossibleAddressValues() throws Exception {

		AddressHierarchyService ahService = Context.getService(AddressHierarchyService.class);

		// lets start with a simple one
		PersonAddress address = new PersonAddress();
		address.setCountry("United States");
		List<String> results = ahService.getPossibleAddressValues(address, "stateProvince");
		Assert.assertEquals(3, results.size());
		Assert.assertTrue(results.contains("Rhode Island"));
		Assert.assertTrue(results.contains("Massachusetts"));
		Assert.assertTrue(results.contains("Hawaii"));

		// test that the search is case insensitive
		address = new PersonAddress();
		address.setCountry("uNiTeD sTaTes");
		results = ahService.getPossibleAddressValues(address, "stateProvince");
		Assert.assertEquals(3, results.size());
		Assert.assertTrue(results.contains("Rhode Island"));
		Assert.assertTrue(results.contains("Massachusetts"));
		Assert.assertTrue(results.contains("Hawaii"));

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
		results = ahService.getPossibleAddressValues(address, "address3");
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
		Assert.assertEquals(7, results.size());
		Assert.assertTrue(results.contains("Plymouth"));
		Assert.assertTrue(results.contains("Cohasset"));
		Assert.assertTrue(results.contains("Boston"));
		Assert.assertTrue(results.contains("Newton"));
		Assert.assertTrue(results.contains("Hingham"));
		Assert.assertTrue(results.contains("Scituate"));
		Assert.assertTrue(results.contains("Ãccénts"));

		// try another tricky one
		address = new PersonAddress();
		address.setCountry("United States");
		results = ahService.getPossibleAddressValues(address, "address3");
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

		// try with unmatched name at top level
		address = new PersonAddress();
		address.setCountry("Blah");
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
		Assert.assertEquals(3, results.size());
		Assert.assertTrue(results.contains("Rhode Island"));
		Assert.assertTrue(results.contains("Massachusetts"));
		Assert.assertTrue(results.contains("Hawaii"));

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
	@Verifies(value = "should generate possible full addresses for AddressHierarchyEntry", method = "generatePossibleFullAddressesForAddressHierarchyEntry(AddressHierarchyEntry)")
	public void generatePossibleFullAddresses_shouldGeneratePossibleFullAddressesForAddressHierarchyEntry() {

		AddressHierarchyService ahService = Context.getService(AddressHierarchyService.class);

		// try a child entry (Jampaica Plain)
		List<String> results = ahService.getPossibleFullAddresses(ahService.getAddressHierarchyEntry(12));
		Assert.assertEquals(1,results.size());
		Assert.assertTrue(results.contains("United States|New England|Massachusetts|Suffolk County|Boston|Jamaica Plain"));

		// try a mid-level entry (Plymouth County)
		results = ahService.getPossibleFullAddresses(ahService.getAddressHierarchyEntry(4));
		Assert.assertEquals(5,results.size());
		Assert.assertTrue(results.contains("United States|New England|Massachusetts|Plymouth County|Scituate"));
		Assert.assertTrue(results.contains("United States|New England|Massachusetts|Plymouth County|Plymouth"));
		Assert.assertTrue(results.contains("United States|New England|Massachusetts|Plymouth County|Cohasset"));
		Assert.assertTrue(results.contains("United States|New England|Massachusetts|Plymouth County|Hingham"));
		Assert.assertTrue(results.contains("United States|New England|Massachusetts|Plymouth County|Ãccénts"));

		// try a entry with blank parent (BlankRegion)
		results = ahService.getPossibleFullAddresses(ahService.getAddressHierarchyEntry(20));
		Assert.assertEquals(1, results.size());
		Assert.assertTrue(results.contains("United States||Hawaii"));

		// try a top-level entry (China)
		results = ahService.getPossibleFullAddresses(ahService.getAddressHierarchyEntry(16));
		Assert.assertEquals(1,results.size());
		Assert.assertTrue(results.contains("China"));

		// make sure it handles null properly
		AddressHierarchyEntry nullTest = null;
		results = ahService.getPossibleFullAddresses(nullTest);
		Assert.assertEquals(0,results.size());
	}


	@Test
	@Verifies(value = "should find possible full addresses that match search string", method = "getPossibleFullAddresses(String)")
	public void searchAddresses_shouldFindPossibleFullAddressesThatMatchSearchString() throws Exception {

		AddressHierarchyService ahService = Context.getService(AddressHierarchyService.class);

		// try a single word that is an exact match for an entry
		Set<String> results = ahService.searchAddresses("boston", null);
		Assert.assertEquals(2,results.size());
		Assert.assertTrue(results.contains("United States|New England|Massachusetts|Suffolk County|Boston|Jamaica Plain"));
		Assert.assertTrue(results.contains("United States|New England|Massachusetts|Suffolk County|Boston|Beacon Hill"));

		results = ahService.searchAddresses("china",null);
		Assert.assertEquals(1,results.size());
		Assert.assertTrue(results.contains("China"));

		// try a partial word
		results = ahService.searchAddresses("bos", null);
		Assert.assertEquals(2,results.size());
		Assert.assertTrue(results.contains("United States|New England|Massachusetts|Suffolk County|Boston|Jamaica Plain"));
		Assert.assertTrue(results.contains("United States|New England|Massachusetts|Suffolk County|Boston|Beacon Hill"));

		results = ahService.searchAddresses("scit", null);
		Assert.assertEquals(2,results.size());
		Assert.assertTrue(results.contains("United States|New England|Massachusetts|Plymouth County|Scituate"));
		Assert.assertTrue(results.contains("United States|New England|Rhode Island|Providence County|Scituate"));

		// test case-sensitive
		results = ahService.searchAddresses("bOsToN", null);
		Assert.assertEquals(2,results.size());
		Assert.assertTrue(results.contains("United States|New England|Massachusetts|Suffolk County|Boston|Jamaica Plain"));
		Assert.assertTrue(results.contains("United States|New England|Massachusetts|Suffolk County|Boston|Beacon Hill"));

		// test multiple words
		results = ahService.searchAddresses("jamaica boston", null);
		Assert.assertEquals(1,results.size());
		Assert.assertTrue(results.contains("United States|New England|Massachusetts|Suffolk County|Boston|Jamaica Plain"));

		// test multiple words
		results = ahService.searchAddresses("boston new england beacon hill", null);
		Assert.assertEquals(1,results.size());
		Assert.assertTrue(results.contains("United States|New England|Massachusetts|Suffolk County|Boston|Beacon Hill"));

		// test with multiple, partial words
		results = ahService.searchAddresses("bos hil", null);
		Assert.assertEquals(1,results.size());
		Assert.assertTrue(results.contains("United States|New England|Massachusetts|Suffolk County|Boston|Beacon Hill"));

		// test a string with commas (or other non-word characters) in it (which should be ignored)
		results = ahService.searchAddresses("boston, beacon hill", null);
		Assert.assertEquals(1,results.size());
		Assert.assertTrue(results.contains("United States|New England|Massachusetts|Suffolk County|Boston|Beacon Hill"));

		// test case with no results
		results = ahService.searchAddresses("boston new england beacon hill plymouth", null);
		Assert.assertEquals(0,results.size());

		// test cases whe matching accented characters
		results = ahService.searchAddresses("Ãccénts", null);
		Assert.assertEquals(1,results.size());
		Assert.assertTrue(results.contains("United States|New England|Massachusetts|Plymouth County|Ãccénts"));

		// test that non-accented characters match accented addresses
		results = ahService.searchAddresses("Accents", null);
		Assert.assertEquals(1,results.size());
		Assert.assertTrue(results.contains("United States|New England|Massachusetts|Plymouth County|Ãccénts"));

	}

	@Test
	@Verifies(value = "should find possible full addresses that match search string", method = "getPossibleFullAddresses(String)")
	public void searchAddresses_shouldRestrictSearchToSpecifiedLevel() throws Exception {

		AddressHierarchyService ahService = Context.getService(AddressHierarchyService.class);

		// try a single word that is an exact match for an entry
		Set<String> results = ahService.searchAddresses("boston", ahService.getAddressHierarchyLevel(5));
		Assert.assertEquals(1,results.size());
		Assert.assertTrue(results.contains("Boston"));

		// make sure that a single word for the wrong level doesn't match
		results = ahService.searchAddresses("boston", ahService.getAddressHierarchyLevel(1));
		Assert.assertEquals(0,results.size());

		// make sure multiple matches are found
		results = ahService.searchAddresses("county", ahService.getAddressHierarchyLevel(2));
		Assert.assertEquals(3,results.size());
		Assert.assertTrue(results.contains("Plymouth County"));
		Assert.assertTrue(results.contains("Suffolk County"));
		Assert.assertTrue(results.contains("Providence County"));

		// make sure matching still works with multiple words
		results = ahService.searchAddresses("plymouth coun", ahService.getAddressHierarchyLevel(2));
		Assert.assertEquals(1,results.size());
		Assert.assertTrue(results.contains("Plymouth County"));
	}

	@Test
	@Verifies(value = "should getAddressToyEntryMap by id", method = "getAddressToyEntryMap(int id)")
	public void getAddressToEntryMap_shouldGetAddressToEntryMapById() throws Exception {
		AddressToEntryMap addressToEntry = Context.getService(AddressHierarchyService.class).getAddressToEntryMap(1);

		Assert.assertEquals(new Integer(2), addressToEntry.getAddress().getId());
		Assert.assertEquals("Scituate", addressToEntry.getEntry().getLocalizedName());
	}

	@Test
	@Verifies(value = "should get AddressToEntryMap by PersonAddress", method = "getAddressToEntryMapByPersonAddress(PersonAddress address)")
	public void getAddressToEntryMapByPersonAddress_shouldGetAddressToEntryMapByPersonAddress() throws Exception {
		PersonAddress address = Context.getPersonService().getPerson(2).getPersonAddress();

		List<AddressToEntryMap> addressToEntryList = Context.getService(AddressHierarchyService.class).getAddressToEntryMapsByPersonAddress(address);

		// this should load the four AddressToEntry records defined in the test dataset
		Assert.assertEquals(4, addressToEntryList.size());

		Set<AddressHierarchyEntry> entries = new HashSet<AddressHierarchyEntry>();

		for (AddressToEntryMap addressToEntry : addressToEntryList) {
			Assert.assertTrue(address.equals(addressToEntry.getAddress()));
			entries.add(addressToEntry.getEntry());
		}

		Assert.assertTrue(entries.contains(Context.getService(AddressHierarchyService.class).getAddressHierarchyEntry(1)));
		Assert.assertTrue(entries.contains(Context.getService(AddressHierarchyService.class).getAddressHierarchyEntry(2))); 
		Assert.assertTrue(entries.contains(Context.getService(AddressHierarchyService.class).getAddressHierarchyEntry(4))); 
		Assert.assertTrue(entries.contains(Context.getService(AddressHierarchyService.class).getAddressHierarchyEntry(7))); 
	}

	@Test
	@Verifies(value = "should save AddressToEntryMap", method = "saveAddressToEntryMap(AddressToEntry addressToEntry)")
	public void saveAddressToEntryMap_shouldSaveAddressToEntryyMap() throws Exception {
		AddressHierarchyService ahService = Context.getService(AddressHierarchyService.class);

		// create and save a new AddressToEntryMap and save it
		PersonAddress address = Context.getPersonService().getPerson(2).getPersonAddress();
		AddressHierarchyEntry entry = ahService.getAddressHierarchyEntry(17);
		ahService.saveAddressToEntryMap(new AddressToEntryMap(address, entry));

		// now load the records for this PersonAddress and make sure it includes the record we just added
		List<AddressToEntryMap> addressToEntryList = ahService.getAddressToEntryMapsByPersonAddress(address);

		Assert.assertEquals(5, addressToEntryList.size());

		Set<AddressHierarchyEntry> entries = new HashSet<AddressHierarchyEntry>();

		for (AddressToEntryMap addressToEntry : addressToEntryList) {
			Assert.assertTrue(address.equals(addressToEntry.getAddress()));
			entries.add(addressToEntry.getEntry());
		}

		Assert.assertTrue(entries.contains(Context.getService(AddressHierarchyService.class).getAddressHierarchyEntry(1)));
		Assert.assertTrue(entries.contains(Context.getService(AddressHierarchyService.class).getAddressHierarchyEntry(2))); 
		Assert.assertTrue(entries.contains(Context.getService(AddressHierarchyService.class).getAddressHierarchyEntry(4))); 
		Assert.assertTrue(entries.contains(Context.getService(AddressHierarchyService.class).getAddressHierarchyEntry(7))); 
		Assert.assertTrue(entries.contains(Context.getService(AddressHierarchyService.class).getAddressHierarchyEntry(17)));  

	}

	@Test
	@Verifies(value = "should delete AddressToEntryMaps associated with PersonAddress", method = "deleteAddressToEntryMapsByPersonAddress(PersonAddress address)")
	public void deleteAddressToEntryMapsByPersonAddress_shouldDeleteAddressToEntryMapsByPersonAddress() throws Exception {
		AddressHierarchyService ahService = Context.getService(AddressHierarchyService.class);

		PersonAddress address = Context.getPersonService().getPerson(2).getPersonAddress();
		ahService.deleteAddressToEntryMapsByPersonAddress(address);

		// confirm that the maps for this address have been deleted
		List<AddressToEntryMap> maps = ahService.getAddressToEntryMapsByPersonAddress(address);
		Assert.assertTrue(maps == null || maps.size() == 0);

		// as a double check, make sure the map in the test data for another address still exists
		Assert.assertNotNull(ahService.getAddressToEntryMap(5));
	}

	@Test
	@Verifies(value = "should create empty set for PersonAddress field with no fields matching address hierarchy entries", method = "updateAddressToEntryMapsForPersonAddress()")
	public void updateAddressToEntryMapsForPersonAddress_shouldCreateEmptySetIfNoMatches() {

		AddressHierarchyService ahService = Context.getService(AddressHierarchyService.class);

		// load an existing Person Address field that doesn't have any matches in the hierarchy
		PersonAddress address = Context.getPersonService().getPerson(2).getPersonAddress();

		// call the method to update the maps for this address
		ahService.updateAddressToEntryMapsForPersonAddress(address);

		// confirm that no maps have been created
		List<AddressToEntryMap> addressToEntryList = ahService.getAddressToEntryMapsByPersonAddress(address);
		Assert.assertEquals(0, addressToEntryList.size());
	}

	@Test
	@Verifies(value = "should create set of AddressToEntryMaps for passed PersonAddress", method = "updateAddressToEntryMapsForPerson()")
	public void updateAddressToEntryMapsForPerson_shouldCreateAddressToEntryMaps() {

		AddressHierarchyService ahService = Context.getService(AddressHierarchyService.class);

		// create a new person address with some sample date
		PersonAddress address = new PersonAddress();
		address.setCountry("united states");
		address.setStateProvince("massachusetts");
		address.setCountyDistrict("suffolk county");
		// skip a level
		address.setAddress3("jamaica plain");

		// add this address to an existing patient and persist it
		Patient patient = Context.getPatientService().getPatient(2);
		patient.addAddress(address);
		Context.getPatientService().savePatient(patient);

		// call the method to update the maps for this patient
		ahService.updateAddressToEntryMapsForPerson(patient);

		// make sure that mapping records have been created for united states, massachusetts and suffolk, and jamaica plain
		List<AddressToEntryMap> addressToEntryList = ahService.getAddressToEntryMapsByPersonAddress(address);
		Assert.assertEquals(4, addressToEntryList.size());

		Set<AddressHierarchyEntry> entries = new HashSet<AddressHierarchyEntry>();

		for (AddressToEntryMap addressToEntry : addressToEntryList) {
			address = ahService.getI18nPersonAddress(address);
			Assert.assertTrue(address.equalsContent(addressToEntry.getAddress()));
			entries.add(addressToEntry.getEntry());
		}

		Assert.assertTrue(entries.contains(Context.getService(AddressHierarchyService.class).getAddressHierarchyEntry(1)));
		Assert.assertTrue(entries.contains(Context.getService(AddressHierarchyService.class).getAddressHierarchyEntry(2)));
		Assert.assertTrue(entries.contains(Context.getService(AddressHierarchyService.class).getAddressHierarchyEntry(5))); 
		Assert.assertTrue(entries.contains(Context.getService(AddressHierarchyService.class).getAddressHierarchyEntry(12)));  
	}


	@Test
	@Verifies(value = "should create set of AddressToEntryMaps for passed PersonAddress", method = "updateAddressToEntryMapsForPerson()")
	public void updateAddressToEntryMapsForPerson_shouldCreateAddressToEntryMapsEvenIfTopLevelEmpty() {

		AddressHierarchyService ahService = Context.getService(AddressHierarchyService.class);

		// now let's test that it works even if the top level is empty
		// create a new person address with some sample date
		PersonAddress address = new PersonAddress();
		address.setStateProvince("massachusetts");
		address.setCountyDistrict("suffolk county");
		// skip a level
		address.setAddress3("jamaica plain");

		// add this address to an existing patient and persist it
		Patient patient = Context.getPatientService().getPatient(2);
		patient.addAddress(address);
		Context.getPatientService().savePatient(patient);

		// call the method to update the maps for this patient
		ahService.updateAddressToEntryMapsForPerson(patient);

		// make sure that mapping records have been created for united states, massachusetts and suffolk, and jamaica plain
		List<AddressToEntryMap> addressToEntryList = ahService.getAddressToEntryMapsByPersonAddress(address);
		Assert.assertEquals(3, addressToEntryList.size());

		Set<AddressHierarchyEntry> entries = new HashSet<AddressHierarchyEntry>();

		for (AddressToEntryMap addressToEntry : addressToEntryList) {
			address = ahService.getI18nPersonAddress(address);
			Assert.assertTrue(address.equalsContent(addressToEntry.getAddress()));
			entries.add(addressToEntry.getEntry());
		}

		Assert.assertTrue(entries.contains(Context.getService(AddressHierarchyService.class).getAddressHierarchyEntry(2)));
		Assert.assertTrue(entries.contains(Context.getService(AddressHierarchyService.class).getAddressHierarchyEntry(5))); 
		Assert.assertTrue(entries.contains(Context.getService(AddressHierarchyService.class).getAddressHierarchyEntry(12)));  
	}

	@Test
	@Verifies(value = "should not create AddressToEntryMap if entry not unique", method = "updateAddressToEntryMapsForPerson()")
	public void updateAddressToEntryMapsForPerson_shouldNotCreateAddressToEntryMapIfEntryNotUnique() {

		AddressHierarchyService ahService = Context.getService(AddressHierarchyService.class);

		// create a person address with non-unique city entry 
		PersonAddress address = new PersonAddress();
		address.setCityVillage("scituate");

		// add this address to an existing patient and persist it
		Patient patient = Context.getPatientService().getPatient(2);
		patient.addAddress(address);
		Context.getPatientService().savePatient(patient);

		// call the method to update the maps for this patient
		ahService.updateAddressToEntryMapsForPerson(patient);

		// make sure that no mapping records have been created
		List<AddressToEntryMap> addressToEntryList = ahService.getAddressToEntryMapsByPersonAddress(address);
		Assert.assertEquals(0, addressToEntryList.size());
	}

	@Test
	@Verifies(value = "should create set of AddressToEntryMaps for passed PersonAddress", method = "updateAddressToEntryMapsForPerson()")
	public void updateAddressToEntryMapsForPatientsWithDateChangedAfter_shouldUpdatePatient() {
		AddressHierarchyService ahService = Context.getService(AddressHierarchyService.class);

		// try the same address as previously, but now try to trigger it via the updateAddressToEntryMapsForPatientsWithDateChangedAfter()
		Date date = Date.from(Instant.now().minusSeconds(1));// get a timestamp BEFORE we update the patient

		PersonAddress address = new PersonAddress();
		address.setStateProvince("massachusetts");
		address.setCountyDistrict("suffolk county");
		address.setAddress3("jamaica plain");

		// add this address to an existing patient and persist it
		Patient patient = Context.getPatientService().getPatient(2);
		patient.addAddress(address);
		Context.getPatientService().savePatient(patient);

		// call the method to update the maps based on date changed
		AddressHierarchyUtil.updateAddressToEntryMapsForPatientsWithDateChangedAfter(date);

		// make sure that mapping records have been created for united states, massachusetts and suffolk, and jamaica plain
		List<AddressToEntryMap> addressToEntryList = ahService.getAddressToEntryMapsByPersonAddress(address);
		Assert.assertEquals(3, addressToEntryList.size());

		Set<AddressHierarchyEntry> entries = new HashSet<>();

		for (AddressToEntryMap addressToEntry : addressToEntryList) {
			address = ahService.getI18nPersonAddress(address);
			Assert.assertTrue(address.equalsContent(addressToEntry.getAddress()));
			entries.add(addressToEntry.getEntry());
		}

		Assert.assertTrue(entries.contains(Context.getService(AddressHierarchyService.class).getAddressHierarchyEntry(2)));
		Assert.assertTrue(entries.contains(Context.getService(AddressHierarchyService.class).getAddressHierarchyEntry(5))); 
		Assert.assertTrue(entries.contains(Context.getService(AddressHierarchyService.class).getAddressHierarchyEntry(12)));  
	}

	@Test
	@Verifies(value = "should create set of AddressToEntryMaps for passed PersonAddress", method = "updateAddressToEntryMapsForPerson()")
	public void updateAddressToEntryMapsForPatientsWithDateChangedAfter_shouldNotUpdatePatient() throws InterruptedException {
		AddressHierarchyService ahService = Context.getService(AddressHierarchyService.class);

		// now perform the same test but set the date changed to be AFTER the patient save

		// try the same address as previously, but now try to trigger it via the updateAddressToEntryMapsForPatientsWithDateChangedAfter()  
		PersonAddress address = new PersonAddress();
		address.setStateProvince("massachusetts");
		address.setCountyDistrict("suffolk county");
		address.setAddress3("jamaica plain");

		// add this address to an existing patient and persist it
		Patient patient = Context.getPatientService().getPatient(2);
		patient.addAddress(address);
		Context.getPatientService().savePatient(patient);

		Thread.sleep(10);
		Date date = new Date(); // get a timestamp AFTER we update the patient

		// call the method to update the maps based on date changed
		AddressHierarchyUtil.updateAddressToEntryMapsForPatientsWithDateChangedAfter(date);

		// make sure that no mappings have been created because the timestamp we test against is AFTER the patient was saved
		List<AddressToEntryMap> addressToEntryList = ahService.getAddressToEntryMapsByPersonAddress(address);
		Assert.assertTrue(addressToEntryList == null || addressToEntryList.size() == 0);
	}

	@Test
	@Verifies(value = "should update set of AddressToEntryMaps for passed PersonAddress", method = "updateAddressToEntryMapsForPerson()")
	public void updateAddressToEntryMapsForPerson_shouldUpdateAddressToEntryMaps() {

		AddressHierarchyService ahService = Context.getService(AddressHierarchyService.class);

		// create a new person address with some sample date
		PersonAddress address = new PersonAddress();
		address.setCountry("united states");
		address.setStateProvince("massachusetts");
		address.setCountyDistrict("suffolk county");
		// skip a level
		address.setAddress3("jamaica plain");

		// add this address to an existing patient and persist it
		Patient patient = Context.getPatientService().getPatient(2);
		patient.addAddress(address);
		Context.getPatientService().savePatient(patient);

		// call the method to update the maps for this patient
		ahService.updateAddressToEntryMapsForPerson(patient);

		// now CHANGE the data for this address
		address.setStateProvince("rhode island");
		address.setCountyDistrict("providence county");
		address.setCityVillage("scituate");
		address.setAddress3("");

		// resave the patient and re-call the method to update the address entry maps for this patient
		Context.getPatientService().savePatient(patient);
		ahService.updateAddressToEntryMapsForPerson(patient);

		// make sure that mapping records have been created for united states, rhode island, province, and scituate
		List<AddressToEntryMap> addressToEntryList = ahService.getAddressToEntryMapsByPersonAddress(address);
		Assert.assertEquals(4, addressToEntryList.size());

		Set<AddressHierarchyEntry> entries = new HashSet<AddressHierarchyEntry>();

		for (AddressToEntryMap addressToEntry : addressToEntryList) {
			Assert.assertTrue(address.equalsContent(addressToEntry.getAddress()));
			entries.add(addressToEntry.getEntry());
		}

		Assert.assertTrue(entries.contains(Context.getService(AddressHierarchyService.class).getAddressHierarchyEntry(1)));
		Assert.assertTrue(entries.contains(Context.getService(AddressHierarchyService.class).getAddressHierarchyEntry(3)));
		Assert.assertTrue(entries.contains(Context.getService(AddressHierarchyService.class).getAddressHierarchyEntry(14)));  
		Assert.assertTrue(entries.contains(Context.getService(AddressHierarchyService.class).getAddressHierarchyEntry(15)));  
	}

	@Test
	@Verifies(value = "should search anywhere within the address name", method = "getAddressHierarchyEntriesByLevelAndLikeName()")
	public void getAddressHierarchyEntriesByLevelAndLikeName_shouldSearchAnywhereWithinTheAddressName() {
		AddressHierarchyService ahService = Context.getService(AddressHierarchyService.class);
		AddressHierarchyLevel level = ahService.getAddressHierarchyLevel(5);

		List<AddressHierarchyEntry> result = ahService.getAddressHierarchyEntriesByLevelAndLikeName(level, "mouth", 10);

		assertThat(result.size(), is(equalTo(1)));
		AddressHierarchyEntry plymouth = result.get(0);
		assertThat(plymouth.getLocalizedName(), is(equalTo("Plymouth")));
	}

	@Test
	@Verifies(value = "should search anywhere within the address name", method = "getAddressHierarchyEntriesByLevelAndLikeNameAndParent()")
	public void getAddressHierarchyEntriesByLevelAndLikeNameAndParent_shouldSearchAnywhereWithinTheAddressName() {
		AddressHierarchyService ahService = Context.getService(AddressHierarchyService.class);
		AddressHierarchyEntry parent = ahService.getAddressHierarchyEntryByUuid("52e41146-e162-11df-9195-001e378eb67f");
		AddressHierarchyLevel level = ahService.getAddressHierarchyLevel(5);

		List<AddressHierarchyEntry> result = ahService.getAddressHierarchyEntriesByLevelAndLikeNameAndParent(level, "mouth", parent);

		assertThat(result.size(), is(equalTo(1)));
		AddressHierarchyEntry plymouth = result.get(0);
		assertThat(plymouth.getLocalizedName(), is(equalTo("Plymouth")));
	}

}