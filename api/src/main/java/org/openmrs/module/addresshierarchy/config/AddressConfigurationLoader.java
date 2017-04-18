package org.openmrs.module.addresshierarchy.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.GlobalProperty;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
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

	protected static final String ADDR_CONFIG_FILE_NAME = "addressConfiguration.xml";

	public static final String NOT_COMPUTABLE_CHECKSUM = "not_computable_checksum";
	public static final String NOT_READABLE_CHECKSUM = "not_readadble_checksum";

	public static final String CHECKSUM_FILE_EXT = "checksum";

	protected static Log log = LogFactory.getLog(AddressConfigurationLoader.class);

	/**
	 * @return The path to the configuration subdirectory.
	 */
	public static String getSubdirConfigPath() {
		return new StringBuilder()
				.append(OpenmrsUtil.getApplicationDataDirectory())
				.append("configuration").append(File.separator).append("addresshierarchy")
				.toString();
	}

	public static void loadAddressConfiguration() {

		final ConfigLoaderUtil configUtil = new ConfigLoaderUtil(getSubdirConfigPath());

		String xmlConfigFileName = ADDR_CONFIG_FILE_NAME;

		File configFile = configUtil.getFile(xmlConfigFileName);
		if (!configFile.exists()) {
			log.warn("Address hierarchy configuration file appears invalid, skipping the loading process: " + xmlConfigFileName);
			return;
		}

		String lastChecksum = "";
		String checksum = "";

		//
		// Processing the XML configuraton file
		//
		AddressConfiguration addressConfiguration = readFromFile(configFile);

		boolean forceReloadEntries = false;
		lastChecksum = configUtil.readLatestChecksum(xmlConfigFileName);
		checksum = configUtil.computeChecksum(xmlConfigFileName);

		if (checksum.equals(lastChecksum)) {
			log.info("Address hierarchy configuration file is unchanged, skipping it: " + xmlConfigFileName);
		}
		else {
			
			log.info("Address hierarchy configuration file has changed, reloading it: " + xmlConfigFileName);
			
			if (!isMatchableLevelConfig(addressConfiguration.getAddressComponents()) && !addressConfiguration.mustWipe()) {
				log.warn("The address hierarchy configuration was not loaded because of a mismatch between the exisiting and provided address hierarchy levels.");
				return;
			}

			if (addressConfiguration.mustWipe()) {
				log.warn("The exisiting address and address hierarchy configuration is being wiped.");
				wipeAddressHierarchy();
			}

			// Address template
			installAddressTemplate(addressConfiguration.getAddressTemplate());

			// Levels
			installAddressHierarchyLevels(addressConfiguration.getAddressComponents());

			configUtil.writeChecksum(xmlConfigFileName, checksum);
			forceReloadEntries = true;  // if anything upstream is changed, we force reload the address entries from CSV
		}

		//
		// Processing the CSV entries file
		//
		String csvEntriesFileName = addressConfiguration.getAddressHierarchyFile().getFilename();
		lastChecksum = configUtil.readLatestChecksum(csvEntriesFileName);
		checksum = configUtil.computeChecksum(csvEntriesFileName);

		if (checksum.equals(lastChecksum) && !forceReloadEntries) {
			log.info("Address hierarchy entries CSV file is unchanged, skipping it: " + csvEntriesFileName);
		}
		else {
			log.info("Address hierarchy entries CSV file has changed, reloading it: " + csvEntriesFileName);
			installAddressHierarchyEntries(addressConfiguration.getAddressHierarchyFile(), forceReloadEntries);
			configUtil.writeChecksum(csvEntriesFileName, checksum);

			log.info("Entries loaded, re-initializing address cache");
			getService().initializeFullAddressCache();
		}
	}

	/**
	 * Wipes the existing address and address hierarchy configuration.
	 * @note Use with care !
	 */
	public static void wipeAddressHierarchy() {

		getService().deleteAllAddressHierarchyEntries();
		
		while (getService().getAddressHierarchyLevelsCount() > 0) {
			getService().deleteAddressHierarchyLevel( getService().getBottomAddressHierarchyLevel() );
		}
	}

	/**
	 * Installs the configured address template by updating the global property
	 */
	public static void installAddressTemplate(Object addressTemplate) {
		try {
			log.info("Installing address template");
			String xml = Context.getSerializationService().getDefaultSerializer().serialize(addressTemplate);
			setGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_ADDRESS_TEMPLATE, xml);
		}
		catch (Exception e) {
			throw new IllegalArgumentException("Unable to serialize and save address template", e);
		}
	}

	public static boolean isMatchableLevelConfig(List<AddressComponent> addressComponents) {
		if (getService().getAddressHierarchyLevelsCount() == 0) {
			return true;
		}
		for (AddressComponent component : addressComponents) {
			if (getService().getAddressHierarchyLevelByAddressField(component.getField()) == null) {
				log.warn("The address field '" + component.getField() + "' provided by the configuration doesn't match any existing address level.");
				return false;
			}
		}
		return true;
	}

	/**
	 * Install the configured address hierarchy levels
	 * Currently we only install the levels if they haven't been installed; no built-in way to edit anything other than "required" at this point
	 */
	public static void installAddressHierarchyLevels(List<AddressComponent> addressComponents) {

		if (getService().getAddressHierarchyLevelsCount() == 0) {

			log.info("Installing address hierarchy levels");
			AddressHierarchyLevel lastLevel = null;
			for (AddressComponent component : addressComponents) {
				AddressHierarchyLevel level = new AddressHierarchyLevel();
				level.setName(component.getNameMapping());
				level.setAddressField(component.getField());
				level.setRequired(component.isRequiredInHierarchy());
				level.setParent(lastLevel);
				getService().saveAddressHierarchyLevel(level);
				lastLevel = level;
			}
		}
		else {

			log.info("Updating address hierarchy levels");

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
	public static void installAddressHierarchyEntries(AddressHierarchyFile file, boolean deleteEntries) {
		log.info("Installing address hierarchy entries");
		if (deleteEntries) {
			log.warn("Deleting existing address hierarchy entries");
			getService().deleteAllAddressHierarchyEntries();
		}
		
		InputStream is = null;
		try {
			is = new FileInputStream( ConfigLoaderUtil.getFile(getSubdirConfigPath(), file.getFilename()) );
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