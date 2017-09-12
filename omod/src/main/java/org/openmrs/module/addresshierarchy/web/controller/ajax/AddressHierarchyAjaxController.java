package org.openmrs.module.addresshierarchy.web.controller.ajax;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.PersonAddress;
import org.openmrs.api.context.Context;
import org.openmrs.module.addresshierarchy.AddressField;
import org.openmrs.module.addresshierarchy.AddressHierarchyEntry;
import org.openmrs.module.addresshierarchy.AddressHierarchyLevel;
import org.openmrs.module.addresshierarchy.exception.AddressHierarchyModuleException;
import org.openmrs.module.addresshierarchy.service.AddressHierarchyService;
import org.openmrs.module.addresshierarchy.util.AddressHierarchyUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Handles all the AJAX requests for this module
 */
@Controller
public class AddressHierarchyAjaxController {

	protected final Log log = LogFactory.getLog(getClass());


	/**
	 * Returns a list of child address hierarchy entries in JSON format
	 *
	 * The parent entry is specified by a string in the format "UNITED STATES|MASSACHUSETTS|PLYMOUTH COUNTY"
	 */
	@RequestMapping("/module/addresshierarchy/ajax/getChildAddressHierarchyEntries.form")
	 public void getChildAddressHierarchyEntries(ModelMap model, HttpServletRequest request, HttpServletResponse response,
					                             @RequestParam(value = "searchString", required = false) String searchString) throws Exception {

		AddressHierarchyService ahService = Context.getService(AddressHierarchyService.class);

		List<String> childEntryNames = new ArrayList<String>();

		// if the search parameter is empty, we just want all items at in the top mapped level
		if (StringUtils.isBlank(searchString)) {
			List<AddressHierarchyLevel> levels = ahService.getOrderedAddressHierarchyLevels(false);
			if (levels != null && levels.size() > 0) {
				for (AddressHierarchyEntry entry : ahService.getAddressHierarchyEntriesByLevel(levels.get(0))) {
					childEntryNames.add(entry.getName());
				}
			}
		}
		else {
			// other, create the appropriate PersonAddress object and then perform the search
			PersonAddress address = new PersonAddress();
			List<AddressHierarchyLevel> levels = ahService.getOrderedAddressHierarchyLevels(false);  // note that we only want the mapped hierarchy levels

			int i = 0;
			// iterate through all the names in the search string to form the PersonAddress object
			for (String name : searchString.split("\\|")) {
				if (StringUtils.isNotBlank(name)) {
					if (levels.size() <= i-1) {  // make sure we haven't reached the bottom level, because this would make no sense
						throw new AddressHierarchyModuleException("Address hierarchy levels have not been properly defined.");
					}
					else {
						AddressHierarchyUtil.setAddressFieldValue(address, levels.get(i).getAddressField(), name);
					}
				}
				i++;
			}

			// now do the actual search
			childEntryNames = ahService.getPossibleAddressValues(address, levels.get(i).getAddressField());

		}

		generateAddressHierarchyEntryNamesResponse(response, childEntryNames);
	}

	/**
	 * Given an search string and an AddressField, returns all the entries at the level mapped to the address field that
	 * contain the search string
	 *
	 * If Name Phonetics module has been configured, does a soundex match instead of a straight string match
     *
	 * @throws IOException
	 */
	@RequestMapping("/module/addresshierarchy/ajax/getPossibleAddressHierarchyEntries.form")
	public void getPossibleAddressHierarchyEntries(ModelMap model, HttpServletRequest request, HttpServletResponse response,
	                                               	@RequestParam("searchString") String searchString,
						                            @RequestParam("addressField") String addressFieldString) throws IOException {

		if (StringUtils.isBlank(searchString) || StringUtils.isBlank(addressFieldString)) {
			log.error("Must specify both an address field and a search string");
			// return an empty response
			generateAddressHierarchyEntryNamesResponse(response, null);
		}

		AddressHierarchyService ahService = Context.getService(AddressHierarchyService.class);

		// find the address hierarchy level associated with the given address field
		AddressHierarchyLevel level = ahService.getAddressHierarchyLevelByAddressField(AddressField.getByName(addressFieldString));

		if (level == null) {
			log.error("Invalid address field or address field has no associated address hierarchy level");
			// return an empty response
			generateAddressHierarchyEntryNamesResponse(response, null);
		}

		Set<String> names = ahService.searchAddresses(searchString, level);
		generateAddressHierarchyEntryNamesResponse(response, new ArrayList<String>(names), searchString);
	}

