<%@ page import="com.wisemapping.security.Utils" %>
<%@ page import="com.wisemapping.model.User" %>
<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ include file="/jsp/init.jsp" %>

<!DOCTYPE HTML>

<%--@elvariable id="mindmap" type="com.wisemapping.model.Mindmap"--%>
<%--@elvariable id="editorTryMode" type="java.lang.Boolean"--%>
<%--@elvariable id="editorTryMode" type="java.lang.String"--%>
<%--@elvariable id="lockInfo" type="com.wisemapping.service.LockInfo"--%>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <base href="${requestScope['site.baseurl']}/">
    <title><spring:message code="SITE.TITLE"/> - <c:out value="${mindmap.title}"/></title>
    <!--[if lt IE 9]>
    <meta http-equiv="X-UA-Compatible" content="chrome=1">
    <![endif]-->
    <link rel="stylesheet/less" type="text/css" href="css/editor.less"/>
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

        $(document).on('loadcomplete', function (resource) {
            try {
                var mapId = '${mindmap.id}';
                // Configure designer options ...
                var options = loadDesignerOptions();

            <c:if test="${!memoryPersistence && !readOnlyMode}">
                options.persistenceManager = new mindplot.RESTPersistenceManager(
                        {
                            documentUrl:"c/restful/maps/{id}/document",
                            revertUrl:"c/restful/maps/{id}/history/latest",
                            lockUrl:"c/restful/maps/{id}/lock",
                            timestamp: "${lockTimestamp}",
                            session: "${lockSession}"
                        }
                );
            </c:if>
            <c:if test="${memoryPersistence || readOnlyMode}">
                options.persistenceManager = new mindplot.LocalStorageManager("c/restful/maps/{id}${hid!=null?'/':''}${hid!=null?hid:''}/document/xml${principal!=null?'':'-pub'}",true);
            </c:if>

                var userOptions = ${mindmap.properties};
                options.zoom = userOptions.zoom;
                options.readOnly = ${!!readOnlyMode};
                options.locale = '${locale}';

                // Set map id ...
                options.mapId = mapId;

                // Build designer ...
                var designer = buildDesigner(options);

                // Load map from XML file persisted on disk...
                var persistence = mindplot.PersistenceManager.getInstance();
                var mindmap = mindmap = persistence.load(mapId);
                designer.loadMap(mindmap);

            } catch (e) {
                logStackTrace(e);
                throw e;
            }

            <c:if test="${mindmapLocked}">
            $notify("<spring:message code="MINDMAP_LOCKED" arguments="${lockInfo.user.fullName},${lockInfo.user.email}"/>", false);
            </c:if>
        });


    </script>
    <%@ include file="/jsp/googleAnalytics.jsf" %>
</head>
<body>

<div id="actionsContainer"></div>

<div id="header">
    <div id="headerInfo">
        <div id="headerActions">
            <c:if test="${!memoryPersistence}">

                <spring:message code="WELCOME"/>, ${principal.firstname} |
                <span><a href="c/maps/"><spring:message code="MY_WISEMAPS"/></a></span> |
                <span><a href="#" target=" " id="tutorialVideo"><spring:message code="TUTORIAL_VIDEO"/></a></span> |
                <span><a href="c/keyboard" id="keyboardShortcuts"><spring:message code="SHORTCUTS"/></a></span> |
                <span><a href="c/logout" title="<spring:message code="LOGOUT"/>"><spring:message
                        code="LOGOUT"/></a></span>
            </c:if>
            <c:if test="${memoryPersistence}">
                <span><a href="c/keyboard" id="tutorialVideo"><spring:message code="TUTORIAL_VIDEO"/></a></span> |
                <span><a href="#" id="keyboardShortcuts"><spring:message code="SHORTCUTS"/></a></span> |
                <span><a href="c/user/registration" title="<spring:message code="REGISTER"/>"><spring:message
                        code="REGISTER"/></a></span>
            </c:if>
        </div>
        <a href="${requestScope['site.homepage']}">
            <div id="headerLogo"></div>
        </a>

        <div id="headerMapTitle"><spring:message code="NAME"/>: <span><c:out value="${mindmap.title}"/></span></div>
    </div>
    <%@ include file="/jsp/mindmapEditorToolbar.jsf" %>
</div>

<div id='load' class="modal fade">
    <div class="modal-dialog">
        <div style="height: 120px; text-align: center; border: 2px solid orange" class="modal-content">
            <img style='margin-top:25px; text-align: center' src="images/ajax-loader.gif">
        </div>
    </div>
</div>

<div id="mindplot" onselectstart="return false;"></div>
<script type="text/javascript" src="js/editor.js"></script>
</body>
</html>
