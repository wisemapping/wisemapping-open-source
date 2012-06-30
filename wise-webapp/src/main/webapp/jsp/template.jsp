<%@page pageEncoding="UTF-8" %>
<%@include file="/jsp/init.jsp" %>

<!DOCTYPE HTML>

<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<tiles:importAttribute name="title" scope="request"/>
<tiles:importAttribute name="details" scope="request"/>

<html>
<head>
    <base href="${baseURL}/">
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
    <link rel="stylesheet" type="text/css" href="css/pageTemplate.css"/>

    <link rel="icon" href="images/favicon.ico" type="image/x-icon"/>
    <link rel="shortcut icon" href="images/favicon.ico" type="image/x-icon"/>

    <script type="text/javascript" language="javascript" src="js/jquery-1.7.2.min.js"></script>
    <script type="text/javascript" language="javascript" src="bootstrap/js/bootstrap.js"></script>
    <script src="js/less.js" type="text/javascript"></script>
</head>
<body>

<jsp:include page="header.jsp"/>

<div class="pageBody">
    <div class="pageBodyContent">
        <tiles:insertAttribute name="body"/>
    </div>
</div>

<jsp:include page="footer.jsp"/>
</body>
</html>
