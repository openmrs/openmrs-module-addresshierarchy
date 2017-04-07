package org.openmrs.module.addresshierarchy.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
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

/**
 * Responsible for loading the address configuration appropriately
 */
public class AddressConfigurationLoader {

    private static final String LATEST_LOADED_ENTRIES = "latest_loaded_entries";

    protected static Log log = LogFactory.getLog(AddressConfigurationLoader.class);

    public static void loadAddressConfiguration() {
        File configFile = getConfigFile("addressConfiguration.xml");
        if (configFile.exists()) {
            AddressConfiguration addressConfiguration = readFromFile(configFile);
            installAddressTemplate(addressConfiguration.getAddressTemplate());
            installAddressHierarchyLevels(addressConfiguration.getAddressComponents());

            String existingChecksum = readLatestChecksum();
            String currentChecksum = computeChecksum(addressConfiguration.getAddressHierarchyFile());

            if (StringUtils.isNotBlank(existingChecksum) && StringUtils.isNotBlank(currentChecksum) && existingChecksum.equals(currentChecksum)) {
                log.info("Address Hierarchy Entry file is unchanged, not reloading entries");
            }
            else {
                log.info("Address Hierarchy Entry file has changed - reloading entries");
                installAddressHierarchyEntries(addressConfiguration.getAddressHierarchyFile());
                writeChecksum(currentChecksum);

                log.info("Entries loaded, re-initializing address cache");
                getService().initializeFullAddressCache();
            }
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
     * @return the configuration file with the passed name
     */
    public static File getConfigFile(String name) {
        StringBuilder path = new StringBuilder();
        path.append(OpenmrsUtil.getApplicationDataDirectory()).append(File.separator);
        path.append("configuration").append(File.separator).append("addresshierarchy").append(File.separator);
        path.append(name);
        return new File(path.toString());
    }

    /**
     * @return the hash of the latest loaded entry file
     */
    public static String readLatestChecksum() {
        try {
            File configFile = getConfigFile(LATEST_LOADED_ENTRIES);
            if (configFile.exists()) {
                return FileUtils.readFileToString(getConfigFile(LATEST_LOADED_ENTRIES), "UTF-8");
            }
        }
        catch (Exception e) {
            log.warn("Error reading latest checksum of entry file from " + LATEST_LOADED_ENTRIES, e);
        }
        return "";
    }

    /**
     * @return the hash of the latest loaded entry file
     */
    public static String computeChecksum(AddressHierarchyFile file) {
        try {
            File entryFile = getConfigFile(file.getFilename());
            long checksum = FileUtils.checksumCRC32(entryFile);
            return Long.toHexString(checksum);
        }
        catch (Exception e) {
            log.warn("Error computing checksum of " + file.getFilename(), e);
        }
        return "";
    }

    /**
     * Write the checksum to file
     */
    public static void writeChecksum(String checksum) {
        try {
            FileUtils.writeStringToFile(getConfigFile(LATEST_LOADED_ENTRIES), checksum, "UTF-8");
        }
        catch (Exception e) {
            log.warn("Error writing hash of address hierarchy entries to file", e);
        }
    }
    
    /**
     * Deletes the checksum file
     */
    public static void deleteChecksum() {
    	try {
			Files.deleteIfExists(getConfigFile(LATEST_LOADED_ENTRIES).toPath());
		} catch (IOException e) {
			log.warn("Error deleting hash of address hierarchy entries to file", e);
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
