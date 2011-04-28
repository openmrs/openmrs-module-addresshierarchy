<%@ include file="/WEB-INF/template/include.jsp"%>

<openmrs:require privilege="Manage Address Hierarchy" otherwise="/login.htm"
	redirect="/module/addresshierarchy/admin/listAddressHierarchyTypes.form" />

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
<b class="boxHeader"><spring:message code="addresshierarchy.admin.types" /></b>
<table cellpadding="2" cellspacing="0" class="box">
	<tr>
		<th><spring:message code="addresshierarchy.admin.name" /></th>
		<th><spring:message code="addresshierarchy.admin.parent" /></th>
		<th><spring:message code="addresshierarchy.admin.addressField" /></th>
		<th>&nbsp;</th>
		<th width="40%">&nbsp;</th>
	</tr>
	<c:forEach items="${types}" var="type" varStatus="i">
		<tr>
			<td>${type.name}</td>
			<td>${type.parentType != null ? type.parentType.name : ''}</td>
			<td>${type.addressField.name}</td>
			<td>
				<a href="${pageContext.request.contextPath}/module/addresshierarchy/admin/editAddressHierarchyType.form?typeId=${type.id}">
				   <spring:message code="addresshierarchy.admin.edit" />
				</a>
				<!-- only show delete option for last element -->
				<c:if test="${i.count == fn:length(types)}">
					&nbsp;|&nbsp;
					<a href="${pageContext.request.contextPath}/module/addresshierarchy/admin/deleteAddressHierarchyType.form?typeId=${type.id}"
					   onclick="return confirm('<spring:message code="addresshierarchy.admin.confirmDeleteType"/>');">
					   <spring:message code="addresshierarchy.admin.delete" />
					</a>
				</c:if>
			</td>
			<td>&nbsp;</td>
		</tr>
	</c:forEach>
	<tr>
		<td colspan="5">
			<a href="${pageContext.request.contextPath}/module/addresshierarchy/admin/editAddressHierarchyType.form">
				<spring:message code="addresshierarchy.admin.addType" />
			</a>
		</td>
	</tr>
</table>
</div>
<%@ include file="/WEB-INF/template/footer.jsp"%>