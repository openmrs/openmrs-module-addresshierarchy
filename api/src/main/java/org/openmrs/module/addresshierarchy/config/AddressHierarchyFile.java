package org.openmrs.module.addresshierarchy.config;

import org.openmrs.util.OpenmrsUtil;

/**
 * Represents the configuration of file containing address hierarchy entries to load
 */
public class AddressHierarchyFile {

    private String filename;
    private String entryDelimiter = "|";
    private String identifierDelimiter = "^";

    public AddressHierarchyFile() {}

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getEntryDelimiter() {
        return entryDelimiter;
    }

    public void setEntryDelimiter(String entryDelimiter) {
        this.entryDelimiter = entryDelimiter;
    }

    public String getIdentifierDelimiter() {
        return identifierDelimiter;
    }

    public void setIdentifierDelimiter(String identifierDelimiter) {
        this.identifierDelimiter = identifierDelimiter;
    }

    @Override
    public int hashCode() {
        int ret = 17;
        ret = 31 * ret + (filename == null ? 0 : filename.hashCode());
        ret = 31 * ret + (entryDelimiter == null ? 0 : entryDelimiter.hashCode());
        ret = 31 * ret + (identifierDelimiter == null ? 0 : identifierDelimiter.hashCode());
        return ret;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof AddressHierarchyFile)) {
            return false;
        }
        AddressHierarchyFile that = (AddressHierarchyFile)obj;
        boolean ret = true;
        ret = ret && OpenmrsUtil.nullSafeEquals(this.getFilename(), that.getFilename());
        ret = ret && OpenmrsUtil.nullSafeEquals(this.getEntryDelimiter(), that.getEntryDelimiter());
        ret = ret && OpenmrsUtil.nullSafeEquals(this.getIdentifierDelimiter(), that.getIdentifierDelimiter());
        return ret;
    }
}
