package org.openmrs.module.addresshierarchy.config;

import org.apache.commons.beanutils.MethodUtils;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsUtil;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Simple component for representing the configuration options when starting up the address hierarchy module.
 */
public class AddressConfiguration {

    // Properties - Those are the fields that must be in the config. XML.

	private boolean wipe = false;
    private List<AddressComponent> addressComponents;
    private List<String> lineByLineFormat;
    private AddressHierarchyFile addressHierarchyFile;

    // Constructor

    public AddressConfiguration() {}

    // Accessors
    
    public boolean mustWipe() {
    	return wipe;
    }

    public List<AddressComponent> getAddressComponents() {
        if (addressComponents == null) {
            addressComponents = new ArrayList<AddressComponent>();
        }
        return addressComponents;
    }

    public void addAddressComponent(AddressComponent component) {
        getAddressComponents().add(component);
    }

    public void setAddressComponents(List<AddressComponent> addressComponents) {
        this.addressComponents = addressComponents;
    }

    public List<String> getLineByLineFormat() {
        if (lineByLineFormat == null) {
            lineByLineFormat = new ArrayList<String>();
        }
        return lineByLineFormat;
    }

    public void addLineByLineFormat(String line) {
        getLineByLineFormat().add(line);
    }

    public void setLineByLineFormat(List<String> lineByLineFormat) {
        this.lineByLineFormat = lineByLineFormat;
    }

    public AddressHierarchyFile getAddressHierarchyFile() {
        return addressHierarchyFile;
    }

    public void setAddressHierarchyFile(AddressHierarchyFile addressHierarchyFile) {
        this.addressHierarchyFile = addressHierarchyFile;
    }

    // Instance methods

    /**
     * @return a new AddressTemplate instance for the given configuration
     */
    @JsonIgnore
    public Object getAddressTemplate() {
        Object addressTemplate = null;
        try {
        	Constructor<?> constructor = Context.loadClass("org.openmrs.layout.web.address.AddressTemplate").getConstructor(String.class);
			addressTemplate = constructor.newInstance("");
        }
        catch (Exception e) {
        	try {
        		Constructor<?> constructor = Context.loadClass("org.openmrs.layout.address.AddressTemplate").getConstructor(String.class);
        		addressTemplate = constructor.newInstance("");
			}
			catch (Exception ex) {
				throw new APIException("Error while getting address template", ex);
			}
        }
        
        Map<String, String> nameMappings = new LinkedHashMap<String, String>();
        Map<String, String> sizeMappings = new LinkedHashMap<String, String>();
        Map<String, String> elementDefaults = new LinkedHashMap<String, String>();
        for (AddressComponent c : getAddressComponents()) {
            nameMappings.put(c.getField().getName(), c.getNameMapping());
            sizeMappings.put(c.getField().getName(), Integer.toString(c.getSizeMapping()));
            if (c.getElementDefault() != null) {
                elementDefaults.put(c.getField().getName(), c.getElementDefault());
            }
        }
        
        try {
			MethodUtils.invokeExactMethod(addressTemplate, "setNameMappings", new Object[]{ nameMappings }, new Class[] { Map.class });
			MethodUtils.invokeExactMethod(addressTemplate, "setSizeMappings", new Object[]{ sizeMappings }, new Class[] { Map.class });
	        MethodUtils.invokeExactMethod(addressTemplate, "setElementDefaults", new Object[]{ elementDefaults }, new Class[] { Map.class });
	        MethodUtils.invokeExactMethod(addressTemplate, "setLineByLineFormat", new Object[]{ getLineByLineFormat() }, new Class[] { List.class });
		}
		catch (Exception e) {
			throw new APIException("Error while getting address template", e);
		}

        return addressTemplate;
    }

    // Overrides

    @Override
    public int hashCode() {
        int ret = 17;
        ret = 31 * ret + getAddressComponents().hashCode();
        ret = 31 * ret + getLineByLineFormat().hashCode();
        ret = 31 * ret + (addressHierarchyFile == null ? 0 : addressHierarchyFile.hashCode());
        return ret;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof AddressConfiguration)) {
            return false;
        }
        AddressConfiguration that = (AddressConfiguration)obj;
        if (this.getAddressComponents().size() != that.getAddressComponents().size()) {
            return false;
        }
        
        if (this.getAddressComponents().size() == that.getAddressComponents().size()) {
            return this.getAddressComponents().stream().allMatch(item -> that.getAddressComponents().contains(item));
        }

        if (this.getLineByLineFormat().size() != that.getLineByLineFormat().size()) {
            return false;
        }

        if (this.getLineByLineFormat().size() == that.getLineByLineFormat().size()) {
            return this.getLineByLineFormat().stream().allMatch(item -> that.getLineByLineFormat().contains(item));
        }

        if (!OpenmrsUtil.nullSafeEquals(this.getAddressHierarchyFile(), that.getAddressHierarchyFile())) {
            return false;
        }
        return true;
    }
}
