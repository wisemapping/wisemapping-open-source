<!DOCTYPE HTML>

<%@ include file="/jsp/init.jsp" %>
<c:url value="export.htm?exportFormat=IMG_EXPORT_FORMAT&imgFormat=PNG&imgSize=MEDIUM&action=export&mapId=${mindmap.id}"
       var="exportImgUrl"/>

<html>
<head>
    <meta http-equiv="Content-type" value="text/html; charset=utf-8">
    <link rel="stylesheet" type="text/css" href="../css/embedded.css">
    <script type='text/javascript' src='../js/mootools-core-1.3.2-full-compat.js'></script>

</head>
<body>


<div id="mapContainer">
    <div id='mindplot'
         style='background: url( ../images/grid.gif ) bottom left repeat !important;overflow:hidden;width:100%;height:100%;'>
        <div style="width:100%;height:100%;background: url( ${exportImgUrl} );background-position: 0px -100px;background-repeat: no-repeat;"></div>
    </div>
    <div id="embFooter">
        <a href="${pageContext.request.contextPath}/c/home.htm" target="new">
            <div id="logo"></div>
        </a>

        <div id="mapDetails">
            <span class="title"><spring:message code="CREATOR"/>:</span><span>${mindmap.creator}</span>
            <span class="title"><spring:message code="DESCRIPTION"/>:</span><span>${mindmap.description}</span>
        </div>
    </div>
</div>
</body>
</html>
