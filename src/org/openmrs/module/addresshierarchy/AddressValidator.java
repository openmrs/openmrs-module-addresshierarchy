package org.openmrs.module.addresshierarchy;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.PersonAddress;
import org.openmrs.api.context.Context;
import org.openmrs.module.addresshierarchy.service.AddressHierarchyService;

public class AddressValidator {

	protected final Log log = LogFactory.getLog(getClass());
	
	// TODO: need make this generic--confirm that is it being used?
	// TODO: this also can be derived from the AddressHiearchyType "mapping" ?
	// TODO: will have to use the reflection utility function I plan to create to fetch address component by reflection
	@SuppressWarnings("deprecation")
    public boolean isAddressStructured(PersonAddress pa){
		
		AddressHierarchyService ahs = ((AddressHierarchyService)Context.getService(AddressHierarchyService.class));
		List<AddressHierarchyEntry> hierarchyList = ahs.getTopOfHierarchyList();
		boolean structured = false;
		String country = pa.getCountry();
		
		
		
		int matchingLocationId = -1;
		if((matchingLocationId = getMatchingLocation(country,hierarchyList)) != -1){
			if((matchingLocationId = getMatchingLocation(pa.getStateProvince(),ahs.getNextComponent(matchingLocationId))) != -1){
				if((matchingLocationId = getMatchingLocation(pa.getCountyDistrict(),ahs.getNextComponent(matchingLocationId))) != -1){
					if((matchingLocationId = getMatchingLocation(pa.getCityVillage(),ahs.getNextComponent(matchingLocationId))) != -1){
						if((matchingLocationId = getMatchingLocation(pa.getNeighborhoodCell(),ahs.getNextComponent(matchingLocationId))) != -1){
							if((matchingLocationId = getMatchingLocation(pa.getAddress1(),ahs.getNextComponent(matchingLocationId))) != -1){
								structured = true;
							}
						}
					}
				}
			}
		}
		
		log.debug("structured val " + structured + " for " + pa);
		return structured;
	}
	
	// TODO: need make this generic
	// TODO: this also can be derived from the AddressHiearchyType "mapping" ?
	// TODO: will have to use the reflection utility function I plan to create to fetch address component by reflection
	@SuppressWarnings("deprecation")
    public String getInvalidReason(PersonAddress pa){
		
		AddressHierarchyService ahs = ((AddressHierarchyService)Context.getService(AddressHierarchyService.class));
		List<AddressHierarchyEntry> hierarchyList = ahs.getTopOfHierarchyList();
		boolean structured = false;
		boolean badCountry = true;
		boolean badProvince = true;
		boolean badDistrict = true;
		boolean badSector = true;
		boolean badCell  = true;
		boolean badUmudugudu = true;
		String country = pa.getCountry();
		
		
		
		int matchingLocationId = -1;
		if((matchingLocationId = getMatchingLocation(country,hierarchyList)) != -1){
			badCountry = false;
			if((matchingLocationId = getMatchingLocation(pa.getStateProvince(),ahs.getNextComponent(matchingLocationId))) != -1){
				badProvince = false;
				if((matchingLocationId = getMatchingLocation(pa.getCountyDistrict(),ahs.getNextComponent(matchingLocationId))) != -1){
					badDistrict = false;
					if((matchingLocationId = getMatchingLocation(pa.getCityVillage(),ahs.getNextComponent(matchingLocationId))) != -1){
						badSector = false;
						if((matchingLocationId = getMatchingLocation(pa.getNeighborhoodCell(),ahs.getNextComponent(matchingLocationId))) != -1){
							badCell = false;
							if((matchingLocationId = getMatchingLocation(pa.getAddress1(),ahs.getNextComponent(matchingLocationId))) != -1){
								badUmudugudu = false;
								structured = true;
							}
						}
					}
				}
			}
		}
		
		log.debug("structured val " + structured + " for " + pa);
		if(badCountry){
			return "country";
		}else if (badProvince){
			return "province";
		}else if (badDistrict){
			return "district";
		}else if (badSector){
			return "sector";
		}else if (badCell){
			return "cell";
		}else if (badUmudugudu){
			return "umudugudu";
		}else{
			return null;
		}

	}
	
	/**
	 * returns the id of the matching location or -1 if not found.
	 * 
	 * @param partToCompare
	 * @param locations
	 */
	private int getMatchingLocation(String partToCompare, List<AddressHierarchyEntry> locations){
		int matchingLocationId = -1;
		AddressHierarchyService ahs = ((AddressHierarchyService)Context.getService(AddressHierarchyService.class));
		for(AddressHierarchyEntry ah : locations){
			if(ah.getLocationName().equalsIgnoreCase(partToCompare)){
				matchingLocationId = ah.getAddressHierarchyEntryId();
				return matchingLocationId;
			}
		}
		return matchingLocationId;
	}
}
