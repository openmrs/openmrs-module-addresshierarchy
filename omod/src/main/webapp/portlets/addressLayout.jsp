<%@ include file="/WEB-INF/template/include.jsp"%>

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
				// value for the selection list is the current value for the field (if one exist), otherwise the default value (if one exists)
				<c:set var="value">${!empty status.value ? status.value : !empty model.layoutTemplate.elementDefaults[hierarchyLevel.addressField.name] ? model.layoutTemplate.elementDefaults[hierarchyLevel.addressField.name] : ''}</c:set>  			

					  // only display selection this for list level if a) the previous level in the hierarchy has a value, b) the level itself has a value, or c) this is the top level in the hierarchy
					<c:if test="${!empty previousValue || !empty status.value || i.count == 1}">
						updateOptions($j('select[name="${status.expression}"]'), "${searchString}", "${value}");  // use double quotes here so as not conflict with ' in location names			
					</c:if>
						
				<c:set var="searchString">${searchString}${!empty value ? value : '*'}|</c:set>
				<c:set var="previousValue">${value}</c:set>
			</spring:bind>
		</c:forEach>

		// register submit handler for validation as required
		if (typeof submitHandlerRegistered === 'undefined') {
			$j('form:has(div[id=addressPortlet])').submit(function () {
				
				var passedValidation = true;
				var errorMessage = "<spring:message code="addresshierarchy.correctErrors"/>:\n";
				
				// iterate through each address portlet on the page
				// (note that this for some reason ignores the "add another address" div on the patient long form... this is what we want, but I'm not sure why it ignores it)
				$j.each($j('div[id=addressPortlet]'), function (index,value) {
					
					// skip validation if this is a voided address
					if ($j(value).find('[name$=voided]').is(':checked')) {
						return;
					}
					
					// otherwise, validate
					<c:forEach var="hierarchyLevel" items="${hierarchyLevels}" varStatus="i">

						<c:if test="${validateRequiredFields}">

							// handle checking that required fields aren't blank
							<c:if test="${hierarchyLevel.required == true}">
									if ($j(value).find('[name$=${hierarchyLevel.addressField.name}]').val() == null || ($j(value).find('[name$=${hierarchyLevel.addressField.name}]').val() == '')) {
										errorMessage = errorMessage + "<spring:message code="${model.layoutTemplate.nameMappings[hierarchyLevel.addressField.name]}"/> <spring:message code="addresshierarchy.requiredField"/>\n";
										passedValidation = false;
									}
							</c:if>

						</c:if>
						
						// handle enforcing no free-text entries if free-text disallowed
					  	if (!allowFreetext && $j(value).find('[name$=${hierarchyLevel.addressField.name}]').hasClass('other')){
					  		errorMessage = errorMessage + "<spring:message code="${model.layoutTemplate.nameMappings[hierarchyLevel.addressField.name]}"/> <spring:message code="addresshierarchy.cannotBeFreetext"/>\n";
							passedValidation = false;
					  	}
					
					</c:forEach>
				
				});
				
				if (!passedValidation) {
					alert(errorMessage);
				}
				
				return passedValidation;
			});
			
			// set this global variable so that we don't register the submit handler twice if there are multiple addresses on a page
			submitHandlerRegistered = true;
		}
 	});
</script>
<!-- END JQUERY -->

<!-- wrap everything in a table or a tbody depending on whether or not show table has been selected -->
<c:choose>
	<c:when test="${model.layoutShowTable == 'true'}">
		<table class="address">
	</c:when>
	<c:otherwise>
		<tbody class="address">
	</c:otherwise>
</c:choose>


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
							<c:if test="${i.count < switchToFreetext}">
									onChange="handleAddressFieldChange($j(this), $j(this).closest('.address').find('.${hierarchyLevels[i.count].addressField.name}'));"
							</c:if> 
							<c:if test="${i.count == switchToFreetext}">
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
		
<c:choose>
	<c:when test="${model.layoutShowTable == 'true'}">
		</table>
	</c:when>
	<c:otherwise>
		</tbody>
	</c:otherwise>
</c:choose>





	