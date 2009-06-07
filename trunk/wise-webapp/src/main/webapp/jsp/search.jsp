<%@ include file="/jsp/init.jsp" %>
<script type="text/javascript">
    window.onload = function() {
        var boxGenerator = RUZEE.ShadedBorder.create({ corner:16,  border:1 });
        boxGenerator.render('userRegistration');
    };
</script>
<div id="userRegistrationBody">
<div id="userRegistrationContent">

<div id="userRegistration" class="sb">
<h1>
    <spring:message code="SEARCH"/>
</h1>
<h2>
Please use a space " " to separate tags.
</h2>    
<form method="post" id="searchForm" name="searchForm" action="<c:url value="search.htm"/>">
    <input type="hidden" name="action" value="search"/>    
<table>
<tbody>
<tr>
    <td class="formLabel">
        <spring:message code="NAME"/>
        :
    </td>
    <td>
        <input id="title"/>
    </td>
</tr>
<tr>
    <td class="formLabel">
        <spring:message code="DESCRIPTION"/>
        :
    </td>
    <td>
        <input id="description"/>
    </td>
</tr>
<tr>
    <td class="formLabel">
        <spring:message code="TAGS"/>
        :
    </td>
    <td>
        <input id="tags"/>
    </td>
</tr>
<tr>
    <td>&nbsp;</td>
    <td><input type="submit" value="<spring:message code="SEARCH"/>" id="submitButton" class="btn-primary">
        <input type="button" value="<spring:message code="CANCEL"/>"
               onclick="window.location='<c:url value="mymaps.htm"/>'" class="btn-primary">
    </td>
</tr>
</tbody>
</table>
</form>
</div>
</div>
</div>
<%--<jsp:include page="footer.jsp"/>--%>
<!--</body>-->
<!--</html>-->
