package org.openmrs.module.addresshierarchy.i18n;

import java.util.List;
import java.util.Locale;

import org.openmrs.PersonAddress;
import org.openmrs.messagesource.MessageSourceService;
	
public interface I18nCache {
	
	/**
	 * Points to the locale that should be used to save into or fetch from the full address cache.
	 * @return The user locale when Ext I18N is present/enabled, the default locale otherwise.
	 */
	public Locale getLocaleForFullAddressCache();
	
	/**
	 * Resets the cache.
	 */
	public void reset();
	
	/**
	 * @return A boolean indicating whether the reverse translation caching is enabled.
	 */
	public boolean isEnabled();
	
	/**
	 * Enables/disables the reverse translation caching. Setting enabled to false also resets the
	 * cache.
	 * 
	 * @param enabled
	 */
	public void setEnabled(boolean enabled);
	
	/**
	 * This method translates a i18n message key based on the provided locale. From the outside if
	 * does the same as {@link MessageSourceService#getMessage(String, Object[], Locale)}, however
	 * while doing so it also caches the reverse translation.
	 * 
	 * @param key The i18n message key.
	 * @param locale The locale to use for the translation.
	 * @return The translation of the i18n message key.
	 */
	public String getMessage(String key, Locale locale);
	
	/**
	 * This method translates a i18n message key based on the context's locale. From the outside if
	 * does the same as {@link MessageSourceService#getMessage(String)}, however while doing so it
	 * also caches the reverse translation.
	 * 
	 * @param key The i18n message key.
	 * @return The translation of the i18n message key.
	 */
	public String getMessage(String key);
	
	/**
	 * @param message A translated expression in the current locale.
	 * @return The i18n message key.
	 */
	public String getMessageKey(String message);

	/**
	 * @return The ordered list of address fields that are subject to i18n.
	 */
	public List<String> getOrderedAddressFields();
	
	/**
	 * To initialize the cache.
	 */
	public void setOrderedAddressFields(List<String> orderedAddressFields);
	
	/**
	 * @param searchString
	 * @return Mathing i18n message keys matching the search string.
	 */
	public List<String> getMessageKeysByLikeName(String searchString);
	
	/**
	 * Reverse translates each field of a {@link PersonAddress} based on what is in the reverse
	 * translation cache.
	 * 
	 * @param address An address where some or all address fields are translated expressions in the
	 *            current locale.
	 * @return An address where all address fields are replaced with i18n messages keys, when
	 *         possible.
	 */
	public PersonAddress getI18nPersonAddress(PersonAddress address);
}
