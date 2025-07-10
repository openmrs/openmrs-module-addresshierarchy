package org.openmrs.module.addresshierarchy.web.controller.ajax;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.test.jupiter.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.ModelMap;

import java.util.ArrayList;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;


public class AddressHierarchyAjaxControllerTest extends BaseModuleContextSensitiveTest {

    protected static final String XML_DATASET_PACKAGE_PATH = "org/openmrs/module/addresshierarchy/include/addressHierarchy-dataset.xml";

    @Autowired
    private AddressHierarchyAjaxController controller;

    @BeforeEach
    public void setupDatabase() throws Exception {
        initializeInMemoryDatabase();
        authenticate();
        executeDataSet(XML_DATASET_PACKAGE_PATH);
    }

    @Test
    public void getPossibleAddressHierarchyEntries_shouldReturnEmptyMapWhenAddressFieldNameIsWrong() throws Exception {
        ArrayList<ModelMap> result = controller.getPossibleAddressHierarchyEntriesWithParents("unions ", "incorrect-address-field", null, null, 10);
        assertThat(result.size(), is(equalTo(0)));
    }

    @Test
    public void getPossibleAddressHierarchyEntries_shouldReturnEmptyLimitForWrongLimit() throws Exception {
        ArrayList<ModelMap> result = controller.getPossibleAddressHierarchyEntriesWithParents("unions ", "cityVillage", null, null, -1);
        assertThat(result.size(), is(equalTo(0)));
    }

    @Test
    public void getPossibleAddressHierarchyEntries_shouldHonourLimit() throws Exception {
        ArrayList<ModelMap> result = controller.getPossibleAddressHierarchyEntriesWithParents("unions ", "cityVillage", null, null, 1);
        assertThat(result.size(), is(equalTo(1)));

        result = controller.getPossibleAddressHierarchyEntriesWithParents("Unions ", "cityVillage", "66e41146-e162-11df-9195-001e378eb67f", null, 2);
        assertThat(result.size(), is(equalTo(1)));

        result = controller.getPossibleAddressHierarchyEntriesWithParents("non-existent-address", "cityVillage", "66e41146-e162-11df-9195-001e378eb67f", null, 1);
        assertThat(result.size(), is(equalTo(0)));
    }

    @Test
    public void getPossibleAddressHierarchyEntries_shouldRetrieveAddressHierarchyEntriesWhenParentNotSpecified() throws Exception {
        ArrayList<ModelMap> result = controller.getPossibleAddressHierarchyEntriesWithParents("Unions ", "cityVillage", null, null, 10);
        assertThat(result.size(), is(equalTo(2)));
        assertThat(result, hasItem(modelMapWithValue("name", "Unions Of Kaliganj Upazila")));
        assertThat(result, hasItem(modelMapWithValue("uuid", "88e41146-e162-11df-9195-001e378eb67f")));
        assertThat(result, hasItem(modelMapWithValue("uuid", "dde41146-e162-11df-9195-001e378eb67f")));
    }

    @Test
    public void getPossibleAddressHierarchyEntries_shouldRetrieveAddressHierarchyEntriesWhenParentSpecified() throws Exception {
        ArrayList<ModelMap> result = controller.getPossibleAddressHierarchyEntriesWithParents("Unions ", "cityVillage", "66e41146-e162-11df-9195-001e378eb67f", null, 10);
        assertThat(result.size(), is(equalTo(1)));
        assertThat(result, hasItem(modelMapWithValue("name", "Unions Of Kaliganj Upazila")));
        assertThat(result, hasItem(modelMapWithValue("uuid", "88e41146-e162-11df-9195-001e378eb67f")));
    }

    @Test
    public void getPossibleAddressHierarchyEntries_shouldReturnAllEntriesWhenParentUuidIsWrong() throws Exception {
        ArrayList<ModelMap> result = controller.getPossibleAddressHierarchyEntriesWithParents("unions ", "cityVillage", "incorrect-parent-uuid", null, 10);
        assertThat(result.size(), is(equalTo(2)));
        assertThat(result, hasItem(modelMapWithValue("name", "Unions Of Kaliganj Upazila")));
    }

    public Matcher<ModelMap> modelMapWithValue(final String field, final String name) {
        return new BaseMatcher<ModelMap>() {
            @Override
            public boolean matches(Object o) {
                return name.equals(((ModelMap)o).get(field));
            }

            @Override
            public void describeTo(Description description) {
            }
        };
    }
}