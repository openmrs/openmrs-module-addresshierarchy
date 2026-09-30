
package org.openmrs.module.addresshierarchy.web.rest.v1_0.resource;

import java.util.List;

import org.openmrs.api.context.Context;
import org.openmrs.module.addresshierarchy.AddressField;
import org.openmrs.module.addresshierarchy.AddressHierarchyLevel;
import org.openmrs.module.addresshierarchy.service.AddressHierarchyService;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.PropertyGetter;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.RefRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingReadableResource;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;

@Resource(
        name = RestConstants.VERSION_1 + "/addresshierarchy-level",
        supportedClass = AddressHierarchyLevel.class,
        supportedOpenmrsVersions = { "2.7 - 9.*" })
public class AddressHierarchyLevelResource extends BaseDelegatingReadableResource<AddressHierarchyLevel> {

    @Override
    public AddressHierarchyLevel newDelegate() {
        return new AddressHierarchyLevel();
    }

    @Override
    public AddressHierarchyLevel getByUniqueId(String uniqueId) {
        return Context.getService(AddressHierarchyService.class)
                .getAddressHierarchyLevelByUuid(uniqueId);
    }

    @Override
    public PageableResult doGetAll(RequestContext context) {
        AddressHierarchyService service = Context.getService(AddressHierarchyService.class);

        Boolean includeUnmapped = context.getParameter("includeUnmapped") == null
                ? false
                : Boolean.valueOf(context.getParameter("includeUnmapped"));

        Boolean includeEmpty = context.getParameter("includeEmpty") == null
                ? true
                : Boolean.valueOf(context.getParameter("includeEmpty"));

        List<AddressHierarchyLevel> levels = service
                .getOrderedAddressHierarchyLevels(includeUnmapped, includeEmpty);

        return new NeedsPaging<AddressHierarchyLevel>(levels, context);
    }

    @Override
    public PageableResult doSearch(RequestContext context) {
        String addressFieldName = context.getParameter("addressField");

        if (addressFieldName == null || addressFieldName.trim().isEmpty()) {
            return doGetAll(context);
        }

        AddressField addressField = AddressField.getByName(addressFieldName);

        if (addressField == null) {
            return new NeedsPaging<AddressHierarchyLevel>(
                    java.util.Collections.<AddressHierarchyLevel>emptyList(), context);
        }

        AddressHierarchyLevel level = Context.getService(AddressHierarchyService.class)
                .getAddressHierarchyLevelByAddressField(addressField);

        List<AddressHierarchyLevel> results = new java.util.ArrayList<AddressHierarchyLevel>();

        if (level != null) {
            results.add(level);
        }

        return new NeedsPaging<AddressHierarchyLevel>(results, context);
    }

    public String getUri(AddressHierarchyLevel level) {
        return RestConstants.URI_PREFIX + "v1/addresshierarchy-level/" + level.getUuid();
    }

    @PropertyGetter("display")
    public String getDisplayString(AddressHierarchyLevel level) {
        return level.getName();
    }

    @PropertyGetter("addressField")
    public String getAddressField(AddressHierarchyLevel level) {
        return level.getAddressField() == null ? null : level.getAddressField().getName();
    }

    @Override
    public DelegatingResourceDescription getRepresentationDescription(Representation representation) {
        DelegatingResourceDescription description = new DelegatingResourceDescription();

        description.addProperty("uuid");
        description.addProperty("display");
        description.addProperty("name");
        description.addProperty("addressField");
        description.addSelfLink();
        description.addLink("full", ".?v=full");

        if (representation instanceof DefaultRepresentation
                || representation instanceof FullRepresentation) {
            description.addProperty("required");
            description.addProperty("parent", Representation.REF);
        }

        if (representation instanceof FullRepresentation) {
            description.addProperty("parent", Representation.FULL);
        }

        return description;
    }

    @Override
    public String getResourceVersion() {
        return "2.7";
    }
}
