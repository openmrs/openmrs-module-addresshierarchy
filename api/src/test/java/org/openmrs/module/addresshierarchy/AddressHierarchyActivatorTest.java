package org.openmrs.module.addresshierarchy;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openmrs.GlobalProperty;
import org.openmrs.api.context.Context;
import org.openmrs.module.addresshierarchy.config.AddressConfigurationLoader;
import org.openmrs.module.addresshierarchy.config.ConfigDirUtil;
import org.openmrs.module.addresshierarchy.service.AddressHierarchyService;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.openmrs.test.Verifies;
import org.openmrs.util.OpenmrsConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

@DirtiesContext
public class AddressHierarchyActivatorTest extends BaseModuleContextSensitiveTest {

  private static String APP_DATA_TEST_DIRECTORY = "testAppDataDir";

  @Autowired
  private AddressHierarchyActivator activator;

  @Before
  public void setup() {

    Context.getAdministrationService().saveGlobalProperty(new GlobalProperty(AddressHierarchyConstants.GLOBAL_PROP_INITIALIZE_ADDRESS_HIERARCHY_CACHE_ON_STARTUP, "true"));

    setAppDataDirPath(APP_DATA_TEST_DIRECTORY);

    Assert.assertTrue(CollectionUtils.isEmpty(Context.getService(AddressHierarchyService.class).getAddressHierarchyLevels()));
  }
  
  private void setAppDataDirPath(String strPath) {
    String path = getClass().getClassLoader().getResource(strPath).getPath() + File.separator;

    OpenmrsConstants.APPLICATION_DATA_DIRECTORY = path; // The 1.9 way
    Properties prop = new Properties();
    prop.setProperty(OpenmrsConstants.APPLICATION_DATA_DIRECTORY_RUNTIME_PROPERTY, path); // The 1.10+ way
    Context.setRuntimeProperties(prop);
  }

  @Test
  @Verifies(value = "should load new address hierarchy configuration from configuration/addresshierarchy", method = "started()")
  public void started_shouldLoadAddressHierachyConfig() {

    // Setup
    AddressHierarchyService ahs = Context.getService(AddressHierarchyService.class);
    ConfigDirUtil.deleteChecksums(AddressConfigurationLoader.getChecksumsPath(), true);

    // Replay
    activator.started();

    // Verif that the test CSV was indeed loaded
    assertConfigurationAsExpected();
  }
  
  private void assertConfigurationAsExpected() {
    AddressHierarchyService ahs = Context.getService(AddressHierarchyService.class);
    AddressHierarchyLevel level;
    List<AddressHierarchyEntry> entries;

    level = ahs.getAddressHierarchyLevelByAddressField(AddressField.NEIGHBORHOOD_CELL);
    entries = ahs.getAddressHierarchyEntriesByLevel(level);
    Set<String> entryNames = new HashSet<String>();
    for (AddressHierarchyEntry entry : entries) {
      entryNames.add(entry.getName());
    }
    Assert.assertTrue(entryNames.contains("Beacon Hill"));
    Assert.assertTrue(entryNames.contains("Jamaica Plain"));

    level = ahs.getAddressHierarchyLevelByAddressField(AddressField.STATE_PROVINCE);
    entries = ahs.getAddressHierarchyEntriesByLevel(level);
    Assert.assertEquals(1, entries.size());
    Assert.assertEquals("Massachusetts", entries.get(0).getName());

    // All levels from the XML should have been created
    Assert.assertNotNull(ahs.getAddressHierarchyLevelByAddressField(AddressField.COUNTRY));
    Assert.assertNotNull(ahs.getAddressHierarchyLevelByAddressField(AddressField.COUNTY_DISTRICT));
    Assert.assertNotNull(ahs.getAddressHierarchyLevelByAddressField(AddressField.CITY_VILLAGE));

    // Other levels should not be there
    Assert.assertNull(ahs.getAddressHierarchyLevelByAddressField(AddressField.ADDRESS_1));
    Assert.assertNull(ahs.getAddressHierarchyLevelByAddressField(AddressField.ADDRESS_2));
    Assert.assertNull(ahs.getAddressHierarchyLevelByAddressField(AddressField.ADDRESS_3));
  }

