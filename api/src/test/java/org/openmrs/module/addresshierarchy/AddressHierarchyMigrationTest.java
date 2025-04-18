package org.openmrs.module.addresshierarchy;

import junit.framework.Assert;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.api.context.Context;
import org.openmrs.module.addresshierarchy.service.AddressHierarchyService;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.openmrs.test.Verifies;

import java.util.List;


public class AddressHierarchyMigrationTest extends BaseModuleContextSensitiveTest {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	protected static final String XML_DATASET_PACKAGE_PATH = "org/openmrs/module/addresshierarchy/include/addressHierarchy-migration-dataset.xml";
	
	@Before
	public void setupDatabase() throws Exception {
		initializeInMemoryDatabase();
		authenticate();
		executeDataSet(XML_DATASET_PACKAGE_PATH);
	}
	
	@Test
	@Verifies(value = "should assign properly parent to address hierarchy levels", method = "setAddressHierarchyLevelParents()")
	public void setAddressHierarchyLevelParents_shouldSetAddressHierarchyLevelParents() throws Exception {

		AddressHierarchyService ahService = Context.getService(AddressHierarchyService.class);

		ahService.setAddressHierarchyLevelParents();
		
		List<AddressHierarchyLevel> levels = ahService.getAddressHierarchyLevels();
	
		// (note that street should have been removed because it has no entries, so there only should be 6 levels) 
		Assert.assertEquals(6, levels.size());
		
		// make sure that the list returned contains all the level
		Assert.assertEquals(null, ahService.getAddressHierarchyLevel(1).getParent());
		Assert.assertEquals(4, Integer.valueOf(ahService.getAddressHierarchyLevel(2).getParent().getId()).intValue());
		Assert.assertEquals(5, Integer.valueOf(ahService.getAddressHierarchyLevel(3).getParent().getId()).intValue());
		Assert.assertEquals(7, Integer.valueOf(ahService.getAddressHierarchyLevel(4).getParent().getId()).intValue());
		Assert.assertEquals(2, Integer.valueOf(ahService.getAddressHierarchyLevel(5).getParent().getId()).intValue());
		Assert.assertEquals(1, Integer.valueOf(ahService.getAddressHierarchyLevel(7).getParent().getId()).intValue());
	
	}
}
