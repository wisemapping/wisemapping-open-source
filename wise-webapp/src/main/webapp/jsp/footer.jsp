<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<div id="footerContainer" class="row">
    <script type="text/javascript"
            src="https://wisemapping.atlassian.net/s/d41d8cd98f00b204e9800998ecf8427e/en_US-f1g0rh-1988229788/6211/26/1.4.5/_/download/batch/com.atlassian.jira.collector.plugin.jira-issue-collector-plugin:issuecollector/com.atlassian.jira.collector.plugin.jira-issue-collector-plugin:issuecollector.js?collectorId=a47981d6"></script>
    <script type="text/javascript"
            src="https://wisemapping.atlassian.net/s/d41d8cd98f00b204e9800998ecf8427e/en_US-f1g0rh-1988229788/6211/26/1.4.5/_/download/batch/com.atlassian.jira.collector.plugin.jira-issue-collector-plugin:issuecollector/com.atlassian.jira.collector.plugin.jira-issue-collector-plugin:issuecollector.js?collectorId=703b9822"></script>

    <script type="text/javascript">
        window.ATL_JQ_PAGE_PROPS = $.extend(window.ATL_JQ_PAGE_PROPS, {
            "a47981d6": {
                triggerFunction: function (showCollectorDialog) {
                    //Requries that jQuery is available!
                    jQuery("#feedbackBtn").click(function (e) {
                        e.preventDefault();
                        showCollectorDialog();
                    });
                }
            },
            "703b9822": {
                triggerFunction: function (showCollectorDialog) {
                    //Requries that jQuery is available!
                    jQuery("#reportIssueBtn").click(function (e) {
                        e.preventDefault();
                        showCollectorDialog();
                    });
                }
            }
        });
    </script>

    <div class="col-md-1 col-md-offset-2">
        <a href="https://twitter.com/share" class="twitter-share-button" data-url="http://www.wisemapping.com"
           data-text="Check out this site" data-via="wisemapping" data-related="wisemapping">Tweet</a>
        <script>!function (d, s, id) {
            var js, fjs = d.getElementsByTagName(s)[0], p = /^http:/.test(d.location) ? 'http' : 'https';
            if (!d.getElementById(id)) {
                js = d.createElement(s);
                js.id = id;
                js.src = p + '://platform.twitter.com/widgets.js';
                fjs.parentNode.insertBefore(js, fjs);
            }
        }(document, 'script', 'twitter-wjs');</script>
    </div>
    <div class="col-md-1">
        <!-- Place this tag where you want the +1 button to render. -->
        <div class="g-plusone" data-href="http://www.wisemapping.com"></div>

        <!-- Place this tag after the last +1 button tag. -->
        <script type="text/javascript">
            (function () {
                var po = document.createElement('script');
                po.type = 'text/javascript';
                po.async = true;
                po.src = 'https://apis.google.com/js/plusone.js';
                var s = document.getElementsByTagName('script')[0];
                s.parentNode.insertBefore(po, s);
            })();
        </script>
    </div>
    <div class="col-md-4" style="text-align: center">
        <a href="" id="feedbackBtn"><spring:message code="FEEDBACK"/></a> |
        <a href="" id="reportIssueBtn"><spring:message code="REPORT_BUG"/></a> |
        <a href="mailto:team@wisemapping.com"><spring:message code="CONTACT_US"/></a> |
        <a href="https://wisemapping.atlassian.net/wiki/display/WS/License  "><spring:message
                code="LICENSE"/></a><br/>
        <a href="http://www.wisemapping.org/"><spring:message code="COPYRIGHT"/></a>
    </div>
    <div class="col-md-1">
        <form action="https://www.paypal.com/cgi-bin/webscr" method="post">
            <input type="hidden" name="cmd" value="_s-xclick"/>
            <input type="image" src="https://www.paypal.com/en_US/i/btn/x-click-but04.gif" name="submit"
                   alt="Make payments with PayPal - it's fast, free and secure!"/>
            <img alt="" style="border: 0;width:1px;height:1px;" src="https://www.paypal.com/en_US/i/scr/pixel.gif"/>
            <input type="hidden" name="encrypted"
                   value="-----BEGIN PKCS7-----MIIHwQYJKoZIhvcNAQcEoIIHsjCCB64CAQExggEwMIIBLAIBADCBlDCBjjELMAkGA1UEBhMCVVMxCzAJBgNVBAgTAkNBMRYwFAYDVQQHEw1Nb3VudGFpbiBWaWV3MRQwEgYDVQQKEwtQYXlQYWwgSW5jLjETMBEGA1UECxQKbGl2ZV9jZXJ0czERMA8GA1UEAxQIbGl2ZV9hcGkxHDAaBgkqhkiG9w0BCQEWDXJlQHBheXBhbC5jb20CAQAwDQYJKoZIhvcNAQEBBQAEgYBvLN5PRNvfylLOCDCi65JktD2se3FdTyRH1+Ptw+OrhDWUX76pT8qt89aCzRjroJikwKfgmiyLHSOw4rDF5xGbzesCdAjpkrv5KwMRxiaf/FEdXDHHufv2pwP591+h7mY36I0+nDdwVykq7KteiQRsfFQeLkHikRsZ6Gtw3eRuBjELMAkGBSsOAwIaBQAwggE9BgkqhkiG9w0BBwEwFAYIKoZIhvcNAwcECNad8bwThZeKgIIBGEkN7nh0XMYn8N6aOZm9Dqtnty8qTW42ACmxf9llJ1wzj4SRT9SEpHfq4tMG3hRRjAhJ6DRW8k+0QacC5exvzddGo1bIFGvNxWnXF3CEUy2yc2Dw/YaUlsZsSYcyChi9yxjmNnrH7YYDgnpAq7V1fcKN89t8gnNA2+KAPENtT6yF8eNzrzf5ckfFBOJXawLW4lACk5h1jrCmF5oWL/SicDsjLMFvXkD6P7tHsxOlLHj1Oe6k+Ejb1xsFpagsiU5/CWyTpP0sjgXyY/z08sJXk9HBYNJOwTXd7u6h9h6mjHKuCb1p5vCQbFY0yDV881ILsnpzguAOGHbMTzmYSenDcdj6JnzQDQxYUQTNYfLgtKgO1Xy3M63UA9mgggOHMIIDgzCCAuygAwIBAgIBADANBgkqhkiG9w0BAQUFADCBjjELMAkGA1UEBhMCVVMxCzAJBgNVBAgTAkNBMRYw FAYDVQQHEw1Nb3VudGFpbiBWaWV3MRQwEgYDVQQKEwtQYXlQYWwgSW5jLjETMBEGA1UECxQKbGl2ZV9jZXJ0czERMA8GA1UEAxQIbGl2ZV9hcGkxHDAaBgkqhkiG9w0BCQEWDXJlQHBheXBhbC5jb20wHhcNMDQwMjEzMTAxMzE1WhcNMzUwMjEzMTAxMzE1WjCBjjELMAkGA1UEBhMCVVMxCzAJBgNVBAgTAkNBMRYwFAYDVQQHEw1Nb3VudGFpbiBWaWV3MRQwEgYDVQQKEwtQYXlQYWwgSW5jLjETMBEGA1UECxQKbGl2ZV9jZXJ0czERMA8GA1UEAxQIbGl2ZV9hcGkxHDAaBgkqhkiG9w0BCQEWDXJlQHBheXBhbC5jb20wgZ8wDQYJKoZIhvcNAQEBBQADgY0AMIGJAoGBAMFHTt38RMxLXJyO2SmS+Ndl72T7oKJ4u4uw+6awntALWh03PewmIJuzbALScsTS4sZoS1fKciBGoh11gIfHzylvkdNe/hJl66/RGqrj5rFb08sAABNTzDTiqqNpJeBsYs/c2aiGozptX2RlnBktH+SUNpAajW724Nv2Wvhif6sFAgMBAAGjge4wgeswHQYDVR0OBBYEFJaffLvGbxe9WT9S1wob7BDWZJRrMIG7BgNVHSMEgbMwgbCAFJaffLvGbxe9WT9S1wob7BDWZJRroYGUpIGRMIGOMQswCQYDVQQGEwJVUzELMAkGA1UECBMCQ0ExFjAUBgNVBAcTDU1vdW50YWluIFZpZXcxFDASBgNVBAoTC1BheVBhbCBJbmMuMRMwEQYDVQQLFApsaXZlX2NlcnRzMREwDwYDVQQDFAhsaXZlX2FwaTEcMBoGCSqGSIb3DQEJARYNcmVAcGF5cGFsLmNvbYIBADAMBgNVHRMEBTADAQH/MA0GCSqGSIb3DQEBBQUAA4GBAIFfOlaagFrl71+jq6OKidbWFSE+Q4FqROvdgIONth +8kSK//Y/4ihuE4Ymvzn5ceE3S/iBSQQMjyvb+s2TWbQYDwcp129OPIbD9epdr4tJOUNiSojw7BHwYRiPh58S1xGlFgHFXwrEBb3dgNbMUa+u4qectsMAXpVHnD9wIyfmHMYIBmjCCAZYCAQEwgZQwgY4xCzAJBgNVBAYTAlVTMQswCQYDVQQIEwJDQTEWMBQGA1UEBxMNTW91bnRhaW4gVmlldzEUMBIGA1UEChMLUGF5UGFsIEluYy4xEzARBgNVBAsUCmxpdmVfY2VydHMxETAPBgNVBAMUCGxpdmVfYXBpMRwwGgYJKoZIhvcNAQkBFg1yZUBwYXlwYWwuY29tAgEAMAkGBSsOAwIaBQCgXTAYBgkqhkiG9w0BCQMxCwYJKoZIhvcNAQcBMBwGCSqGSIb3DQEJBTEPFw0wNzA5MDQxMTMyMTNaMCMGCSqGSIb3DQEJBDEWBBTF2vsxwMzHX7TQrdpdCFCp3Rk6TDANBgkqhkiG9w0BAQEFAASBgJS4fx+wCQaPzs3wvgaJOvbgub23AuGbaMc3fYKGxJf5JTxUVsSkQY9t6itXUr2llwc/GprbKaCvcOnOBXT8NkZ6gWqNX9iwDq83rblm3XI7yrjRUCQrvIkhJ80xKGrhBn48V61FawASYdpE1AmhZoga9XAIZruO0NrnT2QXxe2p-----END PKCS7-----"/>
        </form>
    </div>
    <div class="col-md-1">
        <a href="https://www.w3.org/html/logo/">
            <img src="images/html5-badge-h-graphics.png" width="66" height="32"
                 alt="HTML5 Powered with Graphics, 3D &amp; Effects"
                 title="HTML5 Powered with Graphics, 3D &amp; Effects">
        </a>
    </div>
</div>

<%@ include file="/jsp/googleAnalytics.jsf" %>