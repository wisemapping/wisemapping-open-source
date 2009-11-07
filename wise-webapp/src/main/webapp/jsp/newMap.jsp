<%@ include file="/jsp/init.jsp" %>

<h1>
    
</h1>

<div>
    <form:form method="post" commandName="newMap">
        <table>
            <tr>
                <td class="formLabel">
                    <span class="fieldRequired">*</span>
                    <spring:message code="NAME"/>
                    :
                </td>
                <td>
                    <form:input path="title" id="title" tabindex="1"/>
                    <form:errors path="title" cssClass="errorMsg"/>
                </td>
            </tr>
            <tr>
                <td class="formLabel">
                    <spring:message code="DESCRIPTION"/>
                    :
                </td>
                <td>
                    <form:input path="description" id="description" tabindex="2"/>
                    <form:errors path="description" cssClass="errorMsg"/>
                </td>
            </tr>
            <tr>
                <td>&nbsp;</td>
                <td>
                    <input type="submit" value="<spring:message code="SUBMIT"/>" class="btn-primary">
                    <c:choose>
                    <c:when test="${!errorView}">
                    <input type="button" value="<spring:message code="CANCEL"/>" class="btn-primary"
                           onclick="MOOdalBox.close();">
                    </c:when>
                     <c:otherwise>
                    <input type="button" value="<spring:message code="BACK"/>" class="btn-primary"
                           onclick="window.location='mymaps.htm'">
                    </c:otherwise>
                    </c:choose>
                </td>
            </tr>
        </table>
    </form:form>
</div>