package org.openmrs.module.addresshierarchy.validator;

import org.openmrs.module.addresshierarchy.AddressHierarchyType;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

@Component
public class AddressHierarchyTypeValidator implements Validator {

	@SuppressWarnings("unchecked")
	public boolean supports(Class c) {
		return AddressHierarchyType.class.isAssignableFrom(c);
	}

	
    public void validate(Object obj, Errors errors) {
	    AddressHierarchyType type = (AddressHierarchyType) obj;	    
	    
	    ValidationUtils.rejectIfEmptyOrWhitespace(errors, "name", "addresshierarchy.admin.validation.name.blank");
	    ValidationUtils.rejectIfEmptyOrWhitespace(errors, "addressField", "addresshierarchy.admin.validation.addressField.blank");
	 
	    // a type can't have itself as a parent
	    if (type.getParentType() == type) {
	    	errors.rejectValue("parentType", "addresshierarchy.admin.validation.parentType.ownParent");
	    }
	    
	    // confirm that the selected address field and parent aren't associated with another type
	    // TODO: this has been commented out because when editing an address hierarchy type, all changes are committed, even if the validation
	    // fails; I believe this is because of the call to getAddressHierarchyTypes... hibernate probably commits any changes that have been
	    // made to the domain object; I need to find a way to do this validation without hibernate flushing it's cache
	 /**   for (AddressHierarchyType compareType : Context.getService(AddressHierarchyService.class).getAddressHierarchyTypes()) {
	    	
	    	// we only want to test all OTHER address hierarchy types
	    	
	    	if (type.getId() == null || type.getId() != compareType.getId()) {
	    		
	    		if (compareType.getAddressField() == type.getAddressField()) {
	    			errors.rejectValue("addressField", "addresshierarchy.admin.validation.addressField.alreadyUsed");
	    		}
	    		
	    		if (compareType.getParentType() == type.getParentType()) {
	    			errors.rejectValue("parentType", "addresshierarchy.admin.validation.parentType.alreadyUsed");
	    		}
	    		
	    	}
	    } */
    }
}

