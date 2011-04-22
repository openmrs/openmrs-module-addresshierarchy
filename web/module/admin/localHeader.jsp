<ul id="menu">
	<li class="first"><a
		href="${pageContext.request.contextPath}/admin"><spring:message
		code="admin.title.short" /></a></li>
	
		<li
			<c:if test='<%= request.getRequestURI().contains("manageFlags") %>'>class="active"</c:if>>
		<a
			href="${pageContext.request.contextPath}/module/patientflags/manageFlags.form"><spring:message
			code="patientflags.manageFlags" /></a></li>

		<li
			<c:if test='<%= request.getRequestURI().contains("manageTags") %>'>class="active"</c:if>>
		<a
			href="${pageContext.request.contextPath}/module/patientflags/manageTags.list"><spring:message
			code="patientflags.manageTags" /></a></li>

	
</ul>