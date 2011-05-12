<%@ include file="/WEB-INF/template/include.jsp"%>

<openmrs:require privilege="Manage Address Hierarchy" otherwise="/login.htm"
	redirect="/module/addresshierarchy/admin/editAddressHierarchyLevel.form" />

<%@ include file="/WEB-INF/template/header.jsp"%>
<%@ include file="localHeader.jsp"%>

<!-- SPECIALIZED STYLES FOR THIS PAGE -->
<style type="text/css">
	td {padding-left:4px; padding-right:4px; padding-top:2px; padding-bottom:2px; vertical-align:top}
</style>

<h2><spring:message code="addresshierarchy.admin.title" /></h2>

<br/>
<br/>

<!--  DISPLAY ANY ERROR MESSAGES -->
<c:if test="${fn:length(errors.allErrors) > 0}">
	<c:forEach var="error" items="${errors.allErrors}">
		<span class="error"><spring:message code="${error.code}"/></span><br/><br/>
	</c:forEach>
	<br/>
</c:if>


<div><b class="boxHeader"><spring:message code="addresshierarchy.admin.editLevel" /></b>


<form id="editAddressHierarchyLevel" action="updateAddressHierarchyLevel.form" method="post">
<input type="hidden" name="levelId" value="${level.id}"/>

<table cellspacing="0" cellpadding="0" class="box">

<tr>
	<td style="font-weight:bold"><nobr><spring:message code="addresshierarchy.admin.name" />:</nobr></td>
	<td>
		<input type="text" name="name" value="${level.name}"/>
	</td>
	<td width="60%">&nbsp;</td>
</tr>

<tr>
	<td style="font-weight:bold"><nobr><spring:message code="addresshierarchy.admin.addressField" />:</nobr></td>
	<td>
		<select name="addressField">
			<option value=""></option>
			<c:forEach var="field" items="${addressFields}">
				<c:if test="${nameMappings[field.name] != null}">
					<option value="${field.name}" <c:if test="${level.addressField == field}">selected</c:if> >
						<spring:message code="${nameMappings[field.name]}"/>
					</option>
				</c:if>
			</c:forEach>
			<option value="">(<spring:message code="addresshierarchy.admin.none"/>)</option>
		</select>
	</td>
	<td>&nbsp;</td>
</tr>

<tr>
	<td>
		<button type="submit">
			<spring:message code="addresshierarchy.admin.save" text="Save"/>
		</button>
		<a href="${pageContext.request.contextPath}/module/addresshierarchy/admin/listAddressHierarchyLevels.form">
			<button type="button">
				<spring:message code="addresshierarchy.admin.cancel" text="Cancel"/>
			</button>
		</a>		
	</td>
	<td>&nbsp;</td>
	<td>&nbsp;</td>
</tr>


</table>


</form>

</div>

<%@ include file="/WEB-INF/template/footer.jsp"%>