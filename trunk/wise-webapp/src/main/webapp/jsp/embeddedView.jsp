<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">

<%@ include file="/jsp/init.jsp" %>

<html>
<head>
    <meta http-equiv="Content-type" value="text/html; charset=utf-8">
    <link rel="stylesheet" type="text/css" href="../css/embedded.css">
    <link rel="stylesheet" type="text/css" href="../css/bubble.css">

    <script type='text/javascript' src='../js/mootools.js'></script>
    <script type='text/javascript' src='../js/core.js'></script>

</head>
<body>

<div id="waitDialog" style="display:none">
    <div id="loadingContainer">
        <div class="loadingIcon"></div>
        <div class="loadingText">
            <spring:message code="EDITOR.LOADING"/>
        </div>
    </div>
</div>
<script type="text/javascript">

    //Dialog box display ...
    var waitDialog = new core.WaitDialog();
    waitDialog.activate(true, $("waitDialog"));

    $(window).addEvent("error", function(event) {

        // Show error dialog ...
        waitDialog.changeContent($("errorDialog"), false);
        return false;
    });


    var mapId = '${mindmap.id}';
    var mapXml = '${mapXml}';
    var editorProperties = {zoom:${zoom}};
</script>

<div id="mapContainer">
    <div id="mindplot"></div>
    <div id="embFooter">
        <a href="${pageContext.request.contextPath}/c/home.htm" target="new">
            <div id="logo"></div>
        </a>

        <div id="zoomIn" class="button"></div>
        <div id="zoomOut" class="button"></div>
        <div id="mapDetails">
            <span class="title"><spring:message code="CREATOR"/>:</span><span>${mindmap.creator}</span>
            <span class="title"><spring:message code="DESCRIPTION"/>:</span><span>${mindmap.description}</span>
        </div>
    </div>
</div>
<div id="ffoxworkarround" style="display:none;"><input id="ffoxWorkarroundInput" type="text"></div>
<script type="text/javascript" src="../js/embedded.js"></script>
</body>
</html>
