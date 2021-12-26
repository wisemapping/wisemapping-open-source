<%@page pageEncoding="UTF-8" %>
<%@include file="/jsp/init.jsp" %>

<%--@elvariable id="mindmap" type="com.wisemapping.model.Mindmap"--%>

<!DOCTYPE HTML>

<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <base href="${requestScope['site.baseurl']}/static/mindplot/">
    <title><spring:message code="SITE.TITLE"/> | ${mindmap.title} </title>
    <link rel="stylesheet/less" type="text/css" href="../../css/viewonly.less"/>
    <script type='text/javascript' src="../../js/less.js"/></script>

    <%@ include file="/jsp/commonPageHeader.jsf" %>

   <script type="text/javascript">
          var mapId = '${mindmap.id}';
          var memoryPersistence = true;
          var readOnly = true;
          var userOptions = ${mindmap.properties};
          var locale = '${locale}';
          var isAuth = ${principal != null};
     </script>
     <%@ include file="/jsp/googleAnalytics.jsf" %>
</head>
<body>

<div id="mapContainer">
    <div id="mindplot"></div>
    <div id="printLogo"></div>

    <div id="embFooter">
        <a href="${requestScope['site.homepage']}" target="new">
            <div id="footerLogo"></div>
        </a>

        <div id="zoomOut" class="button"></div>
        <div id="zoomIn" class="button"></div>

        <div id="mapDetails">
            <span class="title"><spring:message code="CREATOR"/>:</span><span><c:out value="${mindmap.creator.fullName}"/></span>
            <span class="title"><spring:message code="DESCRIPTION"/>:</span><span><c:out value="${mindmap.title}"/></span>
        </div>
    </div>
</div>
<script src="loader.js"></script>
</body>
</html>
