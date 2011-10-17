<%@ include file="/jsp/init.jsp" %>
<c:url value="mymaps.htm" var="shareMap">
    <c:param name="action" value="removeCollaborator"/>
    <c:param name="userEmail" value="${pageContext.request.userPrincipal.name}"/>
</c:url>
<c:url value="sharing.htm" var="removeCollaborator">
    <c:param name="actionId" value="removeCollaborator"/>
    <c:param name="mapId" value="${mindmap.id}"/>
</c:url>
<h1>
    <spring:message code="COLLABORATION"/>:<spring:message code="SHARING"/>
    '${mindmap.title}'</h1>

<div id="addCollaboratorPanel">
    <form method="post" name="sharingForm"
          action="${pageContext.request.contextPath}/c/sharing.htm?mapId=${mindmap.id}">

        <div id="userEmails">
            <h2>
                <spring:message code="INVITE_USERS"/>
            </h2>
            <input type="button" id="collaboratorButton" value="<spring:message code="AS_COLLABORATOR"/>"
                   onclick="toogleUserType(this.value)"
                   style="border: 1px solid #39C;padding: 5px;margin:5px;background-color: #838383;color: white;font-weight: bold;"/>
            <input type="button" id="viewerButton" value="<spring:message code="AS_VIEWER"/>"
                   onclick="toogleUserType(this.value)"
                   style="border: 1px solid black;padding: 5px;margin:5px;background-color: #838383;color: white;font-weight: bold;"/>

            <br/>
            <textarea name="userEmails" id="emailList" rows="7"></textarea>
            <h5>
                <spring:message code="COMMA_SEPARATED_EMAILS"/>
            </h5>
            <a href="#" onclick="return toogleInvitation()">
                <spring:message code="CUSTOMIZE_INVITATION"/>
            </a><br/>

            <div id="invitation" style="display:none;">
                <h2>
                    <spring:message code="INVITATION"/>
                </h2>
                <span><spring:message code="SUBJECT"/>: </span><input name="subject" type="text"
                                                                      value="${principal.firstname} <spring:message code="SUBJECT_MESSAGE"/>"
                                                                      style="width:80%;">
                <br>
                <span><spring:message code="MESSAGE"/>:</span>

                <div>
                    <textarea name="message" rows="5" style="width:100%;"><spring:message code="INVITATION_MSG"/>
                    </textarea>
                </div>
            </div>

            <br/>
            <input id="invitationButton" type="submit" value="Invite Collaborators" class="btn-primary">
            <input type="button" value="<spring:message code="CANCEL"/>" class="btn-secondary" id="cancelBtn"/>
        </div>

        <div id="currentUsers">
            <h2>
                <spring:message code="CURRENT_COLLABORATORS"/> (${mindmap.countColaborators})
            </h2>
            <table>
                <colgroup>
                    <col width="95%"/>
                </colgroup>
                <c:forEach items="${mindmap.collaborators}" var="mindmapCollaborator">
                    <tr>
                        <td>
                                ${mindmapCollaborator.username}
                        </td>
                        <td>
                            <a href="${removeCollaborator}&colaboratorId=${mindmapCollaborator.id}"><img
                                    src="../images/close12_1.gif" alt="<spring:message code="DELETE"/>" border="0"/></a>
                        </td>
                    </tr>
                </c:forEach>
            </table>

            <h2>
                <spring:message code="CURRENT_VIEWERS"/> (${mindmap.countViewers})
            </h2>
            <table>
                <colgroup>
                    <col width="95%"/>
                </colgroup>
                <c:forEach items="${mindmap.viewers}" var="mindmapViewer">
                    <tr>
                        <td>
                                ${mindmapViewer.username}
                        </td>
                        <td>
                            <a href="${removeCollaborator}&colaboratorId=${mindmapViewer.id}"><img
                                    src="../images/close12_1.gif" alt="<spring:message code="DELETE"/>" border="0"/></a>
                        </td>
                    </tr>
                </c:forEach>
            </table>
        </div>
        <input type="hidden" name="actionId" value="addCollaborator"/>
        <input type="hidden" name="colaboratorId" value=""/>
    </form>
</div>
</div>

<script type="text/javascript">
    $('cancelBtn').addEvent('click', function(event) {
        MooDialog.Request.active.close();
    });

    function toogleUserType(buttonValue) {
        var collaboratorButton = $("collaboratorButton");
        var viewerButton = $("viewerButton");
        var newDisplay = "Invite Collaborators";
        if (buttonValue == '<spring:message code="AS_VIEWER"/>') {
            newDisplay = "Invite Viewers";
            viewerButton.setStyle('border', '1px solid #39C');
            collaboratorButton.setStyle('border', '1px solid black');
            document.sharingForm.actionId.value = "addViewer";
        } else {
            viewerButton.setStyle('border', '1px solid black');
            collaboratorButton.setStyle('border', '1px solid #39C');
            document.sharingForm.actionId.value = "addCollaborator";
        }

        var invitationButton = document.getElementById("invitationButton");
        invitationButton.value = newDisplay;
    }

    function toogleInvitation() {
        var invitationContainer = $("invitation");
        var newDisplay = "none";
        if (invitationContainer.style.display == 'none') {
            newDisplay = "";
        }
        invitationContainer.style.display = newDisplay;
        return false;
    }


</script>