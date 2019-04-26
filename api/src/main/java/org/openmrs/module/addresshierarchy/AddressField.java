package org.openmrs.module.addresshierarchy;

import org.apache.commons.lang.StringUtils;

/**
 * This is a list of all the valid address fields on PersonAddress.
 * 
 * Note that the names of some of the fields changed between Openmrs 1.7 and 1.8. 
 * The follow fields refer to identical fields in the database: 
 * 
 * ADDRESS_3 = NEIGHBORHOOD_CELL
 * ADDRESS_4 = TOWNSHIP_DIVISION
 * ADDRESS_5 = SUBREGION
 * ADDRESS_6 = REGION
 */
public enum AddressField {
	
	ADDRESS_1("address1"), ADDRESS_2("address2"), ADDRESS_3("address3"), NEIGHBORHOOD_CELL("neighborhoodCell"), 
	ADDRESS_4("address4"), TOWNSHIP_DIVISION("townshipDivision"), ADDRESS_5("address5"), SUBREGION("subregion"), 
	ADDRESS_6("address6"),ADDRESS_7("address7"),ADDRESS_8("address8"),ADDRESS_9("address9"),ADDRESS_10("address10"),
	ADDRESS_11("address11"),ADDRESS_12("address12"),ADDRESS_13("address13"),ADDRESS_14("address14"),ADDRESS_15("address15"),
	REGION("region"), CITY_VILLAGE("cityVillage"), COUNTY_DISTRICT("countyDistrict"), 
	STATE_PROVINCE("stateProvince"), COUNTRY("country"), POSTAL_CODE("postalCode"), LONGITUDE("longitude"),
    LATITUDE("latitude");
	
	String name;
	
	AddressField(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public static final AddressField getByName(String name) {
		
		for (AddressField field : AddressField.values()) {
			if (StringUtils.equals(name, field.getName())) {
				return field;
			}
		}
		
		return null;
	}
}
