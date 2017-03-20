package org.openmrs.module.addresshierarchy.i18n;

import static org.hamcrest.CoreMatchers.equalTo;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

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
import org.openmrs.module.exti18n.api.TestWithAOP;
import org.openmrs.test.Verifies;

public class AddressValuesHibernateInterceptorTest extends I18nModuleContextSensitiveTest {
	
	private Patient patient;
	
	@Override
	protected void setInterceptorAndServices(TestWithAOP testCase) {
		// not adding any AOP here
	}
	
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
	@Verifies(value = "should save the i18n address coming from an address in a specific locale", method = "onSave(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types)")
	public void onSaveAndOnFlushDirty_shouldSaveI18nPersonAddress() {
		
		//
		// Replaying a hierarchical choice of entries from country to neighborhood cell
		//
		AddressHierarchyService ahs = Context.getService(AddressHierarchyService.class);
		
		PersonAddress address = new PersonAddress();
		address.setCountry("United States");
		
		Set<String> states = new HashSet<String>(ahs.getPossibleAddressValues(address, "stateProvince"));
		Assert.assertTrue(states.contains("Massachusetts"));
		address.setStateProvince("Massachusetts");
		
		Set<String> counties = new HashSet<String>(ahs.getPossibleAddressValues(address, "countyDistrict"));
		Assert.assertTrue(counties.contains("Suffolk County"));
		address.setCountyDistrict("Suffolk County");
		
		Set<String> cities = new HashSet<String>(ahs.getPossibleAddressValues(address, "cityVillage"));
		Assert.assertTrue(cities.contains("Boston"));
		address.setCityVillage("Boston");
		
		Set<String> neighborhoodCells = new HashSet<String>(ahs.getPossibleAddressValues(address, "address3"));
		Assert.assertTrue(neighborhoodCells.contains("Jamaica Plain"));
		address.setAddress3("Jamaica Plain");
		
		patient.addAddress(address);
		patient = Context.getPatientService().savePatient(patient);
		
		//
		// The i18n codes must be in database
		//
		Assert.assertNotNull(patient.getId());
		Assert.assertEquals(1, patient.getAddresses().size());
		PersonAddress actualAddress = patient.getPersonAddress();
		
		Assert.assertTrue(address.equalsContent(actualAddress));
		Assert.assertThat(actualAddress.getCountry(), equalTo("addresshierarchy.unitedStates"));
		Assert.assertThat(actualAddress.getStateProvince(), equalTo("addresshierarchy.massachusetts"));
		Assert.assertThat(actualAddress.getCountyDistrict(), equalTo("addresshierarchy.suffolkCounty"));
		Assert.assertThat(actualAddress.getCityVillage(), equalTo("addresshierarchy.boston"));
		Assert.assertThat(actualAddress.getAddress3(), equalTo("addresshierarchy.jamaicaPlain"));
		
		//
		// Now updating the address
		//
		actualAddress.setVoided(true);
		PersonAddress updatedAddress = new PersonAddress();
		updatedAddress.setCountry("United States");
		updatedAddress.setStateProvince("Massachusetts");
		updatedAddress.setCountyDistrict("Suffolk County");
		updatedAddress.setCityVillage("Boston");
		Assert.assertTrue(neighborhoodCells.contains("Beacon Hill"));
		updatedAddress.setAddress3("Beacon Hill");
		
		patient.addAddress(updatedAddress);
		patient = Context.getPatientService().savePatient(patient);
		
		//
		// The updated i18n codes must be in database
		//
		Assert.assertEquals(2, patient.getAddresses().size());
		actualAddress = patient.getPersonAddress(); // will return the latest saved address
		Assert.assertTrue(updatedAddress.equalsContent(actualAddress));
		Assert.assertThat(actualAddress.getCountry(), equalTo("addresshierarchy.unitedStates"));
		Assert.assertThat(actualAddress.getStateProvince(), equalTo("addresshierarchy.massachusetts"));
		Assert.assertThat(actualAddress.getCountyDistrict(), equalTo("addresshierarchy.suffolkCounty"));
		Assert.assertThat(actualAddress.getCityVillage(), equalTo("addresshierarchy.boston"));
		Assert.assertThat(actualAddress.getAddress3(), equalTo("addresshierarchy.beaconHill"));
	}
	
	@Test
	@Verifies(value = "should not touch an address when outside the address hierarchy", method = "onFlushDirty(Object entity, Serializable id, Object[] currentState, Object[] previousState, String[] propertyNames, Type[] types)")
	public void onFlushDirty_shouldLeaveAddressesUntouchedOutsideAddressHierarchy() throws ParseException {
		
		// Setup
		String enabled = Context.getAdministrationService().getGlobalProperty(ExtI18nConstants.GLOBAL_PROP_REV_I18N_SUPPORT);
		Context.getAdministrationService().saveGlobalProperty(new GlobalProperty(ExtI18nConstants.GLOBAL_PROP_REV_I18N_SUPPORT, "false"));
		
		//
		// Setting an i18n address,, at least partially
		//
		PersonAddress address = new PersonAddress();
		address.setCountry("United States");
		address.setStateProvince("Connecticut");
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
		Assert.assertThat(actualAddress.getStateProvince(), equalTo("Connecticut"));
		
		// Tear down
		Context.getAdministrationService().saveGlobalProperty(new GlobalProperty(ExtI18nConstants.GLOBAL_PROP_REV_I18N_SUPPORT, enabled));
	}
	
	@Test
	@Verifies(value = "should save the i18n location coming from a location set in a specific locale", method = "onSave(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types)")
	public void onSaveAndOnFlushDirty_shouldSaveI18nLocation() {
		
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
		location.setCountry("United States");
		location.setStateProvince("Massachusetts");
		location.setCountyDistrict("Suffolk County");
		location.setCityVillage("Boston");
		location.setAddress3("Beacon Hill");
		location = ls.saveLocation(location);
		
		//
		// The i18n messages keys must be in database
		//
		Assert.assertNotNull(location.getId());
		Assert.assertThat(location.getCountry(), equalTo("addresshierarchy.unitedStates"));
		Assert.assertThat(location.getCountyDistrict(), equalTo("addresshierarchy.suffolkCounty"));
		Assert.assertThat(location.getCityVillage(), equalTo("addresshierarchy.boston"));
		Assert.assertThat(location.getAddress3(), equalTo("addresshierarchy.beaconHill"));
		
		//
		// Now updating the location
		//
		location = ls.getLocation(location.getId());
		location.setAddress3("Jamaica Plain");
		location = ls.saveLocation(location); // TODO This doesn't trigger AddressValuesInterceptor!?
		
		//
		// The updated i18n message keys must be in database
		//
		//		Assert.assertThat(location.getAddress3(), equalTo("addresshierarchy.jamaicaPlain"));
	}
}
