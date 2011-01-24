<%--@elvariable id="mindmap" type="com.wisemapping.model.MindMap"--%>
<%--@elvariable id="editorTryMode" type="java.lang.Boolean"--%>
<%--@elvariable id="user" type="com.wisemapping.model.User"--%>


<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">

<%@ include file="/jsp/init.jsp" %>
<c:url value="mymaps.htm" var="shareMap">
    <c:param name="action" value="collaborator"/>
    <c:param name="userEmail" value="${pageContext.request.userPrincipal.name}"/>
</c:url>
<html>
<head>
    <!-- Internet Explorer 8 Hack -->
    <meta http-equiv="Content-type" value="text/html;charset=UTF-8">
    <title><spring:message code="SITE.TITLE"/> - ${mindmap.title} </title>
    <link rel="stylesheet" type="text/css" href="../css/editor.css">
    <link rel="stylesheet" type="text/css" href="../css/bubble.css">
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/thirdparty.css">

    <script type="text/javascript" src="../dwr/engine.js"></script>
    <script type="text/javascript" src="../dwr/interface/LoggerService.js"></script>

    <script type='text/javascript' src='../js/wiseLibrary.js'></script>
    <script type='text/javascript' src='../js/core.js'></script>

    <link rel="icon" href="../images/favicon.ico" type="image/x-icon">
    <link rel="shortcut icon" href="../images/favicon.ico" type="image/x-icon">
</head>
<body>
<jsp:include page="editorHeader.jsp">
    <jsp:param name="onlyActionHeader" value="true"/>
</jsp:include>

<form method="post" id="printForm" name="printForm" action='<c:url value="export.htm"/>' style="height:100%;"
      target="${mindmap.title}">
    <input type="hidden" name="action" value="print"/>
    <input type="hidden" name="mapId" value="${mindmap.id}"/>
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


    var mapId = '${mindmap.id}';
    var mapXml = '${mapXml}';
    var editorProperties = ${mindmap.properties};
    var isTryMode = ${editorTryMode};

    function printMap() {
        document.printForm.submit();
    }
</script>

<div id="colorPalette">
    <div id="paletteHeader"></div>
</div>

<div id="fontFamilyPanel" class="toolbarPanel">
    <div id="times" class="toolbarPanelLink" style="font-family:times;">Times</div>
    <div id="arial" class="toolbarPanelLink" style="font-family:arial;">Arial</div>
    <div id="tahoma" class="toolbarPanelLink" style="font-family:tahoma;">Tahoma</div>
    <div id="verdana" class="toolbarPanelLink" style="font-family:verdana;">Verdana</div>
</div>

<div id="fontSizePanel" class="toolbarPanel">
    <div id="small" class="toolbarPanelLink" style="font-size:8px">Small</div>
    <div id="normal" class="toolbarPanelLink" style="font-size:12px">Normal</div>
    <div id="large" class="toolbarPanelLink" style="font-size:15px">Large</div>
    <div id="huge" class="toolbarPanelLink" style="font-size:24px">Huge</div>
</div>

<div id="topicShapePanel" class="toolbarPanel">
    <!--<div id="automatic" class="toolbarPanelLink">Automatic</div>-->
    <div id="rectagle" class="toolbarPanelLink"><img src="../images/shape-rectangle.png" alt="Rectangle" width="40px"
                                                     height="25px"></div>
    <div id="rounded rectagle" class="toolbarPanelLink"><img src="../images/shape-rectangle-rounded.png"
                                                             alt="Rounded Rectangle" width="40px" height="25px"></div>
    <div id="line" class="toolbarPanelLink"><img src="../images/shape-line.png" alt="Line" width="40px" height="7px">
    </div>
    <div id="elipse" class="toolbarPanelLink"><img src="../images/shape-elipse.png" alt="Elipse" width="40px"
                                                   height="25px"></div>
</div>

