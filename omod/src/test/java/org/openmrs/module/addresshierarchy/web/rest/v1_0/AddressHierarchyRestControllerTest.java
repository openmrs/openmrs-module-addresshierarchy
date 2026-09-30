
/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark of OpenMRS Inc.
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.addresshierarchy.web.rest.v1_0;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.module.addresshierarchy.web.controller.ajax.AddressHierarchyAjaxController;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.response.IllegalRequestException;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceController;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;

public class AddressHierarchyRestControllerTest extends BaseModuleContextSensitiveTest {

        protected static final String XML_DATASET_PACKAGE_PATH =
                "org/openmrs/module/addresshierarchy/include/addressHierarchy-dataset.xml";

        @Autowired
        private AddressHierarchyRestController controller;

        @Autowired
        private AddressHierarchyAjaxController ajaxController;

        @Autowired
        private RequestMappingHandlerMapping requestMappingHandlerMapping;

        @Before
        public void setupDatabase() throws Exception {
              initializeInMemoryDatabase();
              authenticate();
              executeDataSet(XML_DATASET_PACKAGE_PATH);
        }

        @Test
        public void getPossibleValues_shouldReturnResultsEnvelope() throws Exception {
              MockHttpServletRequest request = new MockHttpServletRequest();
              MockHttpServletResponse response = new MockHttpServletResponse();

              request.addParameter("country", "United States");

              SimpleObject result = controller.getPossibleValues(
                        request,
                        response,
                        "stateProvince");

              assertThat(result.containsKey("results"), is(true));

              List<String> results = (List<String>) result.get("results");

              assertThat(results.size(), is(2));
              assertThat(results, hasItem("Massachusetts"));
        }

        @Test
        public void getPossibleValues_shouldAcceptMultipleAddressFields() throws Exception {
              MockHttpServletRequest request = new MockHttpServletRequest();
              MockHttpServletResponse response = new MockHttpServletResponse();

              request.addParameter("country", "United States");
              request.addParameter("stateProvince", "Massachusetts");

              SimpleObject result = controller.getPossibleValues(
                        request,
                        response,
                        "cityVillage");

              assertThat(result.containsKey("results"), is(true));

              List<?> results = (List<?>) result.get("results");

              assertThat(results.isEmpty(), is(false));
        }

        @Test
        public void searchEntryNames_shouldReturnResultsEnvelope() throws Exception {
              MockHttpServletRequest request = new MockHttpServletRequest();
              MockHttpServletResponse response = new MockHttpServletResponse();

              SimpleObject result = controller.searchEntryNames(
                        request,
                        response,
                        "Boston",
                        "cityVillage");

              assertThat(result.containsKey("results"), is(true));

              List<String> results = (List<String>) result.get("results");

              assertThat(results.size(), is(1));
              assertThat(results, hasItem("Boston"));
        }

        @Test
        public void getFullAddresses_shouldReturnAddressesAndComponents() throws Exception {
              MockHttpServletRequest request = new MockHttpServletRequest();
              MockHttpServletResponse response = new MockHttpServletResponse();

              SimpleObject result = controller.getFullAddresses(
                        request,
                        response,
                        null,
                        "a4e41146-e162-11df-9195-001e378eb67f",
                        null,
                        null);

              assertThat(result.containsKey("results"), is(true));

              List<?> results = (List<?>) result.get("results");

              assertThat(results.size(), is(2));

              for (Object value : results) {
                assertThat(value instanceof Map, is(true));

                Map<?, ?> resultMap = (Map<?, ?>) value;

                assertThat(resultMap.containsKey("address"), is(true));
                assertThat(resultMap.containsKey("components"), is(true));
                assertThat(resultMap.get("address") instanceof String, is(true));
                assertThat(resultMap.get("components") instanceof List, is(true));
              }
        }

        @Test
        public void getFullAddresses_shouldSearchByQuery() throws Exception {
              MockHttpServletRequest request = new MockHttpServletRequest();
              MockHttpServletResponse response = new MockHttpServletResponse();

              SimpleObject result = controller.getFullAddresses(
                        request,
                        response,
                        "Boston",
                        null,
                        null,
                        null);

              assertThat(result.containsKey("results"), is(true));

              List<?> results = (List<?>) result.get("results");

              assertThat(results.isEmpty(), is(false));

              for (Object value : results) {
                assertThat(value instanceof Map, is(true));

                Map<?, ?> resultMap = (Map<?, ?>) value;

                assertThat(resultMap.containsKey("address"), is(true));
                assertThat(resultMap.containsKey("components"), is(true));
                assertThat(resultMap.get("address") instanceof String, is(true));
                assertThat(resultMap.get("components") instanceof List, is(true));
              }
        }

