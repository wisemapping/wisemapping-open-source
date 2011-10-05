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

    <meta http-equiv="Content-type" content="text/html; charset=UTF-8"/>
    <title><spring:message code="SITE.TITLE"/> - ${mindmap.title} </title>
    <link rel="stylesheet" type="text/css" href="../css/editor.css"/>

    <script type='text/javascript'
            src='https://ajax.googleapis.com/ajax/libs/mootools/1.3.2/mootools-yui-compressed.js'></script>
    <script type='text/javascript' src='../js/libraries/mootools/mootools-more-1.3.2.1-yui.js'></script>
    <script type="text/javascript" src="../dwr/engine.js"></script>
    <script type="text/javascript" src="../dwr/interface/LoggerService.js"></script>
    <script type='text/javascript' src='../js/core.js'></script>
    <script type='text/javascript' src='../js/editorLib.js'></script>

    <link rel="icon" href="../images/favicon.ico" type="image/x-icon">
    <link rel="shortcut icon" href="../images/favicon.ico" type="image/x-icon">
    <script type="text/javascript">

        var mindReady = false;
        $(document).addEvent('loadcomplete', function(resource) {
            mindReady = resource == 'mind' ? true : mindReady;
            if (mindReady) {

                var mapId = '${mindmap.id}';
                var mapXml = '${mapXml}';
                var editorProperties = ${mindmap.properties};
                editorProperties.collab = 'standalone';

                var isTryMode = ${editorTryMode};
                designer = buildDesigner(editorProperties, isTryMode);

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
<jsp:include page="editorHeader.jsp">
    <jsp:param name="onlyActionHeader" value="true"/>
</jsp:include>

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
            <spring:message code="EDITOR.LOADING"/>
        </div>
    </div>
</div>

<div id="errorDialog" style="display:none">
    <div id="errorContainer">
        <div class="loadingIcon"></div>
        <div class="loadingText">
            <spring:message code="EDITOR.ERROR_LOADING"/>
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

<div id="colorPalette">
    <div id="paletteHeader"></div>
</div>

<div id="toolbar">
    <div id="editTab" class="tabContent">
        <div id="file" class="buttonContainer" title="<spring:message code="FILE"/>">
            <fieldset>
                <legend>
                    <spring:message code="FILE"/>
                </legend>
                <div id="saveButton" class="buttonOn" title="<spring:message code="SAVE"/>">
                    <div class="toolbarLabel"><p><spring:message code="SAVE"/></p></div>
                </div>
                <div id="discardButton" class="buttonOn" title="<spring:message code="CLOSE"/>">
                    <div class="toolbarLabel"><p><spring:message code="CLOSE"/></p></div>
                </div>
                <div id="undoEdition" class="buttonOn" title="<spring:message code="UNDO_EDITION"/>">
                    <div class="toolbarLabel"><p><spring:message code="UNDO"/></p></div>
                </div>
                <div id="redoEdition" class="buttonOn" title="<spring:message code="REDO_EDITION"/>">
                    <div class="toolbarLabel"><p><spring:message code="REDO"/></p></div>
                </div>

                <div id="print" class="buttonOn" title="<spring:message code="PRINT"/>" onclick="printMap();">
                    <div class="toolbarLabel"><p><spring:message code="PRINT"/></p></div>
                </div>

                <div id="export" class="buttonOn" title="<spring:message code="EXPORT"/>">
                    <div class="toolbarLabel"><p><spring:message code="EXPORT"/></p></div>
                    <a id="exportAnchor" href="export.htm?mapId=${mindmap.id}" rel="moodalbox 600px 400px"
                       title="<spring:message code="EXPORT_DETAILS"/>">
                    </a>
                </div>
            </fieldset>
        </div>
        <div id="zoom" class="buttonContainer" title="Zoom In">
            <fieldset>
                <legend>
                    <spring:message code="ZOOM"/>
                </legend>
                <div id="zoomIn" class="buttonOn" title="<spring:message code="ZOOM_IN"/>">
                    <div class="toolbarLabel"><p><spring:message code="IN"/></p></div>
                </div>
                <div id="zoomOut" class="buttonOn" title="<spring:message code="ZOOM_OUT"/>">
                    <div class="toolbarLabel"><p><spring:message code="OUT"/></p></div>
                </div>
            </fieldset>
        </div>
        <div id="node" class="buttonContainer" title="Node Properties">
            <fieldset>
                <legend>
                    <spring:message code="TOPIC"/>
                </legend>
                <div id="topicShape" class="buttonOn" title="<spring:message code="TOPIC_SHAPE"/>">
                    <div class="toolbarLabel"><p><spring:message code="SHAPE"/></p></div>
                </div>
                <div id="addTopic" class="buttonOn" title="<spring:message code="TOPIC_ADD"/>">
                    <div class="toolbarLabel"><p><spring:message code="ADD"/></p></div>
                </div>
                <div id="deleteTopic" class="buttonOn" title="<spring:message code="TOPIC_DELETE"/>">
                    <div class="toolbarLabel"><p><spring:message code="DELETE"/></p></div>
                </div>
                <div id="topicBorder" class="buttonOn" title="<spring:message code="TOPIC_BORDER_COLOR"/>">
                    <div class="toolbarLabel"><p><spring:message code="BORDER"/></p></div>
                </div>
                <div id="topicColor" class="buttonOn" title="<spring:message code="TOPIC_BACKGROUND_COLOR"/>">
                    <div class="toolbarLabel"><p><spring:message code="COLOR"/></p></div>
                </div>
                <div id="topicIcon" class="buttonOn" title="<spring:message code="TOPIC_ICON"/>">
                    <div class="toolbarLabel"><p><spring:message code="ICON"/></p></div>
                </div>
                <div id="topicNote" class="buttonOn" title="<spring:message code="TOPIC_NOTE"/>">
                    <div class="toolbarLabel"><p><spring:message code="NOTE"/></p></div>
                </div>
                <div id="topicLink" class="buttonOn" title="<spring:message code="TOPIC_LINK"/>">
                    <div class="toolbarLabel"><p><spring:message code="LINK"/></p></div>
                </div>
                <div id="topicRelation" class="topicRelation buttonOn"
                     title="<spring:message code="TOPIC_RELATIONSHIP"/>">
                    <div class="relationshiplabel toolbarLabel"><p><spring:message code="TOPIC_RELATIONSHIP"/></p></div>
                </div>
            </fieldset>
        </div>
        <div id="font" class="buttonContainer" title="Font Properties">
            <fieldset>
                <legend>
                    <spring:message code="FONT"/>
                </legend>
                <div id="fontFamily" class="buttonOn" title="<spring:message code="FONT_TYPE"/>">
                    <div class="toolbarLabel"><p><spring:message code="TYPE"/></p></div>
                </div>
                <div id="fontSize" class="buttonOn" title="<spring:message code="FONT_SIZE"/>">
                    <div class="toolbarLabel"><p><spring:message code="SIZE"/></p></div>
                </div>
                <div id="fontBold" class="buttonOn" title="<spring:message code="FONT_BOLD"/>">
                    <div class="toolbarLabel"><p><spring:message code="BOLD"/></p></div>
                </div>
                <div id="fontItalic" class="buttonOn" title="<spring:message code="FONT_ITALIC"/>">
                    <div class="toolbarLabel"><p><spring:message code="ITALIC"/></p></div>
                </div>
                <div id="fontColor" class="buttonOn" title="<spring:message code="FONT_COLOR"/>">
                    <div class="toolbarLabel"><p><spring:message code="COLOR"/></p></div>
                </div>
            </fieldset>
        </div>
        <div id="share" class="buttonContainer" title="Share Properties">
            <c:choose>
                <c:when test="${editorTryMode==false}">
                    <fieldset>
                        <legend>
                            <spring:message code="COLLABORATION"/>
                        </legend>
                        <a id="tagAnchor" href="tags.htm?mapId=${mindmap.id}" rel="moodalbox 400px 200px wizard"
                           title="<spring:message code="TAGS_DETAILS"/>">
                            <div id="tagIt" class="buttonOn" title="<spring:message code="TAG"/>">
                                <div class="toolbarLabel"><p><spring:message code="TAG"/></p></div>
                            </div>
                        </a>
                        <c:choose>
                            <c:when test="${mindmap.owner==user}">
                                <a id="shareAnchor" href="<c:out value="${shareMap}"/>&amp;mapId=${mindmap.id}"
                                   rel="moodalbox 780px 530px wizard" title="<spring:message code="SHARE_DETAILS"/>">
                                    <div id="shareIt" class="buttonOn" title="<spring:message code="COLLABORATION"/>">
                                        <div class="toolbarLabel"><p><spring:message code="SHARE"/></p></div>
                                    </div>
                                </a>
                                <a id="publishAnchor" href="publish.htm?mapId=${mindmap.id}"
                                   rel="moodalbox 600px 400px wizard"
                                   title="<spring:message code="PUBLISH_MSG"/>">
                                    <div id="publishIt" class="buttonOn" title="<spring:message code="PUBLISH"/>">
                                        <div class="toolbarLabel"><p><spring:message code="PUBLISH"/></p></div>
                                    </div>
                                </a>
                            </c:when>
                        </c:choose>
                        <a id="historyAnchor" href="history.htm?action=list&amp;mapId=${mindmap.id}"
                           rel="moodalbox 600px 400px wizard" title="<spring:message code="HISTORY_MSG"/>">
                            <div id="history" class="buttonOn" title="<spring:message code="HISTORY_MSG"/>">
                                <div class="toolbarLabel"><p><spring:message code="HISTORY"/></p></div>
                            </div>
                        </a>
                    </fieldset>
                </c:when>
                <c:otherwise>
                    <fieldset>
                        <legend>
                            <spring:message code="COLLABORATION"/>
                        </legend>
                        <div id="tagIt" class="buttonOn" title="<spring:message code="TAG"/>">
                            <div class="toolbarLabel"><p><spring:message code="TAG"/></p></div>
                        </div>
                        <div id="shareIt" class="buttonOn" title="<spring:message code="COLLABORATE"/>">
                            <div class="toolbarLabel"><p><spring:message code="SHARE"/></p></div>
                        </div>
                        <div id="publishIt" class="buttonOn" title="<spring:message code="PUBLISH"/>">
                            <div class="toolbarLabel"><p><spring:message code="PUBLISH"/></p></div>
                        </div>
                        <div id="history" class="buttonOn" title="<spring:message code="HISTORY_MSG"/>">
                            <div class="toolbarLabel"><p><spring:message code="HISTORY"/></p></div>
                        </div>
                    </fieldset>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
</div>

<div id="mindplot"></div>

<div id="footerEditor">
    <div style="position:absolute; top:0px; width:100%">
        <a href="${pageContext.request.contextPath}/c/home.htm">
            <div id="logo"></div>
        </a>

        <div id='msgLoggerContainer' class="msgLoggerContainer">
            <div id="msgStart"></div>
            <div id='msgLogger'></div>
            <div id="msgEnd"></div>
        </div>
    </div>
    <div id="helpButtonKeyboard"
         style="text-align:center; width:100px; height:20px; background-color:#f5f5f5; border: 1px solid #BBB6D6; cursor:pointer; padding-left:5px; margin-left:3px;float:left;">
        <div style="float:left; position:relative; top:50%; margin-top:-8px; margin-left:5px;"><img
                src="../images/help.png"/></div>
        <div style="float:left; position:relative; top:50%; margin-top:-8px; margin-left:4px;">Shortcuts</div>
    </div>
</div>
<c:if test="${editorTryMode==true}">
    <div id="tryEditorWarning" class="sb">
        <div class="close" id="tryClose"
             style="position:absolute;">
        </div>
        <div>
            <h1>Warning</h1>
            This is a demo editor. That's why you won't be able to save your changes.
            If you want to start creating your maps, <a href="userRegistration.htm">
            <spring:message code="JOIN_NOW"/></a>. Registration is free and takes just a moment.
        </div>
    </div>
    <script type="text/javascript">
        // Register close event ...
        var tryElem = $('tryEditorWarning');
        tryElem.addClass('drag').makeDraggable();
        $('tryClose').addEvent('click', function(event) {
            tryElem.setStyle('visibility', 'hidden');
        });

    </script>
</c:if>
<div id="ffoxworkarround" style="display:none;"><input id="ffoxWorkarroundInput" type="text"></div>
<c:if test="${editorTryMode==false}">
    <script type="text/javascript" src="../dwr/interface/MapEditorService.js"></script>
</c:if>
<script type="text/javascript" src="../js/editor.js"></script>
<%--<script src="http://www.google-analytics.com/urchin.js" type="text/javascript">
</script>
<script type="text/javascript">
    _uacct = "UA-2347723-1";
    urchinTracker();
</script>--%>
</body>
</html>
