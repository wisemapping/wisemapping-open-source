<%@page pageEncoding="UTF-8" %>
<%@include file="/jsp/init.jsp" %>

<%--@elvariable id="mindmap" type="com.wisemapping.model.Mindmap"--%>

<!DOCTYPE HTML>

<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <base href="${requestScope['site.baseurl']}/static/mindplot/">
    <title>${mindmap.title} | <spring:message code="SITE.TITLE"/></title>
    <link rel="stylesheet"  href="../../css/viewonly.css"/>
    <%@ include file="/jsp/pageHeaders.jsf" %>
    <script type="text/javascript">
          var mapId = '${mindmap.id}';
          var historyId = '${hid}';
          var readOnly = true;
          var userOptions = ${mindmap.properties};
          var locale = '${locale}';
          var isAuth = ${principal != null};
     </script>
     <%@ include file="/jsp/googleAnalytics.jsf" %>
</head>
<body>

<div id="mindplot" style={mindplotStyle} className="wise-editor"></div>
<div id="mindplot-tooltips" className="wise-editor"></div>


<a href="${requestScope['site.homepage']}" target="new">
<div id="footerLogo"></div>
</a>

<div id="mapDetails">
    <span class="title"><spring:message code="CREATOR"/>:</span><span>${mindmap.creator.fullName}</span>
    <span class="title"><spring:message code="DESCRIPTION"/>:</span><span>${mindmap.title}</span>
</div>

<script type="text/javascript" src="${requestScope['site.static.js.url']}/mindplot/loader.js" crossorigin="anonymous"></script>

<div id="floating-panel">
    <div id="zoom-button">
        <button id="zoom-plus">
            <img src="../../images/add.svg" />
        </button>
        <button id="zoom-minus">
            <img src="../../images/minus.svg" />
        </button>
        <div id="position">
            <button id="position-button">
                <img src="../../images/center_focus.svg" />
            </button>
        </div>
    </div>
</div>

<script type="text/javascript">
    // Hook zoom events ...
    const zoomInButton = document.getElementById('zoom-plus');
    if (zoomInButton) {
      zoomInButton.addEventListener('click', () => {
        designer.zoomIn();
      });
    }

    const zoomOutButton = document.getElementById('zoom-minus');
    if (zoomOutButton) {
      zoomOutButton.addEventListener('click', () => {
        designer.zoomOut();
      });
    }
    
    const position = document.getElementById('position');
    if (position) {
      position.addEventListener('click', () => {
        designer.zoomToFit();
      });
    }

</script>
</body>
</html>
