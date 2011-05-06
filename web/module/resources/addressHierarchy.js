var $j = jQuery;	


function handleAddressFieldChange(changedField, fieldToUpdate) {
		
	// if it's been a switch to "other"
	if(changedField.val() == '--other--') {
		handleAddressFieldChangeToOther(changedField);
		return;
	}
	
	// if it's a switch with no field to update (ie, the last level in the hierarchy)
	if(fieldToUpdate == '') {
		// find the associated free text field
		var textField = changedField.closest('tr').find(':text');

		// hide the text field and reset the value
		textField.hide();
		textField.val('');
		
		if(changedField.attr('name') == '') {
			// set the name of the selector field to the name of the free text field
			changedField.attr('name',textField.attr('name'));
			// clear the name of the selector
			textField.attr('name','');
		}
		
		return;
	}
	
	// handle the main case of updating the possible addresses
	var searchString = '';

	// we need to iterate through all the address hiearchy levels from top to bottom
	// for all levels *above* the next element, we need to build a search string in the 
	// format "UNITED STATES|MASSACHUSETTS|SUFFOLK";
	// then the element and all levels below it need to be emptied;
	var reachedLevelToUpdate = false;
	var reachedLevelToClearTextField = false;
	
	$j.each(addressHierarchyLevels, function (i, entry) {		
		if (fieldToUpdate.attr('class') == entry) { 
			reachedLevelToUpdate = true; 
		}
		if (changedField.attr('class') == entry) {
			reachedLevelToClearTextField = true;
		}
		
		if (reachedLevelToUpdate == false) {
			// build the search string	
			searchString = searchString + fieldToUpdate.closest('.address').find('.' + entry).val() + "|";
		}
		else {
			// empty the other entries
			fieldToUpdate.closest('.address').find('.' + entry).empty(); 
		}
		
		if (reachedLevelToClearTextField) {
			// find the selector associated with this field
			var selector = changedField.closest('.address').find('.' + entry);
			// find the associated free text field
			var textField = selector.closest('tr').find(':text');
	
			// hide the text field and reset the value
			textField.hide();
			textField.val('');
	
			if(selector.attr('name') == '') {
				// set the name of the selector field to the name of the free text field
				selector.attr('name',textField.attr('name'));
				// clear the name of the selector
				textField.attr('name','');
			}
		}
	});

	// slice off the trailing "|"
	if (searchString != null) {
		searchString = searchString.slice(0,-1);
	}
		
	updateOptions(fieldToUpdate, searchString);
}

function updateOptions(fieldToUpdate, searchString, value) {	
	// TODO: handle the "other" options
	
	// do the JSON call and add the appropriate elements
	$j.getJSON(pageContext + '/module/addresshierarchy/ajax/getChildAddressHierarchyEntries.form',
			{ 'searchString': searchString },
			function (data) {
				fieldToUpdate.append($j(document.createElement('option')).text('--'));
				$j.each(data, function(i, entry) {
					var option = $j(document.createElement('option')).attr('value', entry.name).text(entry.name);
					if (entry.name == value) {
						option.attr('selected',true);
					}
					fieldToUpdate.append(option);
				});
				fieldToUpdate.append($j(document.createElement('option')).attr('value', '--other--').text(other));
			}
		);
}

function handleAddressFieldChangeToOther(field) {
	
	var reachedLevelToSetToOther = false;
	
	// iterate through all the address hierarchy levels
	$j.each(addressHierarchyLevels, function (i, entry) {		
		if (field.attr('class') == entry) { 
			// we only want to operate on the level associated with the current field and all subsequent level
			reachedLevelToSetToOther = true; 
		}
		if (reachedLevelToSetToOther == true) {
			// find the selector associated with this field
			var selector = field.closest('.address').find('.' + entry);
			// find the associated free text field
			var textField = selector.closest('tr').find(':text');
			// empty the selector (as long as it's not the top level field)
			if (entry != field.attr('class')) {
				selector.empty();
			}
			// show the associated free text field
			textField.show();
			
			if(selector.attr('name') != '') {
				// set the name of the free text field to the name of the selector
				textField.attr('name',selector.attr('name'));
				// clear the name of the selector
				selector.attr('name','');
			}
		}
	});
	
}