<div id="actionsContainer">
</div>
<div>
    <c:url value="mymaps.htm" var="shareMap">
        <c:param name="action" value="collaborator"/>
        <c:param name="userEmail" value="${pageContext.request.userPrincipal.name}"/>
    </c:url>
</div>

<div id="toolbar">
    <div id="editTab" class="tabContent">
        <div id="file" class="buttonContainer" title="<spring:message code="FILE"/>">
            <fieldset>
                <legend>
                    <spring:message code="FILE"/>
                </legend>
                <div id="saveButton" class="button" title="<spring:message code="SAVE"/>">
                    <div class="toolbarLabel"><p><spring:message code="SAVE"/></p></div>
                </div>
                <div id="discardButton" class="button" title="<spring:message code="CLOSE"/>">
                    <div class="toolbarLabel"><p><spring:message code="CLOSE"/></p></div>
                </div>
                <div id="undoEdition" class="button" title="<spring:message code="UNDO_EDITION"/>">
                    <div class="toolbarLabel"><p><spring:message code="UNDO"/></p></div>
                </div>
                <div id="redoEdition" class="button" title="<spring:message code="REDO_EDITION"/>">
                    <div class="toolbarLabel"><p><spring:message code="REDO"/></p></div>
                </div>

                <div id="print" class="button" title="<spring:message code="PRINT"/>" onclick="printMap();">
                    <div class="toolbarLabel"><p><spring:message code="PRINT"/></p></div>
                </div>

                <div id="export" class="button" title="<spring:message code="EXPORT"/>">
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
                <div id="zoomIn" class="button" title="<spring:message code="ZOOM_IN"/>">
                    <div class="toolbarLabel"><p><spring:message code="IN"/></p></div>
                </div>
                <div id="zoomOut" class="button" title="<spring:message code="ZOOM_OUT"/>">
                    <div class="toolbarLabel"><p><spring:message code="OUT"/></p></div>
                </div>
            </fieldset>
        </div>
        <div id="node" class="buttonContainer" title="Node Properties">
            <fieldset>
                <legend>
                    <spring:message code="TOPIC"/>
                </legend>
                <div id="topicShape" class="button comboButton" title="<spring:message code="TOPIC_SHAPE"/>">
                    <div class="toolbarLabel"><p><spring:message code="SHAPE"/></p></div>
                </div>
                <div id="addTopic" class="button" title="<spring:message code="TOPIC_ADD"/>">
                    <div class="toolbarLabel"><p><spring:message code="ADD"/></p></div>
                </div>
                <div id="deleteTopic" class="button" title="<spring:message code="TOPIC_DELETE"/>">
                    <div class="toolbarLabel"><p><spring:message code="DELETE"/></p></div>
                </div>
                <div id="topicBorder" class="button comboButton" title="<spring:message code="TOPIC_BORDER_COLOR"/>">
                    <div class="toolbarLabel"><p><spring:message code="BORDER"/></p></div>
                </div>
                <div id="topicColor" class="button comboButton" title="<spring:message code="TOPIC_BACKGROUND_COLOR"/>">
                    <div class="toolbarLabel"><p><spring:message code="COLOR"/></p></div>
                </div>
                <div id="topicIcon" class="button comboButton" title="<spring:message code="TOPIC_ICON"/>">
                    <div class="toolbarLabel"><p><spring:message code="ICON"/></p></div>
                </div>
                <div id="topicNote" class="button comboButton" title="<spring:message code="TOPIC_NOTE"/>">
                    <div class="toolbarLabel"><p><spring:message code="NOTE"/></p></div>
                </div>
                <div id="topicLink" class="button" title="<spring:message code="TOPIC_LINK"/>">
                    <div class="toolbarLabel"><p><spring:message code="LINK"/></p></div>
                </div>
                <div id="topicRelation" class="topicRelation button" title="<spring:message code="TOPIC_RELATIONSHIP"/>">
                    <div class="toolbarLabel"><p><spring:message code="TOPIC_RELATIONSHIP"/></p></div>
                </div>
            </fieldset>
        </div>
        <div id="font" class="buttonContainer" title="Font Properties">
            <fieldset>
                <legend>
                    <spring:message code="FONT"/>
                </legend>
                <div id="fontFamily" class="button comboButton" title="<spring:message code="FONT_TYPE"/>">
                    <div class="toolbarLabel"><p><spring:message code="TYPE"/></p></div>
                </div>
                <div id="fontSize" class="button comboButton" title="<spring:message code="FONT_SIZE"/>">
                    <div class="toolbarLabel"><p><spring:message code="SIZE"/></p></div>
                </div>
                <div id="fontBold" class="button" title="<spring:message code="FONT_BOLD"/>">
                    <div class="toolbarLabel"><p><spring:message code="BOLD"/></p></div>
                </div>
                <div id="fontItalic" class="button" title="<spring:message code="FONT_ITALIC"/>">
                    <div class="toolbarLabel"><p><spring:message code="ITALIC"/></p></div>
                </div>
                <div id="fontColor" class="button comboButton" title="<spring:message code="FONT_COLOR"/>">
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
                            <div id="tagIt" class="button" title="<spring:message code="TAG"/>">
                                <div class="toolbarLabel"><p><spring:message code="TAG"/></p></div>
                            </div>
                        </a>
                        <c:choose>
                            <c:when test="${mindmap.owner==user}">
                                <a id="shareAnchor" href="<c:out value="${shareMap}"/>&mapId=${mindmap.id}"
                                   rel="moodalbox 780px 530px wizard" title="<spring:message code="SHARE_DETAILS"/>">
                                    <div id="shareIt" class="button" title="<spring:message code="COLLABORATION"/>">
                                        <div class="toolbarLabel"><p><spring:message code="SHARE"/></p></div>
                                    </div>
                                </a>
                                <a id="publishAnchor" href="publish.htm?mapId=${mindmap.id}"
                                   rel="moodalbox 600px 400px wizard"
                                   title="<spring:message code="PUBLISH_MSG"/>">
                                    <div id="publishIt" class="button" title="<spring:message code="PUBLISH"/>">
                                        <div class="toolbarLabel"><p><spring:message code="PUBLISH"/></p></div>
                                    </div>
                                </a>
                            </c:when>
                        </c:choose>
                        <a id="historyAnchor" href="history.htm?action=list&mapId=${mindmap.id}"
                           rel="moodalbox 600px 400px wizard" title="<spring:message code="HISTORY_MSG"/>">
                            <div id="history" class="button" title="<spring:message code="HISTORY_MSG"/>">
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
                        <div id="tagIt" class="button" title="<spring:message code="TAG"/>">
                            <div class="toolbarLabel"><p><spring:message code="TAG"/></p></div>
                        </div>
                        <div id="shareIt" class="button" title="<spring:message code="COLLABORATE"/>">
                            <div class="toolbarLabel"><p><spring:message code="SHARE"/></p></div>
                        </div>
                        <div id="publishIt" class="button" title="<spring:message code="PUBLISH"/>">
                            <div class="toolbarLabel"><p><spring:message code="PUBLISH"/></p></div>
                        </div>
                        <div id="history" class="button" title="<spring:message code="HISTORY_MSG"/>">
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
    <div id="helpButtonFirstSteps"
         style="text-align:center; width:90px; height:20px; background-color:#f5f5f5; border: 1px solid #BBB6D6; cursor:pointer; padding-left:5px; margin-left:3px;float:left;">
        <div style="float:left; position:relative; top:50%; margin-top:-8px; margin-left:5px;"><img
                src="../images/help.png"/></div>
        <div style="float:left; position:relative; top:50%; margin-top:-8px; margin-left:4px;">First Steps</div>
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
