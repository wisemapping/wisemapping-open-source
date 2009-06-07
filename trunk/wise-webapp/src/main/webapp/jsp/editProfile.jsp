<%@ include file="/jsp/init.jsp" %>
<div>
    <form:form method="post" commandName="editProfile">
        <table>
            <tr>
                <td class="formLabel">
                    <span class="fieldRequired">*</span>
                    <spring:message code="FIRSTNAME"/>
                    :
                </td>
                <td>
                    <form:input path="firstname" id="firstname" tabindex="1"/>
                    <form:errors path="firstname" cssClass="errorMsg"/>
                </td>
            </tr>
            <tr>
                <td class="formLabel">
                    <span class="fieldRequired">*</span>
                    <spring:message code="LASTNAME"/>
                    :
                </td>
                <td>
                    <form:input path="lastname" id="lastname" tabindex="2"/>
                    <form:errors path="lastname" cssClass="errorMsg"/>
                </td>
            </tr>
            <tr>
                <td class="formLabel">
                    <span class="fieldRequired">*</span>
                    <spring:message code="EMAIL"/>
                    :
                </td>
                <td>
                    <form:input path="email" id="email" tabindex="3"/>
                    <form:errors path="email" cssClass="errorMsg"/>
                </td>
            </tr>
            <tr>
                <td class="formLabel">                                        
                    <spring:message code="NEWSLETTER_DESC"/>
                    :
                </td>
                <td>
                    <form:checkbox path="allowSendEmail" id="allowSendEmail" tabindex="4"/>
                    <form:errors path="allowSendEmail" cssClass="errorMsg"/>
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
