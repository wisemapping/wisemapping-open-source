<%@page pageEncoding="UTF-8" %>
<%@ include file="/jsp/init.jsp" %>

<%--@elvariable id="isHsql" type="boolean"--%>

<script type="text/javascript" src="js/jquery-1.7.2.min.js"></script>
<script type="text/javascript" language="javascript">
    $(function () {
        $('#loginForm').submit(function () {
            $('.btn-primary').button("loading");
        });
    });
</script>

<div class="row-fluid">
    <div class="span5" style="margin-top: 20px">
        <h1><spring:message code="WELCOME_TO_WISEMAPPING"/></h1>
        <spring:message code="WELCOME_DETAILS"/>
    </div>
    <div id="login" class="fform span6">
        <h1><spring:message code="SIGN_IN"/></h1>

        <form action="<c:url value='/c/j_spring_security_check'/>" method="POST" class="form-horizontal" id="loginForm">
            <div class="control-group">
                <label class="control-label" for="email"><spring:message code="EMAIL"/></label>

                <div class="controls">
                    <input type='email' tabindex="1" id="email" name='j_username' required="required" class="span12"/>
                </div>
            </div>
            <div class="control-group">
                <label class="control-label" for="password"><spring:message code="PASSWORD"/></label>

                <div class="controls">
                    <input type='password' tabindex="2" id="password" name='j_password' required="required"
                           class="span12"/>
                </div>
            </div>
            <div class="control-group" style="text-align: right">

                <label>
                    <button class="btn btn-primary" tabindex="4" data-loading-text="<spring:message code="SIGN_ING"/>">
                        <spring:message code="SIGN_IN"/></button>
                    <input type="checkbox" id="rememberme" name="_spring_security_remember_me"
                           tabindex="3"/> <spring:message code="REMEMBER_ME"/>
                </label>

                <div style="text-align: center">
                    <a href="<c:url value="/c/user/resetPassword"/>"><spring:message code="FORGOT_PASSWORD"/></a>
                </div>
            </div>

            <div class="control-group">
                <c:if test="${not empty param.login_error}">
                    <c:choose>
                        <c:when test="${param.login_error == 3}">
                            <div class="alert alert-error"><spring:message code="USER_INACTIVE"/></div>
                        </c:when>
                        <c:otherwise>
                            <div class="alert alert-error"><spring:message code="LOGIN_ERROR"/></div>
                        </c:otherwise>
                    </c:choose>
                </c:if>
            </div>
        </form>
    </div>
</div>

<div class="row">
    <div id="register" class="span12">
        <c:if test="${requestScope['security.type']=='db'}">
            <b><spring:message code="NOT_READY_A_USER"/></b>
            <spring:message code="NOT_READY_A_USER_MESSAGE"/>
            <a href="c/user/registration">
                <spring:message code="JOIN_NOW"/>
            </a>
        </c:if>
    </div>
</div>


<c:if test="${isHsql== 'true'}">
    <div class="row-fluid">
        <div class="alert alert-info span offset12">
            <span class="label label-important"><spring:message code="WARNING"/></span> <spring:message
                code="NO_PRODUCTION_DATABASE_CONFIGURED"/>&nbsp;<a
                href="http://www.wisemapping.org/documentation/configu"><spring:message code="HERE"/></a>.
        </div>
    </div>
</c:if>
