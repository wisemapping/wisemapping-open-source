<!DOCTYPE HTML>

<%@ include file="/jsp/init.jsp" %>

<html>
<head>
    <!--[if lt IE 9]>
    <meta http-equiv="X-UA-Compatible" content="chrome=1">
    <![endif]-->
    <title><spring:message code="SITE.TITLE"/> - ${mindmap.title} </title>
    <meta http-equiv="Content-type" content="text/html; charset=UTF-8"/>

    <link rel="stylesheet" type="text/css" href="../css/embedded.css"/>

    <script type='text/javascript' src='../js/mootools-core.js'></script>
    <script type='text/javascript' src='../js/mootools-more.js'></script>
    <script type='text/javascript' src='../js/core.js'></script>


    <script type="text/javascript">
        var mapId = '${mindmap.id}';
        var mapXml = '${mapXml}';
        var mindReady = false;
        $(document).addEvent('loadcomplete', function(resource) {
            mindReady = resource == 'mind' ? true : mindReady;
            if (mindReady) {

                var editorProperties = {zoom:${zoom},saveOnLoad:true,collab:'standalone',readOnly:true};
                designer = buildDesigner(editorProperties);

                var parser = new DOMParser();
                var domDocument = parser.parseFromString(mapXml, "text/xml");

                var serializer = mindplot.persistence.XMLSerializerFactory.getSerializerFromDocument(domDocument);
                var mindmap = serializer.loadFromDom(domDocument, mapId);

                // Now, load the map ...
                designer.loadMap(mindmap);

                // If not problem has arisen, close the dialog ...
                if (!window.hasUnexpectedErrors) {
                    waitDialog.deactivate();
                }

                $('zoomIn').addEvent('click', function() {
                    designer.zoomIn();
                });

                $('zoomOut').addEvent('click', function() {
                    designer.zoomOut();
                });
            }
        });
    </script>
</head>
<body>

<div id="waitDialog" style="display:none">
    <div id="waitingContainer">
        <div class="loadingIcon"></div>
        <div class="loadingText">
            Loading ...
        </div>
    </div>
</div>

<div id="errorDialog" style="display:none">
    <div id="errorContainer">
        <div class="loadingIcon"></div>
        <div class="loadingText">
            Unexpected error loading your map :(
        </div>
    </div>
</div>

<script type="text/javascript">

    var waitDialog = new core.WaitDialog();
    waitDialog.activate(true, $("waitDialog"));
    $(window).addEvent("error", function(event) {

        // Show error dialog ...
        waitDialog.changeContent($("errorDialog"), false);
        return false;
    });
</script>


<div id="mapContainer">
    <div id="mindplot"></div>
    <div id="embFooter">
        <a href="${pageContext.request.contextPath}/c/home" target="new">
            <div id="logo"></div>
        </a>

        <div id="zoomIn" class="button"></div>
        <div id="zoomOut" class="button"></div>
        <div id="mapDetails">
            <%--<span class="title"><spring:message code="CREATOR"/>:</span><span>${mindmap.creator}</span>--%>
            <span class="title"><spring:message code="DESCRIPTION"/>:</span><span>${mindmap.title}</span>
        </div>
    </div>
</div>
<script type="text/javascript" src="../js/editor.js"></script>
</body>
</html>
