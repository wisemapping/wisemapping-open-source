<%@page pageEncoding="UTF-8" %>
<%@include file="/jsp/init.jsp" %>

<h2><spring:message code="BROWSER_NOT_SUPPORTED_TITLE"/></h2>

<p><spring:message code="BROWSER_NOT_SUPPORTED_MSG"/></p>
<ul>
    <li>Chrome 19 <spring:message code="OR_GREATER"/></li>
    <li>Firefox 11.0 <spring:message code="OR_GREATER"/></li>
    <li>Opera 11 <spring:message code="OR_GREATER"/></li>
    <li>Safari 5 <spring:message code="OR_GREATER"/></li>
    <li>Internet Explorer 8.0 <spring:message code="OR_GREATER"/></li>
</ul>
<p>
    <spring:message code="BROWSER_NOT_SUPPORTED_TRY_AGAIN"/>
</p>

<p>
    <span class="label label-info"><spring:message code="IMPORTANT"/>: </span><spring:message
        code="BROWSER_RECOMMENDATION"/>
</p>
