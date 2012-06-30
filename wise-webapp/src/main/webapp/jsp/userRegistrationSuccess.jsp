<%@page pageEncoding="UTF-8" %>
<%@include file="/jsp/init.jsp" %>

<div>
    <h2 style="font-weight:bold;"><spring:message code="THANKS_FOR_SIGN_UP"/></h2>
    <c:if test="${confirmByEmail==true}">
        <p>
            <spring:message code="SIGN_UP_CONFIRMATION_EMAIL"/>
        </p>
        <br/>

        <p>
            Thanks so much for your interest in WiseMapping.
        </p>
        <br/>

        <p>
            If you have any questions or have any feedback, please don't hesitate to use the on line form.
            We'd love to hear from you.
        </p>
    </c:if>
    <c:if test="${confirmByEmail==false}">
        <p>
            <spring:message code="SIGN_UP_SUCCESS"/>
        </p>
    </c:if>
</div>
