/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.addresshierarchy.i18n;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
import org.openmrs.test.SkipBaseSetup;
import org.openmrs.test.Verifies;
import org.springframework.test.annotation.DirtiesContext;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

import static org.hamcrest.CoreMatchers.equalTo;

@DirtiesContext
@SkipBaseSetup
public class AddressValuesHibernateInterceptorTest extends I18nModuleContextSensitiveTest {
	
	private Patient patient;
	
	@Override
	protected void setInterceptorAndServices(TestWithAOP testCase) {
		// not adding any AOP here
	}
	
	@BeforeEach
	public void setup() throws Exception {
		
		patient = new Patient();
		patient.setGender("M");
		patient.addName(new PersonName("John", "", "Doe"));
		
		List<PatientIdentifierType> patientIdTypes = Context.getPatientService().getAllPatientIdentifierTypes();
		Assertions.assertNotNull(patientIdTypes);
		PatientIdentifier patientIdentifier = new PatientIdentifier();
		patientIdentifier.setIdentifier("123-0");
		patientIdentifier.setIdentifierType(patientIdTypes.get(0));
		patientIdentifier.setLocation(new Location(1));
		patientIdentifier.setPreferred(true);
		
		Set<PatientIdentifier> patientIdentifiers = new TreeSet<>();
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
		Assertions.assertTrue(states.contains("Massachusetts"));
		address.setStateProvince("Massachusetts");
		
		Set<String> counties = new HashSet<String>(ahs.getPossibleAddressValues(address, "countyDistrict"));
		Assertions.assertTrue(counties.contains("Suffolk County"));
		address.setCountyDistrict("Suffolk County");
		
		Set<String> cities = new HashSet<String>(ahs.getPossibleAddressValues(address, "cityVillage"));
		Assertions.assertTrue(cities.contains("Boston"));
		address.setCityVillage("Boston");
		
		Set<String> neighborhoodCells = new HashSet<String>(ahs.getPossibleAddressValues(address, "address3"));
		Assertions.assertTrue(neighborhoodCells.contains("Jamaica Plain"));
		address.setAddress3("Jamaica Plain");
		
		patient.addAddress(address);
		patient = Context.getPatientService().savePatient(patient);
		
		//
		// The i18n codes must be in database
		//
		Assertions.assertNotNull(patient.getId());
		Assertions.assertEquals(1, patient.getAddresses().size());
		PersonAddress actualAddress = patient.getPersonAddress();
		
		Assertions.assertTrue(address.equalsContent(actualAddress));
		MatcherAssert.assertThat(actualAddress.getCountry(), equalTo("addresshierarchy.unitedStates"));
		MatcherAssert.assertThat(actualAddress.getStateProvince(), equalTo("addresshierarchy.massachusetts"));
		MatcherAssert.assertThat(actualAddress.getCountyDistrict(), equalTo("addresshierarchy.suffolkCounty"));
		MatcherAssert.assertThat(actualAddress.getCityVillage(), equalTo("addresshierarchy.boston"));
		MatcherAssert.assertThat(actualAddress.getAddress3(), equalTo("addresshierarchy.jamaicaPlain"));
		
		//
		// Now updating the address
		//
		actualAddress.setVoided(true);
		PersonAddress updatedAddress = new PersonAddress();
		updatedAddress.setCountry("United States");
		updatedAddress.setStateProvince("Massachusetts");
		updatedAddress.setCountyDistrict("Suffolk County");
		updatedAddress.setCityVillage("Boston");
		Assertions.assertTrue(neighborhoodCells.contains("Beacon Hill"));
		updatedAddress.setAddress3("Beacon Hill");
		
		patient.addAddress(updatedAddress);
		patient = Context.getPatientService().savePatient(patient);
		
		//
		// The updated i18n codes must be in database
		//
		Assertions.assertEquals(2, patient.getAddresses().size());
		actualAddress = patient.getPersonAddress(); // will return the latest saved address
		Assertions.assertTrue(updatedAddress.equalsContent(actualAddress));
		MatcherAssert.assertThat(actualAddress.getCountry(), equalTo("addresshierarchy.unitedStates"));
		MatcherAssert.assertThat(actualAddress.getStateProvince(), equalTo("addresshierarchy.massachusetts"));
		MatcherAssert.assertThat(actualAddress.getCountyDistrict(), equalTo("addresshierarchy.suffolkCounty"));
		MatcherAssert.assertThat(actualAddress.getCityVillage(), equalTo("addresshierarchy.boston"));
		MatcherAssert.assertThat(actualAddress.getAddress3(), equalTo("addresshierarchy.beaconHill"));
	}
	
	@Test
	@Verifies(value = "should not touch an address when outside the address hierarchy", method = "onFlushDirty(Object entity, Serializable id, Object[] currentState, Object[] previousState, String[] propertyNames, Type[] types)")
	public void onFlushDirty_shouldLeaveAddressesUntouchedOutsideAddressHierarchy() throws ParseException {
		
		// Setup
		String enabled = Context.getAdministrationService().getGlobalProperty(ExtI18nConstants.GLOBAL_PROP_REV_I18N_SUPPORT);
		Context.getAdministrationService()
		        .saveGlobalProperty(new GlobalProperty(ExtI18nConstants.GLOBAL_PROP_REV_I18N_SUPPORT, "false"));
		
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
		Assertions.assertTrue(address.equalsContent(actualAddress));
		MatcherAssert.assertThat(actualAddress.getCountry(), equalTo("United States"));
		MatcherAssert.assertThat(actualAddress.getStateProvince(), equalTo("Connecticut"));
		
		// Tear down
		Context.getAdministrationService()
		        .saveGlobalProperty(new GlobalProperty(ExtI18nConstants.GLOBAL_PROP_REV_I18N_SUPPORT, enabled));
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
		Assertions.assertNotNull(location.getId());
		MatcherAssert.assertThat(location.getCountry(), equalTo("addresshierarchy.unitedStates"));
		MatcherAssert.assertThat(location.getCountyDistrict(), equalTo("addresshierarchy.suffolkCounty"));
		MatcherAssert.assertThat(location.getCityVillage(), equalTo("addresshierarchy.boston"));
		MatcherAssert.assertThat(location.getAddress3(), equalTo("addresshierarchy.beaconHill"));
		
		//
		// Now updating the location
		//
		location = ls.getLocation(location.getId());
		location.setAddress3("Jamaica Plain");
		location = ls.saveLocation(location); // TODO This doesn't trigger AddressValuesInterceptor!?
		
		//
		// The updated i18n message keys must be in database
		//
		//		MatcherAssert.assertThat(location.getAddress3(), equalTo("addresshierarchy.jamaicaPlain"));
	}
}
