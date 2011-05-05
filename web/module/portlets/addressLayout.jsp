<%@ include file="/WEB-INF/template/include.jsp"%>

<openmrs:htmlInclude file="/scripts/jquery/jquery-1.3.2.min.js"/>
<openmrs:htmlInclude file="/moduleResources/addresshierarchy/addressHierarchy.js"/>

<!-- JQUERY FOR THIS PAGE -->
<script type="text/javascript">

	var $j = jQuery;	

	var addressHierarchyLevels = [ <c:forEach var="hierarchyLevel" items="${hierarchyLevels}">'${hierarchyLevel.addressField.name}',</c:forEach> ];
	var pageContext = '${pageContext.request.contextPath}';
	
	$j(document).ready(function(){
		// create event handlers for the all the address selection lists
		<c:forEach var="hierarchyLevel" items="${hierarchyLevels}" varStatus="i">
		$j('.${hierarchyLevel.addressField.name}').change(function() {
			<c:if test="${i.count < fn:length(hierarchyLevels)}">
				addressFieldChange($j(this).closest('.address').find('.${hierarchyLevels[i.count].addressField.name}'));
			</c:if>
		});
		</c:forEach>

		alert("made it here!");
		
		// TODO: this is a quick hack implementation for testing purposes
		addressFieldChange($j('.countyDistrict'));
 	});
</script>
<!-- END JQUERY -->


<div class="address">
	<table>
		<c:forEach var="hierarchyLevel" items="${hierarchyLevels}">
			<tr id="test">
				<td><spring:message code="${model.layoutTemplate.nameMappings[hierarchyLevel.addressField.name]}"/></td>
				<td><spring:bind path="${hierarchyLevel.addressField.name}">
					<select type="text" name="${status.expression}" class="${hierarchyLevel.addressField.name}" />
				</spring:bind></td>
			</tr>
		</c:forEach>
	</table>
</div>



	