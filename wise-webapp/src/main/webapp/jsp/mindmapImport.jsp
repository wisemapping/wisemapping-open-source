<%@ include file="/jsp/init.jsp" %>
<div id="importContainer">
    <form:form method="post" commandName="importMap" enctype="multipart/form-data">
        <table>
            <tr>
                <td class="formLabel">
                    <spring:message code="NAME"/>:
                </td>
                <td>
                    <form:input path="title" id="title" tabindex="1"/>
                    <form:errors path="title" cssClass="errorMsg"/>
                </td>
            </tr>
            <tr>
                <td class="formLabel">
                    <spring:message code="DESCRIPTION"/>:
                </td>
                <td>
                    <form:input path="description" id="description" tabindex="2"/>
                    <form:errors path="description" cssClass="errorMsg"/>
                </td>
            </tr>
            <tr>
                <td class="formLabel">
                    <span class="fieldRequired">*</span>
                    <spring:message code="FREE_MIND_FILE"/>:
                </td>
                <td>
                    <input type="file" name="mapFile"/>
                    <form:errors path="mapFile" cssClass="errorMsg"/>
                </td>
            </tr>
            <tr>
                <td>&nbsp;</td>
                <td>
                    <input type="submit" value="<spring:message code="IMPORT"/>" class="btn btn-primary">
                    <input type="button" value="<spring:message code="CANCEL"/>" class="btn"
                           onclick="window.location='/c/mymaps.htm'">
                </td>
            </tr>
        </table>
    </form:form>
</div>
