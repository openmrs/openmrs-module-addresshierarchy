package org.openmrs.module.addresshierarchy.db.hibernate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.SessionFactory;
import org.openmrs.Person;
import org.openmrs.PersonAddress;
import org.openmrs.User;
import org.openmrs.module.addresshierarchy.AddressHierarchy;
import org.openmrs.module.addresshierarchy.AddressHierarchyType;
import org.openmrs.module.addresshierarchy.db.AddressHierarchyDAO;

/**
 * The Class HibernateAddressHierarchyDAO which links to the tables address_hierarchy, address_hierarchy_type and person_address. This class does the functions of storing and retrieving addresses.
 */
public class HibernateAddressHierarchyDAO implements AddressHierarchyDAO {
    
    protected final Log log = LogFactory.getLog(getClass());
    /**
     * Hibernate session factory
     */
    private SessionFactory sessionFactory;
    
    /**
     * Set session factory
     * 
     * @param sessionFactory
     */
    
    public void setSessionFactory(SessionFactory sessionFactory) { 
        this.sessionFactory = sessionFactory;
    }
    
    /**
     * Method used to add a location to the address_hierarchy table when an AddressHierarchy object is sent.
     * 
     * @param ahs the AddressHierarchy Object
     * 
     * @return the Location Id
     * 
     * @see org.openmrs.module.addresshierarchy.db.AddressHierarchyDAO#setNextComponent(org.openmrs.module.addresshierarchy.AddressHierarchy)
     */
    @SuppressWarnings({ "deprecation", "unchecked" })
    public Integer setNextComponent(AddressHierarchy ahs){
        Integer x=null;
        List<Integer> y;
        String searchQuery = "from AddressHierarchy where locationName = '"+ahs.getLocationName()+"' and typeId = "+ahs.getTypeId()+" and parentId = "+ahs.getParentId();
        if(sessionFactory.getCurrentSession().find(searchQuery).isEmpty())
        {
            sessionFactory.getCurrentSession().beginTransaction();
            sessionFactory.getCurrentSession().save(ahs);
            sessionFactory.getCurrentSession().getTransaction().commit();
        }
        String query = "select locationId from AddressHierarchy where locationName = '"+ahs.getLocationName()+"' and typeId = "+ahs.getTypeId()+" and parentId = "+ahs.getParentId();
        y = sessionFactory.getCurrentSession().find(query);
        x = y.get(0);
        return x;
        }
    
    
    /**
     * Method used to get the child locations.
     * 
     * @param parent_type_Id the parent_type_ id
     * @param location_Name the location_ name
     * @param parent_Id the parent_ id
     * 
     * @return the next component in an array
     * 
     * @see org.openmrs.module.addresshierarchy.db.AddressHierarchyDAO#getNextComponent(java.lang.Integer, java.lang.String, java.lang.Integer)
     */
    @SuppressWarnings({ "unchecked", "deprecation" })
    public String[] getNextComponent(Integer parent_type_Id,String location_Name,Integer parent_Id){
        
        Integer parent_Location_Id = this.getLocationId(parent_type_Id, location_Name, parent_Id);
        String Query1 = "select locationName from AddressHierarchy where parentId = "+parent_Location_Id;
        String Query2 = "select typeId from AddressHierarchy where parentId = "+parent_Location_Id;
        List result = sessionFactory.getCurrentSession().find(Query1);
        String[] array = new String[result.size()+4];
        Integer id = (Integer) sessionFactory.getCurrentSession().find(Query2).get(0);
        array[0] = location_Name;
        array[1] = parent_type_Id.toString();
        array[2] = parent_Location_Id.toString();
        array[3] = id.toString();
        for(int i=4;i<result.size()+4;i++){
        		array[i] = (String) result.get(i-4);
        }
        return array;
    }
    
    /**
     * Method gives out the total number of locations in the address_hierarchy table.
     * 
     * @return the location count
     * 
     * @see org.openmrs.module.addresshierarchy.db.AddressHierarchyDAO#locationCount()
     */
    @SuppressWarnings({ "unchecked", "deprecation" })
    public Integer locationCount(){
        String query = "from AddressHierarchy";
        List y = sessionFactory.getCurrentSession().find(query);
        Integer x = y.size();
        
        return x;
    }
    