        @Test
        public void fullAddress_shouldBeMappedToAddressHierarchyController() {
              Method targetMethod = null;

              for (Method method : AddressHierarchyRestController.class.getDeclaredMethods()) {
                if ("getFullAddresses".equals(method.getName())) {
                        targetMethod = method;
                        break;
                }
              }

              assertThat(targetMethod != null, is(true));

              RequestMapping mapping = targetMethod.getAnnotation(RequestMapping.class);

              assertThat(mapping != null, is(true));
              assertThat(mapping.value().length, is(1));
              assertThat(mapping.value()[0], is("/fulladdress"));

              ResponseBody responseBody =
                        targetMethod.getAnnotation(ResponseBody.class);

              assertThat(responseBody != null, is(true));
        }

        @Test
        public void fullAddress_shouldRouteToAddressHierarchyController() throws Exception {
              MockHttpServletRequest request = new MockHttpServletRequest();
              request.setMethod("GET");
              request.setRequestURI("/rest/v1/addresshierarchy/fulladdress");

              HandlerExecutionChain handler =
                        requestMappingHandlerMapping.getHandler(request);

              assertThat(handler != null, is(true));

              Object handlerObject = handler.getHandler();

              Method handlerMethod =
                        handlerObject instanceof org.springframework.web.method.HandlerMethod
                                ? ((org.springframework.web.method.HandlerMethod) handlerObject).getMethod()
                                : null;

              assertThat(handlerMethod != null, is(true));
              assertThat(handlerMethod.getName(), is("getFullAddresses"));
              assertThat(handlerMethod.getDeclaringClass(),
                        is(AddressHierarchyRestController.class));
              assertThat(handlerMethod.getDeclaringClass(),
                        is(AddressHierarchyRestController.class));

              assertThat(
                        handlerMethod.getDeclaringClass().isAssignableFrom(
                                MainResourceController.class),
                        is(false));
        }

        @Test(expected = IllegalRequestException.class)
        public void getPossibleValues_shouldRejectUnknownAddressField() throws Exception {
              MockHttpServletRequest request = new MockHttpServletRequest();
              MockHttpServletResponse response = new MockHttpServletResponse();

              controller.getPossibleValues(
                        request,
                        response,
                        "notARealAddressField");
        }

		@Test
public void getPossibleValues_shouldReturnBadRequestForUnknownAddressField() throws Exception {
      MockHttpServletRequest request = new MockHttpServletRequest();
      MockHttpServletResponse response = new MockHttpServletResponse();

      try {
            controller.getPossibleValues(
                      request,
                      response,
                      "notARealAddressField");
      } catch (IllegalRequestException e) {
            controller.handleException(e, request, response);
      }

      assertThat(response.getStatus(), is(400));
}

        @Test(expected = IllegalRequestException.class)
        public void searchEntryNames_shouldRejectMissingSearchTerm() throws Exception {
              MockHttpServletRequest request = new MockHttpServletRequest();
              MockHttpServletResponse response = new MockHttpServletResponse();

              controller.searchEntryNames(
                        request,
                        response,
                        "",
                        "cityVillage");
        }

        @Test
        public void getFullAddresses_shouldSupportEntryNameAndAddressField() throws Exception {
              MockHttpServletRequest request = new MockHttpServletRequest();
              MockHttpServletResponse response = new MockHttpServletResponse();

              SimpleObject result = controller.getFullAddresses(
                        request,
                        response,
                        null,
                        null,
                        "Boston",
                        "cityVillage");

              assertThat(result.containsKey("results"), is(true));

              List<?> results = (List<?>) result.get("results");

              assertThat(results.size(), is(2));

              for (Object value : results) {
                assertThat(value instanceof Map, is(true));

                Map<?, ?> resultMap = (Map<?, ?>) value;

                assertThat(resultMap.containsKey("address"), is(true));
                assertThat(resultMap.containsKey("components"), is(true));
                assertThat(resultMap.get("address") instanceof String, is(true));
                assertThat(resultMap.get("components") instanceof List, is(true));
              }
        }

        @Test
        public void searchEntryNames_shouldMatchLegacyAjaxNames() throws Exception {
              MockHttpServletRequest legacyRequest = new MockHttpServletRequest();
              MockHttpServletResponse legacyResponse = new MockHttpServletResponse();

              ajaxController.getPossibleAddressHierarchyEntries(
                        null,
                        legacyRequest,
                        legacyResponse,
                        "Boston",
                        "cityVillage");

              String legacyJson = legacyResponse.getContentAsString();

              MockHttpServletRequest restRequest = new MockHttpServletRequest();
              MockHttpServletResponse restResponse = new MockHttpServletResponse();

              SimpleObject restResult = controller.searchEntryNames(
                        restRequest,
                        restResponse,
                        "Boston",
                        "cityVillage");

              assertThat(legacyJson.contains("\"Boston\""), is(true));

              List<String> restResults = (List<String>) restResult.get("results");

              assertThat(restResults.size(), is(1));
              assertThat(restResults, hasItem("Boston"));
        }

		@Test
public void handleException_shouldReturnInternalServerErrorForUnexpectedException() throws Exception {
    BaseRestController baseRestController = new BaseRestController();
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();

    baseRestController.handleException(
            new RuntimeException("Unexpected server error"),
            request,
            response);

    assertThat(response.getStatus(), is(500));
}
}
