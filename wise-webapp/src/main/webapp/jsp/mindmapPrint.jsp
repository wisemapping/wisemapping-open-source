<%@page pageEncoding="UTF-8" %>
<%@include file="/jsp/init.jsp" %>

<%--@elvariable id="mindmap" type="com.wisemapping.model.MindMap"--%>
<%--@elvariable id="editorTryMode" type="java.lang.Boolean"--%>
<%--@elvariable id="editorTryMode" type="java.lang.String"--%>
<%--@elvariable id="mapXml" type="com.wisemapping.model.User"--%>

<!DOCTYPE HTML>

<html>
<head>
    <base href="${baseURL}/">
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

    <script type='text/javascript' src='js/mootools-core.js'></script>
    <script type='text/javascript' src='js/mootools-more.js'></script>
    <script type='text/javascript' src='js/core.js'></script>
    <script type='text/javascript' src='js/less.js'></script>


    <link rel="icon" href="images/favicon.ico" type="image/x-icon">
    <link rel="shortcut icon" href="images/favicon.ico" type="image/x-icon">

    <script type="text/javascript">
        var mapId = '${mindmap.id}';
        var mapXml = '${mindmap.xmlAsJsLiteral}';

        $(document).addEvent('loadcomplete', function (resource) {

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

            // Build designer ...
            var designer = buildDesigner(options);

            // Load map from XML ...
            var parser = new DOMParser();
            var domDocument = parser.parseFromString(mapXml, "text/xml");

            var mindmap = mindplot.PersistenceManager.loadFromDom(mapId, domDocument);
            designer.loadMap(mindmap);

            $('zoomIn').addEvent('click', function () {
                designer.zoomIn();
            });

            $('zoomOut').addEvent('click', function () {
                designer.zoomOut();
            });
        });
    </script>
</head>
<body>

<div id="mapContainer">
    <div id="mindplot"></div>
    <div id="printLogo"></div>

    <div id="embFooter">
        <a href="c/home" target="new">
            <div id="footerLogo"></div>
        </a>

        <div id="zoomIn" class="button"></div>
        <div id="zoomOut" class="button"></div>

        <div id="mapDetails">
            <span class="title"><spring:message code="CREATOR"/>:</span><span>${mindmap.creator.fullName}</span>
            <span class="title"><spring:message code="DESCRIPTION"/>:</span><span>${mindmap.title}</span>
        </div>
    </div>
</div>
<script type="text/javascript" src="../js/editor.js"></script>
</body>
</html>
