<%@ include file="/WEB-INF/template/include.jsp"%>

<openmrs:require privilege="Manage Address Hierarchy" otherwise="/login.htm"
	redirect="/module/addresshierarchy/admin/uploadAddressHierarchy.form" />

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