<%@page pageEncoding="UTF-8" %>
<%@include file="/jsp/init.jsp" %>

<script type="text/javascript" language="javascript">
    $(function () {
        $('.btn-primary').click(function () {
            $(this).button("loading");
        });
    });
</script>

<h1>
    <spring:message code="FORGOT_PASSWORD"/>
</h1>

<p><spring:message code="FORGOT_PASSWORD_MESSAGE"/></p>

<form:form method="post" commandName="resetPassword" class="form-horizontal">
    <label for="email" class="col-md-2 control-label"><spring:message code="EMAIL"/>: </label>
    <div class="col-md-5">
        <input id="email" type="email" required="required" name="email" class="form-control"/>
    </div>
    <input type="submit" value="<spring:message code="SEND_ME_A_NEW_PASSWORD"/>" class="btn btn-primary"
           data-loading-text="<spring:message code="SENDING"/>"/>
    <input type="button" value="<spring:message code="CANCEL"/>" class="btn"
           onclick="window.location='<c:url value="c/maps/"/>'"/>
</form:form>

<div id="register">
    <b>
        <spring:message code="NOT_READY_A_USER"/>
    </b>
    <spring:message code="NOT_READY_A_USER_MESSAGE"/>
    <a href="c/user/registration">
        <spring:message code="JOIN_NOW"/>
    </a>
</div>