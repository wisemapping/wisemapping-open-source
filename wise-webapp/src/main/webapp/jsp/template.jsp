<%@page pageEncoding="UTF-8" %>
<%@include file="/jsp/init.jsp" %>

<!DOCTYPE HTML>

<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<tiles:importAttribute name="title" scope="request"/>
<tiles:importAttribute name="details" scope="request"/>
<tiles:importAttribute name="removeSignin" scope="request" ignore="true"/>

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
    <%@ include file="/jsp/commonPageHeader.jsf" %>
    <link rel="stylesheet" type="text/css" href="css/pageTemplate.css"/>
</head>
<body>

<div id="pageContainer">
    <div class="container">
        <div class="row">
            <div class="col-md-offset-1 col-md-10">
                <div class="jumbotron" style="margin:40px 0 15px 0;padding: 10px 60px;">
                    <tiles:insertAttribute name="body"/>
                </div>
            </div>
        </div>
    </div>
</div>
</body>
</html>

