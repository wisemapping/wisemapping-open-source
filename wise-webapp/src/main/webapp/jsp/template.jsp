<!DOCTYPE HTML>
<%@page pageEncoding="UTF-8" %>
<%@ taglib prefix="tiles" uri="http://struts.apache.org/tags-tiles" %>
<%@ include file="/jsp/init.jsp" %>

<tiles:importAttribute name="title" scope="request"/>
<tiles:importAttribute name="details" scope="request"/>
<html>
<head>
    <title>
        <spring:message code="SITE.TITLE"/>
        -
        <c:choose>
            <c:when test="${requestScope.viewTitle!=null}">
                ${requestScope.viewTitle}
            </c:when>
            <c:otherwise>
                <spring:message code="${requestScope.title}"/>
            </c:otherwise>
        </c:choose>
    </title>
    <meta http-equiv="Content-type" content="text/html; charset=UTF-8"/>
    <link rel="stylesheet" type="text/css" href="../css/wisehome.css"/>
    <link rel="icon" href="${pageContext.request.contextPath}/images/favicon.ico" type="image/x-icon"/>
    <link rel="shortcut icon" href="${pageContext.request.contextPath}/images/favicon.ico" type="image/x-icon"/>
    <script type="text/javascript" src="${pageContext.request.contextPath}/js/wiseLibrary.js"></script>
    <script type='text/javascript'
            src='https://ajax.googleapis.com/ajax/libs/mootools/1.3.2/mootools-yui-compressed.js'></script>
    <script type='text/javascript' src='../js/libraries/mootools/mootools-more-1.3.2.1-yui.js'></script>

</head>
<body>

<jsp:include page="header.jsp"/>

<div class="pageBody">

    <div class="pageBodyContent">
        <tiles:insert name="body"/>
    </div>
</div>

<jsp:include page="footer.jsp"/>
</body>
</html>
