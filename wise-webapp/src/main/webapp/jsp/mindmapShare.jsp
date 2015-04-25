<%@page pageEncoding="UTF-8" %>
<%@include file="/jsp/init.jsp" %>

<style type="text/css">
    #sharingContainer {
        height: 220px;
        width: 100%;
        overflow-y: scroll;
        border-top: 1px solid #d3d3d3;
        border-bottom: 1px solid #d3d3d3;
    }

    #sharingContainer table {
        font-size: 100%;
    }

    #collabEmails {
        width: 60%;
        display: inline-block;
    }

    #roleBtn a:hover {
        text-decoration: underline;
    }

    #collabMessage {
        display: block;
        width: 100%;
    }

    .closeBtn {
        font-size: 13px;
        font-weight: 800;
        line-height: 1;
        color: #000;
        text-shadow: 0 1px 0 #fff;
        text-decoration: none;
    }

    #collabsTable td {
        vertical-align: middle;
    }

    .alert-small {
        padding: 7px;
    }

</style>

<p><strong><spring:message code="WHO_CAN_ACCESS"/>:</strong></p>

<div id="sharingContainer">
    <table class="table" id="collabsTable">
        <colgroup>
            <col width="65%"/>
            <col width="30%"/>
            <col width="5%"/>
        </colgroup>
    </table>
</div>

<div class="well" style="margin-bottom: 0px">
    <div id="errorMsg" class="alert alert-danger alert-small"></div>
    <div>

        <p><strong><spring:message code="ADD_PEOPLE"/>:</strong></p>

        <input type="text" id="collabEmails" name="collabEmails" class="form-control"
               placeholder="<spring:message code="COLLABORATORS_SEPARATED_BY_COMA"/>"/>

        <div class="btn-group" id="roleBtn">
            <a class="btn dropdown-toggle" data-toggle="dropdown" href="#"><spring:message code="CAN_EDIT"/>
                <span class="caret"> </span>
            </a>
            <ul class="dropdown-menu" role="menu" id="shareRole" data-role="editor">
                <li><a href="#" data-role="editor" style="text-decoration: none"><spring:message code="CAN_EDIT"/></a></li>
                <li><a href="#" data-role="viewer" style="text-decoration: none"><spring:message code="CAN_VIEW"/></a></li>
            </ul>
        </div>
        <button id="addBtn" class="btn btn-primary"><spring:message code="ADD"/></button>
    </div>
    <div style="margin-top: 10px;">
        <p><strong><spring:message code="EMAIL_NOTIFICATION_MESSAGE"/></strong> - <a href="#"
                                                                                     id="addMessageLink" style="padding-left: 0px"><spring:message
                code="ADD_MESSAGE"/></a></p>
        <textarea cols="4" id="collabMessage" class="form-control" placeholder="<spring:message code="OPTIONAL_CUSTOM_MESSAGE"/>">

        </textarea>
    </div>
</div>


<script type="text/javascript">

$("#errorMsg").hide();
$("#collabMessage").hide();

$("#addMessageLink").click(function (event) {
    $("#collabMessage").toggle().val("");
    event.preventDefault();
});

$("#collabEmails").keyup(function(event) {
    var keyCode = event.keyCode;
    if (keyCode == 13) {
        $('#addBtn').trigger('click');
    }
});

function onDialogShown() {
    $('#collabEmails').focus();
}

var messages = {
    owner:'<spring:message code="IS_OWNER"/>',
    editor:'<spring:message code="CAN_EDIT"/>',
    viewer:'<spring:message code="CAN_VIEW"/>'};

function onClickShare(aElem) {
    var role = $(aElem).attr('data-role');
    var roleDec = messages[role];
    var btnElem = $(aElem).parent().parent().parent().find(".dropdown-toggle");
    btnElem.text(roleDec).append(' <span class="caret"> </span>');
    return role;
}

function addCollaboration(email, role, changeType) {
    var rowStr;
    var tableElem = $("#collabsTable");
    if (role != "owner") {
        tableElem = $("#collabsTable tbody");
        // Add row to the table ...
        var rowTemplate = '\
                   <tr data-collab="{email}" data-role="{role}">\
                   <td><span>{email}{typeIcon}</span></td>\
                   <td>\
                       <div class="btn-group">\
                           <a class="btn dropdown-toggle" data-toggle="dropdown" href="#""></a>\
                           <ul class="dropdown-menu">\
                               <li><a href="#" data-role="editor">' + messages['editor'] + '</a></li>\
                               <li><a href="#" data-role="viewer">' + messages['viewer'] + '</a></li>\
                           </ul>\
                       </div>\
                   </td>\
                 <td><a href="#" class="closeBtn" style="text-decoration: none; color: rgb(201, 1, 1)">x</a></td>\
                </tr>';

        rowStr = rowTemplate.replace(/{email}/g, email);
        rowStr = rowStr.replace(/{role}/g, role);
        rowStr = rowStr.replace(/{typeIcon}/g, changeType ? ' <span class="label label-success">' + changeType + '</span>' : "");
        var elem = tableElem.append(rowStr);

        // Register change role event  ...
        var rowElem = $("#collabsTable tr:last");
        $(rowElem.find(".dropdown-menu a").click(function (event) {
            reportError(null);
            var role = onClickShare(this);
            rowElem.attr('data-role', role);
            event.preventDefault();
        }));
        rowElem.find('.dropdown-menu a[data-role="' + role + '"]').click();

        // Register remove event ...
        rowElem.find("td:last a").click(function (event) {
            reportError(null);
            var email = rowElem.attr('data-collab');
            removeCollab(email);
            event.preventDefault();
        });
    } else {
        rowStr = '<tr data-collab=' + email + ' data-role="' + role + '">\
                            <td><span>' + email + ' (<spring:message code="YOU"/>)</span></td>\
                            <td><button class="btn btn-secondary" style="margin-left: 15px">' + messages[role] + '</button></td>\
                              <td></td>\
                             </tr>';
        tableElem.append(rowStr);

    }
}