    /**
     * Method used to get the location id of a location from the address_hierarchy table.
     * 
     * @param parent_type_Id the parent_type_ id
     * @param location_Name the location_ name
     * @param parent_Id the parent_ id
     * 
     * @return the parent location id
     * 
     * @see org.openmrs.module.addresshierarchy.db.AddressHierarchyDAO#getLocationId(java.lang.Integer, java.lang.String, java.lang.Integer)
     */
    @SuppressWarnings({ "unchecked", "deprecation" })
    public Integer getLocationId(Integer parent_type_Id,String location_Name,Integer parent_Id){
    	if(parent_Id == 0) parent_Id = null;
        String query = "select locationId from AddressHierarchy where typeId = "+parent_type_Id+" and parentId = "+parent_Id+" and locationName = '"+location_Name+"'";
        List<Integer> x = sessionFactory.getCurrentSession().find(query);
        Integer parent_Location_Id = x.get(0).intValue();
        return parent_Location_Id;
    }
    
    /**
     * Method used to get the locations from the top hierarchy.
     * 
     * @return the top hierarchy component locations list
     * 
     * @see org.openmrs.module.addresshierarchy.db.AddressHierarchyDAO#getCountryList()
     */
    @SuppressWarnings({ "deprecation", "unchecked" })
    public String[] getCountryList(){
        String query = "select locationName from AddressHierarchy where parentId =null  and typeId =1 ";
        List<String> list = sessionFactory.getCurrentSession().find(query);
        String[] country = new String[list.size()];
        for(int i=0;i<list.size();i++){
            country[i] = list.get(i);
        }
        return country;
    }
    
    /**
     * Method used to edit a location name.
     * 
     * @param parentLocationId the parent location id
     * @param oldName the old name
     * @param newName the new name
     * 
     * @see org.openmrs.module.addresshierarchy.db.AddressHierarchyDAO#editLocation(java.lang.Integer, java.lang.String, java.lang.String)
     */
    public void editLocation(Integer parentLocationId,String oldName,String newName){
    	String query="";
    	String[] x = {"","country","state_province","county_district","subregion","region","township_division","city_village","neighborhood_cell","postal_code","longitude","latitude"};
    	if(parentLocationId==0){
    		query = "update AddressHierarchy set locationName = '"+newName+"' where locationName = '"+oldName+"' and parentId = null";
    	}
    	else{
    	query = "update AddressHierarchy set locationName = '"+newName+"' where locationName = '"+oldName+"' and parentId = "+parentLocationId;
    	}
    	sessionFactory.getCurrentSession().beginTransaction();
        sessionFactory.getCurrentSession().createQuery(query).executeUpdate();
        sessionFactory.getCurrentSession().getTransaction().commit();
        
        
        try{
        	if(parentLocationId==0)
        		query = "select typeId from AddressHierarchy where parentId = null and locationName = '"+newName+"'";
        	else 
        		query = "select typeId from AddressHierarchy where parentId = "+parentLocationId+" and locationName = '"+newName+"'";
        List<Integer> list = sessionFactory.getCurrentSession().find(query);
        int typeid = list.get(0);
        query = "select person_address_id,date_created,person_id,creator,address1,address2,country,state_province,county_district,subregion,region,township_division,city_village,neighborhood_cell,postal_code,longitude,latitude from person_address where "+x[list.get(0)]+" = '"+oldName+"'";
        List finl = sessionFactory.getCurrentSession().createSQLQuery(query).list();
        for(int i=0;i<finl.size();i++){
        	Object[] obj = (Object[]) finl.get(i);
        	User user = new User();
        	user.setPersonId((Integer) obj[3]);
        	PersonAddress pa = new PersonAddress();
        	pa.setPersonAddressId((Integer) obj[0]);
        	pa.setDateCreated((Date) obj[1]);
        	pa.setCreator(user);
        	Person p = new Person();
        	p.setPersonId((Integer) obj[2]);
        	for(int j=4;j<obj.length;j++){
        		if(obj[j]==null)
        			obj[j]="";
        	}
        	pa.setAddress1(obj[4].toString());
        	pa.setAddress2(obj[5].toString());
        	pa.setCountry(obj[6].toString());
        	pa.setStateProvince(obj[7].toString());
        	pa.setCountyDistrict(obj[8].toString());
        	pa.setSubregion(obj[9].toString());
        	pa.setRegion(obj[10].toString());
        	pa.setTownshipDivision(obj[11].toString());
        	pa.setCityVillage(obj[12].toString());
        	pa.setNeighborhoodCell(obj[13].toString());
        	pa.setPostalCode(obj[14].toString());
        	pa.setLongitude(obj[15].toString());
        	pa.setLatitude(obj[16].toString());
        	pa.setPerson(p);
        	switch(typeid){
        	case 1: pa.setCountry(newName);break;
        	case 2: pa.setStateProvince(newName);break;
        	case 3: pa.setCountyDistrict(newName);break;
        	case 4: pa.setSubregion(newName);break;
        	case 5: pa.setRegion(newName);break;
        	case 6: pa.setTownshipDivision(newName);break;
        	case 7: pa.setCityVillage(newName);break;
        	case 8: pa.setNeighborhoodCell(newName);break;
        	case 9: pa.setPostalCode(newName);break;
        	case 10: pa.setLongitude(newName);break;
        	case 11: pa.setLatitude(newName);break;
        	}
        	sessionFactory.getCurrentSession().beginTransaction();
        	sessionFactory.getCurrentSession().update(pa);
        	sessionFactory.getCurrentSession().getTransaction().commit();
        	sessionFactory.getCurrentSession().flush();
        }
        }
        catch(Exception e){
        	e.printStackTrace();
        }
    }
    
