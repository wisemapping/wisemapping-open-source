<%@ include file="/jsp/init.jsp" %>


<div id="userRegistrationContent">
    <div id="userRegistration" class="sb">
        <h1>
            <spring:message code="USER_REGISTRATION"/>
        </h1>

        <h2>
            <spring:message code="REGISTRATION_TITLE_MSG"/> <spring:message code="FIELD_REQUIRED_MSG"/>
        </h2>
        <form:form method="post" commandName="user">
            <table>
                <tbody>
                <tr>
                    <td class="formLabel">
                        <span class="fieldRequired">*</span>
                        <label for="email"><spring:message code="EMAIL"/>:</label>
                    </td>
                    <td>
                        <form:input path="email" id="email"/>
                        <form:errors path="email" cssClass="errorMsg"/>
                    </td>
                </tr>
                <tr>
                    <td class="formLabel">
                        <span class="fieldRequired">*</span>
                        <label for="username"> <spring:message code="USERNAME"/>:</label>
                    </td>
                    <td>
                        <form:input path="username" id="username"/>
                        <form:errors path="username" cssClass="errorMsg"/>
                    </td>
                </tr>
                <tr>
                    <td class="formLabel">
                        <span class="fieldRequired">*</span>
                        <label for="firstname"><spring:message code="FIRSTNAME"/>:</label>
                    </td>
                    <td>
                        <form:input path="firstname" id="firstname"/>
                        <form:errors path="firstname" cssClass="errorMsg"/>
                    </td>
                </tr>
                <tr>
                    <td class="formLabel">
                        <span class="fieldRequired">*</span>
                        <label for="lastname"><spring:message code="LASTNAME"/>:</label>
                    </td>
                    <td>
                        <form:input path="lastname" id="lastname"/>
                        <form:errors path="lastname" cssClass="errorMsg"/>
                    </td>
                </tr>
                <tr>
                    <td class="formLabel">
                        <span class="fieldRequired">*</span>
                        <label for="password"><spring:message code="PASSWORD"/>:</label>
                    </td>
                    <td>
                        <form:password path="password" id="password"/>
                        <form:errors path="password" cssClass="errorMsg"/>
                    </td>
                </tr>
                <tr>
                    <td class="formLabel">
                        <span class="fieldRequired">*</span>
                        <label for="retypePassword"><spring:message code="RETYPE_PASSWORD"/>:</label>
                    </td>
                    <td>
                        <form:password path="retypePassword" id="retypePassword"/>
                        <form:errors path="retypePassword" cssClass="errorMsg"/>
                    </td>
                </tr>
                <c:if test="${requestScope.captchaEnabled}">
                    <tr>
                        <td class="formLabel">
                        </td>
                        <td>
                            <form:errors path="captcha" cssClass="errorMsg"/>
                                ${requestScope.captchaHtml}
                        </td>
                    </tr>
                </c:if>
                <tr>
                    <td class="formLabel">
                        <spring:message code="TERM_OF_THE_SERVICE"/>
                    </td>
                    <td>
                        <spring:message code="WISEMAPPING_ACCOUNT_MESSAGE"/>
                        <a href="termsOfUse.htm">
                            <spring:message code="HERE"/>
                        </a>.<br/>
                    </td>
                </tr>
                <tr>
                    <td class="formLabel">&nbsp;</td>
                    <td>
                        <spring:message code="REGISTRATION_CLICK_ADVICE"/>
                    </td>
                </tr>
                <tr>
                    <td>&nbsp;</td>
                    <td><input type="submit" value="<spring:message code="REGISTER"/>" id="submitButton"
                               class="btn-primary">
                        <input type="button" value="<spring:message code="CANCEL"/>"
                               onclick="window.location='<c:url value="mymaps.htm"/>'" class="btn-secondary">
                    </td>
                </tr>
                </tbody>
            </table>
        </form:form>
    </div>
</div>
