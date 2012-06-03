<%@ include file="/jsp/init.jsp" %>
<%--@elvariable id="wisemapDetail" type="com.wisemapping.view.MindMapBean"--%>

<div>
    <ul class="nav nav-tabs">
        <li class="active"><a href="#general" data-toggle="pill">General</a></li>
        <li><a href="#collaborators" data-toggle="pill">Collaborators</a></li>
        <li><a href="#publish" data-toggle="pill">Publish</a></li>
    </ul>

    <div class="tab-content">
        <div class="tab-pane fade active in" id="general">
            <ul class="unstyled">
                <li><strong><spring:message code="NAME"/>:</strong> ${wisemapDetail.title}</li>
                <li><strong><spring:message code="DESCRIPTION"/>:</strong> ${wisemapDetail.description}</li>
                <li><strong><spring:message code="OWNER"/>:</strong> ${wisemapDetail.creationUser}</li>
                <li><strong><spring:message code="CREATION_TIME"/>:</strong> ${wisemapDetail.creationTime}</li>
                <li><strong><spring:message code="LAST_UPDATE"/>:</strong> ${wisemapDetail.lastEditTime}</li>
                <li><strong><spring:message code="LAST_UPDATE_BY"/>:</strong> ${wisemapDetail.lastEditor}</li>
                <li><strong> <spring:message code="TAGS"/>:</strong> ${wisemapDetail.tags}</li>
            </ul>
        </div>
        <div class="tab-pane fade" id="collaborators">
            <ul class="unstyled">
                <li><strong><spring:message
                        code="EDITORS"/>(${wisemapDetail.countColaborators}): </strong>
                    <c:forEach items="${wisemapDetail.collaborators}" var="mindmapCollaborator">
                        ${mindmapCollaborator.username}
                    </c:forEach>
                </li>
                <li><strong><spring:message code="VIEWERS"/>(${wisemapDetail.countViewers}): </strong>
                    <c:forEach items="${wisemapDetail.viewers}" var="mindmapViewer">
                        ${mindmapViewer.username}
                    </c:forEach>
                </li>
            </ul>
        </div>

        <div class="tab-pane fade" id="publish">
            <c:choose>
                <c:when test="${wisemapDetail.public}">
                    <ul class="unstyled">
                        <p><spring:message code="ALL_VIEW_PUBLIC"/></p>

                        <li><strong><spring:message code="URL"/>:</strong>
                        <li><input name="url"
                                   value="http://www.wisemapping.com/c/publicView?mapId=${wisemapDetail.id}"
                                   style="width:400px" readonly="readonly"/>
                        </li>
                        <li><strong><spring:message code="BLOG_SNIPPET"/></strong>
                            <pre>&lt;iframe style="border:0;width:600px;height:400px;border: 1px solid black" src="http://www.wisemapping.com/c/embeddedView?mapId=${wisemapDetail.id}&amp;amzoom=1"&gt;&lt;/iframe&gt;</pre>
                        </li>
                        <li><spring:message code="EMBEDDED_MAP_SIZE"/></li>
                    </ul>

                </c:when>
                <c:otherwise>
                    <p><spring:message code="ONLY_VIEW_PRIVATE"/></p>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
</div>
