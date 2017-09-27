package org.openmrs.module.addresshierarchy.i18n;

import java.util.List;
import java.util.Locale;

import org.openmrs.PersonAddress;
import org.openmrs.annotation.OpenmrsProfile;
import org.openmrs.api.context.Context;
import org.openmrs.module.exti18n.ExtI18nConstants;
import org.openmrs.module.exti18n.api.AddressHierarchyI18nCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

@OpenmrsProfile(modules = {"exti18n:*"})
public class I18nCacheImpl implements I18nCache {

	@Override
	public Locale getLocaleForFullAddressCache() {
		return Context.getLocale();
	}
	
	@Autowired
	@Qualifier(ExtI18nConstants.COMPONENT_AH_REVI18N)
	private AddressHierarchyI18nCache cache;
	
	@Override
	public void reset() {
		cache.reset();
	}

	@Override
	public boolean isEnabled() {
		return cache.isEnabled();
	}

	@Override
	public void setEnabled(boolean enabled) {
		cache.setEnabled(enabled);
	}

	@Override
	public String getMessage(String key, Locale locale) {
		return cache.getMessage(key, locale);
	}

	@Override
	public String getMessage(String key) {
		return cache.getMessage(key);
	}

	@Override
	public String getMessageKey(String message) {
		return cache.getMessageKey(message);
	}

	@Override
	public List<String> getOrderedAddressFields() {
		return cache.getOrderedAddressFields();
	}

	@Override
	public void setOrderedAddressFields(List<String> orderedAddressFields) {
		cache.setOrderedAddressFields(orderedAddressFields);
	}

	@Override
	public List<String> getMessageKeysByLikeName(String searchString) {
		return cache.getMessageKeysByLikeName(searchString);
	}

	@Override
	public PersonAddress getI18nPersonAddress(PersonAddress address) {
		return cache.getI18nPersonAddress(address);
	}
}