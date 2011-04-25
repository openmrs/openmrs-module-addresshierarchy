<ul id="menu">
	<li class="first"><a
		href="${pageContext.request.contextPath}/admin"><spring:message
		code="admin.title.short" /></a></li>
	
		<li
			<c:if test='<%= request.getRequestURI().contains("manageTypes") %>'>class="active"</c:if>>
		<a
			href="${pageContext.request.contextPath}/module/addresshierarchy/admin/listAddressHierarchyTypes.form"><spring:message
			code="addresshierarchy.admin.manageTypes" /></a></li>

		<li
			<c:if test='<%= request.getRequestURI().contains("manageTags") %>'>class="active"</c:if>>
		<a
			href="${pageContext.request.contextPath}/module/patientflags/manageTags.list"><spring:message
			code="patientflags.manageTags" /></a></li>

	
</ul>