    /**
     * Method used to delete a location.
     * 
     * @param parentLocationId the parent location id
     * @param name the location name
     * 
     * @see org.openmrs.module.addresshierarchy.db.AddressHierarchyDAO#deleteLocation(java.lang.Integer, java.lang.String)
     */
    @SuppressWarnings({ "deprecation", "unchecked" })
	public void deleteLocation(Integer parentLocationId,String name){
    	Boolean b=true;
    	String query="";
    	
    	if(parentLocationId == 0){
    		query = "select locationId from AddressHierarchy where locationName = '"+name+"' and parentId = null";
    		
    	}
    	else{
    	query = "select locationId from AddressHierarchy where locationName = '"+name+"' and parentId = "+parentLocationId;
    	
    	}
    	List<Integer> x = sessionFactory.getCurrentSession().find(query);
    	//int xcnt = 0;
    	//x.add(parentLocationId);
    	for(int i=0;i<x.size();i++){
    		String queryx = "select locationId from AddressHierarchy where parentId = "+x.get(i);
    	
    		List<Integer> y = sessionFactory.getCurrentSession().find(queryx);
    		if(!y.isEmpty())
    			x.addAll(y);
    		
    	}
    	
    	Collections.sort(x);
    	for(int j=x.size()-1;j>=0;j--){
    		String delete = "";
    		if(x.get(j)==0){
    			delete = "delete from AddressHierarchy where parentId = null and name ='"+name+"'";
    	
    		}
    		else{
    			delete = "delete from AddressHierarchy where parentId = "+x.get(j);

    		}
    		sessionFactory.getCurrentSession().beginTransaction();
            sessionFactory.getCurrentSession().createQuery(delete).executeUpdate();
            sessionFactory.getCurrentSession().getTransaction().commit();
    		
    	}
    	String delete = "delete from AddressHierarchy where locationId = "+x.get(0);
    	sessionFactory.getCurrentSession().beginTransaction();
        sessionFactory.getCurrentSession().createQuery(delete).executeUpdate();
        sessionFactory.getCurrentSession().getTransaction().commit();
    }
    
    /**
     * Method used to list out the locations with similar names.
     * 
     * @param id the type id
     * @param str the string typed at autocomplete box
     * 
     * @return the list
     * 
     * @see org.openmrs.module.addresshierarchy.db.AddressHierarchyDAO#locationsLoader(java.lang.Integer, java.lang.String)
     */
    @SuppressWarnings({ "unchecked", "deprecation" })
	public List locationsLoader(Integer id,String str){
    	String query = "select locationName from AddressHierarchy where typeId = "+id+" and locationName like '"+str+"%'";
    	List<String> arr = sessionFactory.getCurrentSession().find(query);
    	return arr;
    }
    
