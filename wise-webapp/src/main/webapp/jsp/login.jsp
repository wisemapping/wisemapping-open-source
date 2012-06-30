<%@page pageEncoding="UTF-8" %>
<%@ include file="/jsp/init.jsp" %>

<%--@elvariable id="isHsql" type="boolean"--%>

<script type="text/javascript" language="javascript">
    $(function() {
        $('.btn-primary').click(function() {
            $(this).button("loading");
        });
    });
</script>

<div id="loginContent">
    <div class="loginNews">
        <h1><spring:message code="WHAT_IS_NEW"/>: </h1>
        <spring:message code="WHAT_IS_NEW_DETAILS"/>
    </div>

    <div id="login" class="fform">
        <h1>
            <spring:message code="SIGN_IN"/>
        </h1>

        <form action="<c:url value='/c/j_spring_security_check'/>" method="POST">
            <fieldset>
                <label for="email"><spring:message code="EMAIL"/></label>
                <input type='email' tabindex="1" id="email" name='j_username' required="required"/>

                <label for="password"><spring:message code="PASSWORD"/></label>
                <input type='password' tabindex="2" id="password" name='j_password' required="required"/>

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

                <div class="form-inline">
                    <button class="btn btn-primary" data-loading-text="<spring:message code="SIGN_ING"/>">
                        <spring:message code="SIGN_IN"/></button>
                    &nbsp;&nbsp;&nbsp;
                    <input type="checkbox" id="rememberme" name="_spring_security_remember_me"/>
                    <label for="rememberme"><spring:message code="REMEMBER_ME"/></label>
                </div>
            </fieldset>
        </form>
        <a href="<c:url value="/c/user/resetPassword"/>"><spring:message code="FORGOT_PASSWORD"/></a>
    </div>
</div>

<div id="register">
    <b><spring:message code="NOT_READY_A_USER"/></b>
    <spring:message code="NOT_READY_A_USER_MESSAGE"/>
    <a href="c/user/registration">
        <spring:message code="JOIN_NOW"/>
    </a>
</div>

<c:if test="${isHsql== 'true'}">
    <div class="alert alert-info">
       <span class="label label-important"><spring:message code="WARNING"/></span> <spring:message code="NO_PRODUCTION_DATABASE_CONFIGURED"/>&nbsp;<a
            href="http://www.wisemapping.org/documentation/configu"><spring:message code="HERE"/></a>.
    </div>
</c:if>
