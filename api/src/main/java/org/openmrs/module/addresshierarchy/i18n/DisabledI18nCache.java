package org.openmrs.module.addresshierarchy.i18n;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.openmrs.PersonAddress;
import org.openmrs.annotation.OpenmrsProfile;
import org.openmrs.util.LocaleUtility;

/**
 * This is an disabled/inert implementation of {@link I18nCache}
 * used instead of null instances when the i18n caching is inactive.
 */
@OpenmrsProfile(modules = {"!exti18n"})
public class DisabledI18nCache implements I18nCache {

	@Override
	public Locale getLocaleForFullAddressCache() {
		return LocaleUtility.getDefaultLocale();
	}
	
	@Override
	public void reset() {}

	@Override
	public boolean isEnabled() {
		return false;
	}

	@Override
	public void setEnabled(boolean enabled) {}

	@Override
	public String getMessage(String key, Locale locale) {
		return key;
	}

	@Override
	public String getMessage(String key) {
		return key;
	}

	@Override
	public String getMessageKey(String message) {
		return message;
	}

	@Override
	public List<String> getOrderedAddressFields() {
		return Collections.<String>emptyList();
	}

	@Override
	public void setOrderedAddressFields(List<String> orderedAddressFields) {}

	@Override
	public List<String> getMessageKeysByLikeName(String searchString) {
		return Collections.<String>emptyList();
	}

	@Override
	public PersonAddress getI18nPersonAddress(PersonAddress address) {
		return address;
	}
}
