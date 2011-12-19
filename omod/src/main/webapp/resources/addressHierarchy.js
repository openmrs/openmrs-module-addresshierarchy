
var $j = jQuery;	

// the default handler that is called when a change event occurs on any of the address field selection lists
function handleAddressFieldChange(changedField, fieldToUpdate) {
	// if it's been a switch to "other", hand off to the handleSelectOther function
	if (changedField.val() == '--other--') {
		handleSelectOther(changedField, true);
		return;
	}
	
	// if it has been switch to the blank entry, hand off to the handleSelectBlank function
	if (changedField.val() == '') {
		handleSelectBlank(changedField);
		return;
	}
	
	// if it's a switch with no field to update (ie, the last level in the hierarchy), just make sure we've switch back to 
	// a selection list (to handle the case where we are switching out of the "other" state)
	if (fieldToUpdate == '') {
		switchToSelectList(changedField);
		return;
	}
	
	var searchString = '';

	// grab a reference to the overall address associated with the fields 
	// (we can't directly reference the address because there may be multiple addresses on a single page)
	var address = fieldToUpdate.closest('.address');

	// handle the main case of updating the possible addresses
	// we need to iterate through all the address hierarchy levels from top to bottom...
	// for all levels *above* the next element, we need to build a search string in the 
	// format "UNITED STATES|MASSACHUSETTS|SUFFOLK";
	// then the element and all levels below it need to be emptied;
	
	var changedFieldIndex = findIndexOfLevel(changedField.attr('class'));
	
	$j.each(addressHierarchyLevels, function (i, level) {		
		
		// build the search string by concatenating the value of all fields above (and including) the field that changed
		if (i <= changedFieldIndex ) {
			searchString = searchString + address.find('.' + level).val() + "|";
		}
		// empty the other entries
		else {
			var selectList = address.find('.' + level);		
			selectList.empty();
			selectList.append($j(document.createElement('option')).attr('value','').text(''));
			selectList.hide();
		}
		
		// clear the text input field of the changed field and all fields below it in the hierarchy
		if (i >= changedFieldIndex) {
			switchToSelectList(address.find('.' + level));
		}
	});

	if (searchString != '') {
		// update the options of the field to update
		updateOptions(fieldToUpdate, searchString, '');
	}
}

function updateOptions(fieldToUpdate, searchString, value) {	
	
	// do the JSON call and add the appropriate elements
	$j.ajax({
		  type: 'POST',
		  url: pageContext + '/module/addresshierarchy/ajax/getChildAddressHierarchyEntries.form',
		  dataType: 'json',
		  data: { 'searchString': searchString },
		  success: function (data) {
					var foundCurrentValue;
					
					fieldToUpdate.empty();
					fieldToUpdate.append($j(document.createElement('option')).attr('value','').text(''));
					$j.each(data, function(i, entry) {
						var option = $j(document.createElement('option')).attr('value', entry.name).text(entry.name);
						if (entry.name.toLowerCase() == value.toLowerCase()) {
							option.attr('selected',true);
							foundCurrentValue = true;
						}
						fieldToUpdate.append(option);
					});
					if (allowFreetext == true) {
						fieldToUpdate.append($j(document.createElement('option')).attr('value', '--other--').text(other));
					}
						
					// if we haven't found a match against the current value of the field, switch to the "Other" case
					// (note even if allowFreetext == false, we call handleSelectOther, because although we don't want to
					// allow the creation of new freetext values, we want to display any legacy ones that may exist)
					if (!foundCurrentValue && value !='') {		
						if (allowFreetext == true) {
							fieldToUpdate.val('--other--')
						}
						handleSelectOther(fieldToUpdate, false);
					}
					
					// show the select list (if there are any options to display (besides the default empty option)
					if (fieldToUpdate.children('option').length > 1) {
						fieldToUpdate.show();
					}
	  		}
	});
}

// called from the handleAddresesFieldChange to handle the case when a user selects the "blank" option from the list of options
function handleSelectBlank(field) {
	var fieldLevelIndex = findIndexOfLevel(field.attr('class'));
	var address = field.closest('.address');
	
	// iterate through all the address hierarchy levels
	$j.each(addressHierarchyLevels, function (i, level) {		
		// we want to skip all the hierarchy levels before the level of the address field we are operating on
		if (i >= fieldLevelIndex){
			
			var selectList = address.find('.' + level);
			
			// empty and hide all select lists of hierarchy levels below the field that was set to "blank"
			if (i > fieldLevelIndex) {
				selectList.empty();
				selectList.hide();
			}
			
			// make sure the fields are set back to select lists
			switchToSelectList(selectList);
		}
	});
}
 

// called from handleAddressFieldChange to handle the case when a user selects "Other" from the list of options
// also used to switch fields into the "Other" state upon initialization as required
function handleSelectOther(field, emptyTextInput) {
	var fieldLevelIndex = findIndexOfLevel(field.attr('class'));
	var address = field.closest('.address');

	// iterate through all the address hierarchy levels
	$j.each(addressHierarchyLevels, function (i, level) {		
		// we want to skip all the hierarchy levels before the level of the address field we are operating on
		if (i >= fieldLevelIndex){
			
			var selectList = address.find('.' + level);
			
			// empty all select lists of hierarchy levels below the field that was set to "other"
			if (i > fieldLevelIndex) {
				selectList.empty();
				selectList.hide();
			}
			
			// switch to using the text input instead of the select list
			switchToTextInput(selectList, emptyTextInput);
		}
	});
}

// for the specified field, switch to using the text input instead of the select list
function switchToTextInput(selectList, emptyTextInput) {
	// find the associated free text field
	var textInput = selectList.closest('tr').find(':text');
	
	// show the associated free text field
	textInput.show();
	if(emptyTextInput == true) {
		textInput.val('');
	}
	
	if(selectList.attr('name') != '') {
		// set the name of the free text field to the name of the selector
		textInput.attr('name',selectList.attr('name'));
		// clear the name of the selector
		selectList.attr('name','');
	}
}

// for the specified field, switch from using the text input to the select list
function switchToSelectList(selectList)  {
	// find the associated free text field
	var textInput = selectList.closest('tr').find(':text');

	// hide the text field and reset the value
	textInput.hide();
	textInput.val('');

	if(selectList.attr('name') == '') {
		// set the name of the selector field to the name of the free text field
		selectList.attr('name',textInput.attr('name'));
		// clear the name of the selector
		textInput.attr('name','');
	}
}

// find the numerical index of a level within the address hierarchy
function findIndexOfLevel(level) {
	for(var i=0; i<addressHierarchyLevels.length; i++){ 
	       if(addressHierarchyLevels[i] == level) {
	            return i;
	       }
	}
}


