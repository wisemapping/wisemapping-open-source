<%@page pageEncoding="UTF-8" %>
<%@include file="/jsp/init.jsp" %>

<h1><spring:message code="USER_REGISTRATION"/></h1>

<p><spring:message code="REGISTRATION_TITLE_MSG"/></p>
<form:form method="post" commandName="user" class="form-horizontal">
    <div class="form-group">
        <label for="email" class="col-md-2 control-label"><spring:message code="EMAIL"/>: </label>
        <div class="col-md-5">
            <form:input path="email" id="email" type="email" required="required" class="form-control"/>
            <form:errors path="email" cssClass="errorMsg"/>
        </div>
    </div>
    <div class="form-group">
        <label for="firstname" class="col-md-2 control-label"><spring:message code="FIRSTNAME"/>: </label>

        <div class="col-md-5">
            <form:input path="firstname" id="firstname" required="required" class="form-control"/>
            <form:errors path="firstname" cssClass="errorMsg"/>
        </div>
    </div>
    <div class="form-group">
        <label for="lastname" class="col-md-2 control-label"><spring:message code="LASTNAME"/>: </label>

        <div class="col-md-5">
            <form:input path="lastname" id="lastname" required="required" class="form-control"/>
            <form:errors path="lastname" cssClass="errorMsg"/>
        </div>
    </div>
    <div class="form-group">
        <label for="password" class="col-md-2 control-label"><spring:message code="PASSWORD"/>: </label>

        <div class="col-md-5">
            <form:password path="password" id="password" required="required" class="form-control"/>
            <form:errors path="password" cssClass="errorMsg"/>
        </div>
    </div>
    <div class="form-group">
        <label for="retypePassword" class="col-md-2 control-label"><spring:message
                code="RETYPE_PASSWORD"/>: </label>

        <div class="col-md-5">
            <form:password path="retypePassword" id="retypePassword" class="form-control"/>
            <form:errors path="retypePassword" cssClass="errorMsg"/>
        </div>
    </div>
    <div class="form-group">
        <div class="col-md-10 col-md-offset-2">
            <c:if test="${requestScope.captchaEnabled}">
                ${requestScope.captchaHtml}
                <p>
                    <form:errors path="captcha" cssClass="errorMsg"/>
                </p>
            </c:if>
        </div>
    </div>
    <div class="form-group">
        <p>
            <spring:message code="TERM_OF_THE_SERVICE"/>
            <spring:message code="WISEMAPPING_ACCOUNT_MESSAGE"/>
            <a href="c/termsOfUse" target="_blank"><spring:message code="HERE"/></a>.
            <spring:message code="REGISTRATION_CLICK_ADVICE"/>
        </p>
    </div>
    <div class="form-group">
        <div class="col-md-offset-2 col-md-10">
            <input type="submit" value="<spring:message code="REGISTER"/>"
                   data-loading-text="<spring:message code="REGISTER"/> ..." id="submitButton"
                   class="btn btn-primary">
            <input type="button" value="<spring:message code="CANCEL"/>"
                   onclick="window.location='c/<c:url value="maps/"/>'" class="btn">

        </div>
    </div>
</form:form>



