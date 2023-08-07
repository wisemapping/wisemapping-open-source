<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@taglib uri="jakarta.tags.functions" prefix="fn" %>
<%@taglib uri="jakarta.tags.core" prefix="c"%>
<c:set var="baseUrl" value="${requestScope['site.baseurl']}" scope="request" />
<c:set var="baseJsUrl" value="${requestScope['site.static.js.url']}" scope="request" />

<!DOCTYPE html>
<html lang="${fn:substring(locale,0,2)}">
<head>
    <base href="${baseUrl}/static/webapp/" />
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta charset="utf-8" />

    <link rel="preload" href="https://fonts.googleapis.com/css2?family=Montserrat:wght@100;200;300;400;600&display=swap" as="style" onload="this.onload=null;this.rel='stylesheet'" crossorigin>
    <%@ include file="pageHeaders.jsf" %>

    <script>
        window.serverconfig = {
            apiBaseUrl: '${requestScope['site.baseurl']}',
            analyticsAccount: '${requestScope['google.analytics.account']}',
            clientType: 'rest',
            recaptcha2Enabled: ${requestScope['google.recaptcha2.enabled']},
            recaptcha2SiteKey: '${requestScope['google.recaptcha2.siteKey']}',
            googleOauth2Url: '${requestScope['security.oauth2.google.url']}'
        };

        <!-- Hack to force view selection on react to move all the UI to react-->
        window.errorMvcView = '${requestScope['exception']!=null?(fn:indexOf(requestScope['exception'],'SecurityException') gt 1?'securityError':'unexpectedError'):''}';
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
              }, 50);
          };

          window.addEventListener("load", downloadJsAtOnload, false);
      </script>
    </c:if>
</head>

<body>
    <noscript>You need to enable JavaScript to run this app.</noscript>
    <div id="root"></div>

    <script type="text/javascript" src="${baseJsUrl}/webapp/vendors.bundle.js" crossorigin="anonymous" async></script>
    <script type="text/javascript" src="${baseJsUrl}/webapp/app.bundle.js" crossorigin="anonymous" async></script>
</body>

</html>
