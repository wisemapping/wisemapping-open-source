<%@page pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<div id="header">
    <div id="headerToolbar">
        <c:choose>
            <c:when test="${principal != null}">
                <div id="headerActions">
                    <spring:message code="WELCOME"/>, ${principal.firstname}
                    | <span><a href="${pageContext.request.contextPath}/c/maps/"><spring:message code="MY_WISEMAPS"/></a></span>
                    | <span><a href="${pageContext.request.contextPath}/c/settings" title="<spring:message code="SETTINGS_DETAIL"/>"><spring:message code="SETTINGS"/></a></span>
                    | <span><a href="${pageContext.request.contextPath}/c/logout" title="<spring:message code="LOGOUT"/>"><spring:message code="LOGOUT"/></a></span>
                </div>
            </c:when>
            <c:when test="${param.removeSignin!=true}">
                <div id="headerActions">
                    <spring:message code="ALREADY_A_MEMBER"/>
                    <span><a href="${pageContext.request.contextPath}/c/login" title="<spring:message code="SIGN_IN"/>">
                        <spring:message code="SIGN_IN"/>
                    </a></span>
                    </div>
            </c:when>
        </c:choose>
        <div class="header_languages">
            <div class="header_language_flag">
                <a href="?language=en"><img src="../images/uk.gif" alt="English"></a>
            </div>
            <div class="header_language_flag">
                <a href="?language=fr"><img src="../images/fr.gif" alt="Frances"></a>
            </div>
            <div class="header_language_flag">
                <a href="?language=es"><img src="../images/es.gif" alt="Espanol"></a>
            </div>
        </div>
    </div>
</div>
<c:if test="${param.onlyActionHeader!=true}">
    <div id="headerContent">
        <div id="headerButtons">
            <%--<div id="blogLink">--%>
                <%--<a href="${pageContext.request.contextPath}/c/blog" title="<spring:message code="BLOG_TITLE"/>">--%>
                    <%--<spring:message code="BLOG"/>--%>
                <%--</a>--%>
            <%--</div>--%>
        </div>
        <a href="${pageContext.request.contextPath}/c/home" title="Homepage">
            <div id="headerLogo">&nbsp;</div>
        </a>

        <div id="headerSlogan">
            <spring:message code="SITE.SLOGAN"/>
        </div>
    </div>
</c:if>

