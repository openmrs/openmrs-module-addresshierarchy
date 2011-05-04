<%@ include file="/WEB-INF/template/include.jsp"%>

<!-- JQUERY FOR THIS PAGE -->
<script type="text/javascript">

	var $j = jQuery.noConflict();	

	var addressHierarchyLevels = [ <c:forEach var="hierarchyLevel" items="${hierarchyLevels}">'${hierarchyLevel.addressField.name}',</c:forEach> ];

	function updateOptions(element) {
		
		var searchString = '';

		// we need to iterate through all the address hiearchy levels from top to bottom
		// for all levels *above* the next element, we need to build a search string in the 
		// format "UNITED STATES|MASSACHUSETTS|SUFFOLK";
		// then the element and all levels below it need to be emptied;
		var reachedLevelToUpdate = false;
		
		$j.each(addressHierarchyLevels, function (i, entry) {

			if (element.attr('id') == entry) { 
				reachedLevelToUpdate = true; 
			}
			if (reachedLevelToUpdate == false) {
				// build the search string
				searchString = searchString + $j('#' + entry).val() + "|";
			}
			else {
				// empty the other entries
				$j('#' + entry).empty(); 
			}
		});

		// slice off the trailing "|"
		if (searchString != null) {
			searchString = searchString.slice(0,-1);
		}
			
		// now do the actual JSON call and add the appropriate elements
		$j.getJSON('${pageContext.request.contextPath}/module/addresshierarchy/ajax/getChildAddressHierarchyEntries.form',
					{ 'searchString': searchString },
					function (data) {
						element.append($j(document.createElement("option")).text("--"));
						$j.each(data, function(i, entry) {
							element.append($j(document.createElement("option")).attr("value", entry.name).text(entry.name));
						});
					}
				);

	}

	$j(document).ready(function(){
		// create event handlers for the all the address selection lists
		<c:forEach var="hierarchyLevel" items="${hierarchyLevels}" varStatus="i">
		$j('#${hierarchyLevel.addressField.name}').change(function() {
			<c:if test="${i.count < fn:length(hierarchyLevels)}">
				updateOptions($j('#${hierarchyLevels[i.count].addressField.name}'));
			</c:if>
		});
		</c:forEach>


		// TODO: this is a quick hack implementation for testing purposes
		updateOptions($j('#countyDistrict'));
 	});
</script>
<!-- END JQUERY -->


<c:if test="${model.layoutShowTable == 'true'}">
	<div id="${model.portletDivName}">
		<table>
</c:if>


<c:forEach var="hierarchyLevel" items="${hierarchyLevels}">
	<tr>
		<td><spring:message code="${model.layoutTemplate.nameMappings[hierarchyLevel.addressField.name]}"/></td>
		<td><spring:bind path="${hierarchyLevel.addressField.name}">
			<select type="text" name="${status.expression}" id="${hierarchyLevel.addressField.name}" />
		</spring:bind></td>
	</tr>
</c:forEach>



<c:if test="${model.layoutShowTable == 'true'}">
		</table>
	</div>
</c:if>



	