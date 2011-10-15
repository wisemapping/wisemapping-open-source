<%@ include file="/jsp/init.jsp" %>
<h1><spring:message code="TAG"/>'${tag.mindmapTitle}'</h1>

<h2>
    <spring:message code="TAGS_MSG"/>
</h2>

<div>
    <form:form method="post" commandName="tag">

        <table>
            <tbody>
            <tr>
                <td class="formLabel"> &nbsp;</td>
                <td>
                    <form:input path="mindmapTags" id="mindmapTags" tabindex="0"/>
                    <form:errors path="mindmapTags" cssClass="errorMsg"/>
                </td>
            </tr>
            <tr>
                <td>&nbsp;</td>
                <td style="padding: 5px">
                    <input type="submit" value="<spring:message code="SUBMIT"/>" class="btn-primary">
                    <input type="button" value="<spring:message code="CANCEL"/>" class="btn-secondary">
                </td>
            </tr>
            </tbody>
        </table>
    </form:form>
</div>
