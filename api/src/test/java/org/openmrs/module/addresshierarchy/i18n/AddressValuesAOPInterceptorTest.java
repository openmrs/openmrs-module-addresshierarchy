package org.openmrs.module.addresshierarchy.i18n;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.GlobalProperty;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PersonAddress;
import org.openmrs.PersonName;
import org.openmrs.api.LocationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.addresshierarchy.service.AddressHierarchyService;
import org.openmrs.module.exti18n.ExtI18nConstants;
import org.openmrs.test.Verifies;
import org.springframework.test.annotation.DirtiesContext;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static org.hamcrest.CoreMatchers.equalTo;

@DirtiesContext
public class AddressValuesAOPInterceptorTest extends I18nModuleContextSensitiveTest {
	
	private Patient patient;

	@Before
	public void setup() throws Exception {
		
		patient = new Patient();
		patient.setGender("M");
		patient.addName(new PersonName("John", "", "Doe"));
		
		List<PatientIdentifierType> patientIdTypes = Context.getPatientService().getAllPatientIdentifierTypes();
		Assert.assertNotNull(patientIdTypes);
		PatientIdentifier patientIdentifier = new PatientIdentifier();
		patientIdentifier.setIdentifier("123-0");
		patientIdentifier.setIdentifierType(patientIdTypes.get(0));
		patientIdentifier.setLocation(new Location(1));
		patientIdentifier.setPreferred(true);
		
		Set<PatientIdentifier> patientIdentifiers = new LinkedHashSet<PatientIdentifier>();
		patientIdentifiers.add(patientIdentifier);
		patient.setIdentifiers(patientIdentifiers);
	}
	
	@Test
	@Verifies(value = "should return the l10n address coming from an i18n address", method = "invoke(MethodInvocation invocation)")
	public void invoke_shouldReturnL10nPersonAddress() {
		
		//
		// Replaying a hierarchical choice of entries from country to neighborhood cell
		//
		AddressHierarchyService ahs = Context.getService(AddressHierarchyService.class);
		
		PersonAddress address = new PersonAddress();
		address.setCountry("addresshierarchy.unitedStates");
		
		Set<String> states = new HashSet<String>(ahs.getPossibleAddressValues(address, "stateProvince"));
		Assert.assertTrue(states.contains("Massachusetts"));
		address.setStateProvince("addresshierarchy.massachusetts");
		
		Set<String> counties = new HashSet<String>(ahs.getPossibleAddressValues(address, "countyDistrict"));
		Assert.assertTrue(counties.contains("Suffolk County"));
		address.setCountyDistrict("addresshierarchy.suffolkCounty");
		
		Set<String> cities = new HashSet<String>(ahs.getPossibleAddressValues(address, "cityVillage"));
		Assert.assertTrue(cities.contains("Boston"));
		address.setCityVillage("addresshierarchy.boston");
		
		Set<String> neighborhoodCells = new HashSet<String>(ahs.getPossibleAddressValues(address, "address3"));
		Assert.assertTrue(neighborhoodCells.contains("Jamaica Plain"));
		address.setAddress3("addresshierarchy.jamaicaPlain");
		
		patient.addAddress(address);
		patient = Context.getPatientService().savePatient(patient);
		
		//
		// The l10n values keys must be returned
		//
		Assert.assertNotNull(patient.getId());
		Assert.assertEquals(1, patient.getAddresses().size());
		PersonAddress actualAddress = patient.getPersonAddress();
		
		Assert.assertTrue(address.equalsContent(actualAddress));
		Assert.assertThat(actualAddress.getCountry(), equalTo("United States"));
		Assert.assertThat(actualAddress.getStateProvince(), equalTo("Massachusetts"));
		Assert.assertThat(actualAddress.getCountyDistrict(), equalTo("Suffolk County"));
		Assert.assertThat(actualAddress.getCityVillage(), equalTo("Boston"));
		Assert.assertThat(actualAddress.getAddress3(), equalTo("Jamaica Plain"));
		
		//
		// Now updating the address
		//
		actualAddress.setVoided(true);
		PersonAddress updatedAddress = new PersonAddress();
		updatedAddress.setCountry("addresshierarchy.unitedStates");
		updatedAddress.setStateProvince("addresshierarchy.massachusetts");
		updatedAddress.setCountyDistrict("addresshierarchy.suffolkCounty");
		updatedAddress.setCityVillage("addresshierarchy.boston");
		Assert.assertTrue(neighborhoodCells.contains("Beacon Hill"));
		updatedAddress.setAddress3("addresshierarchy.beaconHill");
		
		patient.addAddress(updatedAddress);
		patient = Context.getPatientService().savePatient(patient);
		
		//
		// The l10n values keys must be returned
		//
		Assert.assertEquals(2, patient.getAddresses().size());
		actualAddress = patient.getPersonAddress(); // will return the latest saved address
		Assert.assertTrue(updatedAddress.equalsContent(actualAddress));
		Assert.assertThat(actualAddress.getCountry(), equalTo("United States"));
		Assert.assertThat(actualAddress.getStateProvince(), equalTo("Massachusetts"));
		Assert.assertThat(actualAddress.getCountyDistrict(), equalTo("Suffolk County"));
		Assert.assertThat(actualAddress.getCityVillage(), equalTo("Boston"));
		Assert.assertThat(actualAddress.getAddress3(), equalTo("Beacon Hill"));
	}
	
