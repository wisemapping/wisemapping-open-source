<%@page pageEncoding="UTF-8" %>
<%@include file="/jsp/init.jsp" %>

<div>
    <form:form method="post" commandName="changePassword">
        <table>
            <tr>
                <td class="formLabel">
                    <span class="fieldRequired">*</span>
                    <spring:message code="PASSWORD"/>
                    :
                </td>
                <td>
                    <form:password path="password" id="password" tabindex="1"/>
                    <form:errors path="password" cssClass="errorMsg"/>
                </td>
            </tr>
            <tr>
                <td class="formLabel">
                    <span class="fieldRequired">*</span>
                    <spring:message code="RETYPE_PASSWORD"/>
                    :
                </td>
                <td>
                    <form:password path="retryPassword" id="retryPassword" tabindex="2"/>
                    <form:errors path="retryPassword" cssClass="errorMsg"/>
                </td>
            </tr>
            <tr>
                <td>&nbsp;</td>
                <td>
                    <input type="submit" value="<spring:message code="SUBMIT"/>" class="btn-primary">
                    <input type="button" value="<spring:message code="CANCEL"/>" class="btn-primary"
                           onclick="MOOdalBox.close();">
                </td>
            </tr>
        </table>
    </form:form>
</div>
