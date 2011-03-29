<%@ include file="/jsp/init.jsp" %>
<%--@elvariable id="wisemapDetail" type="com.wisemapping.view.MindMapBean"--%>

<script type="text/javascript">
    window.onload = function() {
        var boxGenerator = RUZEE.ShadedBorder.create({ corner:16,  border:1 });
        boxGenerator.render('detailContent');
        boxGenerator.render('detail');
    };

    MOOdalBox.reloadRequered = true;

</script>

<c:url value="mymaps.htm" var="shareMap">
    <c:param name="action" value="collaborator"/>
    <c:param name="userEmail" value="${pageContext.request.userPrincipal.name}"/>
</c:url>

<div id="detailContent" class="sb">
<div id="detailTitle">
    <c:out value="${wisemapDetail.title}"/>
    <c:choose>
        <c:when test="${not wisemapDetail.public}">
            <img src="../images/key.png" alt="<spring:message code="PRIVATE"/>" title="<spring:message code="PRIVATE"/>"/>
            <span>(<spring:message code="PRIVATE"/>)</span>
        </c:when>
        <c:otherwise>
            <img src="../images/world2.png" alt="<spring:message code="PUBLIC"/>" title="<spring:message code="PUBLIC"/>"/>
            <span>(<spring:message code="PUBLIC"/>)</span>
        </c:otherwise>
    </c:choose>
</div>

<div id="detail" class="sb">
<spring:message code="DETAIL_INFORMATION"/>
<table>
<tr class="evenRow">
    <td class="formLabel">
        <spring:message code="CREATOR"/>
        :
    </td>
    <td>${wisemapDetail.creationUser}</td>
</tr>
<tr class="oddRow">
    <td class="formLabel">
        <a href="renameMap.htm?mapId=${wisemapDetail.id}"
           rel="moodalbox 400px 200px wizard"
           title="<spring:message code="RENAME_DETAILS"/>">
            <spring:message code="DESCRIPTION"/>
            :
        </a>
    </td>
    <td>${wisemapDetail.description}</td>
</tr>
<tr class="evenRow">
    <td class="formLabel">
        <spring:message code="CREATION_TIME"/>
        :
    </td>
    <td>${wisemapDetail.creationTime}</td>
</tr>
<tr class="oddRow">
    <td class="formLabel">
        <c:choose>
            <c:when test="${wisemapDetail.owner==requestScope.user}">
                <a href="<c:out value="${shareMap}"/>&amp;mapId=${wisemapDetail.id}" rel="moodalbox 780px 530px wizard"
                   title="<spring:message code="SHARE_DETAILS"/>">
                    <spring:message code="COLLABORATORS"/>
                </a>
            </c:when>
            <c:otherwise>
                <spring:message code="COLLABORATORS"/>
            </c:otherwise>
        </c:choose>:<br/>
    </td>

    <td>
        <div id="divCollaboratorsList">
            <table>
                <tr>
                    <td>
                        <b>
                            <spring:message code="EDITORS"/>
                        </b>
                        (
                        <c:out value="${wisemapDetail.countColaborators}"/>
                        )
                        <br/>
                        <c:forEach items="${wisemapDetail.collaborators}" var="mindmapCollaborator">
                            ${mindmapCollaborator.username}
                            <br/>
                        </c:forEach>
                    </td>
                    <td>
                        <b>
                            <spring:message code="VIEWERS"/>
                        </b>
                        (
                        <c:out value="${wisemapDetail.countViewers}"/>
                        )
                        <br/>
                        <c:forEach items="${wisemapDetail.viewers}" var="mindmapViewer">
                            ${mindmapViewer.username}
                            <br/>
                        </c:forEach>
                    </td>
                </tr>
            </table>
        </div>
    </td>
</tr>
<tr class="evenRow">
    <td class="formLabel">
        <a href="tags.htm?mapId=${wisemapDetail.id}"
           rel="moodalbox 400px 200px wizard"
           title="<spring:message code="TAGS_DETAILS"/>">
            <spring:message code="TAGS"/>
        </a>
        :
    </td>
    <td>
        <c:out value="${wisemapDetail.tags}"/>
    </td>
</tr>
<tr class="oddRow">
    <td class="formLabel">
        <c:choose>
            <c:when test="${wisemapDetail.owner==requestScope.user}">
                <a href="publish.htm?mapId=${wisemapDetail.id}" rel="moodalbox 600px 400px wizard"
                   title="<spring:message code="PUBLISH_MSG"/>">
                    <spring:message code="PUBLISH"/>
                </a>
            </c:when>
            <c:otherwise>
                <spring:message code="PUBLISH"/>
            </c:otherwise>
        </c:choose>
        :
        <div id="divSendLink"></div>
    </td>
    <td>
        <c:choose>
            <c:when test="${wisemapDetail.public}">
                <spring:message code="ALL_VIEW_PUBLIC"/>
                <br/>

                <table class="oddRow">
                    <colgroup>
                        <col width="10%"/>
                        <col width="90%"/>
                    </colgroup>
                    <tbody>
                        <tr>
                            <td class="formLabel">
                                <spring:message code="URL"/>
                                :
                            </td>
                            <td>
                                <input name="url"
                                       value="http://www.wisemapping.com/c/publicView.htm?mapId=${wisemapDetail.id}"
                                       style="width:400px" readonly="readonly"/>
                            </td>
                        </tr>
                        <tr>
                            <td class="formLabel">
                                <spring:message code="DIRECT_LINK"/>
                                :
                            </td>
                            <td>
                                <textarea style="width:400px;height:30px;overflow:hidden;" cols="55" rows="3"
                                          readonly="readonly">
                                    &lt;a
                                    href="http://www.wisemapping.com/c/publicView.htm?mapId=${wisemapDetail.id}">${wisemapDetail.title}&lt;/a></textarea>
                            </td>
                        </tr>
                        <tr>
                            <td class="formLabel" style="white-space:normal;">
                                <spring:message code="BLOG_INCLUSION"/>
                                :
                            </td>
                            <td>
                                <textarea style="width:400px;height:70px;overflow:hidden;" cols="55" rows="5"
                                          readonly="readonly">
                                    &lt;iframe
                                    style="border:0;width:600px;height:400px;border: 1px solid black"
                                    src="http://www.wisemapping.com/c/embeddedView.htm?mapId=${wisemapDetail.id}&amp;amzoom=1"&gt;
                                    &lt;/iframe&gt;
                                </textarea>

                                <p><spring:message code="EMBEDDED_MAP_SIZE"/></p>
                            </td>
                        </tr>
                    </tbody>
                </table>
            </c:when>
            <c:otherwise>
                <spring:message code="ONLY_VIEW_PRIVATE"/>
            </c:otherwise>
        </c:choose>
    </td>
</tr>
</table>
</div>
</div>