    @RequestMapping("/module/addresshierarchy/ajax/getPossibleAddressHierarchyEntriesWithParents.form")
    @ResponseBody
    public ArrayList<ModelMap> getPossibleAddressHierarchyEntriesWithParents(@RequestParam(value = "searchString") String searchString,
                                                                             @RequestParam(value = "addressField") String addressFieldString,
                                                                             @RequestParam(value = "parentUuid", required = false) String parentUuid,
                                                                             @RequestParam(value = "userGeneratedIdForParent", required = false) String userGeneratedIdForParent,
                                                                             @RequestParam(value = "limit", required = false, defaultValue = "20") int limit) throws IOException {

        if (StringUtils.isBlank(searchString) || StringUtils.isBlank(addressFieldString)) {
            log.error("Must specify both an address field and a search string");
            return new ArrayList<ModelMap>();
        }

        if (limit <= 0) {
            log.error("Limit should be greater than zero");
            return new ArrayList<ModelMap>();
        }

        AddressHierarchyService ahService = Context.getService(AddressHierarchyService.class);

        // find the address hierarchy level associated with the given address field
        AddressHierarchyLevel level = ahService.getAddressHierarchyLevelByAddressField(AddressField.getByName(addressFieldString));

        if (level == null) {
            log.error("Invalid address field or address field has no associated address hierarchy level");
            return new ArrayList<ModelMap>();
        }
		AddressHierarchyEntry parentEntry = ahService.getAddressHierarchyEntryByUuid(parentUuid);
		if (parentEntry == null) {
			parentEntry = ahService.getAddressHierarchyEntryByUserGenId(userGeneratedIdForParent);
		}

		return getAddresses(retrieveAddressHierarchyEntries(ahService, level, searchString, parentEntry, limit));
    }

	private List<AddressHierarchyEntry> retrieveAddressHierarchyEntries(AddressHierarchyService ahService, AddressHierarchyLevel level, String searchString, AddressHierarchyEntry parentEntry, int limit) {
		if (parentEntry != null) {
			return limit(ahService.getAddressHierarchyEntriesByLevelAndLikeNameAndParent(level, searchString, parentEntry), limit);
		}
		return ahService.getAddressHierarchyEntriesByLevelAndLikeName(level, searchString, limit);
	}

    private <T>List<T> limit(List<T> list, int limit) {
        return limit > list.size()? list: list.subList(0, limit);
    }

    private ArrayList<ModelMap> getAddresses(List<AddressHierarchyEntry> entries) {
		ArrayList<ModelMap> addresses = new ArrayList<ModelMap>();

		for (AddressHierarchyEntry entry : entries) {
            addresses.add(getAddressAndParents(entry));
        }
		return addresses;
	}

	private ModelMap getAddressAndParents(AddressHierarchyEntry entry) {
        ModelMap address = new ModelMap();
        address.addAttribute("name", entry.getName());
        address.addAttribute("uuid", entry.getUuid());
        address.addAttribute("userGeneratedId", entry.getUserGeneratedId());
        AddressHierarchyEntry parent = entry.getParent();
        if (parent != null) {
            address.addAttribute("parent", getAddressAndParents(parent));
        }
        return address;
    }

	/**
	 * Returns a list of full addresses that contain address hierarchy entries with the specified name at the specified hierarchy level
	 *
	 * Specify a separator if you want the full address strings returned to be delimited by something other than the pipe (|)
	 */
	@RequestMapping("/module/addresshierarchy/ajax/getPossibleFullAddressesForAddressHierarchyEntry.form")
	public void getPossibleFullAddressesForAddressHierarchyEntry(ModelMap model, HttpServletRequest request, HttpServletResponse response,
					                             @RequestParam(value = "entryName", required = false) String entryName,
					                             @RequestParam(value = "addressField", required = false) String addressField,
					                             @RequestParam(value = "separator", required = false) String separator) throws Exception {

		AddressHierarchyService ahService = Context.getService(AddressHierarchyService.class);

		if (StringUtils.isBlank(entryName) || StringUtils.isBlank(addressField)) {
			log.error("Must specify both an address field and a entry name");
			// return an empty response
			generateFullAddressResponse(response, null, separator);
		}
		else {
			// do the exact-match search
			AddressHierarchyLevel level = ahService.getAddressHierarchyLevelByAddressField(AddressField.getByName(addressField));

			if (level == null) {
				log.error("Invalid address field passed to getPossbleFullAddressEntries");
			}

			// find all the entries for the matching level and name
			List<AddressHierarchyEntry> entries = ahService.getAddressHierarchyEntriesByLevelAndName(level, entryName);

			// now generate all the possible addresses for these entries
			Set<String> addresses = new HashSet<String>();
			for (AddressHierarchyEntry entry : entries) {
				addresses.addAll(ahService.getPossibleFullAddresses(entry));
			}

			// generate the response
			generateFullAddressResponse(response, addresses, separator);
		}
	}

