<%@page pageEncoding="UTF-8" %>
<%@include file="/jsp/init.jsp" %>

<!DOCTYPE HTML>

<%--@elvariable id="mindmap" type="com.wisemapping.model.Mindmap"--%>
<%--@elvariable id="editorTryMode" type="java.lang.Boolean"--%>
<%--@elvariable id="editorTryMode" type="java.lang.String"--%>


<html>
<head>
    <base href="${requestScope['site.baseurl']}/">
    <title><spring:message code="SITE.TITLE"/> - ${mindmap.title} </title>

    <!--[if lt IE 9]>
    <meta http-equiv="X-UA-Compatible" content="chrome=1">
    <![endif]-->

    <link rel="stylesheet/less" type="text/css" href="css/embedded.less"/>
    <script type='text/javascript' src='js/jquery.js'></script>
    <script type='text/javascript' src='js/jquery-mousewheel.js'></script>
    <script type='text/javascript' src='js/hotkeys.js'></script>
    <script type='text/javascript' src='js/underscorejs.js'></script>
    <script type='text/javascript' src='bootstrap/js/bootstrap.min.js'></script>
    <script type='text/javascript' src='js/mootools-core.js'></script>
    <script type='text/javascript' src='js/core.js'></script>
    <script type='text/javascript' src='js/less.js'></script>


    <script type="text/javascript">
        var mapId = '${mindmap.id}';

        $(document).on('loadcomplete', function (resource) {

            // Configure designer options ...
            var options = loadDesignerOptions();
            options.size.height = options.size.height + 50;
            options.locale = '${locale}';


            var userOptions = ${mindmap.properties};
            options.zoom = ${zoom};

            // Set map id ...
            options.mapId = mapId;

            // Print is read only ...
            options.readOnly = true;

            // Configure persistence ...
            options.persistenceManager = new mindplot.LocalStorageManager("c/restful/maps/{id}/document/xml${principal!=null?'':'-pub'}",true);

            // Build designer ...
            var designer = buildDesigner(options);

            // Load map ...
            var persistence = mindplot.PersistenceManager.getInstance();
            var mindmap = mindmap = persistence.load(mapId);
            designer.loadMap(mindmap);
        });
    </script>
</head>
<body>
<div id="mapContainer">
    <div id="mindplot" onselectstart="return false;"></div>

    <div id="embFooter">
        <a href="${requestScope['site.homepage']}" target="new">
            <div id="footerLogo"></div>
        </a>

        <div id="zoomIn" class="button"></div>
        <div id="zoomOut" class="button"></div>

        <div id="mapDetails">
            <span class="title"><spring:message code="CREATOR"/>:</span><span><c:out value="${mindmap.creator.fullName}"/></span>
            <span class="title"><spring:message code="DESCRIPTION"/>:</span><span><c:out value="${mindmap.title}"/></span>
        </div>
    </div>
</div>
<script type="text/javascript" src="js/editor.js"></script>
<%@ include file="/jsp/googleAnalytics.jsf" %>
</body>
</html>
