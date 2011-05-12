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
		<th><spring:message code="addresshierarchy.admin.level" /></th>
		<th><spring:message code="addresshierarchy.admin.name" /></th>
		<th><spring:message code="addresshierarchy.admin.exampleEntry" /></th>
		<th><spring:message code="addresshierarchy.admin.mappedAddressField" /></th>
		<th>&nbsp;</th>
		<th width="30%">&nbsp;</th>
	</tr>
	<c:forEach items="${levels}" var="level" varStatus="i">
		<tr>
			<td>${i.count}</td>
			<td>${level.name}</td>
			<td>${sampleEntries[i.count-1][0]} (${sampleEntries[i.count-1][1]} <spring:message code="addresshierarchy.admin.totalEntries"/>)</td>
			<td>
				<c:choose>
					<c:when test="${! empty level.addressField}">
						<spring:message code="${nameMappings[level.addressField.name]}"/>
					</c:when>
					<c:otherwise>
						(<spring:message code="addresshierarchy.admin.none"/>)
					</c:otherwise>
				</c:choose>
			</td>
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

<br/>
<br/>

<!--  DISPLAY ANY ERROR MESSAGES -->
<c:if test="${fn:length(messages) > 0}">
	<c:forEach var="message" items="${messages}">
		<span class="error"><spring:message code="${message}"/></span><br/><br/>
	</c:forEach>
	<br/>
</c:if>

<div><b class="boxHeader"><spring:message code="addresshierarchy.admin.uploadAddressHierarchy" /></b>

<form id="uploadAddressHierarchy" action="uploadAddressHierarchy.form" method="post" enctype="multipart/form-data">

<table cellspacing="0" cellpadding="0" class="box">

<tr>
	<td style="font-weight:bold"><nobr><spring:message code="addresshierarchy.admin.fileName" />:</nobr></td>
	<td><input type="file" name="file" size="50" /></td>
	<td width="60%">&nbsp;</td>
</tr>

<tr>
	<td style="font-weight:bold"><nobr><spring:message code="addresshierarchy.admin.delimiter" />:</nobr></td>
	<td><input type="text" name="delimiter" size="1" value="${delimiter}" /></td>
	<td>&nbsp;</td>
</tr>

<tr>
	<td style="font-weight:bold"><nobr><spring:message code="addresshierarchy.admin.overwrite" />:</nobr></td>
	<td><input type="checkbox" name="overwrite" <c:if test="${overwrite}">checked</c:if>/></td>
	<td>&nbsp;</td>
</tr>

<tr>
	<td>
		<button type="submit">
			<spring:message code="addresshierarchy.admin.save" text="Upload"/>
		</button>
		<button type="reset">
			<spring:message code="addresshierarchy.admin.cancel" text="Cancel"/>
		</button>	
	</td>
	<td>&nbsp;</td>
	<td>&nbsp;</td>
</tr>


</table>


</form>

</div>

<%@ include file="/WEB-INF/template/footer.jsp"%>