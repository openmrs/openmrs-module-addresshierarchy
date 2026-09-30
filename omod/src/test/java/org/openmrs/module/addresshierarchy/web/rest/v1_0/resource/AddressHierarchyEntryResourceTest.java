
/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.addresshierarchy.web.rest.v1_0.resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.module.addresshierarchy.AddressHierarchyEntry;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.RefRepresentation;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.test.BaseModuleContextSensitiveTest;

public class AddressHierarchyEntryResourceTest extends BaseModuleContextSensitiveTest {

    private static final String XML_DATASET_PACKAGE_PATH =
            "org/openmrs/module/addresshierarchy/include/addressHierarchy-dataset.xml";

    private AddressHierarchyEntryResource resource;

    @Before
    public void setupDatabase() throws Exception {
        initializeInMemoryDatabase();
        authenticate();
        executeDataSet(XML_DATASET_PACKAGE_PATH);

        resource = new AddressHierarchyEntryResource();
    }

    @Test
    public void getByUniqueId_shouldReturnTheRequestedEntry() {
        AddressHierarchyEntry entry =
                resource.getByUniqueId("22e41146-e162-11df-9195-001e378eb67f");

        assertNotNull(entry);
        assertEquals(
                "22e41146-e162-11df-9195-001e378eb67f",
                entry.getUuid());
        assertEquals("United States", entry.getName());
        assertEquals(Integer.valueOf(1), entry.getAddressHierarchyEntryId());
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
    public void doSearch_shouldReturnEntriesForLevel() {
        org.springframework.mock.web.MockHttpServletRequest request =
                new org.springframework.mock.web.MockHttpServletRequest();

        org.openmrs.module.webservices.rest.web.RequestContext context =
                new org.openmrs.module.webservices.rest.web.RequestContext();

        context.setRequest(request);
        request.setParameter(
                "level", "15e41146-e162-11df-9195-001e378eb67f");

        PageableResult result = resource.doSearch(context);

        assertNotNull(result);
    }

    @Test
    public void doSearch_shouldReturnChildrenForParent() {
        org.springframework.mock.web.MockHttpServletRequest request =
                new org.springframework.mock.web.MockHttpServletRequest();

        org.openmrs.module.webservices.rest.web.RequestContext context =
                new org.openmrs.module.webservices.rest.web.RequestContext();

        context.setRequest(request);
        request.setParameter(
                "parent", "a4e41146-e162-11df-9195-001e378eb67f");

        PageableResult result = resource.doSearch(context);

        assertNotNull(result);
    }

    @Test
    public void doSearch_shouldSearchEntriesByNameAndLevel() {
        org.springframework.mock.web.MockHttpServletRequest request =
                new org.springframework.mock.web.MockHttpServletRequest();

        org.openmrs.module.webservices.rest.web.RequestContext context =
                new org.openmrs.module.webservices.rest.web.RequestContext();

        context.setRequest(request);
        request.setParameter("q", "Bost");
        request.setParameter(
                "level", "15e41146-e162-11df-9195-001e378eb67f");
        request.setParameter("limit", "20");

        PageableResult result = resource.doSearch(context);

        assertNotNull(result);
    }

    @Test
    public void doSearch_shouldSearchEntriesByNameLevelAndParent() {
        org.springframework.mock.web.MockHttpServletRequest request =
                new org.springframework.mock.web.MockHttpServletRequest();

        org.openmrs.module.webservices.rest.web.RequestContext context =
                new org.openmrs.module.webservices.rest.web.RequestContext();

        context.setRequest(request);
        request.setParameter("q", "Hill");
        request.setParameter(
                "level", "12e41446-e162-11df-9195-001e378eb67f");
        request.setParameter(
                "parent", "a4e41146-e162-11df-9195-001e378eb67f");
        request.setParameter("limit", "20");

        PageableResult result = resource.doSearch(context);

        assertNotNull(result);
    }

    @Test
    public void doSearch_shouldFindEntryByUserGeneratedId() {
        org.springframework.mock.web.MockHttpServletRequest request =
                new org.springframework.mock.web.MockHttpServletRequest();

        org.openmrs.module.webservices.rest.web.RequestContext context =
                new org.openmrs.module.webservices.rest.web.RequestContext();

        context.setRequest(request);
        request.setParameter("userGeneratedId", "BlankRegion");

        PageableResult result = resource.doSearch(context);

        assertNotNull(result);
    }

    @Test
    public void doSearch_shouldReturnTopLevelEntriesWhenNoFiltersProvided() {
        org.springframework.mock.web.MockHttpServletRequest request =
                new org.springframework.mock.web.MockHttpServletRequest();

        org.openmrs.module.webservices.rest.web.RequestContext context =
                new org.openmrs.module.webservices.rest.web.RequestContext();

        context.setRequest(request);

        PageableResult result = resource.doSearch(context);

        assertNotNull(result);
    }

    @Test
    public void getRepresentationDescription_shouldIncludeFullEntryProperties() {
        DelegatingResourceDescription description =
                resource.getRepresentationDescription(new FullRepresentation());

        assertNotNull(description);
        assertNotNull(description.getProperties());
        assertEquals(true, description.getProperties().containsKey("latitude"));
        assertEquals(true, description.getProperties().containsKey("longitude"));
        assertEquals(true, description.getProperties().containsKey("elevation"));
        assertEquals(true, description.getProperties().containsKey("level"));
        assertEquals(true, description.getProperties().containsKey("parent"));
    }

    @Test
    public void getRepresentationDescription_shouldIncludeDefaultEntryProperties() {
        DelegatingResourceDescription description =
                resource.getRepresentationDescription(new DefaultRepresentation());

        assertNotNull(description);
        assertNotNull(description.getProperties());
        assertEquals(true, description.getProperties().containsKey("name"));
        assertEquals(true, description.getProperties().containsKey("userGeneratedId"));
        assertEquals(true, description.getProperties().containsKey("level"));
        assertEquals(true, description.getProperties().containsKey("parent"));
    }
}
