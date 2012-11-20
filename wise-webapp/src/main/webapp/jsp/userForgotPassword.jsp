<%@page pageEncoding="UTF-8" %>
<%@include file="/jsp/init.jsp" %>

<script type="text/javascript" language="javascript">
    $(function () {
        $('.btn-primary').click(function () {
            $(this).button("loading");
        });
    });
</script>

<div>
    <div class="fform">
        <h1>
            <spring:message code="FORGOT_PASSWORD"/>
        </h1>

        <p><spring:message code="FORGOT_PASSWORD_MESSAGE"/></p>

        <form:form method="post" commandName="resetPassword">
            <fieldset>
                <label for="email"><spring:message code="EMAIL"/></label>
                <input id="email" type="email" required="required" name="email"/>

                <input type="submit" value="<spring:message code="SEND_ME_A_NEW_PASSWORD"/>" class="btn btn-primary"
                       data-loading-text="<spring:message code="SENDING"/>"/>
                <input type="button" value="<spring:message code="CANCEL"/>" class="btn"
                       onclick="window.location='<c:url value="c/maps/"/>'"/>
            </fieldset>
        </form:form>
    </div>
</div>

<div id="register">
    <b>
        <spring:message code="NOT_READY_A_USER"/>
    </b>
    <spring:message code="NOT_READY_A_USER_MESSAGE"/>
    <a href="c/user/registration">
        <spring:message code="JOIN_NOW"/>
    </a>
</div>