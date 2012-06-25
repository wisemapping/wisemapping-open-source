<%@page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<div id="settings-dialog-modal" class="modal fade">
    <div class="modal-header">
        <button class="close" data-dismiss="modal">x</button>
        <h3><spring:message code="ACCOUNT"/></h3>
    </div>
    <div class="modal-body">

    </div>
    <div class="modal-footer">
        <button class="btn btn-cancel" data-dismiss="modal"><spring:message code="CLOSE"/></button>
    </div>
</div>
<div id="header">
    <div id="headerToolbar">
        <c:choose>
            <c:when test="${principal != null}">
                <div id="headerActions">
                    <spring:message code="WELCOME"/>, ${principal.firstname}
                    | <span><a href="/c/maps/"><spring:message
                        code="MY_WISEMAPS"/></a></span>
                    | <span><a id="userSettingsBtn" href="#"
                               title="<spring:message code="ACCOUNT_DETAIL"/>"><spring:message
                        code="ACCOUNT"/></a></span>
                    | <span><a href="/c/logout"
                               title="<spring:message code="LOGOUT"/>"><spring:message code="LOGOUT"/></a></span>
                </div>
            </c:when>
            <c:when test="${param.removeSignin!=true}">
                <div id="headerActions">
                    <spring:message code="ALREADY_A_MEMBER"/>
                    <span><a href="/c/login" title="<spring:message code="SIGN_IN"/>">
                        <spring:message code="SIGN_IN"/>
                    </a></span>
                </div>
            </c:when>
        </c:choose>
        <div class="header_languages">
            <div class="header_language_flag">
                <a href="/c/login?language=en"><img src="/images/flag-uk.gif" alt="English"></a>
            </div>
            <div class="header_language_flag">
                <a href="/c/login?language=fr"><img src="/images/flag-fr.gif" alt="Frances"></a>
            </div>
            <div class="header_language_flag">
                <a href="/c/login?language=es"><img src="/images/flag-es.gif" alt="Español"></a>
            </div>
        </div>
    </div>
</div>
<c:if test="${param.onlyActionHeader!=true}">
    <div id="headerContent">
        <a href="/c/home" title="Homepage">
            <div id="headerLogo">&nbsp;</div>
        </a>

        <div id="headerSlogan">
            <spring:message code="SITE.SLOGAN"/>
        </div>
    </div>
</c:if>


<script type="text/javascript">
    $('#userSettingsBtn').click(
            function(event) {
                $('#settings-dialog-modal .modal-body').load("/c/account/settings"),function() {
                    $('#settings-dialog-modal .btn-accept').unbind('click').click(function() {
                        // hacer lago ...
                    });
                };
                $('#settings-dialog-modal').modal();
                event.preventDefault();

            });

</script>


