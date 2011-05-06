<%@ include file="/WEB-INF/template/include.jsp"%>

<openmrs:htmlInclude file="/scripts/jquery/jquery-1.3.2.min.js"/>
<openmrs:htmlInclude file="/moduleResources/addresshierarchy/addressHierarchy.js"/>

<!-- JQUERY FOR THIS PAGE -->
<script type="text/javascript">

	var $j = jQuery;	

	var addressHierarchyLevels = [ <c:forEach var="hierarchyLevel" items="${hierarchyLevels}">'${hierarchyLevel.addressField.name}',</c:forEach> ];
	var pageContext = '${pageContext.request.contextPath}';
	
	$j(document).ready(function(){

		// initialize all the address field (if necessary)
		<c:forEach var="hierarchyLevel" items="${hierarchyLevels}" varStatus="i">
			<spring:bind path="${hierarchyLevel.addressField.name}">
				<c:if test="${not empty status.value || i.count == 1}">
					updateOptions($j('select[name=${status.expression}]'), "${searchString}", "${status.value}");  // use double quotes here so as not conflict with ' in location names
					<c:set var="searchString">${searchString}${fn:length(searchString) > 0 ? '|' : ''}${status.value}</c:set>
				</c:if>
				</spring:bind>
		</c:forEach>
	
 	});
</script>
<!-- END JQUERY -->


<div class="address">
	<table>
		<c:forEach var="hierarchyLevel" items="${hierarchyLevels}" varStatus="i">
			<tr id="test">
				<td><spring:message code="${model.layoutTemplate.nameMappings[hierarchyLevel.addressField.name]}"/></td>
				<td><spring:bind path="${hierarchyLevel.addressField.name}">
					<select type="text" name="${status.expression}" class="${hierarchyLevel.addressField.name}" 
						<c:if test="${i.count < fn:length(hierarchyLevels)}">
							onChange="handleAddressFieldChange($j(this).closest('.address').find('.${hierarchyLevels[i.count].addressField.name}'));"
						</c:if> />
				</spring:bind></td>
			</tr>
		</c:forEach>
	</table>
</div>



	