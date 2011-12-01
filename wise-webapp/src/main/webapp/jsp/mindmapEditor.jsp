<%--@elvariable id="mindmap" type="com.wisemapping.model.MindMap"--%>
<%--@elvariable id="editorTryMode" type="java.lang.Boolean"--%>
<%--@elvariable id="user" type="com.wisemapping.model.User"--%>
<!DOCTYPE HTML>

<%@ include file="/jsp/init.jsp" %>
<c:url value="mymaps.htm" var="shareMap">
    <c:param name="action" value="collaborator"/>
    <c:param name="userEmail" value="${pageContext.request.userPrincipal.name}"/>
</c:url>
<html>
<head>
    <!--[if lt IE 9]>
    <meta http-equiv="X-UA-Compatible" content="chrome=1">
    <![endif]-->
    <title><spring:message code="SITE.TITLE"/> - ${mindmap.title} </title>
    <meta http-equiv="Content-type" content="text/html; charset=UTF-8"/>
    <link rel="icon" href="../images/favicon.ico" type="image/x-icon">
    <link rel="shortcut icon" href="../images/favicon.ico" type="image/x-icon">
    <link rel="stylesheet/less" type="text/css" href="../css/editor.less"/>

    <script type='text/javascript' src='../js/libraries/mootools/mootools-core-1.3.2-full-compress.js'></script>
    <script type='text/javascript' src='../js/libraries/mootools/mootools-more-1.3.2.1-yui.js'></script>

    <script type="text/javascript" src="../dwr/engine.js"></script>
    <script type="text/javascript" src="../dwr/interface/MapEditorService.js"></script>
    <script type="text/javascript" src="../dwr/interface/LoggerService.js"></script>

    <script type='text/javascript' src='../js/core.js'></script>
    <script type='text/javascript' src='../js/less-1.1.3.min.js'></script>


    <script type="text/javascript">
        var mapId = '${mindmap.id}';
        var mapXml = '${mapXml}';
        var mindReady = false;
        $(document).addEvent('loadcomplete', function(resource) {
            mindReady = resource == 'mind' ? true : mindReady;
            if (mindReady) {
                // Configure default persistence ...
                mindplot.PersitenceManager.init(new mindplot.DwrPersitenceManager());
                var persitence = mindplot.PersitenceManager.getInstance();

                // Initialize editor ...
                var editorProperties = ${mindmap.properties};
                editorProperties.collab = 'standalone';
                editorProperties.readOnly = false;
                designer = buildDesigner(editorProperties);

                var domDocument = core.Utils.createDocumentFromText(mapXml);
                var mindmap = persitence.loadFromDom(mapId, domDocument);

                // Now, load the map ...
                designer.loadMap(mindmap);

                // If not problem has arisen, close the dialog ...
                if (!window.hasUnexpectedErrors) {
                    waitDialog.deactivate();
                }
            }
        });
    </script>

</head>
<body>

<form method="post" id="printForm" name="printForm" action='<c:url value="export.htm"/>' style="height:100%;"
      target="${mindmap.title}">
    <input type="hidden" name="action" value="print">
    <input type="hidden" name="mapId" value="${mindmap.id}">
    <input type="hidden" name="mapSvg" value="">
</form>

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

    function printMap() {
        document.printForm.mapSvg.value = $("workspaceContainer").innerHTML;
        document.printForm.submit();
    }
</script>


<div id="actionsContainer"></div>
<div>
    <c:url value="mymaps.htm" var="shareMap">
        <c:param name="action" value="collaborator"/>
        <c:param name="userEmail" value="${pageContext.request.userPrincipal.name}"/>
    </c:url>
</div>

<div id="header">
    <div id="headerInfo">
        <div id="headerActions">

            <spring:message code="WELCOME"/>, ${principal.firstname}|<span><a
                href="${pageContext.request.contextPath}/c/mymaps.htm"><spring:message code="MY_WISEMAPS"/></a></span> |
            <span><a id="settings" href="${pageContext.request.contextPath}/c/settings.htm"
                     title="<spring:message code="SETTINGS_DETAIL"/>"><spring:message code="SETTINGS"/></a></span>
            | <span><a href="${pageContext.request.contextPath}/c/logout.htm" title="<spring:message code="LOGOUT"/>">
            <spring:message code="LOGOUT"/>
        </a></span>
        </div>
        <a href="${pageContext.request.contextPath}/c/mymaps.htm">
            <div id="headerLogo"></div>
        </a>

        <div id="headerMapTitle">Title: <span>${mindmap.title}</span></div>
    </div>
    <%@ include file="/jsp/toolbar.jsf" %>
</div>

<div id="mindplot"></div>
<script type="text/javascript" src="../js/editor.js"></script>
</body>
</html>
