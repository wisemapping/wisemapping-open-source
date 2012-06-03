<%@ page import="java.util.List" %>
<%@ page import="com.wisemapping.view.HistoryBean" %>
<%@ include file="/jsp/init.jsp" %>
<script type="text/javascript">
    function revertHistory(mapId, historyId)
    {
        document.revertForm.mapId.value = mapId;
        document.revertForm.historyId.value = historyId;
        document.revertForm.submit();
        return false;
    }
</script>
<table style="border:1px gray dashed;width:100%;margin-top:10px;">
    <thead>
        <tr style="border:1px gray dashed;color:white;background:black;">
            <td>
                <spring:message code="MODIFIED"/>
            </td>
            <td>
                <spring:message code="BY"/>
            </td>
            <td>
                &nbsp;
            </td>
        </tr>
    </thead>
    <tbody>
        <%
            final List<HistoryBean> list = (List<HistoryBean>) request.getAttribute("historyBeanList");
            if (list != null && !list.isEmpty()) {
        %>
        <%
            for (HistoryBean history : list) {

        %>

        <tr>
            <td>
                <%=history.getCreation(request.getLocale())%>
            </td>
            <td>
                <%=history.getAuthor()%>
            </td>
            <td>
                <a onclick="return revertHistory('<%=history.getMindMapId()%>',<%=history.getHistoryId()%>)" href=""><spring:message code="REVERT"/></a>
            </td>
        </tr>
        <%
            }
        } else {
        %>

        <td colspan="3">
            <spring:message code="NO_HISTORY_RESULTS"/>
        </td>
        <%
            }
        %>
    </tbody>
</table>
<form name="revertForm" action="<c:url value="history"/>">
    <input type="hidden" name="action" value="revert"/>
    <%        
        if (request.getAttribute("goToMindmapList") != null)
        {
    %>
        <input type="hidden" name="goToMindmapList"/>
    <%
        }
    %>
    <input type="hidden" name="mapId"/>
    <input type="hidden" name="historyId"/>
</form>

