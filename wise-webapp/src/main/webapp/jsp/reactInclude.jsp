<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta charset="utf-8" />
    <base href="${requestScope['site.baseurl']}/static/webapp/">
    <link rel="preconnect" href="https://fonts.gstatic.com" />
    <link href="https://fonts.googleapis.com/css2?family=Montserrat:wght@100;200;300;400;600&display=swap" rel="stylesheet" />

    <%@ include file="/jsp/pageHeaders.jsf" %>

    <title>Loading | WiseMapping</title>

    <script>
        window.serverconfig = {
            apiBaseUrl: '',
            analyticsAccount: '${requestScope['google.analytics.account']}',
            clientType: 'rest',
            recaptcha2Enabled: ${requestScope['google.recaptcha2.enabled']},
            recaptcha2SiteKey: '${requestScope['google.recaptcha2.siteKey']}'
        };

    </script>
    <%@ include file="/jsp/googleAnalytics.jsf" %>

    <c:if test="${requestScope['google.analytics.enabled']}">
      <!-- Google Ads Sense Config-->
      <script async src="https://pagead2.googlesyndication.com/pagead/js/adsbygoogle.js?client=ca-pub-4996113942657337" crossorigin="anonymous"></script>
    </c:if>
</head>

<body>
    <noscript>You need to enable JavaScript to run this app.</noscript>
    <div id="root"></div>

    <script type="text/javascript" src="<c:out value="${requestScope['site.static.js.url']}"/>vendors.bundle.js"></script>
    <script type="text/javascript" src="<c:out value="${requestScope['site.static.js.url']}"/>app.bundle.js"></script>
</body>

</html>
