<%@page pageEncoding="UTF-8" %>
<%@include file="/jsp/init.jsp" %>

<div>
    <ul class="nav nav-tabs">
        <c:if test="${principal.databaseSchema}">
            <li class="active"><a href="#changeUserPanel" data-toggle="pill"><spring:message code="GENERAL"/></a></li>
            <li><a href="#changePasswordPanel" data-toggle="pill"><spring:message code="SECURITY"/></a></li>
        </c:if>
        <li><a href="#languagePanel" data-toggle="pill"><spring:message code="LANGUAGE"/></a></li>
        <li><a href="#deleteAccountPanel" data-toggle="pill"><spring:message code="DELETE__ACCOUNT"/></a></li>
    </ul>

    <div class="tab-content">
        <div class="tab-pane fade ${principal.databaseSchema?'active in':''}" id="changeUserPanel">
            <div id="changeInfoMsg" class="alert">
            </div>
            <form action="#" method="POST" id="changeUserForm">
                <fieldset>
                    <label for="email"><strong><spring:message code="EMAIL"/>:</strong></label>
                    <input type="text" name="password" id="email" required="required" readonly="readonly"
                           value="${user.email}"/>

                    <label for="firstname"><strong><spring:message code="FIRSTNAME"/>:</strong></label>
                    <input type="text" name="firstname" id="firstname" required="required" value="${user.firstname}"/>

                    <label for="lastname"><strong><spring:message code="LASTNAME"/>:</strong></label>
                    <input type="text" name="lastname" id="lastname" required="required" value="${user.lastname}"/>
                    <br/>
                    <input type="submit" id="changeUserInfoBtn" class="btn btn-primary"
                           value="<spring:message code="SAVE_CHANGES"/>"/>
                </fieldset>
            </form>

        </div>
        <div class="tab-pane fade" id="changePasswordPanel">
            <div id="changePasswordMsg" class="alert">
            </div>
            <form action="#" method="POST" id="changePasswordForm">
                <fieldset>
                    <label for="password"><strong><spring:message code="NEW_PASSWORD"/>:</strong></label>
                    <input type="password" name="password" id="password" required="required"/>

                    <label for="repassword"><strong><spring:message code="CONFIRM_NEW_PASSWORD"/>:</strong></label>
                    <input type="password" name="password" id="repassword" required="required"/>
                    <br/>
                    <input type="submit" id="changePasswordBtn" class="btn btn-primary"
                           value="<spring:message code="CHANGE_PASSWORD"/>"/>
                </fieldset>
            </form>
        </div>
        <div class="tab-pane fade ${principal.databaseSchema?'':'active in'}" id="languagePanel">
            <div id="languageMsg" class="alert">
            </div>
            <form action="#" method="POST" id="languageForm">
                <fieldset>
                    <label for="language"><strong><spring:message code="LANGUAGE"/>:</strong></label>
                    <select name="language" id="language">
                        <option value="en">English</option>
                        <option value="es" <c:if test="${user.locale=='es'}">selected="selected" </c:if>>Spanish -
                            español
                        </option>
                        <option value="fr" <c:if test="${user.locale=='fr'}">selected="selected" </c:if>>French -
                            français
                        </option>
                        <option value="de" <c:if test="${user.locale=='de'}">selected="selected" </c:if>>German -
                            Deutsch
                        </option>
                        <option value="it" <c:if test="${user.locale=='it'}">selected="selected" </c:if>>Italian -
                            italiano
                        </option>
                        <option value="pt_BR" <c:if test="${user.locale=='pt_BR'}">selected="selected" </c:if>>
                            Portuguese
                            (Brazil) - português (Brasil)
                        </option>
                        <option value="zh_CN" <c:if test="${user.locale=='zh_CN'}">selected="selected" </c:if>>Chinese
                            (Simplified Han) - 中文（简体中文）
                        </option>
                        <option value="zh_TW" <c:if test="${user.locale=='zh_TW'}">selected="selected" </c:if>>Chinese
                            (Traditional Han) - 中文 (繁體中文)
                        </option>
                        <option value="ca" <c:if test="${user.locale=='ca'}">selected="selected" </c:if>>Catalan -
                            català
                        </option>
                    </select>
                    <br/>
                    <input type="submit" id="changeLanguageBtn" class="btn btn-primary"
                           value="<spring:message code="CHANGE_LANGUAGE"/>"/>
                </fieldset>
            </form>
        </div>
        <div class="tab-pane fade" id="deleteAccountPanel">
            <div id="deleteAccountMsg" class="alert alert-error" style="display: block;"><spring:message code="WARNING_DELETE_USER"/></div>
            <form action="#" method="POST" id="deleteAccountForm">
                <fieldset>
                    <input type="checkbox" name="confirmAccountDelete" id="accountMarkedForDelete" required="required"/>
                    <input type="submit" id="deleteAccountBtn" class="btn btn-primary"
                           value="<spring:message code="DELETE__ACCOUNT"/>"/>
                </fieldset>
            </form>
        </div>
    </div>
