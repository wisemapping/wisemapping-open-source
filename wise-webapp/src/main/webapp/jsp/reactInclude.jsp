<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta charset="utf-8" />
    <base href="${requestScope['site.baseurl']}/static/webapp/">

    <link rel="preconnect" href="https://fonts.googleapis.com" crossorigin>
    <link rel="stylesheet" media="print" onload="this.onload=null;this.removeAttribute('media');" href="https://fonts.googleapis.com/css2?family=Montserrat:wght@100;200;300;400;600&display=swap"/>

    <%@ include file="/jsp/pageHeaders.jsf" %>

    <title>WiseMapping</title>

    <script>
        window.serverconfig = {
            apiBaseUrl: '${requestScope['site.baseurl']}',
            analyticsAccount: '${requestScope['google.analytics.account']}',
            clientType: 'rest',
            recaptcha2Enabled: ${requestScope['google.recaptcha2.enabled']},
            recaptcha2SiteKey: '${requestScope['google.recaptcha2.siteKey']}'
        };

    </script>
    <c:if test="${requestScope['google.analytics.enabled']}">
      <!-- Google Ads Sense Config. Lazy loading optimization -->
      <script type="text/javascript">
          function downloadJsAtOnload() {
              setTimeout(function downloadJs() {
                  var element = document.createElement("script");
                  element.setAttribute("data-ad-client", "ca-pub-4996113942657337");
                  element.async = true;
                  element.src = "https://pagead2.googlesyndication.com/pagead/js/adsbygoogle.js";
                  document.body.appendChild(element);
              }, 0);
          };

          window.addEventListener("load", downloadJsAtOnload, false);
      </script>
    </c:if>
</head>

<body>
    <noscript>You need to enable JavaScript to run this app.</noscript>
    <div id="root"></div>

    <script type="text/javascript" src="${requestScope['site.static.js.url']}/webapp/vendors.bundle.js" crossorigin="anonymous"></script>
    <script type="text/javascript" src="${requestScope['site.static.js.url']}/webapp/app.bundle.js" crossorigin="anonymous"></script>
</body>

</html>
