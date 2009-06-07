<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<div id="editorHeader">
    <div id="headerToolbar">
        <c:if test="${mindmap != null}">
            <div id="headerTitle">${mindmap.title}<span id="headerSubTitle">&nbsp;<spring:message
                    code='EDITOR.LAST_SAVED' arguments='${mindmap.lastModifierUser}'/></span></div>
        </c:if>
        <c:choose>
            <c:when test="${principal != null}">
                <div id="headerActions">
                    <spring:message code="WELCOME"/>
                    , ${principal.firstname}
                    | <a href="${pageContext.request.contextPath}/c/mymaps.htm" title="<spring:message code="MY_WISEMAPS"/>">
                    <spring:message code="MY_WISEMAPS"/>
                </a>
                    | <a href="${pageContext.request.contextPath}/c/settings.htm" rel="moodalbox 400px 250px wizard"
                         title="<spring:message code="SETTINGS_DETAIL"/>">
                    <spring:message code="SETTINGS"/>
                </a>
                    | <a href="${pageContext.request.contextPath}/c/logout.htm" title="<spring:message code="LOGOUT"/>">
                    <spring:message code="LOGOUT"/>
                </a>
                </div>
            </c:when>
            <c:when test="${param.removeSignin!=true}">
                <div id="signUpHeader">
                    <spring:message code="ALREADY_A_MEMBER"/>
                    <a href="${pageContext.request.contextPath}/c/login.htm" title="<spring:message code="SIGN_IN"/>">
                        <spring:message code="SIGN_IN"/>
                    </a>
                </div>
            </c:when>
        </c:choose>
    </div>
</div>
<div id="headerLoading">
    <spring:message code="LOADING_MSG"/>
</div>