  @Test
  @Verifies(value = "should not load again an address hierarchy configuration from configuration/addresshierarchy", method = "started()")
  public void started_shouldNotReLoadAddressHierachyConfig() {

    // Setup
    AddressHierarchyService ahs = Context.getService(AddressHierarchyService.class);
    ConfigDirUtil.deleteChecksums(AddressConfigurationLoader.getChecksumsPath(), true);

    // Replay
    activator.started();

    // Editing and re-saving an entry
    AddressHierarchyLevel level = ahs.getAddressHierarchyLevelByAddressField(AddressField.NEIGHBORHOOD_CELL);
    List<AddressHierarchyEntry> entries = ahs.getAddressHierarchyEntriesByLevelAndName(level, "Jamaica Plain");
    Assert.assertEquals(1, entries.size());
    AddressHierarchyEntry entry = entries.get(0);
    entry.setName("Havana Plain");
    String uuid = entry.getUuid();
    ahs.saveAddressHierarchyEntry(entry);

    // Reloading the activator without deleting the checksum file
    activator.started();

    // Verifying that the edited entry hasn't been touched
    entry = ahs.getAddressHierarchyEntryByUuid(uuid);
    Assert.assertEquals("Havana Plain", entry.getName());
  }

  @Test
  @Verifies(value = "should keep existing entries when wipe is set to false", method = "started()")
  public void started_shouldNotWipeExistingEntries() {

    // Setup
    AddressHierarchyService ahs = Context.getService(AddressHierarchyService.class);
    ConfigDirUtil.deleteChecksums(AddressConfigurationLoader.getChecksumsPath(), true);
    activator.started();

    // Adding entries that are not in the import CSV
    List<AddressHierarchyEntry> entries;
    entries = ahs.getAddressHierarchyEntriesByLevelAndName(ahs.getAddressHierarchyLevelByAddressField(AddressField.COUNTY_DISTRICT), "Suffolk County");
    Assert.assertEquals(entries.size(), 1);
    AddressHierarchyEntry suffolkCounty = entries.get(0);

    AddressHierarchyEntry winthrop = new AddressHierarchyEntry();
    winthrop.setName("Winthrop");
    winthrop.setLevel( ahs.getAddressHierarchyLevelByAddressField(AddressField.CITY_VILLAGE) );
    winthrop.setParent(suffolkCounty);
    ahs.saveAddressHierarchyEntry(winthrop);

    AddressHierarchyEntry pointShirley = new AddressHierarchyEntry();
    pointShirley.setName("Point Shirley");
    pointShirley.setLevel( ahs.getAddressHierarchyLevelByAddressField(AddressField.NEIGHBORHOOD_CELL) );
    pointShirley.setParent(winthrop);
    ahs.saveAddressHierarchyEntry(pointShirley);

    // Replay
    activator.started();

    // Verifying that CSV and additional data are there
    assertConfigurationAsExpected();
    
    entries = ahs.getAddressHierarchyEntriesByLevelAndName(ahs.getAddressHierarchyLevelByAddressField(AddressField.CITY_VILLAGE), "Winthrop");
    Assert.assertEquals(entries.size(), 1);
    winthrop = entries.get(0);
    Assert.assertEquals("Winthrop", winthrop.getName());
    
    entries = ahs.getAddressHierarchyEntriesByLevelAndName(ahs.getAddressHierarchyLevelByAddressField(AddressField.NEIGHBORHOOD_CELL), "Point Shirley");
    Assert.assertEquals(entries.size(), 1);
    pointShirley = entries.get(0);
    Assert.assertEquals("Point Shirley", pointShirley.getName());
  }
  
  @Ignore
  @Test
  @Verifies(value = "should wipe existing entries when wipe is set to true", method = "started()")
  public void started_shouldWipeExistingEntries() {

    // Setup
    AddressHierarchyService ahs = Context.getService(AddressHierarchyService.class);
    ConfigDirUtil.deleteChecksums(AddressConfigurationLoader.getChecksumsPath(), true);
    
    //
    // Going through CSV 1 
    //

    // Replay
    activator.started();

    // Verif
    assertConfigurationAsExpected();

    //
    // Going through CSV 2 
    //
    
    // Setup
    setAppDataDirPath("testAppDataDir2");
    ConfigDirUtil.deleteChecksums(AddressConfigurationLoader.getChecksumsPath(), true);
    
    // Replay
    activator.started();
    
    // Verif
    AddressHierarchyLevel level;
    List<AddressHierarchyEntry> entries;

    level = ahs.getAddressHierarchyLevelByAddressField(AddressField.NEIGHBORHOOD_CELL);
    entries = ahs.getAddressHierarchyEntriesByLevel(level);
    Set<String> entryNames = new HashSet<String>();
    for (AddressHierarchyEntry entry : entries) {
      entryNames.add(entry.getName());
    }
    Assert.assertFalse(entryNames.contains("Beacon Hill"));
    Assert.assertFalse(entryNames.contains("Jamaica Plain"));
    Assert.assertTrue(entryNames.contains("Auburndale"));
    Assert.assertTrue(entryNames.contains("Chestnut Hill"));
  }
}