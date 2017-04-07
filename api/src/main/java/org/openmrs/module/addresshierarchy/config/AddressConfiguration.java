package org.openmrs.module.addresshierarchy.config;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.MethodUtils;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsUtil;

/**
 * Simple component for representing the configuration options when starting up the address hierarchy module.
 */
public class AddressConfiguration {

    // Properties

    private List<AddressComponent> addressComponents;
    private List<String> lineByLineFormat;
    private AddressHierarchyFile addressHierarchyFile;

    // Constructor

    public AddressConfiguration() {}

    // Accessors

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
     * @see https://github.com/dkayiwa/openmrs-module-aijar/blob/2b85d2f8d4ab15d0e151504513d7aa8f6b2d103f/api/src/main/java/org/openmrs/module/aijar/api/deploy/bundle/AddressMetadataBundle.java#L75-L113
     */
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
        
        Map<String, String> nameMappings = new HashMap<String, String>();
        Map<String, String> sizeMappings = new HashMap<String, String>();
        Map<String, String> elementDefaults = new HashMap<String, String>();
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
        for (int i=0; i<this.getAddressComponents().size(); i++) {
            if (!this.getAddressComponents().get(i).equals(that.getAddressComponents().get(i))) {
                return false;
            }
        }
        if (this.getLineByLineFormat().size() != that.getLineByLineFormat().size()) {
            return false;
        }
        for (int i=0; i<this.getLineByLineFormat().size(); i++) {
            if (!this.getLineByLineFormat().get(i).equals(that.getLineByLineFormat().get(i))) {
                return false;
            }
        }
        if (!OpenmrsUtil.nullSafeEquals(this.getAddressHierarchyFile(), that.getAddressHierarchyFile())) {
            return false;
        }
        return true;
    }
}
