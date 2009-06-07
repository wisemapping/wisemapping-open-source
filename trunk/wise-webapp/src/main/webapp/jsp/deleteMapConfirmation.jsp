<%@ include file="/jsp/init.jsp" %>
<c:url value="mymaps.htm" var="deleteMapUrl">
    <c:param name="action" value="delete"/>
    <c:param name="mapId" value="${mindmap.id}"/>
    <c:param name="userEmail" value="${pageContext.request.userPrincipal.name}"/>
</c:url>
<div>
    <spring:message code="DELETE_SELECTED_CONFIRMATION"/>
</div>
<form method="post" id="deleteConfirmation" action="<c:out value="${deleteMapUrl}"/>">
    <input type="hidden" name="action" value="deleteMap">
    <input type="submit" value="<spring:message code="YES"/>">
    <input type="button" value="<spring:message code="NO"/>" onclick="MOOdalBox.close();">
</form>