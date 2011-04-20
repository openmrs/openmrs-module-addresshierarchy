package org.openmrs.module.addresshierarchy.web.dwr;

import java.util.List;

import org.openmrs.api.context.Context;
import org.openmrs.module.addresshierarchy.AddressHierarchy;
import org.openmrs.module.addresshierarchy.AddressHierarchyService;


/**
 * The Class DWRAddressHierarchyService which does the back end support for javascript in the web pages.
 */
public class DWRAddressHierarchyService {
    
    /**
     * Gets the service.
     * 
     * @return the service
     */
    private AddressHierarchyService getService(){
        return (AddressHierarchyService)Context.getService(AddressHierarchyService.class);
    }
    
    /**
     * Method used to get the locations from the top hierarchy.
     * 
     * @return the top hierarchy component locations list
     * 
     * @see org.openmrs.module.addresshierarchy.db.AddressHierarchyDAO#getCountryList()
     */
    public String[] getCountryList(){
        
        String[] list = getService().getCountryList();
        return list;
    }
    /**
     * Method used to get the child locations.
     * 
     * @param arrayList the array with parent_type_id,location_name,parent_id
     * 
     * @return the next component in an array
     * 
     * @see org.openmrs.module.addresshierarchy.db.AddressHierarchyDAO#getNextComponent(java.lang.Integer, java.lang.String, java.lang.Integer)
     */
    public String[] getNextComponent(String[] arrayList){
        
    	Integer typeId = Integer.valueOf(arrayList[0]);
    	Integer parentId;
    	if(arrayList[1]==""){
    		parentId = null;
    	}
    	parentId = Integer.valueOf(arrayList[1]);
        String locationName = arrayList[2];
        String[] list = getService().getNextComponent(typeId,locationName,parentId);
        
        return list;
    }
    
    /**
     * Gets the next location type name.
     * 
     * @param typeId the type id
     * 
     * @return the next location type name
     */
    public String getNextParamName(Integer typeId){
        String[] list = {"","country","state","sublocation1","sublocation2","sublocation3","sublocation4"
                        ,"sublocation5","sublocation6","postalcode","longitude","latitude"};
        return list[typeId];
    }
    
    
    /**
     * Gets the location id when typeid and parentid is given.
     * 
     * @param arrayList the array contains typeid and parentid
     * 
     * @return the location id
     */
    public Integer getLocationId(String[] arrayList){
        Integer typeId = Integer.valueOf(arrayList[0]);
        Integer parentId = Integer.valueOf(arrayList[1]);
        String locationName = arrayList[2];
        Integer locationId = getService().getLocationId(typeId, locationName, parentId);
        if(locationId==null){locationId=0;}
        return locationId;
    }
    
    
    /**
     * Method gives out the total number of locations in the address_hierarchy table.
     * 
     * @return the location count
     * 
     * @see org.openmrs.module.addresshierarchy.db.AddressHierarchyDAO#locationCount()
     */
    public Integer getLocationCount(){
        Integer count = getService().locationCount();
        return count;
    }
    
    /**
     * Method used to list out the locations with similar names.
     * 
     * @param typeId the type id
     * @param str the string typed at autocomplete box
     * 
     * @return the list
     * 
     * @see org.openmrs.module.addresshierarchy.db.AddressHierarchyDAO#locationsLoader(java.lang.Integer, java.lang.String)
     */
    
    public List autocomplete(Integer typeId,String str){
    	List arr = getService().locationsLoader(typeId, str);
    	return arr;
    }
    
    
    /**
     * Creates the location.
     * 
     * @param name the name
     * @param typeId the type id
     * @param parentId the parent id
     * 
     * @return the string[]
     */
    public String[] createLocation(String name, Integer typeId, Integer parentId  ){
    	
    	if(parentId == 0){
    		parentId = null;
    	}
    	
    	typeId = typeId + 1;
    	AddressHierarchy ahs = new AddressHierarchy();
    	ahs.setLocationId(0);
    	ahs.setLocationName(name);
    	ahs.setParentId(parentId);
    	ahs.setTypeId(typeId);
    	Integer locationId = getService().setNextComponent(ahs);
    	String[] arr = new String[5];
    	arr[0] = name;
    	arr[1] = locationId.toString();
    	arr[2] = typeId.toString();
    	if(parentId == null){
    		arr[3]="0";
    	}
    	else
    	arr[3] = parentId.toString();
    	
    	return arr;
    }

    /**
     * Method which sends the necessary data to servlet to feed the tree.
     * 
     * @return the complete locations
     * 
     * @see org.openmrs.module.addresshierarchy.db.AddressHierarchyDAO#getCompleteLocations()
     */
    public String getList(){
    	
    	String arr = getService().getCompleteLocations();
    	return arr;	
    }
    /**
     * Edits the location.
     * 
     * @param parentLocationId the parent location id
     * @param oldName the old name
     * @param newName the new name
     */
    public void editLocation(Integer parentLocationId,String oldName,String newName){
    	getService().editLocation(parentLocationId, oldName, newName);
    }
    /**
     * Delete location.
     * 
     * @param parentId the parent id
     * @param childName the child name
     */
    public void deleteLocation(Integer parentId, String childName){
    	getService().deleteLocation(parentId, childName);
    	
    }
    /**
     * Gets the location type name.
     * 
     * @return the location type name
     */
    public String[] getLocationType(){
    	return getService().getLocationType();
    }
    
    /**
     * Update location type table.
     * 
     * @param comp the components of the location type table in an array
     */
    public void updateLocationTypeTable(String[] comp){
    	String str="";
    	String[] list = getService().getAddressHierarchyTypeList(0);
    	if(list.length == comp.length){
    		for(int i=0;i<comp.length;i++){
    			str = "update AddressHierarchyType set name = '"+comp[i]+"' where name = '"+list[i]+"'";
    			getService().execQuery(str);
    		}
    	}
    	
    }

    /**
     * Gets the address hierarchy type list.
     * 
     * @param typeid the typeid
     * 
     * @return the address hierarchy type list in an array
     * 
     * @see org.openmrs.module.addresshierarchy.db.AddressHierarchyDAO#getAddressHierarchyTypeList(java.lang.Integer)
     */
    public String[] getAddressHierarchyTypeList(Integer typeid){
    	String[] arr = getService().getAddressHierarchyTypeList(typeid);
    	return arr;
    }

    /**
     * Gets the size of person table.
     * 
     * @return the size of person table
     * 
     * @see org.openmrs.module.addresshierarchy.db.AddressHierarchyDAO#getSizeOfPersonTable()
     */
    public Integer getSizeOfPersonTable(){
    	return getService().getSizeOfPersonTable();
    }
    

    /**
     * Gets the location lists when the type id of the previous component is provided.
     * 
     * @param id the type id
     * 
     * @return the location lists in an array
     * 
     * @see org.openmrs.module.addresshierarchy.db.AddressHierarchyDAO#getLocation(java.lang.Integer)
     */
	public String[] getLocation(Integer id){
		return getService().getLocation(id);
	}
	

}
