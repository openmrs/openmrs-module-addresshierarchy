/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.addresshierarchy.validator;

import org.openmrs.api.context.Context;
import org.openmrs.module.addresshierarchy.AddressHierarchyLevel;
import org.openmrs.module.addresshierarchy.service.AddressHierarchyService;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class AddressHierarchyLevelValidator implements Validator {
	
	public boolean supports(@SuppressWarnings("rawtypes") Class c) {
		return AddressHierarchyLevel.class.isAssignableFrom(c);
	}
	
	public void validate(Object obj, Errors errors) {
		AddressHierarchyLevel level = (AddressHierarchyLevel) obj;
		
		// a level can't have itself as a parent
		if (level.getParent() == level) {
			errors.rejectValue("parent", "addresshierarchy.admin.validation.parent.ownParent");
		}
		
		// confirm that the selected address field and parent aren't associated with another level
		for (AddressHierarchyLevel compareLevel : Context.getService(AddressHierarchyService.class)
		        .getAddressHierarchyLevels()) {
			
			// we only want to test all OTHER address hierarchy types
			
			if (level.getId() == null || level.getId() != compareLevel.getId()) {
				
				if (level.getAddressField() != null && compareLevel.getAddressField() == level.getAddressField()) {
					errors.rejectValue("addressField", "addresshierarchy.admin.validation.addressField.alreadyUsed");
				}
				
				if (compareLevel.getParent() == level.getParent()) {
					errors.rejectValue("parentType", "addresshierarchy.admin.validation.parentType.alreadyUsed");
				}
				
			}
		}
	}
}
