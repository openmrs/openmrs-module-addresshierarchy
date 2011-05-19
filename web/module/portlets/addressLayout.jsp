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

		// register submit handler for validation as required
		$j('form:has(div[class=address])').submit(function () {
			passedValidation = true;
			var errorMessage = "<spring:message code="addresshierarchy.correctErrors"/>:\n";
			
			<c:forEach var="hierarchyLevel" items="${hierarchyLevels}" varStatus="i">
				<spring:bind path="${hierarchyLevel.addressField.name}">
					<c:if test="${hierarchyLevel.required == true}">
						<c:if test="${fn:contains(status.expression,'.')}">  // hacky way to ignore the "add another address" fields on the edit patient long form page 
							// handle checking that required fields aren't blank
					  		if ($j('[name=${status.expression}]').val() == null || $j('[name=${status.expression}]').val() == '') {
								errorMessage = errorMessage + "<spring:message code="${model.layoutTemplate.nameMappings[hierarchyLevel.addressField.name]}"/> <spring:message code="addresshierarchy.requiredField"/>\n";
								passedValidation = false;
							}
						</c:if>
					</c:if>
					// handle enforcing no free-text entries if free-text disallowed
				  	if (!allowFreetext && $j('[name=${status.expression}]').hasClass('other')){
				  		errorMessage = errorMessage + "<spring:message code="${model.layoutTemplate.nameMappings[hierarchyLevel.addressField.name]}"/> <spring:message code="addresshierarchy.cannotBeFreetext"/>\n";
						passedValidation = false;
				  	}
				</spring:bind>
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
	
		<!-- handle the "preferred" checkbox if we are in extended mode (this is copied from the existing 1.6 addressLayout.jsp) -->
		<c:if test="${model.layoutShowExtended == 'true'}">
			<tr>
				<td><spring:message code="general.preferred"/></td>
				<td>
					<spring:bind path="preferred">
						<input type="hidden" name="_${status.expression}">
						<input type="checkbox" name="${status.expression}" onclick="if (preferredBoxClick) preferredBoxClick(this)" value="true" alt="personAddress" <c:if test="${status.value == true}">checked</c:if> />
						<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
					</spring:bind>
				</td>
			</tr>
		</c:if>
	
		<!--  handles the main display of the hierarchy levels -->
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
				<td><input type="text" style="display:none" value="${status.value}" class="other"/></td>
				</spring:bind>
			</tr>
		</c:forEach>
		
		
		<!-- handle the created by and void options if we are in extended mode (this is copied from the existing 1.6 addressLayout.jsp) -->
		<c:if test="${model.layoutShowExtended == 'true'}">
				<spring:bind path="creator">
					<c:if test="${!(status.value == null)}">
						<tr>
							<td><spring:message code="general.createdBy" /></td>
							<td colspan="4">
								${status.value.personName} -
								<openmrs:formatDate path="dateCreated" type="long" />
							</td>
						</tr>
					</c:if>
				</spring:bind>
                         <c:if test="${model.layoutHideVoidOption != 'true'}">
					<tr>
						<td><spring:message code="general.voided"/></td>
						<td>
							<spring:bind path="voided">
								<input type="hidden" name="_${status.expression}"/>
								<input type="checkbox" name="${status.expression}" 
									   <c:if test="${status.value == true}">checked="checked"</c:if> 
							</spring:bind>
									   onClick="toggleLayer('<spring:bind path="personAddressId">voidReasonRow-${status.value}</spring:bind>'); if (voidedBoxClicked) voidedBoxClicked(this); "
								/>
						</td>
					</tr>
					<tr id="<spring:bind path="personAddressId">voidReasonRow-${status.value}</spring:bind>"
						style="<spring:bind path="voided"><c:if test="${status.value == false}">display: none;</c:if></spring:bind>">
						<td><spring:message code="general.voidReason"/></td>
						<spring:bind path="voidReason">
							<td colspan="4">
								<input type="text" name="${status.expression}" value="${status.value}" size="43" />
								<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
							</td>
						</spring:bind>
					</tr>
					<spring:bind path="voidedBy">
						<c:if test="${!(status.value == null)}">
							<tr>
								<td><spring:message code="general.voidedBy" /></td>
								<td colspan="4">
									${status.value.personName} -
									<openmrs:formatDate path="dateVoided" type="long" />
								</td>
							</tr>
						</c:if>
					</spring:bind>
              </c:if>
		</c:if>
		
	</table>
</div>



	