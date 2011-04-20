package org.openmrs.module.addresshierarchy;

import org.openmrs.api.context.Context;
import org.openmrs.module.addresshierarchy.web.dwr.DWRAddressHierarchyService;
import org.openmrs.test.BaseModuleContextSensitiveTest;

public class AddressHierarchyTest extends BaseModuleContextSensitiveTest {
	
    private static Integer x;
    private static String[] y;
    
    protected void onSetUpBeforeTransaction() throws Exception {
        super.onSetUpBeforeTransaction();
        //authenticate();
        Context.authenticate("admin", "test");
    }
    /*
	public void testSaveLocation()throws Exception{
	    AddressHierarchy ahs = new AddressHierarchy();
	    ahs.setLocationName("tamilnadu");
	    ahs.setTypeId(2);
	    ahs.setParentId(9);
	    x = ((AddressHierarchyService)Context.getService(AddressHierarchyService.class)).setNextComponent(ahs);
	    setComplete();
	    System.out.println(x);
	}
    
    public void testChumma() throws Exception{
    	String[] chumma = ((AddressHierarchyService)Context.getService(AddressHierarchyService.class)).getNextComponent(1, "india", null);
    	System.out.println("Test");
    	for(int i = 0; i<chumma.length ; i++){
    		System.out.println(chumma[i]);
    	}
    }
   
    public void testDeleteLocation() throws Exception{
    	((AddressHierarchyService)Context.getService(AddressHierarchyService.class)).deleteLocation(6, "h");
    	
    }*/
    
    public void testEditLocation() throws Exception{
    	((AddressHierarchyService)Context.getService(AddressHierarchyService.class)).editLocation(0, "India", "Indiana");
    }
    /*
    public void testDWR()throws Exception{
        DWRAddressHierarchyService dwr = new DWRAddressHierarchyService();
        System.out.println("Test for getLocationId");
        String[] arrayList = new String[3];
        arrayList[0] = "1";
        arrayList[1] = "0";
        arrayList[2] = "united arabs";
        String[] array = dwr.getNextComponent(arrayList);
        for(int i=0;i<array.length;i++){
        	System.out.println(array[i]);
        }
        
    }
	
	public void testGetLocation() throws Exception{
		String[] arr = {"0","0","india"};
	    //y = ((AddressHierarchyService)Context.getService(AddressHierarchyService.class)).getNextComponent(arr);
    	DWRAddressHierarchyService dwr = new DWRAddressHierarchyService();
    	y = dwr.getNextComponent(arr);
	    //setComplete();
	    for(int i=0;i<y.length;i++)
	    System.out.println(y[i]);
	}
	
	public void testLocationCount() throws Exception{
	    x = ((AddressHierarchyService)Context.getService(AddressHierarchyService.class)).locationCount();
	    System.out.println(x);
	}
    
    /*public void testGetLocations() throws Exception{
    	String x = ((AddressHierarchyService)Context.getService(AddressHierarchyService.class)).getCompleteLocations();
    	
    	System.out.println(x);
    	
    }
    
    public void testDWRCreate(){
    	DWRAddressHierarchyService dwr = new DWRAddressHierarchyService();
    	dwr.createLocation("buna", 0, 0);
    	
    }
    
    /*
    public void testParentChild(){
    	Parent par = new Parent();
    	Child chi = new Child();
    	par.setLocationName("asdhasd");
    	par.setLocationId("1");
    	par.setParentId("2");
    	chi.setLocationName("blabla");
    	chi.setLocationId("3");
    	chi.setParentId("4");
    	String childid="child1";
    	Map children2 = new HashMap();
		children2.put(childid, chi);
		par.setChildren(children2);
		
    	JSONObject obj = JSONObject.fromObject(par);
    	System.out.println(obj);
    }
    
    public void testGetLocationType(){
    	String[] arr = ((AddressHierarchyService)Context.getService(AddressHierarchyService.class)).getLocationType();
    	for(int i=0;i<arr.length;i++){
    		System.out.println(arr[i]);
    	}
    	
    }
    
    public void testGetLocationTypeTable(){
    	String[] arr = ((AddressHierarchyService)Context.getService(AddressHierarchyService.class)).getLocationTypeTable();
    	for(int i=0;i<arr.length;i=i+4){
    		System.out.println(arr[i]+" "+arr[i+1]+" "+arr[i+2]+" "+arr[i+3]);
    	}
    	
    }
   
    public void testUpdateLocationType(){
    	
    	String[] arr = {"2","0","1","1","4","1","3","0","5","1","6","1","7","0","8","1","9","0","11","0","10","1"};
    	//((AddressHierarchyService)Context.getService(AddressHierarchyService.class)).updateLocationTypeTable(arr);
    	DWRAddressHierarchyService dwr = new DWRAddressHierarchyService();
    	dwr.updateLocationTypeTable(arr);
    }
    
    public void testgetAddressHierarchyTypeList(){
    	DWRAddressHierarchyService dwr = new DWRAddressHierarchyService();
    	String[] arr = dwr.getAddressHierarchyTypeList(0);
    	for(int i=0;i<arr.length;i++){
    		System.out.println(arr[i]);
    	}
    }
	/*
	
	public void testLayoutPortlet(){
		org.openmrs.layout.web.address.AddressSupport as = new AddressSupport();
		LayoutSupport layoutSupport = as.getInstance();
		LayoutTemplate layoutTemplate = layoutSupport.getDefaultLayoutTemplate();
		System.out.println(layoutTemplate);
		
	}
    
    public void testSizeofPerson(){
    	DWRAddressHierarchyService dwr = new DWRAddressHierarchyService();
    	System.out.println(dwr.getSizeOfPersonTable());
    }
    
    public void testLastRow(){
    	List y = ((AddressHierarchyService)Context.getService(AddressHierarchyService.class)).getLastRow();
    	
    	Object[] x = (Object[]) y.get(y.size()-1);
    	
    	for(int i=0;i<x.length;i++)
    		if(x[i] == null)
    			System.out.println("null");
    		else
    			System.out.println(x[i].toString());
    }
    
    public void testGetLocation(){
    	String[] x = ((AddressHierarchyService)Context.getService(AddressHierarchyService.class)).getLocation(237);
    	for(int i=0;i<x.length;i++)
    		System.out.println(x[i]);
    }
   
    /*
    public void testSqlupdate(){
    	
    	PersonAddress pa = new PersonAddress();
    	Person p = new Person();
    	User user = new User();
		user.setUserId(1);
		p.setPersonId(333);
		pa.setPersonAddressId(509);
		pa.setAddress1("");
		pa.setVoided(false);
		pa.setCreator(user);
		pa.setPerson(p);
		pa.setDateCreated(new Date());
		//((AddressHierarchyService)Context.getService(AddressHierarchyService.class)).modifyPersonAddress("update", pa);
		
    }*/
	@Override
	public Boolean useInMemoryDatabase()
	{
	       return false;
	}
	
	

}
