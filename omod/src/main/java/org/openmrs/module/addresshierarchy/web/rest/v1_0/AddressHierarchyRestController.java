package org.openmrs.module.addresshierarchy.web.rest.v1_0;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openmrs.PersonAddress;
import org.openmrs.api.context.Context;
import org.openmrs.module.addresshierarchy.AddressField;
import org.openmrs.module.addresshierarchy.AddressHierarchyEntry;
import org.openmrs.module.addresshierarchy.AddressHierarchyLevel;
import org.openmrs.module.addresshierarchy.service.AddressHierarchyService;
import org.openmrs.module.addresshierarchy.util.AddressHierarchyUtil;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.response.IllegalRequestException;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/rest/v1/addresshierarchy")
public class AddressHierarchyRestController extends BaseRestController {

    @RequestMapping(
            value = "/possiblevalue",
            method = RequestMethod.GET)
    @ResponseBody
    public SimpleObject getPossibleValues(
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestParam(value = "addressField", required = true) String addressField) {

        AddressHierarchyService service =
                Context.getService(AddressHierarchyService.class);

        AddressField targetField = AddressField.getByName(addressField);

        if (targetField == null) {
            throw new IllegalRequestException(
                    "Unknown address field: " + addressField);
        }

        PersonAddress address = new PersonAddress();

        Enumeration<String> parameterNames =
                request.getParameterNames();

        while (parameterNames.hasMoreElements()) {
            String parameterName = parameterNames.nextElement();

            if ("addressField".equals(parameterName)
                    || "limit".equals(parameterName)
                    || "startIndex".equals(parameterName)) {
                continue;
            }

            AddressField field =
                    AddressField.getByName(parameterName);

            if (field == null) {
                throw new IllegalRequestException(
                        "Unknown address field: " + parameterName);
            }

            AddressHierarchyUtil.setAddressFieldValue(
                    address,
                    field,
                    request.getParameter(parameterName));
        }

        List<String> values =
                service.getPossibleAddressValues(
                        address,
                        targetField);

        return createPagedResponse(
                values == null
                        ? new ArrayList<String>()
                        : values,
                request);
    }

    @RequestMapping(
            value = "/entryname",
            method = RequestMethod.GET)
    @ResponseBody
    public SimpleObject searchEntryNames(
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestParam(value = "q", required = true) String searchString,
            @RequestParam(value = "addressField", required = true) String addressField) {

        if (searchString == null || searchString.trim().isEmpty()) {
            throw new IllegalRequestException(
                    "Parameter q is required");
        }

        AddressHierarchyService service =
                Context.getService(AddressHierarchyService.class);

        AddressField field =
                AddressField.getByName(addressField);

        if (field == null) {
            throw new IllegalRequestException(
                    "Unknown address field: " + addressField);
        }

        AddressHierarchyLevel level =
                service.getAddressHierarchyLevelByAddressField(field);

        if (level == null) {
            throw new IllegalRequestException(
                    "No address hierarchy level found for address field: "
                            + addressField);
        }

        Set<String> addresses =
                service.searchAddresses(
                        searchString,
                        level);

        List<String> results =
                new ArrayList<String>();

        if (addresses != null) {
            results.addAll(addresses);
        }

        return createPagedResponse(
                results,
                request);
    }

