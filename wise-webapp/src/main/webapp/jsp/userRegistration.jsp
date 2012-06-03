<%@ include file="/jsp/init.jsp" %>


<div>
    <div class="fform">

        <h1><spring:message code="USER_REGISTRATION"/></h1>

        <p><spring:message code="REGISTRATION_TITLE_MSG"/></p>
        <form:form method="post" commandName="user">
        <fieldset>
            <label for="email"><spring:message code="EMAIL"/></label>
                <form:input path="email" id="email" type="email" required="required"/>
                <form:errors path="email" cssClass="errorMsg"/>

            <label for="username"> <spring:message code="USERNAME"/></label>
                <form:input path="username" id="username" required="required"/>
                <form:errors path="username" cssClass="errorMsg"/>


            <label for="firstname"><spring:message code="FIRSTNAME"/></label>
                <form:input path="firstname" id="firstname" required="required"/>
                <form:errors path="firstname" cssClass="errorMsg"/>

            <label for="lastname"><spring:message code="LASTNAME"/></label>
                <form:input path="lastname" id="lastname" required="required"/>
                <form:errors path="lastname" cssClass="errorMsg"/>

            <label for="password"><spring:message code="PASSWORD"/></label>
                <form:password path="password" id="password" required="required"/>
                <form:errors path="password" cssClass="errorMsg"/>

            <label for="retypePassword"><spring:message code="RETYPE_PASSWORD"/></label>
                <form:password path="retypePassword" id="retypePassword"/>
                <form:errors path="retypePassword" cssClass="errorMsg"/>

            <c:if test="${requestScope.captchaEnabled}">
                <form:errors path="captcha" cssClass="errorMsg"/>
                ${requestScope.captchaHtml}
            </c:if>

            <div>
                <p>
                    <spring:message code="TERM_OF_THE_SERVICE"/>
                    <spring:message code="WISEMAPPING_ACCOUNT_MESSAGE"/> <a href="c/termsOfUse"><spring:message
                        code="HERE"/></a>
                    <spring:message code="REGISTRATION_CLICK_ADVICE"/>
                </p>
            </div>

            <input type="submit" value="<spring:message code="REGISTER" />" id="submitButton"
                   class="btn btn-primary">
            <input type="button" value="<spring:message code="CANCEL"/>"
                   onclick="window.location='c/<c:url value="maps/"/>'" class="btn">
            </form:form>
    </div>
</div>
