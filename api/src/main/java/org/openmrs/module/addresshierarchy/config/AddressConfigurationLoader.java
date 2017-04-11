package org.openmrs.module.addresshierarchy.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.GlobalProperty;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.layout.web.address.AddressTemplate;
import org.openmrs.module.addresshierarchy.AddressHierarchyLevel;
import org.openmrs.module.addresshierarchy.service.AddressHierarchyService;
import org.openmrs.module.addresshierarchy.util.AddressHierarchyImportUtil;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.OpenmrsUtil;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

import liquibase.util.file.FilenameUtils;

/**
 * Responsible for loading the address configuration appropriately
 */
public class AddressConfigurationLoader {

  protected static final String ADDR_CONFIG_FILE_NAME = "addressConfiguration.xml";

  public static final String NOT_COMPUTABLE_CHECKSUM = "not_computable_checksum";
  public static final String NOT_READABLE_CHECKSUM = "not_readadble_checksum";

  protected static final String CHECKSUM_FILE_EXT = "crc32";

  protected static Log log = LogFactory.getLog(AddressConfigurationLoader.class);

  public static String getConfigDirPath() {
    return new StringBuilder()
      .append(OpenmrsUtil.getApplicationDataDirectory())
      .append("configuration").append(File.separator).append("addresshierarchy")
      .toString();
  }
  
  public static void loadAddressConfiguration() {

    String xmlConfigFileName = ADDR_CONFIG_FILE_NAME;
    
    File configFile = getConfigFile(xmlConfigFileName);
    if (!configFile.exists()) {
      log.warn("Address hierarchy configuration file appears invalid, skipping the loading process: " + xmlConfigFileName);
    }

    String lastChecksum = "";
    String checksum = "";
    
    //
    // Processing the XML configuraton file
    //
    AddressConfiguration addressConfiguration = readFromFile(configFile);

    boolean forceReloadEntries = false;
    lastChecksum = readLatestChecksum(xmlConfigFileName);
    checksum = computeChecksum(xmlConfigFileName);
    
    if (checksum.equals(lastChecksum)) {
      log.info("Address hierarchy configuration file is unchanged, skipping it: " + xmlConfigFileName);
    }
    else {
      log.info("Address hierarchy configuration file has changed, reloading it: " + xmlConfigFileName);
      installAddressTemplate(addressConfiguration.getAddressTemplate());
      installAddressHierarchyLevels(addressConfiguration.getAddressComponents());
      writeChecksum(xmlConfigFileName, checksum);
      forceReloadEntries = true;  // if anything upstream is changed, we force reload the address entries from CSV
    }

    //
    // Processing the CSV entries file
    //
    String csvEntriesFileName = addressConfiguration.getAddressHierarchyFile().getFilename();
    lastChecksum = readLatestChecksum(csvEntriesFileName);
    checksum = computeChecksum(csvEntriesFileName);

    if (checksum.equals(lastChecksum) && !forceReloadEntries) {
      log.info("Address hierarchy entries CSV file is unchanged, skipping it: " + csvEntriesFileName);
    }
    else {
      log.info("Address hierarchy entries CSV file has changed, reloading it: " + csvEntriesFileName);
      installAddressHierarchyEntries(addressConfiguration.getAddressHierarchyFile());
      writeChecksum(csvEntriesFileName, checksum);

      log.info("Entries loaded, re-initializing address cache");
      getService().initializeFullAddressCache();
    }
  }

