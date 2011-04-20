package org.openmrs.module.addresshierarchy;


import java.util.Map;

import junit.framework.TestCase;

import org.openmrs.module.Extension.MEDIA_TYPE;
import org.openmrs.module.addresshierarchy.extension.html.AdminList;

/**
 * This test validates the AdminList extension class
 */
public class AdminListExtensionTest extends TestCase {

    /**
     * Get the links for the extension class
     */
    public void testValidatesLinks() {
        AdminList ext = new AdminList();
        
        Map<String, String> links = ext.getLinks();
        
        assertNotNull("Some links should be returned", links);
        
        assertTrue("There should be a positive number of links", links.values().size() > 0);
        
        System.out.println(links.toString());
        
    }
    
    /**
     * Check the media type of this extension class
     */
    public void testMediaTypeIsHtml() {
        AdminList ext = new AdminList();
        
        assertTrue("The media type of this extension should be html", ext.getMediaType().equals(MEDIA_TYPE.html));
    }
    
    public void testGetTitle(){
        
        AdminList test = new AdminList();
        System.out.println(test.getTitle());
        
    }
    
}
