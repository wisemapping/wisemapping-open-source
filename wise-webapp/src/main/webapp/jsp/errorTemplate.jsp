<!DOCTYPE HTML>

<%@ page autoFlush="true" buffer="none" %>
<%@ include file="/jsp/init.jsp" %>

<h2>
    <spring:message code="${requestScope.title}"/>
</h2>

<strong>
    <spring:message code="${requestScope.details}"/>
</strong>

