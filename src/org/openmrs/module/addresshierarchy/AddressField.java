package org.openmrs.module.addresshierarchy;

/**
 * This is a list of all the valid address fields on PersonAddress Note that the names of some of
 * the fields changed between Openmrs 1.7 and 1.8. The follow fields refer to the identical field in
 * the database: 
 * 
 * ADDRESS_3 = NEIGHBORHOOD_CELL
 * ADDRESS_4 = TOWNSHIP_DIVISION
 * ADDRESS_5 = SUBREGION
 * ADDRESS_6 = REGION
 */
public enum AddressField {
	
	ADDRESS_1("address1"), ADDRESS_2("address2"), ADDRESS_3("address3"), NEIGHBORHOOD_CELL("neighborhoodCell"), 
	ADDRESS_4("address4"), TOWNSHIP_DIVISION("townshipDivision"), ADDRESS_5("address5"), SUBREGION("subregion"), 
	ADDRESS_6("address6"), REGION("region"), CITY_VILLAGE("cityVillage"), COUNTY_DISTRICT("countyDistrict"), 
	STATE_PROVINCE("stateProvince"), COUNTRY("country");
	
	String name;
	
	AddressField(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
}
