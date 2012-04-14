<%@ include file="/jsp/init.jsp" %>

<html>
<head>
    <title>
        <spring:message code="SITE.TITLE"/>
        -</title>
    !--[if lt IE 9]>
    <![endif]-->

    <script type="text/javascript">
        function removeViewer(viewerEmail)
        {
            document.removeViewerForm.userEmail.value = viewerEmail;
            document.removeViewerForm.submit();
        }
    </script>
</head>
<body>

<jsp:include page="header.jsp"/>

<h1>${mindmap.title}</h1>

<div>${mindmap.description}</div>
<div>${mindmap.userRole}</div>

<form method="post" action="<c:url value="sharing.htm"/>">
    <input type="hidden" name="action" value="addViewer"/>
    <input type="hidden" name="mapId" value="${mindmap.id}"/>

    <div id="sharing1">
        <fieldset id="sharing">

            <legend>
                <spring:message code="VIEWERS"/>
            </legend>

            <table>
                <tr>
                    <td>
                        <div id="addSharing">
                            <label for="viewers" accesskey="a">
                                <spring:message code="ADD_VIEWERS"/>
                                :</label>
                            <textarea name="userEmails" id="viewers" tabindex="1" cols="50" rows="5"></textarea><br/>
                            <input type="submit" value="<spring:message code="ADD"/>">
                            <input type="button" value="<spring:message code="CANCEL"/>"
                                   onclick="window.location='<c:url value="mymaps.htm"/>'">
                        </div>
                    </td>
                    <td>
                        <div id="currentSharing">
                            <label for="viewers" accesskey="v">
                                <spring:message code="CURRENT_VIEWERS"/>
                                :</label><br/>
                            <table>
                                <c:forEach items="${mindmap.viewers}" var="mindmapViewer">
                                    <tr>
                                        <td>
                                                ${mindmapViewer}
                                        </td>
                                        <td>
                                            <a href="javascript:removeViewer('${mindmapViewer}')"><img
                                                    src="../images/removeUser.jpeg" border="0"
                                                    title="<spring:message code="REMOVE"/>"
                                                    alt="<spring:message code="REMOVE"/>"/></a>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </table>
                        </div>
                    </td>
                </tr>
            </table>
        </fieldset>
    </div>
</form>

<form name="removeViewerForm" action="<c:url value="sharing.htm"/>">
    <input type="hidden" name="action" value="removeViewer"/>
    <input type="hidden" name="mapId" value="${mindmap.id}"/>
    <input type="hidden" name="userEmail" value=""/>
</form>

</body>
</html>