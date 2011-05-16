<%@ include file="/WEB-INF/template/include.jsp"%>

<openmrs:htmlInclude file="/scripts/jquery/jquery-1.3.2.min.js"/>
<openmrs:htmlInclude file="/moduleResources/addresshierarchy/addressHierarchy.js"/>

<!-- JQUERY FOR THIS PAGE -->
<script type="text/javascript">

	var $j = jQuery;	

	var addressHierarchyLevels = [ <c:forEach var="hierarchyLevel" items="${hierarchyLevels}">'${hierarchyLevel.addressField.name}',</c:forEach> ];
	var pageContext = '${pageContext.request.contextPath}';
	var other = "<spring:message code="addresshierarchy.other"/>";
	var allowFreetext = ${allowFreetext};
	
	
	$j(document).ready(function(){

		// initialize all the address field (if necessary) by updating the options in the relevant select list
		// note that we need to reference the select lists by name (as opposed to class) because there may be multiple
		// instances of the address portlet (and therefore multiple addresses) on a single page
		// note that we build the search string for each hierarchy level by concatenating the values of the previous levels
		<c:forEach var="hierarchyLevel" items="${hierarchyLevels}" varStatus="i">
			<spring:bind path="${hierarchyLevel.addressField.name}">
				<c:if test="${not empty status.value || i.count == 1}">
					updateOptions($j('select[name=${status.expression}]'), "${searchString}", "${status.value}");  // use double quotes here so as not conflict with ' in location names
				</c:if>
				<c:set var="searchString">${searchString}${!empty status.value ? status.value : '*'}|</c:set>
			</spring:bind>
		</c:forEach>

		// register submit handler for validation as requird
		$j('form:has(div[class=address])').submit(function () {
			passedValidation = true;
			var errorMessage = "<spring:message code="addresshierarchy.requiredFields"/>:\n";
			
			<c:forEach var="hierarchyLevel" items="${hierarchyLevels}" varStatus="i">
				<c:if test="${hierarchyLevel.required == true}">
					<spring:bind path="${hierarchyLevel.addressField.name}">
					  if ($j('[name=${status.expression}]').val() == null || $j('[name=${status.expression}]').val() == '') {
							errorMessage = errorMessage + "<spring:message code="${model.layoutTemplate.nameMappings[hierarchyLevel.addressField.name]}"/>\n";
							passedValidation = false;
						}
					</spring:bind>
				</c:if>
			</c:forEach>

			if (!passedValidation) {
				alert(errorMessage);
			}
			
			return passedValidation;
		});
 	});
</script>
<!-- END JQUERY -->


<div class="address">
	<table>
		<c:forEach var="hierarchyLevel" items="${hierarchyLevels}" varStatus="i">
			<tr>
				<td><spring:message code="${model.layoutTemplate.nameMappings[hierarchyLevel.addressField.name]}"/></td>
				<spring:bind path="${hierarchyLevel.addressField.name}">
				<td>
					<c:choose>
						<c:when test="${i.count <= switchToFreetext}">
							<select style="display:none" name="${status.expression}" class="${hierarchyLevel.addressField.name}" 
							<c:if test="${i.count < fn:length(hierarchyLevels)}">
									onChange="handleAddressFieldChange($j(this), $j(this).closest('.address').find('.${hierarchyLevels[i.count].addressField.name}'));"
							</c:if> 
							<c:if test="${i.count == fn:length(hierarchyLevels)}">
									onChange="handleAddressFieldChange($j(this), '');"
							</c:if>
							/>
						</c:when>
						<c:otherwise>
							<!-- for lower levels in the hierarchy that there are no defined entries for -->
							<input type="text" name="${status.expression}" value="${status.value}" size="30"/>
						</c:otherwise>
					</c:choose>
				</td>
				<td><input type="text" style="display:none" value="${status.value}"/></td>
				</spring:bind>
			</tr>
		</c:forEach>
	</table>
</div>



	