<%@ include file="/WEB-INF/template/include.jsp"%>



<c:if test="${model.layoutShowTable == 'true'}">
	<div id="${model.portletDivName}">
		<table>
</c:if>


<c:forEach var="hierarchyLevel" items="${hierarchyLevels}">
	<tr>
		<td><spring:message code="${model.layoutTemplate.nameMappings[hierarchyLevel.addressField.name]}"/></td>
		<td><spring:bind path="${hierarchyLevel.addressField.name}">
			<input type="text" name="${status.expression}" value="${status.value}"  />
		</spring:bind></td>
	</tr>
</c:forEach>



<c:if test="${model.layoutShowTable == 'true'}">
		</table>
	</div>
</c:if>



	