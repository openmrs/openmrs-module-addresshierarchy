<ul id="menu">
	<li class="first"><a
		href="${pageContext.request.contextPath}/admin"><spring:message
		code="admin.title.short" /></a></li>
	
		<li
			<c:if test='<%= request.getRequestURI().contains("manageHierarchy") %>'>class="active"</c:if>>
		<a
			href="${pageContext.request.contextPath}/module/addresshierarchy/admin/manageAddressHierarchy.form"><spring:message
			code="addresshierarchy.admin.manageHierarchy" /></a></li>

</ul>