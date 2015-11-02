<%@page pageEncoding="UTF-8" %>
<%@include file="/jsp/init.jsp" %>

<%--@elvariable id="mindmap" type="com.wisemapping.model.Mindmap"--%>
<%--@elvariable id="editorTryMode" type="java.lang.Boolean"--%>
<%--@elvariable id="editorTryMode" type="java.lang.String"--%>

<!DOCTYPE HTML>

<html>
<head>
    <base href="${requestScope['site.baseurl']}/">
    <title><spring:message code="SITE.TITLE"/> - ${mindmap.title} </title>

    <!--[if lt IE 9]>
    <meta http-equiv="X-UA-Compatible" content="chrome=1">
    <![endif]-->

    <link rel="stylesheet/less" type="text/css" href="css/embedded.less"/>

    <style type="text/css" media="print">
        @page {
            size: A4 landscape;
        }

        body {
            overflow: visible
        }

        #embFooter {
            display: none;
        }

        #footerLogo {
            display: block;
        }

        div#printLogo {
            width: 114px;
            height: 56px;
            position: absolute;
            display: list-item;
            list-style-image: url(../images/logo-xsmall.png);
            list-style-position: inside;
            right: 10px;
            bottom: -30px;
        }

    </style>

    <script type='text/javascript' src='js/jquery.js'></script>
    <script type='text/javascript' src='js/jquery-mousewheel.js'></script>
    <script type='text/javascript' src='js/hotkeys.js'></script>
    <script type='text/javascript' src='js/underscorejs.js'></script>
    <script type='text/javascript' src='bootstrap/js/bootstrap.min.js'></script>
    <script type='text/javascript' src='js/mootools-core.js'></script>
    <script type='text/javascript' src='js/core.js'></script>
    <script type='text/javascript' src='js/less.js'></script>


    <link rel="icon" href="images/favicon.ico" type="image/x-icon">
    <link rel="shortcut icon" href="images/favicon.ico" type="image/x-icon">

    <script type="text/javascript">
        var mapId = '${mindmap.id}';

        $(document).on('loadcomplete', function (resource) {

            // Configure designer options ...
            var options = loadDesignerOptions();
            options.locale = '${locale}';
            options.size.height = options.size.height + 50;

            var userOptions = ${mindmap.properties};
            options.zoom = userOptions.zoom;

            // Set map id ...
            options.mapId = mapId;

            // Print is read only ...
            options.readOnly = true;

            // Configure loader ...
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
<script type="text/javascript" src="js/editor.js"></script>
<%@ include file="/jsp/googleAnalytics.jsf" %>
</body>
</html>
