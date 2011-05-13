package org.openmrs.module.addresshierarchy;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.openmrs.PersonAddress;
import org.openmrs.module.addresshierarchy.util.AddressHierarchyUtil;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.openmrs.test.Verifies;


public class AddressHierarchyUtilTest extends BaseModuleContextSensitiveTest {

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
	@Verifies(value = "should do case-insensitive compare on list of strings", method = "caseInsensitiveStringContains")
	public void caseInsensitiveStringContains_shouldDoCaseInsensitiveCompare() {
		List<String> sampleList = new ArrayList<String>();
		sampleList.add("Apple");
		sampleList.add("Pear");
		
		Assert.assertTrue(AddressHierarchyUtil.caseInsensitiveStringContains(sampleList, "Apple"));
		Assert.assertTrue(AddressHierarchyUtil.caseInsensitiveStringContains(sampleList, "Pear"));
		Assert.assertTrue(AddressHierarchyUtil.caseInsensitiveStringContains(sampleList, "aPpLe"));
		Assert.assertTrue(!AddressHierarchyUtil.caseInsensitiveStringContains(sampleList, "Orange"));
	}
	
}
