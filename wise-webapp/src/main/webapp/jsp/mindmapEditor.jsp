<!DOCTYPE HTML>

<%--@elvariable id="mindmap" type="com.wisemapping.model.MindMap"--%>
<%--@elvariable id="editorTryMode" type="java.lang.Boolean"--%>
<%--@elvariable id="editorTryMode" type="java.lang.String"--%>
<%--@elvariable id="mapXml" type="com.wisemapping.model.User"--%>

<%@ include file="/jsp/init.jsp" %>
<html>
<head>
    <base href="${pageContext.request.contextPath}/"/>
    <title><spring:message code="SITE.TITLE"/> - ${mindmap.title} </title>
    <meta http-equiv="Content-type" content="text/html; charset=UTF-8"/>
    <!--[if lt IE 9]>
    <meta http-equiv="X-UA-Compatible" content="chrome=1">
    <![endif]-->
    <link rel="stylesheet/less" type="text/css" href="css/editor.less"/>
    <script type='text/javascript' src='js/mootools-core.js'></script>
    <script type='text/javascript' src='js/mootools-more.js'></script>
    <script type='text/javascript' src='js/core.js'></script>
    <script type='text/javascript' src='js/less.js'></script>

    <link rel="icon" href="images/favicon.ico" type="image/x-icon">
    <link rel="shortcut icon" href="images/favicon.ico" type="image/x-icon">

    <script type="text/javascript">

        $(document).addEvent('loadcomplete', function(resource) {
            var mapId = '${mindmap.id}';
            var mapXml = '${mindmap.xmlAsJsLiteral}';

            // Configure designer options ...
            var options = loadDesignerOptions();
            options.persistenceManager = new mindplot.RESTPersistenceManager("service/maps/{id}/document");
            var userOptions = ${mindmap.properties};
            options.zoom = userOptions.zoom;
            options.readOnly = ${!!readOnlyMode};

            // Set map id ...
            options.mapId = mapId;

            // Build designer ...
            var designer = buildDesigner(options);

            // Load map from XML ...
            var parser = new DOMParser();
            var domDocument = parser.parseFromString(mapXml, "text/xml");

            var persistence = mindplot.PersistenceManager.getInstance();
            var mindmap = persistence.loadFromDom(mapId, domDocument);
            designer.loadMap(mindmap);
        });
    </script>
</head>
<body>

<div id="actionsContainer"></div>

<div id="header">
    <div id="headerInfo">
        <div id="headerActions">

            <spring:message code="WELCOME"/>, ${principal.firstname}|<span><a
                href="c/maps/"><spring:message code="MY_WISEMAPS"/></a></span> |
            <%--<span><a id="settings" href="c/settings"--%>
                     <%--title="<spring:message code="ACCOUNT_DETAIL"/>"><spring:message code="ACCOUNT"/></a></span> |--%>
            <span><a href="c/logout" title="<spring:message code="LOGOUT"/>">
            <spring:message code="LOGOUT"/>
        </a></span>
        </div>
        <a href="c/maps/">
            <div id="headerLogo"></div>
        </a>

        <div id="headerMapTitle">Title: <span>${mindmap.title}</span></div>
    </div>
    <%@ include file="/jsp/mindmapEditorToolbar.jsf" %>
</div>

<div id="mindplot"></div>
<script type="text/javascript" src="js/editor.js"></script>
</body>
</html>
