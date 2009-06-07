<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<div id="footer">
    <div style="width:20%; float:left;">&nbsp;</div>
    <div style="float:left; width:60%;">
        <a href="mailto:team@wisemapping.com"><spring:message code="CONTACT"/></a>

        <p><spring:message code="COPYRIGHT"/></p>
    </div>
    <div style="float:left; text-align:left;padding:5px;">
        <form action="https://www.paypal.com/cgi-bin/webscr" method="post">
            <input type="hidden" name="cmd" value="_s-xclick">
            <input type="image" src="https://www.paypal.com/en_US/i/btn/x-click-but04.gif" border="0" name="submit" alt="Make payments with PayPal - it's fast, free and secure!">
            <img alt="" border="0" src="https://www.paypal.com/en_US/i/scr/pixel.gif" width="1" height="1">
            <input type="hidden" name="encrypted" value="-----BEGIN PKCS7-----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-----END PKCS7-----">
        </form>
    </div>
</div>
<script src="http://www.google-analytics.com/urchin.js" type="text/javascript">
</script>
<script type="text/javascript">
    _uacct = "UA-2347723-1";
    urchinTracker();
</script>