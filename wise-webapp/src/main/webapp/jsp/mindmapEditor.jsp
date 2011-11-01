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
    <link rel="stylesheet/less" type="text/css" href="../css/editor2.css"/>

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

                var editorProperties = ${mindmap.properties};
                editorProperties.collab = 'standalone';
                editorProperties.readOnly = false;
                designer = buildDesigner(editorProperties);

                var domDocument = core.Utils.createDocumentFromText(mapXml);
                var serializer = mindplot.XMLMindmapSerializerFactory.getSerializerFromDocument(domDocument);
                var mindmap = serializer.loadFromDom(domDocument, mapId);

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
    <div id="toolbar">
        <div id="persist" class="buttonContainer">
            <div id="save" class="buttonOn" title="Save">
                <img src="../images/save.png"/>
            </div>
            <div id="discard" class="buttonOn" title="Discard">
                <img src="../images/discard.png"/>
            </div>
            <div id="print" class="buttonOn" title="Print">
                <img src="../images/print.png"/>
            </div>
            <div id="export" class="buttonOn" title="Export">
                <img src="../images/export.png"/>
            </div>
        </div>
        <div id="edit" class="buttonContainer">
            <div id="undoEdition" class="buttonOn" title="Undo Edition">
                <img src="../images/undo.png"/>
            </div>
            <div id="redoEdition" class="buttonOn" title="Redo Edition">
                <img src="../images/redo.png"/>
            </div>
        </div>
        <div id="zoom" class="buttonContainer">
            <div id="zoomIn" class="buttonOn" title="Zoom In">
                <img src="../images/zoom-in.png"/>
            </div>
            <div id="zoomOut" class="buttonOn" title="Zoom Out">
                <img src="../images/zoom-out.png"/>
            </div>
        </div>
        <div id="node" class="buttonContainer">
            <div id="topicShape" class="buttonExtOn" title="Topic Shape">
                <img src="../images/topic-shape.png"/>
            </div>
            <div id="addTopic" class="buttonOn" title="Add Topic">
                <img src="../images/topic-add.png"/>
            </div>
            <div id="deleteTopic" class="buttonOn" title="Delete">
                <img src="../images/topic-delete.png"/>
            </div>
            <div id="topicBorder" class="buttonOn" title="Border Color">
                <img src="../images/topic-border.png"/>
            </div>
            <div id="topicColor" class="buttonExtOn" title="Background Color">
                <img src="../images/topic-color.png"/>
            </div>
            <div id="topicIcon" class="buttonExtOn" title="Add Icon">
                <img src="../images/topic-icon.png"/>
            </div>
            <div id="topicNote" class="buttonOn" title="Add Note">
                <img src="../images/topic-note.png"/>
            </div>
            <div id="topicLink" class="buttonOn" title="Add Link">
                <img src="../images/topic-link.png"/>
            </div>
            <div id="topicRelation" class="buttonOn" title="Add Relationship">
                <img src="../images/topic-relation.png"/>
            </div>
        </div>
        <div id="font" class="buttonContainer">
            <div id="fontFamily" class="buttonOn" title="Font Style">
                <img src="../images/font-type.png"/>
            </div>
            <div id="fontSize" class="buttonExtOn" title="Font Size">
                <img src="../images/font-size.png"/>
            </div>
            <div id="fontBold" class="buttonOn" title="Bold Style">
                <img src="../images/font-bold.png"/>
            </div>
            <div id="fontItalic" class="buttonOn" title="Italic Style">
                <img src="../images/font-italic.png"/>
            </div>
            <div id="fontColor" class="buttonExtOn" title="Fond Color" style="padding-top:4px">
                <img src="../images/font-color.png"/>
            </div>
        </div>
        <div id="collaboration" class="buttonContainer">
            <div id="tagIt" class="buttonOn" title="Tag">
                <img src="../images/tag.png"/>
            </div>
            <div id="shareIt" class="buttonOn" title="Share">
                <img src="../images/share.png"/>
            </div>
            <div id="publishIt" class="buttonOn" title="Publish">
                <img src="../images/public.png"/>
            </div>
            <div id="history" class="buttonOn" title="History">
                <img src="../images/history.png"/>
            </div>
        </div>
    </div>
</div>

<div id="mindplot"></div>
<script type="text/javascript" src="../js/editor.js"></script>
</body>
</html>