</div>

<script type="text/javascript">
    $('#changePasswordMsg').hide();
    $('#changeInfoMsg').hide();
    $('#languageMsg').hide();

    function postChange(url, postBody, msgContainerId, successMsg) {
        // Change success message ...
        jQuery.ajax(url, {
            async: false,
            dataType: 'json',
            data: postBody,
            type: 'PUT',
            contentType: "text/plain; charset=utf-8",
            success: function (data, textStatus, jqXHR) {
                $('#' + msgContainerId).removeClass('alert-error').addClass('alert-info').show();
                $('#' + msgContainerId).text(successMsg);
            },
            error: function (jqXHR, textStatus, errorThrown) {
                $('#' + msgContainerId).removeClass('alert-info').addClass('alert-error').show();
                $('#' + msgContainerId).text(textStatus);
            }
        });
    }

    $('#changePasswordForm').submit(function (event) {

        var inputVal = $('#changePasswordForm #password').val();
        var rinputVal = $('#changePasswordForm #repassword').val();
        if (inputVal != rinputVal) {
            // Password mismatch message ...
            $('#changePasswordMsg').removeClass('alert-info').addClass('alert-error').show();
            $('#changePasswordMsg').text('<spring:message code="PASSWORD_MISSMATCH"/>');
        } else {
            postChange("c/restful/account/password", inputVal, 'changePasswordMsg', "<spring:message code="CHANGE_PASSWORD_SUCCESS"/>");
        }
        event.preventDefault();
    });

    $('#changeUserForm').submit(function (event) {

        var fistname = $('#changeUserForm #firstname').val();
        var lastname = $('#changeUserForm #lastname').val();
        postChange("c/restful/account/firstname", fistname, 'changeInfoMsg', "<spring:message code="INFO_UPDATE_SUCCESS"/>");
        postChange("c/restful/account/lastname", lastname, 'changeInfoMsg', "<spring:message code="INFO_UPDATE_SUCCESS"/>");
        event.preventDefault();
    });

    $('#languageForm').submit(function (event) {

        var locale = $('#languageForm option:selected').val();
        postChange("c/restful/account/locale", locale, 'languageMsg', "<spring:message code="INFO_UPDATE_SUCCESS"/>");
        event.preventDefault();
    });

    $('#deleteAccountForm').submit(function (event) {

        var locale = $('#deleteAccountForm option:selected').val();
//FIXME: este metodo hay que unificarlo con postChange, con callbacks o eventos para los success and error y pasandole el type
        jQuery.ajax("c/restful/account", {
            async: false,
            dataType: 'json',
            data: locale,
            type: 'DELETE',
            contentType: "text/plain; charset=utf-8",
            success: function (data, textStatus, jqXHR) {
                window.location.href = "/c/logout"
            },
            error: function (jqXHR, textStatus, errorThrown) {
                $('#' + 'deleteAccountMsg').removeClass('alert-info').addClass('alert-error').show();
                $('#' + 'deleteAccountMsg').text(textStatus);
            }
        });
        event.preventDefault();
    });


</script>
