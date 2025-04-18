package org.openmrs.module.addresshierarchy;

import junit.framework.Assert;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.PersonAddress;
import org.openmrs.api.context.Context;
import org.openmrs.module.addresshierarchy.service.AddressHierarchyService;
import org.openmrs.module.addresshierarchy.util.AddressHierarchyUtil;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.openmrs.test.Verifies;


public class AddressHierarchyUtilTest extends BaseModuleContextSensitiveTest {

	protected final Log log = LogFactory.getLog(getClass());
	
	protected static final String XML_DATASET_PACKAGE_PATH = "org/openmrs/module/addresshierarchy/include/addressHierarchy-dataset.xml";
	
	@Before
	public void setupDatabase() throws Exception {
		initializeInMemoryDatabase();
		authenticate();
		executeDataSet(XML_DATASET_PACKAGE_PATH);
	}
	
	@Test
	@Verifies(value = "should fetch a value off a PersonAddress field", method = "getAddressFieldValue()")
	public void getAddressFieldValue_shouldFetchAddressFieldValue() throws Exception {
		PersonAddress address = new PersonAddress();
		address.setCountry("United States");
		Assert.assertEquals("United States", AddressHierarchyUtil.getAddressFieldValue(address, AddressField.COUNTRY));
	}
	
	@Test
	@Verifies(value = "should set a value on a PersonAddress field", method = "getAddressFieldValue()")
	public void setAddressFieldValue_shouldSetAddressFieldValue() throws Exception {
		PersonAddress address = new PersonAddress();
		AddressHierarchyUtil.setAddressFieldValue(address, AddressField.COUNTRY, "United States");
		Assert.assertEquals("United States", address.getCountry());
	}
	
	@Test
	@Verifies(value = "should return false if not a descendant", method = "isDescendantOf()")
	public void isDescendantOf_shouldReturnFalseIfNotDescendant() {
		AddressHierarchyEntry descendant = Context.getService(AddressHierarchyService.class).getAddressHierarchyEntry(12);
		AddressHierarchyEntry ancestor = Context.getService(AddressHierarchyService.class).getAddressHierarchyEntry(3);
		Assert.assertFalse(AddressHierarchyUtil.isDescendantOf(descendant, ancestor));
	}
	
	@Test
	@Verifies(value = "should return true if descendant", method = "isDescendantOf()")
	public void isDescendantOf_shouldReturnTrueIfNotDescendant() {
		AddressHierarchyEntry descendant = Context.getService(AddressHierarchyService.class).getAddressHierarchyEntry(7);
		AddressHierarchyEntry ancestor = Context.getService(AddressHierarchyService.class).getAddressHierarchyEntry(2);
		Assert.assertTrue(AddressHierarchyUtil.isDescendantOf(descendant, ancestor));
	}
	
}