	@Test
	@Verifies(value = "should not touch an address when outside the address hierarchy", method = "invoke(MethodInvocation invocation)")
	public void invoke_shouldLeaveAddressesUntouchedOutsideAddressHierarchy() throws ParseException {
		
		// Setup
		String enabled = Context.getAdministrationService().getGlobalProperty(ExtI18nConstants.GLOBAL_PROP_REV_I18N_SUPPORT);
		Context.getAdministrationService().saveGlobalProperty(new GlobalProperty(ExtI18nConstants.GLOBAL_PROP_REV_I18N_SUPPORT, "false"));
		
		//
		// Setting translatable address, at least partially
		//
		PersonAddress address = new PersonAddress();
		address.setCountry("United States");
		address.setStateProvince("addresshierarchy.massachusetts");
		patient.addAddress(address);
		address = (PersonAddress) address.clone();
		patient = Context.getPatientService().savePatient(patient);
		
		//
		// Updating something unrelated
		//
		patient.setBirthdate((new SimpleDateFormat("dd-MM-yyyy")).parse("11-11-2012"));
		patient = Context.getPatientService().savePatient(patient);
		
		//
		// The address should still be as it was originally
		//
		PersonAddress actualAddress = patient.getPersonAddress();
		Assert.assertTrue(address.equalsContent(actualAddress));
		Assert.assertThat(actualAddress.getCountry(), equalTo("United States"));
		Assert.assertThat(actualAddress.getStateProvince(), equalTo("addresshierarchy.massachusetts"));
		
		// Tear down
		Context.getAdministrationService().saveGlobalProperty(new GlobalProperty(ExtI18nConstants.GLOBAL_PROP_REV_I18N_SUPPORT, enabled));
	}
	
	@Test
	@Verifies(value = "should return the l10n location coming from a location set in a specific locale", method = "invoke(MethodInvocation invocation)")
	public void invoke_shouldReturnL10nLocation() {
		
		//
		// Setup, assuming that the i18n cache is filled up.
		//
		Context.setLocale(Locale.ENGLISH);
		LocationService ls = Context.getLocationService();
		
		//
		// Creating a localized location
		//
		Location location = new Location();
		location.setName("My Location");
		location.setCountry("addresshierarchy.unitedStates");
		location.setStateProvince("addresshierarchy.massachusetts");
		location.setCountyDistrict("addresshierarchy.suffolkCounty");
		location.setCityVillage("addresshierarchy.boston");
		location.setAddress3("addresshierarchy.beaconHill");
		location = ls.saveLocation(location);
		
		//
		// The l10n values keys must be returned
		//
		Assert.assertNotNull(location.getId());
		Assert.assertThat(location.getCountry(), equalTo("United States"));
		Assert.assertThat(location.getStateProvince(), equalTo("Massachusetts"));
		Assert.assertThat(location.getCountyDistrict(), equalTo("Suffolk County"));
		Assert.assertThat(location.getCityVillage(), equalTo("Boston"));
		Assert.assertThat(location.getAddress3(), equalTo("Beacon Hill"));
		
		//
		// Now updating the location
		//
		location = ls.getLocation(location.getId());
		location.setAddress3("Jamaica Plain");
		location = ls.saveLocation(location);
		
		//
		// The updated value should be returned as l10n
		//
		Assert.assertThat(location.getAddress3(), equalTo("Jamaica Plain"));
	}
}
