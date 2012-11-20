<%@page pageEncoding="UTF-8" %>
<%@include file="/jsp/init.jsp" %>

<!DOCTYPE HTML>

<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<tiles:importAttribute name="title" scope="request"/>
<tiles:importAttribute name="details" scope="request"/>
<tiles:importAttribute name="removeSignin" scope="request" ignore="true"/>

<html>
<head>
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
    <link rel="stylesheet" type="text/css" href="css/pageTemplate.css"/>

    <link rel="icon" href="images/favicon.ico" type="image/x-icon"/>
    <link rel="shortcut icon" href="images/favicon.ico" type="image/x-icon"/>

    <script type="text/javascript" language="javascript" src="js/jquery-1.7.2.min.js"></script>
    <script type="text/javascript" language="javascript" src="bootstrap/js/bootstrap.js"></script>
    <script src="js/less.js" type="text/javascript"></script>
</head>
<body>

<div id="pageContainer">
    <jsp:include page="header.jsp">
        <jsp:param name="removeSignin" value="${requestScope.removeSignin}"/>
    </jsp:include>

    <div class="bodyContainer">
        <div class="row-fluid">
            <div class="span2"></div>
            <div class="pageBodyContent span8">
                <tiles:insertAttribute name="body"/>
            </div>
            <div class="span1" style="padding-top:0px">
                <c:if test="${requestScope['google.ads.enabled']}">
                    <script type="text/javascript"><!--
                    google_ad_client = "ca-pub-7564778578019285";
                    /* WiseMapping Page Template */
                    google_ad_slot = "2051548516";
                    google_ad_width = 120;
                    google_ad_height = 600;
                    //-->
                    </script>
                    <script type="text/javascript"
                            src="http://pagead2.googlesyndication.com/pagead/show_ads.js">
                    </script>
                </c:if>
            </div>
        </div>
    </div>

    <jsp:include page="footer.jsp"/>
</div>
</body>
</html>

