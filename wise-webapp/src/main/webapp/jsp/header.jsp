<%@page pageEncoding="UTF-8" %>

<%@ page import="com.wisemapping.model.User" %>
<%@ page import="com.wisemapping.security.Utils" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<%
    User user = Utils.getUser(false);
    if (user != null) {
        request.setAttribute("principal", user);
    }
%>

<div id="settings-dialog-modal" class="modal fade">
  <div class="modal-dialog">
    <div class="modal-content">
        <div class="modal-header">
            <button class="close" data-dismiss="modal">x</button>
            <h3><spring:message code="ACCOUNT"/></h3>
        </div>
        <div class="modal-body">

        </div>
        <div class="modal-footer">
            <button class="btn btn-cancel"><spring:message code="CLOSE"/></button>
        </div>
    </div>
    </div>
</div>
<div id="header">
    <div id="headerToolbar">
        <c:choose>
            <c:when test="${principal != null}">
                <div id="headerActions">
                    <spring:message code="WELCOME"/>, ${principal.firstname}
                    | <span><a href="c/maps/"><spring:message
                        code="MY_WISEMAPS"/></a></span>
                    | <span><a id="userSettingsBtn" href="#"
                               title="<spring:message code="ACCOUNT_DETAIL"/>"><spring:message
                        code="ACCOUNT"/></a></span>
                    | <span><a href="c/logout"
                               title="<spring:message code="LOGOUT"/>"><spring:message code="LOGOUT"/></a></span>
                </div>
            </c:when>
            <c:when test="${!param.removeSignin && !requestScope.removeSignin}">
                <div id="headerActions">
                    <spring:message code="ALREADY_A_MEMBER"/>
                    <span><a href="c/login" title="<spring:message code="SIGN_IN"/>">
                        <spring:message code="SIGN_IN"/>
                    </a></span>
                </div>
            </c:when>
        </c:choose>
    </div>
</div>
<c:if test="${param.onlyActionHeader!=true}">
    <div id="headerContent">
        <a href="${requestScope['site.homepage']}" title="Homepage">
            <div id="headerLogo">&nbsp;</div>
        </a>

        <div id="headerSlogan">
            <spring:message code="SITE.SLOGAN"/>
        </div>
    </div>
</c:if>


<script type="text/javascript">
    var userSettingsLink = $('#userSettingsBtn');
    if (userSettingsLink) {
        userSettingsLink.click(
                function (event) {
                    $('#settings-dialog-modal .modal-body').load("c/account/settings",
                            function () {
                                $('#settings-dialog-modal .btn-cancel').unbind('click').click(function () {
                                    $('#settings-dialog-modal').modal("hide");
                                    window.location.reload();
                                });
                            }
                    );
                    $('#settings-dialog-modal').modal();
                    event.preventDefault();

                });
    }

</script>

