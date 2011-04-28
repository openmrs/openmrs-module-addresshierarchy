package org.openmrs.module.addresshierarchy;

import java.util.List;

import junit.framework.Assert;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.api.context.Context;
import org.openmrs.module.addresshierarchy.service.AddressHierarchyService;
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
	@Verifies(value = "should get hierarchy type by id", method = "getAddressHierarchyType(int id)")
	public void getHierarchyType_shouldHierarchyTypeById() throws Exception {
		AddressHierarchyType type = Context.getService(AddressHierarchyService.class).getAddressHierarchyType(1);
		
		Assert.assertEquals("Country", type.getName());
		Assert.assertEquals("country", type.getAddressField().getName());
		
	}
	
	@Test
	@Verifies(value = "should get top level hierarchy type", method = "getTopLevelAddressHierarchyType(int id)")
	public void getTopLevelAddressHierarchyType_shouldGetTopLevelAddressHierarchyType() throws Exception {
		AddressHierarchyType type = Context.getService(AddressHierarchyService.class).getTopLevelAddressHierarchyType();
		
		Assert.assertEquals("Country", type.getName());
		Assert.assertEquals("country", type.getAddressField().getName());
		
	}
	
	@Test
	@Verifies(value = "should get bottom level hierarchy type", method = "getBottomLevelHierarchyType(int id)")
	public void getBottomLevelAddressHierarchyType_shouldGetBottomLevelAddressHierarchyType() throws Exception {
		AddressHierarchyType type = Context.getService(AddressHierarchyService.class).getBottomLevelAddressHierarchyType();
		
		Assert.assertEquals("Neighborhood", type.getName());
		Assert.assertEquals("region", type.getAddressField().getName());
		
	}
	
	@Test
	@Verifies(value = "should get all address hierarchy types", method = "getAddressHierarchyTypes()")
	public void getAddressHierarchyTypes_shouldGetAllAddressHierarchyTypes() throws Exception {
		
		AddressHierarchyService ahService = Context.getService(AddressHierarchyService.class);
		
		List<AddressHierarchyType> types = ahService.getAddressHierarchyTypes();
		
		Assert.assertEquals(5, types.size());
			
		// make sure that the list returned contains all the types
		Assert.assertTrue(types.contains(ahService.getAddressHierarchyType(1)));
		Assert.assertTrue(types.contains(ahService.getAddressHierarchyType(4)));
		Assert.assertTrue(types.contains(ahService.getAddressHierarchyType(2)));
		Assert.assertTrue(types.contains(ahService.getAddressHierarchyType(5)));
		Assert.assertTrue(types.contains(ahService.getAddressHierarchyType(3)));
	}
	
	@Test
	@Verifies(value = "should get all address hierarchy types in order", method = "getOrderedAddressHierarchyTypes()")
	public void getOrderedAddressHierarchyTypes_shouldGetAllAddressHierarchyTypesInOrder() throws Exception {
		
		AddressHierarchyService ahService = Context.getService(AddressHierarchyService.class);
		
		List<AddressHierarchyType> types = ahService.getOrderedAddressHierarchyTypes();
		
		Assert.assertEquals(5, types.size());
			
		// make sure that the list returns the types in the proper order
		Assert.assertTrue(types.get(0) == (ahService.getAddressHierarchyType(1)));
		Assert.assertTrue(types.get(1) == (ahService.getAddressHierarchyType(4)));
		Assert.assertTrue(types.get(2) == (ahService.getAddressHierarchyType(2)));
		Assert.assertTrue(types.get(3) == (ahService.getAddressHierarchyType(5)));
		Assert.assertTrue(types.get(4) == (ahService.getAddressHierarchyType(3)));
	}
	
	@Test
	@Verifies(value = "should find address hierarchy entry by id", method = "getAddressHierarchy(int)")
	public void searchHierarchy_shouldFindAddressHierarchyById() throws Exception {	
		AddressHierarchyService ahService = Context.getService(AddressHierarchyService.class);
		
		Assert.assertTrue(ahService.getAddressHierarchyEntry(3).getName().equals("Maine"));
		Assert.assertTrue(ahService.getAddressHierarchyEntry(5).getName().equals("Middlesex"));
		
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
