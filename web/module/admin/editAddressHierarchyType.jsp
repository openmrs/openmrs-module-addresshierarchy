<%@ include file="/WEB-INF/template/include.jsp"%>

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
<div><b class="boxHeader"><spring:message code="addresshierarchy.admin.editType" /></b>


<form id="editAddressHierarchyType" action="updateAddressHierarchyType.form" method="post">

<!--  DISPLAY ANY ERROR MESSAGES -->
<c:if test="${fn:length(type.allErrors) > 0}">
	<c:forEach var="error" items="${type.allErrors}">
		<span class="error"><spring:message code="${error.code}"/></span><br/><br/>
	</c:forEach>
	<br/>
</c:if>

<table cellspacing="0" cellpadding="0" class="box">

<tr>
	<td style="font-weight:bold"><nobr><spring:message code="addresshierarchy.admin.name" />:</nobr></td>
	<td><input type="text" size="10" name="name" value="${type.name}"/></td>
</tr>

<tr>
	<td style="font-weight:bold"><nobr><spring:message code="addresshierarchy.admin.parent" />:</nobr></td>
	<td>
		<select name="parentType">
			<option value=""></option>
			<c:forEach var="t" items="${types}">
				<option value="${t.id}" <c:if test="${type.parentType == t}">selected</c:if> >${t.name}</option>
			</c:forEach>
		</select>
	</td>
</tr>

<tr>
	<td style="font-weight:bold"><nobr><spring:message code="addresshierarchy.admin.parent" />:</nobr></td>
	<td>
		<select name="childType">
			<option value=""></option>
			<c:forEach var="t" items="${types}">
				<option value="${t.id}" <c:if test="${type.parentType == t}">selected</c:if> >${t.name}</option>
			</c:forEach>
		</select>
	</td>
</tr>

<tr>
	<td style="font-weight:bold"><nobr><spring:message code="addresshierarchy.admin.addressField" />:</nobr></td>
	<td>
		<select name="addressField">
			<option value=""></option>
			<c:forEach var="field" items="${addressFields}">
				<option value="${field.name}" <c:if test="${type.addressField == field}">selected</c:if> >${field.name}</option>
			</c:forEach>
		</select>
	</td>
</tr>

</table>

<button type="submit">
	<spring:message code="addresshierarchy.admin.save" text="Save"/>
</button>
<button type="reset">
	<spring:message code="addresshierarchy.admin.cancel" text="Cancel"/>
</button>

</form>

</div>

<%@ include file="/WEB-INF/template/footer.jsp"%>