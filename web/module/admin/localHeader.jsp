<ul id="menu">
	<li class="first"><a
		href="${pageContext.request.contextPath}/admin"><spring:message
		code="admin.title.short" /></a></li>
	
		<li
			<c:if test='<%= request.getRequestURI().contains("manageLevels") %>'>class="active"</c:if>>
		<a
			href="${pageContext.request.contextPath}/module/addresshierarchy/admin/listAddressHierarchyLevels.form"><spring:message
			code="addresshierarchy.admin.manageLevels" /></a></li>

		<li
			<c:if test='<%= request.getRequestURI().contains("uploadAddressHierarchy") %>'>class="active"</c:if>>
		<a
			href="${pageContext.request.contextPath}/module/addresshierarchy/admin/uploadAddressHierarchy.form"><spring:message
			code="addresshierarchy.admin.uploadAddressHierarchy" /></a></li>
</ul>