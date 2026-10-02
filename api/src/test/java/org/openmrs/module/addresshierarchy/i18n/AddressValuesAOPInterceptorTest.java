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
public class AddressValuesAOPInterceptorTest extends I18nModuleContextSensitiveTest {
	
	private Patient patient;
	
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
	@Verifies(value = "should return the l10n address coming from an i18n address", method = "invoke(MethodInvocation invocation)")
	public void invoke_shouldReturnL10nPersonAddress() {
		
		//
		// Replaying a hierarchical choice of entries from country to neighborhood cell
		//
		AddressHierarchyService ahs = Context.getService(AddressHierarchyService.class);
		
		PersonAddress address = new PersonAddress();
		address.setCountry("addresshierarchy.unitedStates");
		
		Set<String> states = new HashSet<String>(ahs.getPossibleAddressValues(address, "stateProvince"));
		Assertions.assertTrue(states.contains("Massachusetts"));
		address.setStateProvince("addresshierarchy.massachusetts");
		
		Set<String> counties = new HashSet<String>(ahs.getPossibleAddressValues(address, "countyDistrict"));
		Assertions.assertTrue(counties.contains("Suffolk County"));
		address.setCountyDistrict("addresshierarchy.suffolkCounty");
		
		Set<String> cities = new HashSet<String>(ahs.getPossibleAddressValues(address, "cityVillage"));
		Assertions.assertTrue(cities.contains("Boston"));
		address.setCityVillage("addresshierarchy.boston");
		
		Set<String> neighborhoodCells = new HashSet<String>(ahs.getPossibleAddressValues(address, "address3"));
		Assertions.assertTrue(neighborhoodCells.contains("Jamaica Plain"));
		address.setAddress3("addresshierarchy.jamaicaPlain");
		
		patient.addAddress(address);
		patient = Context.getPatientService().savePatient(patient);
		
		//
		// The l10n values keys must be returned
		//
		Assertions.assertNotNull(patient.getId());
		Assertions.assertEquals(1, patient.getAddresses().size());
		PersonAddress actualAddress = patient.getPersonAddress();
		
		Assertions.assertTrue(address.equalsContent(actualAddress));
		MatcherAssert.assertThat(actualAddress.getCountry(), equalTo("United States"));
		MatcherAssert.assertThat(actualAddress.getStateProvince(), equalTo("Massachusetts"));
		MatcherAssert.assertThat(actualAddress.getCountyDistrict(), equalTo("Suffolk County"));
		MatcherAssert.assertThat(actualAddress.getCityVillage(), equalTo("Boston"));
		MatcherAssert.assertThat(actualAddress.getAddress3(), equalTo("Jamaica Plain"));
		
		//
		// Now updating the address
		//
		actualAddress.setVoided(true);
		PersonAddress updatedAddress = new PersonAddress();
		updatedAddress.setCountry("addresshierarchy.unitedStates");
		updatedAddress.setStateProvince("addresshierarchy.massachusetts");
		updatedAddress.setCountyDistrict("addresshierarchy.suffolkCounty");
		updatedAddress.setCityVillage("addresshierarchy.boston");
		Assertions.assertTrue(neighborhoodCells.contains("Beacon Hill"));
		updatedAddress.setAddress3("addresshierarchy.beaconHill");
		
		patient.addAddress(updatedAddress);
		patient = Context.getPatientService().savePatient(patient);
		
		//
		// The l10n values keys must be returned
		//
		Assertions.assertEquals(2, patient.getAddresses().size());
		actualAddress = patient.getPersonAddress(); // will return the latest saved address
		Assertions.assertTrue(updatedAddress.equalsContent(actualAddress));
		MatcherAssert.assertThat(actualAddress.getCountry(), equalTo("United States"));
		MatcherAssert.assertThat(actualAddress.getStateProvince(), equalTo("Massachusetts"));
		MatcherAssert.assertThat(actualAddress.getCountyDistrict(), equalTo("Suffolk County"));
		MatcherAssert.assertThat(actualAddress.getCityVillage(), equalTo("Boston"));
		MatcherAssert.assertThat(actualAddress.getAddress3(), equalTo("Beacon Hill"));
	}
	
	@Test
	@Verifies(value = "should not touch an address when outside the address hierarchy", method = "invoke(MethodInvocation invocation)")
	public void invoke_shouldLeaveAddressesUntouchedOutsideAddressHierarchy() throws ParseException {
		
		// Setup
		String enabled = Context.getAdministrationService().getGlobalProperty(ExtI18nConstants.GLOBAL_PROP_REV_I18N_SUPPORT);
		Context.getAdministrationService()
		        .saveGlobalProperty(new GlobalProperty(ExtI18nConstants.GLOBAL_PROP_REV_I18N_SUPPORT, "false"));
		
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
		Assertions.assertTrue(address.equalsContent(actualAddress));
		MatcherAssert.assertThat(actualAddress.getCountry(), equalTo("United States"));
		MatcherAssert.assertThat(actualAddress.getStateProvince(), equalTo("addresshierarchy.massachusetts"));
		
		// Tear down
		Context.getAdministrationService()
		        .saveGlobalProperty(new GlobalProperty(ExtI18nConstants.GLOBAL_PROP_REV_I18N_SUPPORT, enabled));
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
		Assertions.assertNotNull(location.getId());
		MatcherAssert.assertThat(location.getCountry(), equalTo("United States"));
		MatcherAssert.assertThat(location.getStateProvince(), equalTo("Massachusetts"));
		MatcherAssert.assertThat(location.getCountyDistrict(), equalTo("Suffolk County"));
		MatcherAssert.assertThat(location.getCityVillage(), equalTo("Boston"));
		MatcherAssert.assertThat(location.getAddress3(), equalTo("Beacon Hill"));
		
		//
		// Now updating the location
		//
		location = ls.getLocation(location.getId());
		location.setAddress3("Jamaica Plain");
		location = ls.saveLocation(location);
		
		//
		// The updated value should be returned as l10n
		//
		MatcherAssert.assertThat(location.getAddress3(), equalTo("Jamaica Plain"));
	}
}
