<%@page pageEncoding="UTF-8" %>
<%@include file="/jsp/init.jsp" %>

<!DOCTYPE HTML>

<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<tiles:importAttribute name="title" scope="request"/>
<tiles:importAttribute name="details" scope="request"/>

<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <base href="${requestScope['site.baseurl']}/">
    <title>
        <spring:message code="SITE.TITLE"/>-
        <c:choose>
            <c:when test="${requestScope.viewTitle!=null}">
                ${requestScope.viewTitle}
            </c:when>
            <c:otherwise>
                <spring:message code="${requestScope.title}"/>
            </c:otherwise>
        </c:choose>
    </title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <base href="${requestScope['site.baseurl']}/static/mindplot/">
    <title><spring:message code="SITE.TITLE"/> - <c:out value="${mindmap.title}"/></title>
    <link rel="stylesheet/less" type="text/css" href="../../css/error.less"/>
    <script type='text/javascript' src="../../js/less.js"/></script>
    <%@ include file="/jsp/pageHeaders.jsf" %>

</head>
<body>
<div id="errorContainer">
    <div class='col'>
        <h1>Opps !</h1>
        <h2>
            <spring:message code="${requestScope.title}"/>
        </h2>
        <p>
            <spring:message code="${requestScope.details}"/>
        </p>
    </div>
     <div class='column'>
      <a href="https://www.wisemapping.com"><div id="icon"></div></a>
    </div>
</div>
<div id="bottom-logo"></div>
</body>
</html>

