<!DOCTYPE HTML>
<%@page pageEncoding="UTF-8"%>
<%@ taglib prefix="tiles" uri="http://struts.apache.org/tags-tiles" %>
<%@ include file="/jsp/init.jsp" %>

<html>
<head>
    <title>
        <spring:message code="SITE.TITLE"/>
        -
        <spring:message code="INSTALL_CFG"/>

    </title>
    <meta http-equiv="Content-type" content="text/html; charset=UTF-8"/>
    <link rel="stylesheet" type="text/css" href="../css/wisehome.css"/>
    <link rel="icon" href="${pageContext.request.contextPath}/images/favicon.ico" type="image/x-icon"/>
    <link rel="shortcut icon" href="${pageContext.request.contextPath}/images/favicon.ico" type="image/x-icon"/>
    <script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jquery/1.5.0/jquery.min.js"></script>
    <script type="text/javascript"
            src="http://ajax.googleapis.com/ajax/libs/chrome-frame/1/CFInstall.min.js"></script>
</head>
<body>

<jsp:include page="header.jsp"/>

<div class="pageBody">

    <div class="pageBodyContent">
        <div style="width:100%; font-size:130%;">
            <spring:message code="INSTALL_CFG_REASON"/>
            <br/><br/>
            <span style="font-size:20px; font-weight:bold;"><spring:message code="INSTALL_CFG_BROWSERS"/></span>
            <br/><br/>
            <div id="div" style="text-decoration:underline; cursor:pointer; color:blue;"><spring:message code="INSTALL_CFG_CLICK_HERE"/></div>
        </div>

        <div id="prompt">
            <!-- if IE without GCF, prompt goes here -->
        </div>
        <script>
            // The conditional ensures that this code will only execute in IE,
            // Therefore we can use the IE-specific attachEvent without worry
            $(document).ready(function(){
                $("#div").click(function(event){
                    $(".chromeFrameOverlayContent").css("display","block");
                    $(".chromeFrameOverlayUnderlay").css("display","block");
                });
            });
            window.attachEvent("onload", function() {
                CFInstall.check({
                    mode: "overlay" // the default
                });
                $(".chromeFrameOverlayContent").css("display","none");
                $(".chromeFrameOverlayUnderlay").css("display","none");
            });
        </script>
    </div>
</div>

<jsp:include page="footer.jsp"/>
</body>
</html>