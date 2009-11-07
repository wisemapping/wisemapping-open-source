<%@ include file="/jsp/init.jsp" %>
<h1>
    <spring:message code="TAG"/>
    '${tag.mindmapTitle}'</h1>

<h2>
    <spring:message code="TAGS_MSG"/>
</h2>

<div>
    <form:form method="post" commandName="tag">

        <table>
            <tbody>
                <tr>
                    <td class="formLabel">
                        <spring:message code="TAGS"/>
                        :
                    </td>
                    <td>
                        <%--<input type="text" id="tags" name="tags" value="<c:out value="${tag.mindmapTags}"/>"/>--%>
                        <form:input path="mindmapTags" id="mindmapTags" tabindex="0"/>
                        <form:errors path="mindmapTags" cssClass="errorMsg"/>
                    </td>
                </tr>
                <%--<tr>--%>
                <%--<td class="formLabel">--%>
                <%--<spring:message code="AVAILABLE_TAGS"/>--%>
                <%--:--%>
                <%--</td>--%>
                <%--<td>--%>
                <%--${userTags}--%>
                <%--</td>--%>
                <%--</tr>--%>
                <tr>
                    <td>&nbsp;</td>
                    <td>
                        <input type="submit" value="<spring:message code="SUBMIT"/>" class="btn-primary">
                        <input type="button" value="<spring:message code="CANCEL"/>" class="btn-primary"
                           onclick="MOOdalBox.close();">
                    </td>
                </tr>
            </tbody>
        </table>
    </form:form>
</div>
