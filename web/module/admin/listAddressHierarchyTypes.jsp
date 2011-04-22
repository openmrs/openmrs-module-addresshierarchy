<%@ include file="/WEB-INF/template/include.jsp"%>

<%@ include file="/WEB-INF/template/header.jsp"%>
<%@ include file="localHeader.jsp"%>


<!-- SPECIALIZED STYLES FOR THIS PAGE -->
<style type="text/css">
	td {padding-left:4px; padding-right:4px; padding-top:2px; padding-bottom:2px; vertical-align:top}
</style>

<h2><spring:message code="addresshierarchy.admin.title" /></h2>
<a href="${pageContext.request.contextPath}/module/addresshierarchy/admin/editAddressHierarchyType.form">
	<spring:message code="addresshierarchy.admin.addType" />
</a>
<br/>
<br/>

<!--  display the address hierarchy types -->
<div>
<b class="boxHeader"><spring:message code="addresshierarchy.admin.types" /></b>
<table cellpadding="2" cellspacing="0" class="box">
	<tr>
		<th><spring:message code="addresshierarchy.admin.name" /></th>
		<th><spring:message code="addresshierarchy.admin.parent" /></th>
		<th><spring:message code="addresshierarchy.admin.child" /></th>
		<th><spring:message code="addresshierarchy.admin.addressField" /></th>
		<th>&nbsp;</th>
	</tr>
	<c:forEach items="${types}" var="type">
		<td>${type.name}</td>
		<td>${type.parentType != null ? type.parentType.name : ''}</td>
		<td>${type.childType != null ? type.parentType.name : ''}</td>
		<td>${type.addressField}</td>
		<th>&nbsp;</th>
	</c:forEach>
</table>
</div>
<%@ include file="/WEB-INF/template/footer.jsp"%>