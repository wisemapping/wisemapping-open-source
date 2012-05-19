<!DOCTYPE HTML>
<%@page pageEncoding="UTF-8" %>
<%@ taglib prefix="tiles" uri="http://struts.apache.org/tags-tiles" %>
<%@ include file="/jsp/init.jsp" %>

<tiles:importAttribute name="title" scope="request"/>
<tiles:importAttribute name="details" scope="request"/>
<html>
<head>
    <base href="${pageContext.request.contextPath}/"/>
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
    <link rel="stylesheet" type="text/css" href="css/pageTemplate.css"/>

    <link rel="icon" href="${pageContext.request.contextPath}/images/favicon.ico" type="image/x-icon"/>
    <link rel="shortcut icon" href="${pageContext.request.contextPath}/images/favicon.ico" type="image/x-icon"/>

    <script type="text/javascript" language="javascript" src="js/jquery-1.7.2.min.js"></script>
    <script type="text/javascript" language="javascript" src="bootstrap/js/bootstrap.js"></script>

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
