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

    <meta name="author" content="WiseMapping">
    <meta name="publisher" content="WiseMapping Open Source">
    <meta name="keywords"
          content="mindmap,mind map,mind maps,mindmaps,ideas,brainstorming,organize,thoughts,structure,collaboration,free,fast,simple,online,tool,knowledge,share,sharing,publish">
    <meta name="description"
          content="WiseMapping is a free, fast and simple online mind mapping editor for individuals and business. Sign up to start organizing and sharing your ideas and thoughts.">
    <meta property="og:title" content="WiseMapping"/>
    <meta property="og:type" content="website"/>
    <meta property="og:url" content="http://www.wisemapping.com"/>
    <meta property="og:image" content="http://www.wisemapping.com/images/logo.png"/>
    <meta property="og:site_name" content="WiseMapping.com"/>

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
            <c:if test="${requestScope['google.ads.enabled']}">
                <div class="row" style="text-align: center;margin: 5px">
                    <script type="text/javascript"><!--
                    google_ad_client = "ca-pub-7564778578019285";
                    /* WiseMapping Login */
                    google_ad_slot = "2192653431";
                    google_ad_width = 728;
                    google_ad_height = 90;
                    //-->
                    </script>
                    <script type="text/javascript"
                            src="https://pagead2.googlesyndication.com/pagead/show_ads.js">
                    </script>
                </div>
            </c:if>
        </div>
    </div>

    <jsp:include page="footer.jsp"/>
</div>
</body>
</html>

