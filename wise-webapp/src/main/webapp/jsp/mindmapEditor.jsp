<%@ page import="com.wisemapping.security.Utils" %>
<%@ page import="com.wisemapping.model.User" %>
<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ include file="/jsp/init.jsp" %>

<%--@elvariable id="mindmap" type="com.wisemapping.model.Mindmap"--%>
<%--@elvariable id="editorTryMode" type="java.lang.Boolean"--%>
<%--@elvariable id="editorTryMode" type="java.lang.String"--%>
<%--@elvariable id="lockInfo" type="com.wisemapping.service.LockInfo"--%>

<!DOCTYPE HTML>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <base href="${requestScope['site.baseurl']}/static/mindplot/">
    <title><spring:message code="SITE.TITLE"/> - <c:out value="${mindmap.title}"/></title>
    <link rel="stylesheet"  href="../../css/editor.css"/>
    <%@ include file="/jsp/pageHeaders.jsf" %>

    <script type="text/javascript">
        var mapId = '${mindmap.id}';
        var memoryPersistence = ${memoryPersistence};
        var readOnly = ${readOnlyMode};
        var lockTimestamp = '${lockTimestamp}';
        var lockSession = '${lockSession}';
        var locale = '${locale}';
        var mindmapLocked = ${mindmapLocked};
        var mindmapLockedMsg = '<spring:message code="MINDMAP_LOCKED" arguments="${lockInfo.user.fullName},${lockInfo.user.email}"/>';
        var userOptions = ${mindmap.properties};
        var isAuth = ${principal != null};
        var accountName = '${principal.fullName}';
        var accountEmail = '${principal.email}';
    </script>
    <%@ include file="/jsp/googleAnalytics.jsf" %>
</head>

<body>

<div id="header">
    <%@ include file="/jsp/mindmapEditorToolbar.jsf" %>
</div>
<div id="mindplot" onselectstart="return false;"></div>

<div id="floating-panel">
    <div id="keyboardShortcuts" class="buttonExtOn">
        <img src="../../images/editor/keyboard.svg"/>
    </div>
    <div id="zoom-button">
        <button id="zoom-plus">
            <img src="../../images/editor/add.svg" />
        </button>
        <button id="zoom-minus">
            <img src="../../images/editor/minus.svg" />
        </button>
    </div>
    <div id="position">
        <button id="position-button">
            <img src="../../images/editor/center_focus.svg" />
        </button>
    </div>
</div>

<div id="bottom-logo"></div>
<div id="headerNotifier"></div>

<%-- Try message dialog --%>
<c:if test="${memoryPersistence}">
    <div id="tryInfoPanel">
        <p><spring:message code="TRY_WELCOME"/></p>
        <p><b><spring:message code="TRY_WELCOME_DESC"/></b></p>
        <a href="/c/registration"><div class="actionButton"><spring:message code="SIGN_UP"/></div></a>
    </div>
</c:if>

<script type="text/javascript" src="${requestScope['site.static.js.url']}/mindplot/loader.js"></script>


</body>
</html>
