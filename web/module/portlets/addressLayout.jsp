<%@ include file="/WEB-INF/template/include.jsp"%>


<!-- JQUERY FOR THIS PAGE -->
<script type="text/javascript">

	var $j = jQuery.noConflict();	

	var addressHierarchyLevels = [ <c:forEach var="hierarchyLevel" items="${hierarchyLevels}">'${hierarchyLevel.addressField.name}',</c:forEach> ];

	function updateOptions(parentEntry, addressHierarchyLevelId, nextElement) {

		// first we need to empty the current field and all fields after it in the hierarchy
		var del = false;
		$j.each(addressHierarchyLevels, function(i, entry) {
			if (nextElement.attr('id') == entry) { del = true; }
			if (del) { $j('#' + entry).empty(); }
		});
		
		// now do the actual JSON call and add the appropriate elements
		$j.getJSON('${pageContext.request.contextPath}/module/addresshierarchy/ajax/getChildAddressHierarchyEntries.form',
					{ 'parentEntry': parentEntry, 'addressHierarchyLevel': addressHierarchyLevelId },
					function (data) {
						nextElement.append($j(document.createElement("option")).text("--"));
						$j.each(data, function(i, entry) {
							nextElement.append($j(document.createElement("option")).attr("value", entry.name).text(entry.name));
						});
					}
				);

	}

	$j(document).ready(function(){

		// create event handlers for the all the address selection lists
		<c:forEach var="hierarchyLevel" items="${hierarchyLevels}" varStatus="i">
		$j('#${hierarchyLevel.addressField.name}').change(function() {
			<c:if test="${i.count < fn:length(hierarchyLevels)}">
				updateOptions($j(this).val(), ${hierarchyLevel.id}, $j('#${hierarchyLevels[i.count].addressField.name}'));
			</c:if>
		});
		</c:forEach>
		
		// TODO: this is a quick hack implementation for testing purposes
		updateOptions('', ${hierarchyLevels[0].id}, $j('#countyDistrict'));
		
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



	