    /**
     * Method which sends the necessary data to servlet to feed the tree.
     * 
     * @return the complete locations
     * 
     * @see org.openmrs.module.addresshierarchy.db.AddressHierarchyDAO#getCompleteLocations()
     */
    @SuppressWarnings({ "deprecation", "unchecked" })
	public String getCompleteLocations(){
    	String query = "Select locationName,locationId,typeId from AddressHierarchy where parentId = null order by locationName";
    	Iterator x = sessionFactory.getCurrentSession().find(query).iterator();
    	String ping = "";
		ping = "{treeNodes : [ { title : \"Start\" , locationId : \"0\", typeId : \"0\" , parentId : \"0\", children : [";
		for(int b1 = 0;x.hasNext();b1++){
			if(b1!=0){
				ping = ping +",";
			}
    		Object[] y = (Object[]) x.next();
    		String query2 = "select locationName,locationId,typeId,parentId from AddressHierarchy where parentId = "+y[1].toString()+" order by locationName";
    		Iterator x1  = sessionFactory.getCurrentSession().find(query2).iterator();
    		ping = ping + "{ title : \""+y[0].toString()+"\" , locationId : \""+y[1].toString()+"\", typeId : \""+y[2].toString()+"\" , parentId : \"0\"";
    		if(x1.hasNext()){
    			ping = ping + ", children : [ ";
    		}
    		else{
    			ping = ping +"}";continue;
    		}
    		for(int b2 = 0;x1.hasNext();b2++){
				if(b2!=0){
					ping = ping +",";
				}
				Object[] y1 = (Object[]) x1.next();
				String query3 = "select locationName,locationId,typeId,parentId from AddressHierarchy where parentId = "+y1[1].toString()+" order by locationName";
    			Iterator x2 = sessionFactory.getCurrentSession().find(query3).iterator();
    			ping = ping + "{ title : \""+y1[0].toString()+"\" , locationId : \""+y1[1].toString()+"\", typeId : \""+y1[2].toString()+"\" , parentId : \""+y1[3].toString()+"\"";
        		if(x2.hasNext()){
        			ping = ping + ", children : [ ";
        		}
        		else{
        			ping = ping +"}";continue;
        		}
        		for(int b3= 0;x2.hasNext();b3++){
    				if(b3!=0){
    					ping = ping +",";
    				}
    				Object[] y2 = (Object[]) x2.next();
    				String query4 = "select locationName,locationId,typeId,parentId from AddressHierarchy where parentId = "+y2[1].toString()+" order by locationName";
        			Iterator x3 = sessionFactory.getCurrentSession().find(query4).iterator();
        			ping = ping + "{ title : \""+y2[0].toString()+"\" , locationId : \""+y2[1].toString()+"\", typeId : \""+y2[2].toString()+"\" , parentId : \""+y2[3].toString()+"\"";
            		if(x3.hasNext()){
            			ping = ping + ", children : [ ";
            		}
            		else{
            			ping = ping +"}";continue;
            		}
            		for(int b4 = 0;x3.hasNext();b4++){
        				if(b4!=0){
        					ping = ping +",";
        				}
        				Object[] y3 = (Object[]) x3.next();
        	   			String query5 = "select locationName,locationId,typeId,parentId from AddressHierarchy where parentId = "+y3[1].toString()+" order by locationName";
            			Iterator x4 = sessionFactory.getCurrentSession().find(query5).iterator();
            			ping = ping + "{ title : \""+y3[0].toString()+"\" , locationId : \""+y3[1].toString()+"\", typeId : \""+y3[2].toString()+"\" , parentId : \""+y3[3].toString()+"\"";
                		if(x4.hasNext()){
                			ping = ping + ", children : [ ";
                		}
                		else{
                			ping = ping +"}";continue;
                		}
                		for(int b5 = 0;x4.hasNext();b5++){
            				if(b5!=0){
            					ping = ping +",";
            				}
            				Object[] y4 = (Object[]) x4.next();
            				String query6 = "select locationName,locationId,typeId,parentId from AddressHierarchy where parentId = "+y4[1].toString()+" order by locationName";
                			Iterator x5 = sessionFactory.getCurrentSession().find(query6).iterator();
                			ping = ping + "{ title : \""+y4[0].toString()+"\" , locationId : \""+y4[1].toString()+"\", typeId : \""+y4[2].toString()+"\" , parentId : \""+y4[3].toString()+"\"";
                    		if(x5.hasNext()){
                    			ping = ping + ", children : [ ";
                    		}
                    		else{
                    			ping = ping +"}";continue;
                    		}
                    		for(int b6 = 0;x5.hasNext();b6++){
                				if(b6!=0){
                					ping = ping +",";
                				}
                				Object[] y5 = (Object[]) x5.next();
                				String query7 = "select locationName,locationId,typeId,parentId from AddressHierarchy where parentId = "+y5[1].toString()+" order by locationName";
                    			Iterator x6 = sessionFactory.getCurrentSession().find(query7).iterator();
                    			ping = ping + "{ title : \""+y5[0].toString()+"\" , locationId : \""+y5[1].toString()+"\", typeId : \""+y5[2].toString()+"\" , parentId : \""+y5[3].toString()+"\"";
                        		if(x6.hasNext()){
                        			ping = ping + ", children : [ ";
                        		}
                        		else{
                        			ping = ping +"}";continue;
                        		}
                        		for(int b7 = 0;x6.hasNext();b7++){
                    				if(b7!=0){
                    					ping = ping +",";
                    				}
                    				Object[] y6 = (Object[]) x6.next();
                    				String query8 = "select locationName,locationId,typeId,parentId from AddressHierarchy where parentId = "+y6[1].toString()+" order by locationName";
                        			Iterator x7 = sessionFactory.getCurrentSession().find(query8).iterator();
                        			ping = ping + "{ title : \""+y6[0].toString()+"\" , locationId : \""+y6[1].toString()+"\", typeId : \""+y6[2].toString()+"\" , parentId : \""+y6[3].toString()+"\"";
                            		if(x7.hasNext()){
                            			ping = ping + ", children : [ ";
                            		}
                            		else{
                            			ping = ping +"}";continue;
                            		}
                            		for(int b8 = 0;x7.hasNext();b8++){
                        				if(b8!=0){
                        					ping = ping +",";
                        				}
                        				Object[] y7 = (Object[]) x7.next();
                        				@SuppressWarnings("unused")
										String query9 = "select locationName,locationId,typeId,parentId from AddressHierarchy where parentId = "+y7[1].toString()+" order by locationName";
                            			Iterator x8 = sessionFactory.getCurrentSession().find(query3).iterator();
                            			ping = ping + "{ title : \""+y7[0].toString()+"\" , locationId : \""+y7[1].toString()+"\", typeId : \""+y7[2].toString()+"\" , parentId : \""+y7[3].toString()+"\"";
                                		if(x8.hasNext()){
                                			ping = ping + ", children : [ ";
                                		}
                                		else{
                                			ping = ping +"},";continue;
                                		}
                                		for(int b9 = 0;x8.hasNext();b9++){
                            				if(b9!=0){
                            					ping = ping +",";
                            				}
                            				Object[] y8 = (Object[]) x8.next();
                            				String query10 = "select locationName,locationId,typeId,parentId from AddressHierarchy where parentId = "+y8[1].toString()+" order by locationName";
                                			Iterator x9 = sessionFactory.getCurrentSession().find(query10).iterator();
                                			ping = ping + "{ title : \""+y8[0].toString()+"\" , locationId : \""+y8[1].toString()+"\", typeId : \""+y8[2].toString()+"\" , parentId : \""+y8[3].toString()+"\"";
                                    		if(x9.hasNext()){
                                    			ping = ping + ", children : [ ";
                                    		}
                                    		else{
                                    			ping = ping +"}";
                                    			continue;
                                    		}
                                    		for(int b10 = 0;x9.hasNext();b10++){
                                				if(b10!=0){
                                					ping = ping +",";
                                				}
                                				Object[] y9 = (Object[]) x9.next();
                                				@SuppressWarnings("unused")
												String query11 = "select locationName,locationId,typeId,parentId from AddressHierarchy where parentId = "+y9[1].toString()+" order by locationName";
                                    			Iterator x10 = sessionFactory.getCurrentSession().find(query3).iterator();
                                    			ping = ping + "{ title : \""+y9[0].toString()+"\" , locationId : \""+y9[1].toString()+"\", typeId : \""+y9[2].toString()+"\" , parentId : \""+y9[3].toString()+"\"";
                                        		if(x10.hasNext()){
                                        			ping = ping + ", children : [ ";
                                        		}
                                        		else{
                                        			ping = ping +"}";
                                        		}
                                        		for(int b11 = 0;x10.hasNext();b11++){
                                    				if(b11!=0){
                                    					ping  = ping+",";
                                    				}
                                    				Object[] y10 = (Object[]) x10.next();
                                    				ping = ping + "{ title : \""+y10[0].toString()+"\" , locationId : \""+y10[1].toString()+"\", typeId : \""+y10[2].toString()+"\" , parentId : \""+y10[3].toString()+"\"";
                                            		ping = ping +"}";
                                            		@SuppressWarnings("unused")
													String query12 = "select locationName,locationId,typeId,parentId from AddressHierarchy where parentId = "+y10[1].toString()+" order by locationName";
                                        			@SuppressWarnings("unused")
													Iterator x11 = sessionFactory.getCurrentSession().find(query3).iterator();
                                        		}
                                    			ping = ping + "]}";
                                    		}
                                			ping = ping + "]}";
                                		}
                            			ping = ping + "]}";
                            		}
                        			ping = ping + "]}";
                        		}
                    			ping = ping + "]}";
                    		}
                			ping = ping + "]}";
                		}
            			ping = ping + "]}";
            		}
        			ping = ping + "]}";
        		}
    			ping = ping + "]}";
    		}
    		ping = ping + "]}";
		}
		ping = ping + "]}]}";
    	return ping;
    }
    
    
    /**
     * Gets the location type component names.
     * 
     * @return the location type component names in an array
     * 
     * @see org.openmrs.module.addresshierarchy.db.AddressHierarchyDAO#getLocationType()
     */
    @SuppressWarnings({ "unchecked", "deprecation" })
	public String[] getLocationType(){
    	List<String> arrList = null;
    	String query = "select name from AddressHierarchyType";
    	arrList = sessionFactory.getCurrentSession().find(query);
    	String[] arr = new String[arrList.size()];
    	for(int i=0;i<arrList.size();i++){
    		arr[i] = arrList.get(i);
    	}
    	return arr;
    }
    
    
    /**
     * Update location type table.
     * 
     * @param aht the aht
     * 
     * @see org.openmrs.module.addresshierarchy.db.AddressHierarchyDAO#updateLocationTypeTable(org.openmrs.module.addresshierarchy.AddressHierarchyType)
     */
    public void updateLocationTypeTable(AddressHierarchyType aht){
    	sessionFactory.getCurrentSession().beginTransaction();
        sessionFactory.getCurrentSession().createQuery("update AddressHierarchyType sets where name = '"+aht.getName()+"'").executeUpdate();
        sessionFactory.getCurrentSession().getTransaction().commit();
        
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
    @SuppressWarnings({ "unchecked", "deprecation" })
	public String[] getAddressHierarchyTypeList(Integer typeid){
    	String query = "select name from AddressHierarchyType where typeId > "+typeid;
    	List<String> x = sessionFactory.getCurrentSession().find(query);
    	String[] arr = new String[x.size()];
    	for(int i=0;i<x.size();i++){
    		arr[i]=x.get(i);
    	}
    	return arr;
    }
    
    
    /**
     * Gets the size of person table.
     * 
     * @return the size of person table
     * 
     * @see org.openmrs.module.addresshierarchy.db.AddressHierarchyDAO#getSizeOfPersonTable()
     */
    @SuppressWarnings("unchecked")
	public Integer getSizeOfPersonTable(){
	    List y = sessionFactory.getCurrentSession().createSQLQuery("select * from person").list();
        Integer x = y.size();
        
        return x;
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
    @SuppressWarnings("unchecked")
	public String[] getLocation(Integer id){
    	String[] x = {"address1","address2","country","state_province","county_district","subregion","region","township_division","city_village","neighborhood_cell","postal_code","longitude","latitude"};
    	String query ="select person_address_id,preferred,";
    	for(int i=0;i<x.length;i++){
    		query += x[i];
    		if(i!=x.length-1){
    			query += ",";
    		}
    	}
    	query += " from person_address where person_id = "+id+" order by preferred desc";
    	
    	List y = sessionFactory.getCurrentSession().createSQLQuery(query).list();
    	List<String> finalarr = new ArrayList();
    	for(int l=0;l<y.size();l++){
    	Object[] obj = (Object[]) y.get(l);
    	
    	for(int j=1;j<obj.length;j++){
    		if(obj[j]!=null)
    			finalarr.add(obj[j].toString());
    		else
    			finalarr.add("");
    		
    	}
    	}
    	String[] fin = new String[finalarr.size()];
    	for(int p=0;p<finalarr.size();p++){
    		fin[p]=finalarr.get(p);
    		
    	}
    	return fin;
    }
    
    /**
     * Execute an update query when a string query is passed.
     * 
     * @param query the update query
     * 
     * @see org.openmrs.module.addresshierarchy.db.AddressHierarchyDAO#execQuery(java.lang.String)
     */
    public void execQuery(String query){
    	
    	sessionFactory.getCurrentSession().beginTransaction();
        sessionFactory.getCurrentSession().createQuery(query).executeUpdate();
        sessionFactory.getCurrentSession().getTransaction().commit();
    	
    }
    
    
}