    @RequestMapping(
            value = "/fulladdress",
            method = RequestMethod.GET)
    @ResponseBody
    public SimpleObject getFullAddresses(
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestParam(value = "q", required = false) String searchString,
            @RequestParam(value = "entry", required = false) String entryUuid,
            @RequestParam(value = "entryName", required = false) String entryName,
            @RequestParam(value = "addressField", required = false) String addressField) {

        AddressHierarchyService service =
                Context.getService(AddressHierarchyService.class);

        Set<String> addresses =
                new LinkedHashSet<String>();

        boolean hasEntry =
                entryUuid != null
                        && !entryUuid.trim().isEmpty();

        boolean hasSearch =
                searchString != null
                        && !searchString.trim().isEmpty();

        boolean hasEntryName =
                entryName != null
                        && !entryName.trim().isEmpty();

        if (hasEntry) {

            AddressHierarchyEntry entry =
                    service.getAddressHierarchyEntryByUuid(
                            entryUuid);

            if (entry == null) {
                throw new IllegalRequestException(
                        "No address hierarchy entry found for uuid: "
                                + entryUuid);
            }

            List<String> fullAddresses =
                    service.getPossibleFullAddresses(entry);

            if (fullAddresses != null) {
                addresses.addAll(fullAddresses);
            }

        } else if (hasEntryName) {

            if (addressField == null
                    || addressField.trim().isEmpty()) {
                throw new IllegalRequestException(
                        "addressField is required when entryName is provided");
            }

            AddressField field =
                    AddressField.getByName(addressField);

            if (field == null) {
                throw new IllegalRequestException(
                        "Unknown address field: " + addressField);
            }

            AddressHierarchyLevel level =
                    service.getAddressHierarchyLevelByAddressField(field);

            if (level == null) {
                throw new IllegalRequestException(
                        "No address hierarchy level found for address field: "
                                + addressField);
            }

            List<AddressHierarchyEntry> entries =
                    service.getAddressHierarchyEntriesByLevelAndName(
                            level,
                            entryName);

            if (entries != null) {
                for (AddressHierarchyEntry entry : entries) {

                    List<String> fullAddresses =
                            service.getPossibleFullAddresses(entry);

                    if (fullAddresses != null) {
                        addresses.addAll(fullAddresses);
                    }
                }
            }

        } else if (hasSearch) {

            Set<String> searchResults =
                    service.searchAddresses(searchString);

            if (searchResults != null) {
                addresses.addAll(searchResults);
            }

        } else {
            throw new IllegalRequestException(
                    "Provide either q, entry, or entryName");
        }

        List<SimpleObject> results =
                new ArrayList<SimpleObject>();

        for (String address : addresses) {

            SimpleObject result =
                    new SimpleObject();

            result.add("address", address);
            result.add(
                    "components",
                    splitAddress(address));

            results.add(result);
        }

        return createPagedResponse(
                results,
                request);
    }

    private <T> SimpleObject createPagedResponse(
            List<T> results,
            HttpServletRequest request) {

        RequestContext context =
                createRequestContext(request);

        NeedsPaging<T> pagedResults =
                new NeedsPaging<T>(
                        results,
                        context);

        SimpleObject response =
                new SimpleObject();

        response.add(
                "results",
                pagedResults.getPageOfResults());

        return response;
    }

    private RequestContext createRequestContext(
            HttpServletRequest request) {

        RequestContext context =
                new RequestContext();

        context.setRequest(request);

        String limitParameter =
                request.getParameter("limit");

        String startIndexParameter =
                request.getParameter("startIndex");

        Integer limit = 50;
        Integer startIndex = 0;

        if (limitParameter != null
                && !limitParameter.trim().isEmpty()) {
            try {
                limit = Integer.valueOf(limitParameter);
            } catch (NumberFormatException e) {
                throw new IllegalRequestException(
                        "Invalid limit: " + limitParameter);
            }
        }

        if (startIndexParameter != null
                && !startIndexParameter.trim().isEmpty()) {
            try {
                startIndex = Integer.valueOf(startIndexParameter);
            } catch (NumberFormatException e) {
                throw new IllegalRequestException(
                        "Invalid startIndex: " + startIndexParameter);
            }
        }

        context.setLimit(limit);
        context.setStartIndex(startIndex);

        return context;
    }

    private List<String> splitAddress(String address) {

        List<String> components =
                new ArrayList<String>();

        if (address == null) {
            return components;
        }

        String[] values =
                address.split("\\|", -1);

        for (String value : values) {
            components.add(value);
        }

        return components;
    }
}