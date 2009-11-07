<%@ include file="/jsp/init.jsp" %>
<script type="text/javascript">
    window.onload = function() {
        var simpleButtonGenerator = RUZEE.ShadedBorder.create({ corner:8,  border:1 });
        simpleButtonGenerator.render('login');

        $('submitButton').onclick = displayLoading;
    };
</script>

<div id="loginContent">

    <div id="news" class="sb">
        <h1>What's New:</h1>
        <ul>
            <li>New Toolbar</li>
            <li>Add icons to Topics</li>
            <li>History changes for the last 10 modifications</li>
            <li>Auto Save</li>
        </ul>
        <h1>Upcoming Features:</h1>
            <li>Add notes to Topics</li>
        <ul>
        </ul>
    </div>
    <div id="login" class="sb">
        <h1>
            <spring:message code="SIGN_IN"/>
        </h1>

        <form action="<c:url value='j_acegi_security_check'/>" method="POST">
            <table>
                <tbody>
                    <tr>
                        <c:if test="${not empty param.login_error}">
                        <td>
                            &nbsp;
                        </td>
                        <td class="errorMsg">
                            <c:choose>
                                <c:when test="${param.login_error == 3}">
                                    <spring:message code="USER_INACTIVE"/>
                                </c:when>
                                <c:otherwise>
                                    <spring:message code="LOGIN_ERROR"/>
                                </c:otherwise>
                            </c:choose>
                        </td>
                    </tr>
                    </c:if>
                    <tr>
                        <td class="formLabel">
                            <spring:message code="EMAIL"/>
                            :
                        </td>
                        <td>
                            <input type='text' tabindex="1" id="email" name='j_username'/>
                        </td>
                    </tr>
                    <tr>
                        <td class="formLabel">
                            <spring:message code="PASSWORD"/>
                            :
                        </td>
                        <td>
                            <input type='password' tabindex="2" id="password" name='j_password'/>
                        </td>
                    </tr>
                    <tr>
                        <td class="formLabel">
                            <input type="checkbox" id="rememberme" name="_acegi_security_remember_me"/>
                        </td>
                        <td>
                            <spring:message code="REMEMBER_ME"/>
                        </td>
                    </tr>
                    <tr>
                        <td>&nbsp;</td>
                        <td>
                            <input type="submit" class="btn-primary" id="submitButton"
                                   value="<spring:message code="SIGN_IN"/>">

                            <div style="text-align:right;"><a href="<c:url value="forgotPassword.htm"/>">
                                <spring:message code="FORGOT_PASSWORD"/>
                            </a></div>
                        </td>
                    </tr>
                </tbody>
            </table>
        </form>
    </div>
</div>

<div id="register">
    <b>
        <spring:message code="NOT_READY_A_USER"/>
    </b>
    <spring:message code="NOT_READY_A_USER_MESSAGE"/>
    <a href="userRegistration.htm">
        <spring:message code="JOIN_NOW"/>
    </a>
</div>
