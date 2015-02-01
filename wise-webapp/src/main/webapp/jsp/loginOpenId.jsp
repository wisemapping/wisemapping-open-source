<%@page pageEncoding="UTF-8" %>
<%@ include file="/jsp/init.jsp" %>

<%--@elvariable id="isHsql" type="boolean"--%>
<!-- Simple OpenID Selector -->
<link type="text/css" rel="stylesheet" href="css/openid.css"/>
<script type="text/javascript" language="javascript" src="js/jquery.js"></script>
<script type="text/javascript" language="javascript" src="js/openid-jquery.js"></script>
<script type="text/javascript" language="javascript" src="js/openid-en.js"></script>
<!-- /Simple OpenID Selector -->
<script type="text/javascript">
    $(document).ready(function () {
        openid.init('openid_identifier');
    });
</script>

<script type="text/javascript" src="js/jquery.js"></script>
<script type="text/javascript" language="javascript">
    $(function () {
        $('#loginForm').submit(function () {
            $('.btn-primary').button("loading");
        });
    });
</script>
<div class="row" style="padding: 10px 0px">
    <h1><spring:message code="OPENID_LOGIN"/></h1>
    <spring:message code="LOGING_OPENID_DETAILS"/>
</div>
<div class="row">
    <div id="login" class="fform col-md-8">
        <form action="/c/j_spring_openid_security_check" method="get" id="openid_form">
            <input type="hidden" name="action" value="verify" class="form-control"/>
            <fieldset>
                <div id="openid_choice">
                    </br>
                    <div id="openid_btns"></div>
                </div>
                <div id="openid_input_area">
                    <input id="openid_identifier" name="openid_identifier" type="text" value="http://" class="form-control"/>
                    <input id="openid_submit" type="submit" value="Sign-In" class="form-control" class="btn-primary btn"/>
                </div>
                <noscript>
                    <p>OpenID is service that allows you to log-on to many different websites using a single indentity.
                        Find out <a href="http://openid.net/what/">more about OpenID</a> and <a
                                href="http://openid.net/get/">how to get an OpenID enabled account</a>.</p>
                </noscript>
            </fieldset>
        </form>
        <!-- /Simple OpenID Selector -->
    </div>
    <div class="col-md-4" style="background-color: #FFEFC6;padding: 10px">
        <spring:message code="WHY_OPENID"/>
    </div>
</div>
</br></br>

