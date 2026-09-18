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

import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.api.context.Context;
import org.openmrs.module.addresshierarchy.service.AddressHierarchyService;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.openmrs.test.SkipBaseSetup;
import org.openmrs.test.Verifies;
import org.springframework.test.annotation.DirtiesContext;

import junit.framework.Assert;

/**
 * Exercises the address hierarchy service against a database where no address hierarchy has been
 * configured at all - no levels and no entries - as on a fresh install of the module.
 * <p>
 * This deliberately does <em>not</em> load addressHierarchy-dataset.xml. The hierarchy cannot simply
 * be wiped at the start of a test instead. The module's own wipe path,
 * {@link org.openmrs.module.addresshierarchy.config.AddressConfigurationLoader#wipeAddressHierarchy()},
 * calls deleteAllAddressHierarchyEntries() and then deletes the levels from the bottom up; it never
 * clears address_hierarchy_address_to_entry_map, and there is no service method that clears those
 * mappings other than per-PersonAddress. deleteAllAddressHierarchyEntries() itself deletes only the
 * top-level entries and relies on a delete cascade that, as HibernateAddressHierarchyDAO's own
 * comment records, exists only at the database level in MySQL - which is also why the unit test for
 * it is commented out. Under the in-memory H2 database nothing cascades, so the surviving
 * address-to-entry mappings still reference the entries being deleted and the flush fails on a
 * referential integrity constraint. Starting from a database that never had a hierarchy in it is
 * both simpler and a truer reproduction of the reported bug.
 */
@DirtiesContext
@SkipBaseSetup
public class AddressHierarchyServiceUnconfiguredTest extends BaseModuleContextSensitiveTest {

	protected final Log log = LogFactory.getLog(getClass());

	@Before
	public void setupDatabase() throws Exception {
		initializeInMemoryDatabase();
		authenticate();
		executeDataSet(INITIAL_XML_DATASET_PACKAGE_PATH);
		executeDataSet(EXAMPLE_XML_DATASET_PACKAGE_PATH);

		// this global property normally arrives with addressHierarchy-dataset.xml, which is not loaded here;
		// initializeFullAddressCache() reads it unconditionally, so it has to exist
		Context.getAdministrationService().setGlobalProperty(
		    AddressHierarchyConstants.GLOBAL_PROP_INITIALIZE_ADDRESS_HIERARCHY_CACHE_ON_STARTUP, "true");
	}

	@Test
	@Verifies(value = "should return an empty result, not throw, when no address hierarchy is configured", method = "searchAddresses(String,AddressHierarchyLevel)")
	public void searchAddresses_shouldReturnEmptyResultIfNoLevelsOrEntriesConfigured() throws Exception {

		AddressHierarchyService ahService = Context.getService(AddressHierarchyService.class);

		// preconditions: nothing is configured, so there is no top level to start a cache build from
		Assert.assertEquals(Integer.valueOf(0), ahService.getAddressHierarchyLevelsCount());
		Assert.assertEquals(Integer.valueOf(0), ahService.getAddressHierarchyEntryCount());
		Assert.assertNull(ahService.getTopAddressHierarchyLevel());

		ahService.resetFullAddressCache();

		// building the full address cache used to throw a NullPointerException on the very first search,
		// because getAddressHierarchyEntriesByLevel(null) returned null; worse, the (empty) cache for the
		// locale had already been published by then, so every later search was served that empty cache as a
		// successful result for the life of the JVM
		Set<String> firstSearch = ahService.searchAddresses("boston", null);
		Assert.assertNotNull(firstSearch);
		Assert.assertTrue(firstSearch.isEmpty());

		Set<String> secondSearch = ahService.searchAddresses("boston", null);
		Assert.assertNotNull(secondSearch);
		Assert.assertTrue(secondSearch.isEmpty());
	}
}
