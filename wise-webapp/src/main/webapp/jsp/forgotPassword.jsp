<%@ include file="/jsp/init.jsp" %>


<div>
    <div class="fform">
        <h1>
            <spring:message code="FORGOT_PASSWORD"/>
        </h1>
        <p><spring:message code="FORGOT_PASSWORD_MESSAGE"/></p>

        <form:form method="post" commandName="forgotPassword">
            <fieldset>
                <label for="email"><spring:message code="EMAIL"/></label>
                <form:input path="email" id="email" tabindex="1" type="email" required="required"/>
                <form:errors path="email" cssClass="errorMsg"/>

                <input type="submit" value="<spring:message code="SUBMIT"/>" class="btn btn-primary"/>
                <input type="button" value="<spring:message code="CANCEL"/>" class="btn"
                       onclick="window.location='<c:url value="c/mymaps.htm"/>'"/>
            </fieldset>
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