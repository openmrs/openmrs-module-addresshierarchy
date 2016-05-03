package org.openmrs.module.addresshierarchy.config;

import org.openmrs.module.addresshierarchy.AddressField;
import org.openmrs.util.OpenmrsUtil;

/**
 * Represents a component of a hierarchical address
 * This aims to combine the fields required to set up the address template in the system, and to align this with the
 * levels needed by the address hierarchy
 */
public class AddressComponent {

    private AddressField field;
    private String nameMapping;
    private int sizeMapping;
    private String elementDefault;
    private boolean requiredInHierarchy;

    public AddressComponent() {}

    public AddressComponent(AddressField field, String nameMapping, int sizeMapping, String elementDefault, boolean requiredInHierarchy) {
        this.field = field;
        this.nameMapping = nameMapping;
        this.sizeMapping = sizeMapping;
        this.elementDefault = elementDefault;
        this.requiredInHierarchy = requiredInHierarchy;
    }

    public AddressField getField() {
        return field;
    }

    public void setField(AddressField field) {
        this.field = field;
    }

    public String getNameMapping() {
        return nameMapping;
    }

    public void setNameMapping(String nameMapping) {
        this.nameMapping = nameMapping;
    }

    public int getSizeMapping() {
        return sizeMapping;
    }

    public void setSizeMapping(int sizeMapping) {
        this.sizeMapping = sizeMapping;
    }

    public String getElementDefault() {
        return elementDefault;
    }

    public void setElementDefault(String elementDefault) {
        this.elementDefault = elementDefault;
    }

    public boolean isRequiredInHierarchy() {
        return requiredInHierarchy;
    }

    public void setRequiredInHierarchy(boolean requiredInHierarchy) {
        this.requiredInHierarchy = requiredInHierarchy;
    }

    @Override
    public int hashCode() {
        int ret = 17;
        ret = 31 * ret + (field == null ? 0 : field.hashCode());
        ret = 31 * ret + (nameMapping == null ? 0 : nameMapping.hashCode());
        ret = 31 * ret + sizeMapping;
        ret = 31 * ret + (elementDefault == null ? 0 : elementDefault.hashCode());
        ret = 31 * ret + (requiredInHierarchy ? 1 : 0);
        return ret;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof AddressComponent)) {
            return false;
        }
        AddressComponent that = (AddressComponent)obj;
        boolean ret = true;
        ret = ret && OpenmrsUtil.nullSafeEquals(this.getField(), that.getField());
        ret = ret && OpenmrsUtil.nullSafeEquals(this.getNameMapping(), that.getNameMapping());
        ret = ret && OpenmrsUtil.nullSafeEquals(this.getSizeMapping(), that.getSizeMapping());
        ret = ret && OpenmrsUtil.nullSafeEquals(this.getElementDefault(), that.getElementDefault());
        ret = ret && OpenmrsUtil.nullSafeEquals(this.isRequiredInHierarchy(), that.isRequiredInHierarchy());
        return ret;
    }
}
