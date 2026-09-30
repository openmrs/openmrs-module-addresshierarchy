package org.openmrs.module.addresshierarchy.web.rest.v1_0.resource;

import java.util.ArrayList;
import java.util.List;

import org.openmrs.api.context.Context;
import org.openmrs.module.addresshierarchy.AddressHierarchyEntry;
import org.openmrs.module.addresshierarchy.AddressHierarchyLevel;
import org.openmrs.module.addresshierarchy.service.AddressHierarchyService;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.PropertyGetter;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingReadableResource;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;

@Resource(
        name = RestConstants.VERSION_1 + "/addresshierarchy-entry",
        supportedClass = AddressHierarchyEntry.class,
        supportedOpenmrsVersions = { "2.7 - 9.*" })
public class AddressHierarchyEntryResource extends BaseDelegatingReadableResource<AddressHierarchyEntry> {

    @Override
    public AddressHierarchyEntry newDelegate() {
        return new AddressHierarchyEntry();
    }

    @Override
    public AddressHierarchyEntry getByUniqueId(String uniqueId) {
        return Context.getService(AddressHierarchyService.class)
                .getAddressHierarchyEntryByUuid(uniqueId);
    }

    @Override
    public PageableResult doGetAll(RequestContext context) {
        List<AddressHierarchyEntry> entries = Context.getService(AddressHierarchyService.class)
                .getAddressHierarchyEntriesAtTopLevel();

        return new NeedsPaging<AddressHierarchyEntry>(entries, context);
    }

    @Override
    public PageableResult doSearch(RequestContext context) {
        AddressHierarchyService service = Context.getService(AddressHierarchyService.class);

        String levelUuid = context.getParameter("level");
        String parentUuid = context.getParameter("parent");
        String search = context.getParameter("q");
        String userGeneratedId = context.getParameter("userGeneratedId");

        if (userGeneratedId != null && !userGeneratedId.trim().isEmpty()) {
            List<AddressHierarchyEntry> results = new ArrayList<AddressHierarchyEntry>();

            AddressHierarchyEntry entry =
                    service.getAddressHierarchyEntryByUserGenId(userGeneratedId);

            if (entry != null) {
                results.add(entry);
            }

            return new NeedsPaging<AddressHierarchyEntry>(results, context);
        }

        AddressHierarchyLevel level = null;
        AddressHierarchyEntry parent = null;

        if (levelUuid != null && !levelUuid.trim().isEmpty()) {
            level = service.getAddressHierarchyLevelByUuid(levelUuid);
        }

        if (parentUuid != null && !parentUuid.trim().isEmpty()) {
            parent = service.getAddressHierarchyEntryByUuid(parentUuid);
        }

        List<AddressHierarchyEntry> results;

        if (search != null && !search.trim().isEmpty()) {
            if (level == null) {
                return new NeedsPaging<AddressHierarchyEntry>(
                        new ArrayList<AddressHierarchyEntry>(), context);
            }

            if (parent != null) {
                results = service.getAddressHierarchyEntriesByLevelAndLikeNameAndParent(
                        level, search, parent);

                if (results != null && results.size() > 20) {
                    results = new ArrayList<AddressHierarchyEntry>(
                            results.subList(0, 20));
                }
            } else {
                results = service.getAddressHierarchyEntriesByLevelAndLikeName(
                        level, search, context.getLimit());
            }
        } else if (parent != null) {
            results = service.getChildAddressHierarchyEntries(parent);
        } else if (level != null) {
            results = service.getAddressHierarchyEntriesByLevel(level);
        } else {
            results = service.getAddressHierarchyEntriesAtTopLevel();
        }

        return new NeedsPaging<AddressHierarchyEntry>(results, context);
    }

    public String getUri(AddressHierarchyEntry entry) {
        return RestConstants.URI_PREFIX + "v1/addresshierarchy-entry/" + entry.getUuid();
    }

    @PropertyGetter("display")
    public String getDisplayString(AddressHierarchyEntry entry) {
        return entry.getLocalizedName();
    }

    @Override
    public DelegatingResourceDescription getRepresentationDescription(Representation representation) {
        DelegatingResourceDescription description = new DelegatingResourceDescription();

        description.addProperty("uuid");
        description.addProperty("display");
        description.addSelfLink();
        description.addLink("full", ".?v=full");

        if (representation instanceof DefaultRepresentation
                || representation instanceof FullRepresentation) {
            description.addProperty("name");
            description.addProperty("userGeneratedId");
            description.addProperty("level", Representation.REF);
            description.addProperty("parent", Representation.REF);
        }

        if (representation instanceof FullRepresentation) {
            description.addProperty("latitude");
            description.addProperty("longitude");
            description.addProperty("elevation");
            description.addProperty("level", Representation.FULL);
            description.addProperty("parent", Representation.FULL);
        }

        return description;
    }

    @Override
    public String getResourceVersion() {
        return "2.7";
    }
}