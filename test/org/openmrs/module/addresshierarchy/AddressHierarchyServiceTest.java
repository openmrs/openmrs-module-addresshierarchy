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
		
	}
	
	@Test
	@Verifies(value = "should get all address hierarchy type", method = "getAddressHierarchyTypes()")
	public void getAddressHierarchyTypes_shouldGetAllAddressHierarchyTypes() throws Exception {
		
		AddressHierarchyService ahService = Context.getService(AddressHierarchyService.class);
		
		List<AddressHierarchyType> types = ahService.getAddressHierarchyTypes();
		
		Assert.assertEquals(5, types.size());
		
		Assert.assertTrue(types.contains(ahService.getHierarchyType(1)));
		Assert.assertTrue(types.contains(ahService.getHierarchyType(2)));
		Assert.assertTrue(types.contains(ahService.getHierarchyType(3)));
		Assert.assertTrue(types.contains(ahService.getHierarchyType(4)));
		Assert.assertTrue(types.contains(ahService.getHierarchyType(5)));
		
	}
	
}
