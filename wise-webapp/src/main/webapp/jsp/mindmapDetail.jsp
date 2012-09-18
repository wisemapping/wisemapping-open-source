<%@page pageEncoding="UTF-8" %>
<%@include file="/jsp/init.jsp" %>
<%--@elvariable id="mindmap" type="com.wisemapping.view.MindMapBean"--%>

<div>
    <ul class="nav nav-tabs">
        <li class="active"><a href="#general" data-toggle="pill"><spring:message code="DESCRIPTION"/></a></li>
        <li><a href="#collaborators" data-toggle="pill"><spring:message code="SHARED"/></a></li>
        <li><a href="#publish" data-toggle="pill"><spring:message code="PUBLIC"/></a></li>
    </ul>

    <div class="tab-content">
        <div class="tab-pane fade active in" id="general">
            <ul class="unstyled">
                <li><strong><spring:message code="NAME"/>:</strong> <c:out value="${mindmap.title}"/></li>
                <li><strong><spring:message code="DESCRIPTION"/>:</strong> <c:out value="${mindmap.description}"/></li>
                <li><strong><spring:message code="CREATOR"/>:</strong> <c:out value="${mindmap.creator.fullName}"/></li>
                <li><strong><spring:message code="CREATION_TIME"/>:</strong> ${mindmap.creationTime}</li>
                <li><strong><spring:message code="LAST_UPDATE"/>:</strong> ${mindmap.lastEditTime}</li>
                <li><strong><spring:message code="LAST_UPDATE_BY"/>:</strong> <c:out value="${mindmap.lastEditor}"/>
                </li>
                <li><strong> <spring:message code="STARRED"/>:</strong> ${mindmap.starred}</li>
            </ul>
        </div>
        <div class="tab-pane fade" id="collaborators">
            <ul class="unstyled">
                <li>
                    <strong><spring:message code="YOUR_ROLE"/></strong>: ${mindmap.role}
                </li>
                <c:if test="${mindmap.owner}">
                    <li><strong><spring:message
                            code="EDITORS"/>(${mindmap.countCollaborators}): </strong>
                        <c:forEach items="${mindmap.collaborators}" var="mindmapCollaborator">
                            ${mindmapCollaborator.username}
                        </c:forEach>
                    </li>
                    <li><strong><spring:message code="VIEWERS"/>(${mindmap.countViewers}): </strong>
                        <c:forEach items="${mindmap.viewers}" var="mindmapViewer">
                            ${mindmapViewer.username}
                        </c:forEach>
                    </li>
                </c:if>
            </ul>
        </div>

        <div class="tab-pane fade" id="publish">
            <c:choose>
                <c:when test="${mindmap.public}">
                    <ul class="unstyled">
                        <p><spring:message code="ALL_VIEW_PUBLIC"/></p>
                        <li><strong><spring:message code="DIRECT_LINK_EXPLANATION"/></strong>
                        <li><input name="url"
                                   value="${baseUrl}/c/maps/${mindmap.id}/public"
                                   style="width:400px;cursor: text" readonly="readonly"/>
                        </li>
                        <li><strong><spring:message code="BLOG_SNIPPET"/></strong>
                            <pre>&lt;iframe style="border:0;width:600px;height:400px;border: 1px solid black" src="${baseUrl}/c/maps/${mindmap.id}/embed?zoom=1"&gt;&lt;/iframe&gt;</pre>
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
