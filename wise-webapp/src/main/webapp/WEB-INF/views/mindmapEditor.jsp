<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ include file="init.jsp" %>

<%--@elvariable id="mindmap" type="com.wisemapping.model.Mindmap"--%>
<%--@elvariable id="editorTryMode" type="java.lang.Boolean"--%>
<%--@elvariable id="editorTryMode" type="java.lang.String"--%>
<%--@elvariable id="lockInfo" type="com.wisemapping.service.LockInfo"--%>

<!DOCTYPE html>
<html lang="${fn:substring(locale,0,2)}">
<head>
    <base href="${requestScope['site.baseurl']}/static/webapp/"/>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta charset="utf-8" />

    <link rel="preload" href="https://fonts.googleapis.com/css2?family=Montserrat:wght@100;200;300;400;600&display=swap" as="style" onload="this.onload=null;this.rel='stylesheet'" crossorigin>
    <%@ include file="pageHeaders.jsf" %>

    <title>Loading ... | WiseMapping</title>

    <script>
        window.serverconfig = {
            apiBaseUrl: '${requestScope['site.baseurl']}',
            analyticsAccount: '${requestScope['google.analytics.account']}',
            clientType: 'rest',
            recaptcha2Enabled: ${requestScope['google.recaptcha2.enabled']},
            recaptcha2SiteKey: '${requestScope['google.recaptcha2.siteKey']}'
        };

    </script>
    <script type="text/javascript">
        var mapId = '${mindmap.id}';
        var mindmapLocked = ${mindmapLocked};
        var mindmapLockedMsg = '<spring:message code="MINDMAP_LOCKED" arguments="${lockInfo.user.fullName},${lockInfo.user.email}"/>';
        var userOptions = ${mindmap.properties};
        var accountName = '${fn:replace(principal.fullName,'\'','\\\'')}';
        var accountEmail = '${principal.email}';
        var mapTitle = '${fn:replace(mindmap.title,'\'','\\\'')}';
    </script>
</head>

<body>
    <noscript>You need to enable JavaScript to run this app.</noscript>
    <div id="root" class="mindplot-div-container"></div>
    
    <script type="text/javascript" src="${requestScope['site.static.js.url']}/webapp/vendors.bundle.js" crossorigin="anonymous" defer></script>
    <script type="text/javascript" src="${requestScope['site.static.js.url']}/webapp/app.bundle.js" crossorigin="anonymous" defer></script>

</body>

</html>
