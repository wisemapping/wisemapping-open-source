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

<style>
    .btn-header {
        background-color: #171a17;
        border-color: #575757;
    }
    .btn-header:hover {
        background-color: #F7C931;
    }
    .WelcomeHeader {
        position: relative;
        float: left;
        padding: 5px 10px;
        font-size: 14px;
        line-height: 1.5;
        border: 1px solid transparent;
    }
</style>

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

                <div class='WelcomeHeader' >
                    <spring:message code="WELCOME"/>, ${principal.firstname}
                </div>

                <div class="btn-group" id="headerActions">

                    <button type="button" onclick="location.href='c/maps/'" class="btn btn-sm btn-header"><spring:message code="MY_WISEMAPS"/></button>

                    <button type="button" onclick="accountInfo()" title="<spring:message code="ACCOUNT_DETAIL"/>" class="btn btn-sm btn-header">
                        <spring:message code="ACCOUNT"/>
                    </button>

                    <button onclick="location.href='c/logout'" title="<spring:message code="LOGOUT"/>" type="button" class="btn btn-sm btn-header">
                        <spring:message code="LOGOUT"/>
                    </button>

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

    function accountInfo(event) {
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
    }

</script>

