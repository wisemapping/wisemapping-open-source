<%@page pageEncoding="UTF-8" %>
<%@ include file="/jsp/init.jsp" %>

<%--@elvariable id="isHsql" type="boolean"--%>

<script type="text/javascript" src="js/jquery-2.1.0.min.js"></script>
<script type="text/javascript" language="javascript">
    $(function () {
        $('#loginForm').submit(function () {
            $('.btn-primary').button("loading");
        });
    });
</script>

<div class="row">
    <div class="col-md-5" style="margin-top: 20px">
        <h1><spring:message code="WELCOME_TO_WISEMAPPING"/></h1>
        <spring:message code="WELCOME_DETAILS"/>
    </div>
    <div id="login" class="fform col-md-6">
        <h1><spring:message code="SIGN_IN"/></h1>

        <form action="<c:url value='/c/j_spring_security_check'/>" method="POST" class="form-horizontal" id="loginForm">
            <div class="form-group">
                <label class="col-sm-2 control-label" for="email"><spring:message code="EMAIL"/></label>
                <div class="col-md-10">
                    <input type='email' tabindex="1" id="email" name='j_username' required="required" class="form-control"/>
                </div>
            </div>
            <div class="form-group">
                <label class="col-sm-2 control-label" for="password"><spring:message code="PASSWORD"/></label>
                <div class="col-md-10">
                    <input type='password' tabindex="2" id="password" name='j_password' required="required" class="form-control"/>
                </div>
            </div>
            <div class="form-group" style="text-align: center" >
                <div class="col-md-offset-2 col-md-10" style="text-align: center" >
                    <button class="btn btn-primary" tabindex="4" data-loading-text="<spring:message code="SIGN_ING"/>"><spring:message code="SIGN_IN"/></button>
                    <input type="checkbox" id="rememberme" name="_spring_security_remember_me"/>  <label for="rememberme"><spring:message code="REMEMBER_ME"/></label>
                </div>
            </div>
            <div class="form-group" style="text-align: center" >
                <a href="<c:url value="/c/user/resetPassword"/>"><spring:message code="FORGOT_PASSWORD"/></a>
            </div>
            <div class="form-group">
                <c:if test="${not empty param.login_error}">
                    <c:choose>
                        <c:when test="${param.login_error == 3}">
                            <div class="alert alert-danger"><spring:message code="USER_INACTIVE"/></div>
                        </c:when>
                        <c:otherwise>
                            <div class="alert alert-danger"><spring:message code="LOGIN_ERROR"/></div>
                        </c:otherwise>
                    </c:choose>
                </c:if>
            </div>
        </form>
    </div>
</div>

<div class="row">
    <div id="register" class="col-md-10">
        <c:if test="${requestScope['security.type']=='db'}">
            <b><spring:message code="NOT_READY_A_USER"/></b>
            <spring:message code="NOT_READY_A_USER_MESSAGE"/>
            <a href="c/user/registration">
                <spring:message code="JOIN_NOW"/>
            </a>
        </c:if>
        <c:if test="${requestScope['security.openid.enabled']}">
            <p>
                <spring:message code="LOGIN_USING_OPENID"/> <a href="/c/loginopenid"><b><spring:message code="HERE"/></b></a>.
            </p>
        </c:if>
    </div>
</div>


<c:if test="${isHsql== 'true'}">
    <div class="row">
        <div class="alert alert-info col-md-offset12">
            <span class="label label-danger"><spring:message code="WARNING"/></span> <spring:message
                code="NO_PRODUCTION_DATABASE_CONFIGURED"/>&nbsp;<a
                href="http://www.wisemapping.org/documentation/configu"><spring:message code="HERE"/></a>.
        </div>
    </div>
</c:if>