var removeCollab = function (email) {
    // Remove html entry ...
    $('#collabsTable tr[data-collab="' + email + '"]').detach();
};

$(function () {
    jQuery.ajax("c/restful/maps/${mindmap.id}/collabs", {
        async:false,
        dataType:'json',
        type:'GET',
        contentType:"text/plain",
        success:function (data, textStatus, jqXHR) {
            // Owner roles is the first in the table ...
            var collabs = data.collaborations.sort(function (a, b) {
                return a.id > b.id;
            });

            // Add all the columns in the table ...
            for (var i = 0; i < collabs.length; i++) {
                var collab = collabs[i];
                addCollaboration(collab.email, collab.role);
            }

        },
        error:function (jqXHR, textStatus, errorThrown) {
            alert(textStatus);
        }
    });
});

$("#addBtn").click(function (event) {
    var i, email;
    var emailsStr = $("#collabEmails").val();
    var role = $("#shareRole").attr('data-role');

    reportError(null);

    // Split emails ...
    var valid = true;
    if (emailsStr.length > 0) {
        var emails = jQuery.grep(emailsStr.split(/[;|,|\s]/), function (val) {
            return val.length > 0
        });

        var model = buildCollabModel();
        for (i = 0; i < emails.length; i++) {
            email = emails[i];
            if (!isValidEmailAddress(email)) {
                reportError("Invalid email address:" + email);
                valid = false;
            }

            // Is there a collab with the same email  ?
            var useExists = jQuery.grep(model.collaborations,
                    function (val) {
                        return val.email.trim() == email.trim();
                    }).length > 0;

            if (useExists) {
                reportError(email + " is already in the shared list");
                valid = false;
            }

        }
    } else {
        reportError("Emails address can not be empty");
        valid = false;
    }


    if (valid) {
        // Emails are valid, add them as rows ...
        $("#collabEmails").val("");
        for (i = 0; i < emails.length; i++) {
            email = emails[i];
            addCollaboration(email, role, '<spring:message code="NEW"/>');
        }
    }
});

// Register change event  ...
$("#shareRole a").click(function (event) {
    var role = onClickShare(this);
    $(this).closest("ul").attr('data-role', role);

    event.preventDefault();
});

function isValidEmailAddress(emailAddress) {
    var pattern = new RegExp(/^((([a-z]|\d|[!#\$%&'\*\+\-\/=\?\^_`{\|}~]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])+(\.([a-z]|\d|[!#\$%&'\*\+\-\/=\?\^_`{\|}~]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])+)*)|((\x22)((((\x20|\x09)*(\x0d\x0a))?(\x20|\x09)+)?(([\x01-\x08\x0b\x0c\x0e-\x1f\x7f]|\x21|[\x23-\x5b]|[\x5d-\x7e]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(\\([\x01-\x09\x0b\x0c\x0d-\x7f]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF]))))*(((\x20|\x09)*(\x0d\x0a))?(\x20|\x09)+)?(\x22)))@((([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])*([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])))\.)+(([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])*([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])))\.?$/i);
    return pattern.test(emailAddress);
}

function reportError(msg) {
    if (msg) {
        $('#errorMsg').show();
        $('#errorMsg').append("<p>" + msg + "</p>");
    } else {
        $("#errorMsg").text("").hide();
    }
}

function buildCollabModel() {
    var collabs = $('#collabsTable tr').map(function () {
        return {
            email:$(this).attr('data-collab'),
            role:$(this).attr('data-role')
        };
    });
    collabs = jQuery.makeArray(collabs);

    return {
        count:collabs.length,
        collaborations:collabs
    };
}

// Hook for interaction with the main parent window ...
function submitDialogForm() {

    var collabs = buildCollabModel();
    collabs.collaborations = jQuery.grep(collabs.collaborations, function () {
        return this.role != 'owner';
    });
    collabs['message'] = $("#collabMessage").val();

    jQuery.ajax("c/restful/maps/${mindmap.id}/collabs", {
        async:false,
        dataType:'json',
        type:'PUT',
        data:JSON.stringify(collabs),
        contentType:"application/json",
        success:function (data, textStatus, jqXHR) {
            $('#share-dialog-modal').modal('hide');
        },
        error:function (jqXHR, textStatus, errorThrown) {
            reportError(textStatus);
        }
    });
}
</script>