  /**
   * Installs the configured address template by updating the global property
   */
  public static void installAddressTemplate(AddressTemplate template) {
    try {
      log.info("Installing Address Template");
      String xml = Context.getSerializationService().getDefaultSerializer().serialize(template);
      setGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_ADDRESS_TEMPLATE, xml);
    }
    catch (Exception e) {
      throw new IllegalArgumentException("Unable to serialize and save address template", e);
    }
  }

  /**
   * Install the configured address hierarchy levels
   * Currently we only install the levels if they haven't been installed; no built-in way to edit anything other than "required" at this point
   */
  public static void installAddressHierarchyLevels(List<AddressComponent> addressComponents) {
    int numberOfLevels = getService().getAddressHierarchyLevelsCount();
    if (numberOfLevels == 0) {
      log.info("Installing Address Hierarchy Levels");
      AddressHierarchyLevel lastLevel = null;
      for (AddressComponent component : addressComponents) {
        AddressHierarchyLevel level = new AddressHierarchyLevel();
        String name = component.getField().toString() + " | " + component.getNameMapping();
        level.setName(name);
        level.setAddressField(component.getField());
        level.setRequired(component.isRequiredInHierarchy());
        level.setParent(lastLevel);
        getService().saveAddressHierarchyLevel(level);
        lastLevel = level;
      }
    }
    else {
      log.info("Updating Address Hierarchy Levels");
      for (AddressComponent component : addressComponents) {
        AddressHierarchyLevel level = getService().getAddressHierarchyLevelByAddressField(component.getField());
        level.setRequired(component.isRequiredInHierarchy());
        getService().saveAddressHierarchyLevel(level);
      }
    }
  }

  /**
   * Install the address hierarchy entries as defined by the AddressHierarchyFile configuration
   */
  public static void installAddressHierarchyEntries(AddressHierarchyFile file) {
    log.info("Installing Address Hierarchy Entries");
    Context.getService(AddressHierarchyService.class).deleteAllAddressHierarchyEntries();
    InputStream is = null;
    try {
      is = new FileInputStream(getConfigFile(file.getFilename()));
      AddressHierarchyImportUtil.importAddressHierarchyFile(is, file.getEntryDelimiter(), file.getIdentifierDelimiter());
    }
    catch (Exception e) {
      throw new IllegalArgumentException("Unable to import address hierarchy from file", e);
    }
    finally {
      IOUtils.closeQuietly(is);
    }
  }

  /**
   * Reads from a String representing the address configuration into an AddressConfiguration object
   */
  public static AddressConfiguration readFromFile(File file) {
    try {
      String configuration = FileUtils.readFileToString(file, "UTF-8");
      return readFromString(configuration);
    }
    catch (IOException e) {
      throw new IllegalArgumentException("Unable to load address configuration from configuration file.  Please check the format of this file", e);
    }
  }

  /**
   * Reads from a String representing the address configuration into an AddressConfiguration object
   */
  public static AddressConfiguration readFromString(String configuration) {
    try {
      return (AddressConfiguration) getSerializer().fromXML(configuration);
    }
    catch (Exception e) {
      throw new IllegalArgumentException("Unable to load address configuration from configuration file.  Please check the format of this file", e);
    }
  }

  /**
   * Writes a serialized String representing the address configuration from an AddressConfiguration object
   */
  public static String writeToString(AddressConfiguration configuration) {
    return getSerializer().toXML(configuration);
  }

  /**
   * @return the serializer instance used to load configuration from file
   */
  public static XStream getSerializer() {
    XStream xs = new XStream(new DomDriver());
    xs.alias("addressConfiguration", AddressConfiguration.class);
    xs.alias("addressComponent", AddressComponent.class);
    xs.alias("addressHierarchyFile", AddressHierarchyFile.class);
    return xs;
  }
  
  /**
   * Fetches the config. file based on its name, according to the conventions
   * regarding the location of configuration files.
   * 
   * @param configFileName The config file name, eg. "addressConfiguration.xml"
   * @return The {@link File} instance.
   */
  public static File getConfigFile(String configFileName) {
    StringBuilder path = new StringBuilder(getConfigDirPath());
    path.append(File.separator).append(configFileName);
    return new File(path.toString());
  }
  
  /**
   * @param configFileName The config file name, eg. "addressConfiguration.xml"
   * @return The checksum fime name, eg. "addressConfiguration.crc32"
   */
  public static String getChecksumFileName(String configFileName) {
    // addressConfiguration.xml -> addressConfiguration.crc32 
    return FilenameUtils.getBaseName(configFileName) + "." + CHECKSUM_FILE_EXT;
  }
  
  /**
   * 
   * @param configFileName The config file name, eg. "addressConfiguration.xml"
   * @return The hash of the latest loaded config. file.
   */
  public static String readLatestChecksum(String configFileName) {

    String checksum = NOT_READABLE_CHECKSUM;

    final String hashFileName = getChecksumFileName(configFileName);
    try {
      final File hashFile = getConfigFile(hashFileName);
      if (hashFile.exists()) {
        checksum = FileUtils.readFileToString(hashFile, "UTF-8");
      }
    }
    catch (Exception e) {
      log.warn("Error reading latest checksum of entry file from " + hashFileName, e);
    }
    return checksum;
  }

  /**
   * Compute the checksum of a configuration file.
   * 
   * @param configFileName The config. file name.
   * @return The hash of the file.
   */
  public static String computeChecksum(String configFileName) {
    
    String checksum = NOT_COMPUTABLE_CHECKSUM;
    
    File file = getConfigFile(configFileName);
    if (file.exists()) {
      try {
        checksum = Long.toHexString( FileUtils.checksumCRC32(file) );
      }
      catch (Exception e) {
        log.warn("Error computing checksum of config. file: " + configFileName, e);
      }
    }
    return checksum;
  }

  /**
   * Writes the the checksum of a config. file to the corresponding .crc32 file.
   * 
   * @param configFileName The config. file name.
   * @param checksum The hash of the config. file.
   */
  public static void writeChecksum(String configFileName, String checksum) {
    
    deleteChecksum(configFileName);
    
    if (NOT_COMPUTABLE_CHECKSUM.equals(checksum)) {
      return;
    }
    
    final String hashFileName = getChecksumFileName(configFileName);
    try {
      FileUtils.writeStringToFile(getConfigFile(hashFileName), checksum, "UTF-8");
    }
    catch (Exception e) {
      log.error("Error writing hash ('" + checksum + "') of configuration file to: " + hashFileName, e);
    }
  }

  /**
   * Deletes the the .crc32 checksum file of a config. file.
   * 
   * @param configFileName The config. file name.
   */
  public static void deleteChecksum(String configFileName) {
    
    final String hashFileName = getChecksumFileName(configFileName);
    try {
      Files.deleteIfExists(getConfigFile(hashFileName).toPath());
    } catch (IOException e) {
      log.warn("Error deleting hash of configuration file: " + configFileName, e);
    }
  }
  
  /**
   * Removes all the checksum .crc32 files from the configuration directory.
   */
  public static void deleteChecksums() {
    final File[] hashFiles = new File(getConfigDirPath()).listFiles(new FilenameFilter() {
      
      @Override
      public boolean accept(File dir, String name) {
        String ext = FilenameUtils.getExtension(name);
        if (StringUtils.isEmpty(ext)) { // to be safe, ext can only be null if name is null
          return false;
        }
        if (ext.equals(CHECKSUM_FILE_EXT)) {
          return true; // filtering only checksum files based on their extension
        }
        return false;
      }
    });
    
    for(File file : hashFiles) {
      file.delete();
    }
  }

  /**
   * Update the global property with the given name to the given value, creating it if it doesn't exist
   */
  public static void setGlobalProperty(String propertyName, String propertyValue) {
    AdministrationService administrationService = Context.getAdministrationService();
    GlobalProperty gp = administrationService.getGlobalPropertyObject(propertyName);
    if (gp == null) {
      gp = new GlobalProperty(propertyName);
    }
    gp.setPropertyValue(propertyValue);
    administrationService.saveGlobalProperty(gp);
  }

  public static AddressHierarchyService getService() {
    return Context.getService(AddressHierarchyService.class);
  }
}