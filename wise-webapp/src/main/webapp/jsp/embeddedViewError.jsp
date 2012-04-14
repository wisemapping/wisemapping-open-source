<!DOCTYPE HTML>
<%@ include file="/jsp/init.jsp" %>
<html>
<head>
    <title>
        <spring:message code="NO_ENOUGH_PERMISSIONS"/>
    </title>
    <meta http-equiv="Content-type" value="text/html; charset=utf-8">
    <link rel="stylesheet" type="text/css" href="../css/embedded.css">
    <link rel="icon" href="${pageContext.request.contextPath}/images/favicon.ico" type="image/x-icon">
    <link rel="shortcut icon" href="${pageContext.request.contextPath}/images/favicon.ico" type="image/x-icon">
    <!--[if lt IE 9]>
    <link rel="stylesheet" type="text/css" href="../css/wisehomeOldIE.css"/>
    <![endif]-->
</head>
<body>

    <div class="pageBodyContent" style="padding:40px;padding-top:100px;">
    <h1>
     <spring:message code="NO_ENOUGH_PERMISSIONS"/>
     </h1>
    <p>
    <spring:message code="NO_ENOUGH_PERMISSIONS_DETAILS"/>
    </p>
    </div>
    <div id="embFooter">
        <a href="${pageContext.request.contextPath}/c/home.htm" target="new">
            <div id="logo"></div>
        </a>
    </div>
</body>
</html>



