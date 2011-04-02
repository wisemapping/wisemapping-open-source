<%@ include file="/jsp/init.jsp" %>

<script type="text/javascript">
    if(typeof isOldIE != "undefined"){
        window.onload = function() {
            var simpleButtonGenerator = RUZEE.ShadedBorder.create({ corner:8,  border:1 });
            simpleButtonGenerator.render('forgotPasswordContainer');

            $('submitButton').onclick = displayLoading;
        };
    }
</script>

<div id="forgotPasswordContent">
    <div id="forgotPasswordContainer" class="sb">
        <h1>
            <spring:message code="FORGOT_PASSWORD"/>
        </h1>

        <h2>
            <spring:message code="FORGOT_PASSWORD_MESSAGE"/>
        </h2>
        <form:form method="post" commandName="forgotPassword">
            <table>
                <tbody>
                    <tr>
                        <td>
                            <spring:message code="EMAIL"/>
                            :
                        </td>
                        <td>
                            <form:input path="email" id="email" tabindex="1"/>
                            <form:errors path="email" cssClass="errorMsg"/>
                        </td>
                    </tr>
                    <tr>
                        <td>&nbsp;</td>
                        <td>
                            <input type="submit" value="<spring:message code="SUBMIT"/>" class="btn-primary"
                                   id="submitButton">
                            <input type="button" value="<spring:message code="CANCEL"/>" class="btn-primary"
                                   onclick="window.location='<c:url value="mymaps.htm"/>'">
                        </td>
                    </tr>
                </tbody>
            </table>
        </form:form>
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