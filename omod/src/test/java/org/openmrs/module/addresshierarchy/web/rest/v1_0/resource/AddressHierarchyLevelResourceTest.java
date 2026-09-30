
/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/.
 *
 * OpenMRS is distributed under the terms of the Healthcare Disclaimer located
 * at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.addresshierarchy.web.rest.v1_0.resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.module.addresshierarchy.AddressHierarchyLevel;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.RefRepresentation;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.test.BaseModuleContextSensitiveTest;

public class AddressHierarchyLevelResourceTest extends BaseModuleContextSensitiveTest {

    private static final String XML_DATASET_PACKAGE_PATH =
            "org/openmrs/module/addresshierarchy/include/addressHierarchy-dataset.xml";

    private static final String COUNTRY_UUID =
            "12e41146-e162-11df-9195-001e378eb67f";

    private AddressHierarchyLevelResource resource;

    @Before
    public void setupDatabase() throws Exception {
        initializeInMemoryDatabase();
        authenticate();
        executeDataSet(XML_DATASET_PACKAGE_PATH);

        resource = new AddressHierarchyLevelResource();
    }

    @Test
    public void getByUniqueId_shouldReturnTheRequestedLevel() {
        AddressHierarchyLevel level = resource.getByUniqueId(COUNTRY_UUID);

        assertNotNull(level);
        assertEquals(COUNTRY_UUID, level.getUuid());
        assertEquals(Integer.valueOf(1), level.getId());
        assertEquals("Country", level.getName());
    }

    @Test
    public void getRepresentationDescription_shouldSupportRefRepresentation() {
        DelegatingResourceDescription description =
                resource.getRepresentationDescription(new RefRepresentation());

        assertNotNull(description);
    }

    @Test
    public void getRepresentationDescription_shouldSupportDefaultRepresentation() {
        DelegatingResourceDescription description =
                resource.getRepresentationDescription(new DefaultRepresentation());

        assertNotNull(description);
    }

    @Test
    public void getRepresentationDescription_shouldSupportFullRepresentation() {
        DelegatingResourceDescription description =
                resource.getRepresentationDescription(new FullRepresentation());

        assertNotNull(description);
    }

    @Test
    public void getRepresentationDescription_shouldIncludeRefLevelProperties() {
        DelegatingResourceDescription description =
                resource.getRepresentationDescription(new RefRepresentation());

        assertNotNull(description);
        assertNotNull(description.getProperties());
        assertEquals(true, description.getProperties().containsKey("uuid"));
        assertEquals(true, description.getProperties().containsKey("display"));
        assertEquals(true, description.getProperties().containsKey("name"));
        assertEquals(true, description.getProperties().containsKey("addressField"));
    }

    @Test
    public void getRepresentationDescription_shouldIncludeDefaultLevelProperties() {
        DelegatingResourceDescription description =
                resource.getRepresentationDescription(new DefaultRepresentation());

        assertNotNull(description);
        assertNotNull(description.getProperties());
        assertEquals(true, description.getProperties().containsKey("uuid"));
        assertEquals(true, description.getProperties().containsKey("display"));
        assertEquals(true, description.getProperties().containsKey("name"));
        assertEquals(true, description.getProperties().containsKey("addressField"));
        assertEquals(true, description.getProperties().containsKey("required"));
        assertEquals(true, description.getProperties().containsKey("parent"));
    }

    @Test
    public void getRepresentationDescription_shouldIncludeFullLevelProperties() {
        DelegatingResourceDescription description =
                resource.getRepresentationDescription(new FullRepresentation());

        assertNotNull(description);
        assertNotNull(description.getProperties());
        assertEquals(true, description.getProperties().containsKey("uuid"));
        assertEquals(true, description.getProperties().containsKey("display"));
        assertEquals(true, description.getProperties().containsKey("name"));
        assertEquals(true, description.getProperties().containsKey("addressField"));
        assertEquals(true, description.getProperties().containsKey("required"));
        assertEquals(true, description.getProperties().containsKey("parent"));
    }
}
