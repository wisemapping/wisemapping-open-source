<%@ page import="org.apache.log4j.Logger" %>
<%@ page autoFlush="true" buffer="none" %>
<%@ include file="/jsp/init.jsp" %>

<%!
    final Logger logger = Logger.getLogger("com.wisemapping");
%>
<h1>
    <spring:message code="${requestScope.title}"/>
</h1>

<p style="font-weight:bold;">
     <spring:message code="${requestScope.details}"/>
</p>
<!--
<%
    final Throwable exception = (Throwable) request.getAttribute("exception");
    if (exception != null) {
        exception.printStackTrace(response.getWriter());
        String usrMail = "anonymous";
        if(user!=null)
        {
            usrMail = user.getEmail();
        }

        logger.error("Unexpected error on user '" + usrMail+ " ':", exception);

    }
%>

-->

