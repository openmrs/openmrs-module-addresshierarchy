<%@ include file="/WEB-INF/template/include.jsp"%>

<openmrs:require privilege="Manage Address Hierarchy" otherwise="/login.htm"
	redirect="/module/addresshierarchy/admin/listAddressHierarchyLevels.form" />

<%@ include file="/WEB-INF/template/header.jsp"%>
<%@ include file="localHeader.jsp"%>


<!-- SPECIALIZED STYLES FOR THIS PAGE -->
<style type="text/css">
	td {padding-left:4px; padding-right:4px; padding-top:2px; padding-bottom:2px; vertical-align:top}
</style>

<h2><spring:message code="addresshierarchy.admin.title" /></h2>
<br/>
<br/>

<!--  display the address hierarchy types -->
<div>
<b class="boxHeader"><spring:message code="addresshierarchy.admin.levels" /></b>
<table cellpadding="2" cellspacing="0" class="box">
	<tr>
		<th><spring:message code="addresshierarchy.admin.addressField" /></th>
		<th><spring:message code="addresshierarchy.admin.parent" /></th>
		<th>&nbsp;</th>
		<th width="40%">&nbsp;</th>
	</tr>
	<c:forEach items="${levels}" var="level" varStatus="i">
		<tr>
			<td>${nameMappings[level.addressField.name]}</td>
			<td><spring:message code="${level.parent != null ? nameMappings[level.parent.addressField.name] : ''}"/></td>
			<td>
				<a href="${pageContext.request.contextPath}/module/addresshierarchy/admin/editAddressHierarchyLevel.form?levelId=${level.id}">
				   <spring:message code="addresshierarchy.admin.edit" />
				</a>
				<!-- only show delete option for last element -->
				<c:if test="${i.count == fn:length(levels)}">
					&nbsp;|&nbsp;
					<a href="${pageContext.request.contextPath}/module/addresshierarchy/admin/deleteAddressHierarchyLevel.form?levelId=${level.id}"
					   onclick="return confirm('<spring:message code="addresshierarchy.admin.confirmDeleteLevel"/>');">
					   <spring:message code="addresshierarchy.admin.delete" />
					</a>
				</c:if>
			</td>
			<td>&nbsp;</td>
		</tr>
	</c:forEach>
	<tr>
		<td colspan="4">
			<a href="${pageContext.request.contextPath}/module/addresshierarchy/admin/editAddressHierarchyLevel.form">
				<spring:message code="addresshierarchy.admin.addLevel" />
			</a>
		</td>
	</tr>
</table>
</div>
<%@ include file="/WEB-INF/template/footer.jsp"%>