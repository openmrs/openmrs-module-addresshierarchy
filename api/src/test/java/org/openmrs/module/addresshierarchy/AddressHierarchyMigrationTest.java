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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.api.context.Context;
import org.openmrs.module.addresshierarchy.service.AddressHierarchyService;
import org.openmrs.test.jupiter.BaseModuleContextSensitiveTest;
import org.openmrs.test.SkipBaseSetup;
import org.openmrs.test.Verifies;
import org.springframework.test.annotation.DirtiesContext;

import java.util.List;

@DirtiesContext
@SkipBaseSetup
public class AddressHierarchyMigrationTest extends BaseModuleContextSensitiveTest {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	protected static final String XML_DATASET_PACKAGE_PATH = "org/openmrs/module/addresshierarchy/include/addressHierarchy-migration-dataset.xml";
	
	@BeforeEach
	public void setupDatabase() throws Exception {
		initializeInMemoryDatabase();
		authenticate();
		executeDataSet(XML_DATASET_PACKAGE_PATH);
	}
	
	@Test
	@Verifies(value = "should assign properly parent to address hierarchy levels", method = "setAddressHierarchyLevelParents()")
	public void setAddressHierarchyLevelParents_shouldSetAddressHierarchyLevelParents() throws Exception {
		
		AddressHierarchyService ahService = Context.getService(AddressHierarchyService.class);
		
		ahService.setAddressHierarchyLevelParents();
		
		List<AddressHierarchyLevel> levels = ahService.getAddressHierarchyLevels();
		
		// (note that street should have been removed because it has no entries, so there only should be 6 levels) 
		Assertions.assertEquals(6, levels.size());
		
		// make sure that the list returned contains all the level
		Assertions.assertEquals(null, ahService.getAddressHierarchyLevel(1).getParent());
		Assertions.assertEquals(4, Integer.valueOf(ahService.getAddressHierarchyLevel(2).getParent().getId()).intValue());
		Assertions.assertEquals(5, Integer.valueOf(ahService.getAddressHierarchyLevel(3).getParent().getId()).intValue());
		Assertions.assertEquals(7, Integer.valueOf(ahService.getAddressHierarchyLevel(4).getParent().getId()).intValue());
		Assertions.assertEquals(2, Integer.valueOf(ahService.getAddressHierarchyLevel(5).getParent().getId()).intValue());
		Assertions.assertEquals(1, Integer.valueOf(ahService.getAddressHierarchyLevel(7).getParent().getId()).intValue());
		
	}
}
