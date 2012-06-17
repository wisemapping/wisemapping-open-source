<%@ include file="/jsp/init.jsp" %>
<div>
    <ul>
        <li><a rel="moodalbox 400px 200px wizard"
               href="<c:out value="${pageContext.request.contextPath}/c/changePassword"/>"
               title="<spring:message code="CHANGE_PASSWORD"/>">
            <spring:message code="CHANGE_PASSWORD"/>
        </a>
        </li>
        <li><a rel="moodalbox 400px 250px wizard"
               href="<c:out value="${pageContext.request.contextPath}/c/editProfile"/>"
               title="<spring:message code="EDIT_PROFILE"/>">
            <spring:message code="EDIT_PROFILE"/>
        </a>
        </li>
    </ul>
</div>
