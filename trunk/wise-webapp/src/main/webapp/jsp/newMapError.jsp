<%@ include file="/jsp/init.jsp" %>

<h1>
    <spring:message code="${requestScope.title}"/>
</h1>

<p style="font-weight:bold;">
    <spring:message code="${requestScope.details}"/>
</p>

<jsp:include page="/jsp/newMap.jsp"/>