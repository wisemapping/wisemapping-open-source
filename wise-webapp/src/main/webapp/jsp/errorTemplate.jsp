<!DOCTYPE HTML>

<%@ page import="org.apache.log4j.Logger" %>
<%@ page import="com.wisemapping.security.Utils" %>
<%@ page import="com.wisemapping.model.User" %>
<%@ page autoFlush="true" buffer="none" %>
<%@ include file="/jsp/init.jsp" %>

<%!
    final Logger logger = Logger.getLogger("com.wisemapping");
%>
<h2>
    <spring:message code="${requestScope.title}"/>
</h2>

<strong>
    <spring:message code="${requestScope.details}"/>
</strong>
<!--
<%
    final Throwable exception = (Throwable) request.getAttribute("exception");
    if (exception != null) {
        exception.printStackTrace(response.getWriter());
        String usrMail = "anonymous";
        final User user = Utils.getUser(false);
        if (user != null) {
            usrMail = user.getEmail();
        }

        logger.error("Unexpected error on user '" + usrMail + " ':", exception);

    }
%>

-->

