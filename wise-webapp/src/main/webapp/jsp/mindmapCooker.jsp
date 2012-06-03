<%@ include file="/jsp/init.jsp" %>

<h1>Mindmap Cooker</h1>

<div>
    <form action='<c:url value="/c/cooker"/>' method="post">
        <input type="hidden" name="action" value="save"/>
        <input type="hidden" name="mapId" value="${mindmap.id}"/>
        <table>
            <tbody>
                <tr>
                    <td class="formLabel">
                        Native XML:
                    </td>
                    <td>
                        <textarea name="xml" id="xml" rows="20" cols="70">${mindmap.xml}</textarea>
                    </td>
                </tr>
                <tr>
                    <td>&nbsp;</td>
                    <td>
                        <input type="submit" value="<spring:message code="SUBMIT"/>" class="btn-primary"/>
                        <input type="button" value="<spring:message code="CANCEL"/>" class="btn-primary"/>
                    </td>
                </tr>
            </tbody>
        </table>
    </form>
</div>