	/**
	 * Returns a list of full addresses in string format that match the given search string;
	 *
	 * If Name Phonetics module has been configured, does a soundex match instead of a straight string match
	 * Specify a separator if you want the full address strings returned to be delimited by something other than the pipe (|)
	 *
	 * (See docs on the underlying getPossibleFullAddresses(String) method for more information
	 */
	@RequestMapping("/module/addresshierarchy/ajax/getPossibleFullAddresses.form")
	public void getPossibleFullAddresses(ModelMap model, HttpServletRequest request, HttpServletResponse response,
					                             @RequestParam(value = "searchString", required = false) String searchString,
					                             @RequestParam(value = "separator", required = false) String separator) throws Exception {

		AddressHierarchyService ahService = Context.getService(AddressHierarchyService.class);

		// determine what kind of a search to do based on parameters specified
		if (StringUtils.isBlank(searchString)) {
			log.error("Must specific a search string");
			// return an empty response
			generateFullAddressResponse(response, null, separator);
		}
		else {
			// perform the search string search
			Set<String> addresses = ahService.searchAddresses(searchString, null);
			generateFullAddressResponse(response, addresses, separator);
		}
	}

    /**
     * Returns ordered array of hierarchy levels.
     */
    @RequestMapping("/module/addresshierarchy/ajax/getOrderedAddressHierarchyLevels.form")
    @ResponseBody
    public ArrayList<ModelMap> getOrderedAddressHierarchyLevels() throws Exception {
        AddressHierarchyService ahService = Context.getService(AddressHierarchyService.class);
        List<AddressHierarchyLevel> hierarchyLevels = ahService.getOrderedAddressHierarchyLevels();
        ArrayList<ModelMap> map = new ArrayList<ModelMap>();
        for (AddressHierarchyLevel hierarchyLevel : hierarchyLevels) {
            ModelMap modelMap = new ModelMap();
            modelMap.addAttribute("name", hierarchyLevel.getName());
            String fieldName = (hierarchyLevel.getAddressField() != null) ? hierarchyLevel.getAddressField().getName() : null;
            modelMap.addAttribute("addressField", fieldName);
            modelMap.addAttribute("required", hierarchyLevel.getRequired());
			if (hierarchyLevel.getAddressField() != null ) {
				map.add(modelMap);
			}
        }
        return map;
    }

	/**
	 * Utility methods
	 */

	/**
	 * Helper method used to generate the AJAX response the getChildAddressHierarchyEntries and getPossibleAddressHierarchyEntries methods return
	 */
	private void generateAddressHierarchyEntryNamesResponse(HttpServletResponse response, List<String> names, String exactMatch) throws IOException {
		response.setContentType("text/json");
		response.setCharacterEncoding("UTF-8");
		PrintWriter out = response.getWriter();

		// start the JSON
		out.print("[");

		if (names != null) {
			// sort names
			Collections.sort(names);

			// if there is an exact match, move it to the front of the list
			Iterator<String> i = names.iterator();
			while (i.hasNext()) {
				String next = i.next();
				if (next.equalsIgnoreCase(exactMatch)) {
					i.remove();
					names.add(0, next);
					break;
				}
			}

			// add the elements: ie, { "name": "Boston" }
			i = names.iterator();
			while (i.hasNext()) {
				out.print("{ \"name\": \"" + i.next() + "\" }");

				// print comma as a delimiter for all but the last option in the list
				if (i.hasNext()) {
					out.print(",");
				}
			}
		}

		// close the JSON
		out.print("]");
	}

	private void generateAddressHierarchyEntryNamesResponse(HttpServletResponse response, List<String> names) throws IOException {
		generateAddressHierarchyEntryNamesResponse(response, names, null);
	}

	/**
	 * Helper method used to generate the AJAX response the getPossibleFullAddressEntries method returns
	 */
	private void generateFullAddressResponse(HttpServletResponse response, Set<String> addresses, String separator) throws IOException {

		String delimiter = null;
		if(!StringUtils.equals(separator, "|")){
			delimiter = separator;
		}

		response.setContentType("text/json");
    	response.setCharacterEncoding("UTF-8");
    	PrintWriter out = response.getWriter();

    	out.print("[");

		if (addresses != null && addresses.size() > 0) {
			Iterator<String> i = addresses.iterator();
			if(StringUtils.isNotBlank(delimiter)){
				while (i.hasNext()) {
					out.print("{ \"address\": \"" + StringUtils.replace(i.next(), "|", delimiter) + "\" }");
					// print comma between entries for all but the last option in the list
					if (i.hasNext()) {
						out.print(",");
					}
				}
			}
			else{
				while (i.hasNext()) {
					out.print("{ \"address\": \"" + i.next() + "\" }");
					// print comma between entries for all but the last option in the list
					if (i.hasNext()) {
						out.print(",");
					}
				}
			}
		}

    	out.print("]");
	}
}
