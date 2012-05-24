<%--@elvariable id="isHsql" type="boolean"--%>

<%@ include file="/jsp/init.jsp" %>

<div id="loginContent">
    <div class="loginNews">
        <h1>What is New: </h1>
        <ul>
            <li>Links Between Nodes</li>
            <li>FreeMind 0.9 Update</li>
            <li>Improved HTML 5.0 Support</li>
            <li>Firefox 12 officially supported</li>
        </ul>
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
                    <input type="submit" class="btn btn-primary" value="<spring:message code="SIGN_IN"/>"
                           data-toggle="button">&nbsp;&nbsp;&nbsp;
                    <input type="checkbox" id="rememberme" name="_spring_security_remember_me"/>
                    <label for="rememberme"><spring:message code="REMEMBER_ME"/></label>
                </div>
            </fieldset>
        </form>
        <a href="<c:url value="/c/forgotPassword.htm"/>"><spring:message code="FORGOT_PASSWORD"/></a>
    </div>
</div>

<div id="register">
    <b><spring:message code="NOT_READY_A_USER"/></b>
    <spring:message code="NOT_READY_A_USER_MESSAGE"/>
    <a href="c/userRegistration.htm">
        <spring:message code="JOIN_NOW"/>
    </a>
</div>

<c:if test="${isHsql== 'true'}">
    <div style="padding:10px;background-color: #E0EFFF; border-radius: 5px 5px 5px 5px;border-style:solid;border-color:gray">
        <img src="../images/info.png" style="margin:0 4px" alt="info">
        <spring:message code="NO_PRODUCTION_DATABASE_CONFIGURED"/>&nbsp;<a
            href="http://www.wisemapping.org/documentation/configu">here</a>.
    </div>
</c:if>
