package org.openmrs.module.addresshierarchy;

import java.util.List;

import junit.framework.Assert;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.openmrs.test.Verifies;

public class AddressHierarchyServiceTest extends BaseModuleContextSensitiveTest {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	protected static final String XML_DATASET_PACKAGE_PATH = "org/openmrs/module/addresshierarchy/include/addressHierarchy-dataset.xml";
	
	@Before
	public void setupDatabase() throws Exception {
		initializeInMemoryDatabase();
		authenticate();
		executeDataSet(XML_DATASET_PACKAGE_PATH);
	}
	
	@Test
	@Verifies(value = "should get hierarchy type by id", method = "getHierarchyType(int id)")
	public void getHierarchyType_shouldHierarchyTypeById() throws Exception {
		AddressHierarchyType type = Context.getService(AddressHierarchyService.class).getHierarchyType(1);
		
		Assert.assertEquals("Country", type.getName());
		Assert.assertEquals("country", type.getAddressField().getName());
		
	}
	
	@Test
	@Verifies(value = "should get top level hierarchy type", method = "getHierarchyType(int id)")
	public void getTopLevelAddressHierarchyType_shouldGetTopLevelAddressHierarchyType() throws Exception {
		AddressHierarchyType type = Context.getService(AddressHierarchyService.class).getTopLevelAddressHierarchyType();
		
		Assert.assertEquals("Country", type.getName());
		Assert.assertEquals("country", type.getAddressField().getName());
		
	}
	
	@Test
	@Verifies(value = "should get all address hierarchy type", method = "getAddressHierarchyTypes()")
	public void getAddressHierarchyTypes_shouldGetAllAddressHierarchyTypes() throws Exception {
		
		AddressHierarchyService ahService = Context.getService(AddressHierarchyService.class);
		
		List<AddressHierarchyType> types = ahService.getAddressHierarchyTypes();
		
		Assert.assertEquals(5, types.size());
			
		// make sure that the list returns the types in the proper order
		Assert.assertTrue(types.get(0) == (ahService.getHierarchyType(1)));
		Assert.assertTrue(types.get(1) == (ahService.getHierarchyType(4)));
		Assert.assertTrue(types.get(2) == (ahService.getHierarchyType(2)));
		Assert.assertTrue(types.get(3) == (ahService.getHierarchyType(5)));
		Assert.assertTrue(types.get(4) == (ahService.getHierarchyType(3)));
	}
	
	@Test
	@Verifies(value = "should find address hierarchy entry by id", method = "getAddressHierarchy(int)")
	public void searchHierarchy_shouldFindAddressHierarchyById() throws Exception {	
		AddressHierarchyService ahService = Context.getService(AddressHierarchyService.class);
		
		Assert.assertTrue(ahService.getAddressHierarchy(3).getName().equals("Maine"));
		Assert.assertTrue(ahService.getAddressHierarchy(5).getName().equals("Middlesex"));
		
	}
	
	@Test
	@Verifies(value = "should find appropriate address hierarchy entries", method = "searchHierarchy(String, int)")
	public void searchHierarchy_shouldFindAppropriateHierarchyEntries() throws Exception {
		AddressHierarchyService ahService = Context.getService(AddressHierarchyService.class);
		
		// first try a few basic searches
		Assert.assertTrue(ahService.searchHierarchy("Boston", -1).get(0).getName().equals("Boston"));
		Assert.assertTrue(ahService.searchHierarchy("Scituate", -1).get(0).getName().equals("Scituate"));
		
		// now make sure there is no match if the address hierarchy type id is wrong
		Assert.assertTrue(ahService.searchHierarchy("Boston", 4).size() == 0);
		
		// but make sure there is a match if the address hierarchy type id correct
		Assert.assertTrue(ahService.searchHierarchy("Scituate", 5).get(0).getName().equals("Scituate"));
		
		// test that exact/non-exact flag works properly 
		Assert.assertTrue(ahService.searchHierarchy("Bosto", -1, false).get(0).getName().equals("Boston"));
		Assert.assertTrue(ahService.searchHierarchy("Bosto", -1, true).size() == 0);
		
	}
	
}
