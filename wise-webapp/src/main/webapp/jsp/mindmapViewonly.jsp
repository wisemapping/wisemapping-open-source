<%@page pageEncoding="UTF-8" %>
<%@include file="/jsp/init.jsp" %>

<%--@elvariable id="mindmap" type="com.wisemapping.model.Mindmap"--%>

<!DOCTYPE HTML>

<html>
<head>
    <meta name="viewport" content="initial-scale=1">
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <base href="${requestScope['site.baseurl']}/static/mindplot/">
    <title>${mindmap.title} | <spring:message code="SITE.TITLE"/></title>
    <link rel="stylesheet"  href="../../css/viewonly.css"/>
    <%@ include file="/jsp/pageHeaders.jsf" %>
    <script type="text/javascript">
          var mapId = '${mindmap.id}';
          var historyId = '${hid}';
          var userOptions = ${mindmap.properties};
          var locale = '${locale}';
          var isAuth = ${principal != null};
     </script>
    <c:if test="${requestScope['google.analytics.enabled']}">
        <!-- Global site tag (gtag.js) - Google Analytics -->
        <script async src="https://www.googletagmanager.com/gtag/js?id=${requestScope['google.analytics.account']}"></script>
        <script>
          window.dataLayer = window.dataLayer || [];
          function gtag(){dataLayer.push(arguments);}
          gtag('js', new Date());
          gtag('config', '${requestScope['google.analytics.account']}',
          {
            'page_title' : 'Public View'
          });
        </script>
    </c:if>

	<style>
		body {
			height: 100vh;
			width: 100vw;
			min-width: 100vw;
			min-height: 100vh;
			margin: 0px;
		}

		#root {
			height: 100%;
			width: 100%;
		}

	</style>

</head>
<body>
	<div id="root">
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
