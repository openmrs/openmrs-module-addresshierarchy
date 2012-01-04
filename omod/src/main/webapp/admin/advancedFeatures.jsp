<%@ include file="/WEB-INF/template/include.jsp"%>

<openmrs:require privilege="Manage Address Hierarchy" otherwise="/login.htm"
	redirect="/module/addresshierarchy/admin/manageAddressHierarchy.form" />

<%@ include file="/WEB-INF/template/header.jsp"%>
<%@ include file="localHeader.jsp"%>

<!-- SPECIALIZED STYLES FOR THIS PAGE -->
<style type="text/css">
	td.tableCell {padding-left:4px; padding-right:4px; padding-top:2px; padding-bottom:2px; vertical-align:top}
</style>

<h2><spring:message code="addresshierarchy.admin.advancedFeatures" /></h2>
<br/>
<br/>

<!--  DISPLAY ANY ERROR MESSAGES -->
<c:if test="${fn:length(messages) > 0}">
	<c:forEach var="message" items="${messages}">
		<span class="error"><spring:message code="${message}"/></span><br/><br/>
	</c:forEach>
	<br/>
</c:if>


<div><b class="boxHeader"><spring:message code="addresshierarchy.admin.configureAddressToEntryMappings" /></b>

<form id="scheduleAddressToEntryMapping" action="scheduleAddressToEntryMapping.form" method="post">

<table cellspacing="0" cellpadding="0" class="box">

<tr>
	<td class="tableCell"><spring:message code="addresshierarchy.admin.configureAddressToEntryMappings.instructions" /></td>
</tr>

<tr>
	<td class="tableCell"><nobr><input type="checkbox" name="updaterStarted" <c:if test="${updaterStarted}">checked</c:if>/> <spring:message code="addresshierarchy.admin.scheduleAddressToEntryMappings" arguments='<input type="text" name="repeatInterval" value="${repeatInterval}" size="6"/>'/></nobr></td>
</tr>

<tr>
	<td class="tableCell"><nobr><input type="checkbox" name="recalculateMappings" <c:if test="${recalculateMappings}">checked</c:if>/> <spring:message code="addresshierarchy.admin.recalculateAddressToEntryMappings" /></nobr></td>
</tr>

<tr>
	<td class="tableCell">
		<button type="submit">
			<spring:message code="general.save" text="Save"/>
		</button>
		<a href="${pageContext.request.contextPath}/module/addresshierarchy/admin/manageAddressHierarchy.form">
			<button type="button">
				<spring:message code="general.cancel" text="Cancel"/>
			</button>
		</a>	
	</td>
	<td class="tableCell">&nbsp;</td>
	<td class="tableCell">&nbsp;</td>
</tr>


</table>


</form>

</div>

<%@ include file="/WEB-INF/template/footer.jsp"%>