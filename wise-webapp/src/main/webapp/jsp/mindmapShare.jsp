<%@ include file="/jsp/init.jsp" %>
<style type="text/css">
    #sharingContainer {
        height: 180px;
        width: 100%;
        overflow-y: scroll;
        border-top: 1px solid #d3d3d3;
        border-bottom: 1px solid #d3d3d3;
    }

    #sharingContainer table {
        font-size: 100%;
    }

    #collabEmails {
        float: left;
        width: 300px;
    }

    #roleBtn {
        float: left;
        margin: 0px 10px;
    }

</style>

<p><strong>Who has access:</strong></p>

<div id="sharingContainer">
    <table class="table" id="collabsTable">
    </table>
</div>

<div class="well">
    <div id="errorMsg" class="alert alert-error"></div>
    <p>Add People: </p>
    <input type="text" id="collabEmails" name="collabEmails"
           placeholder="Enter collaborators emails separared by comas."/>

    <div class="btn-group" id="roleBtn">
        <a class="btn dropdown-toggle" data-toggle="dropdown" href="#">Can edit
            <span class="caret"> </span>
        </a>
        <ul class="dropdown-menu" data-role="editor" id="shareRole">
            <li><a href="#" data-role="editor">Can edit</a></li>
            <li><a href="#" data-role="viewer">Can view</a></li>
        </ul>
    </div>
    <button id="addBtn" class="btn btn-primary">Add</button>
</div>

<script type="text/javascript">

    $("#errorMsg").hide();

    function onClickShare(aElem) {
        var role = $(aElem).attr('data-role');
        var roleDec = role == 'viewer' ? "Can view" : "Can edit";
        var btnElem = $(aElem).parent().parent().parent().find(".dropdown-toggle");
        btnElem.text(roleDec).append(' <span class="caret"> </span>');
        return role;
    }

    function addCollaborator(email, role) {
        // Add row to the table ...
        var tableElem = $("#collabsTable");
        var rowTemplate = '\
                   <tr data-collab="{email}" data-role="{role}">\
                   <td>{email}</td>\
                   <td>\
                       <div class="btn-group">\
                           <a class="btn dropdown-toggle" data-toggle="dropdown" href="#""></a>\
                           <ul class="dropdown-menu">\
                               <li><a href="#" data-role="editor">Can edit</a></li>\
                               <li><a href="#" data-role="viewer">Can view</a></li>\
                           </ul>\
                       </div>\
                   </td>\
                 <td><a href="#">x</a></td>\
                </tr>';

        var rowStr = rowTemplate.replace(/{email}/g, email);
        rowStr = rowStr.replace(/{role}/g, role);
        var elem = tableElem.append(rowStr);

        // Register change role event  ...
        var rowElem = $("#collabsTable tr:last");
        $(rowElem.find(".dropdown-menu a").click(function() {
            var role = onClickShare(this);
            rowElem.attr('data-role', role);
            event.preventDefault();
        }));
        rowElem.find('.dropdown-menu a[data-role="' + role + '"]').click();

        // Register remove event ...
        rowElem.find("td:last a").click(function(event) {
            var email = rowElem.attr('data-collab');
            removeCollab(email);
            event.preventDefault();
        });
    }

    var removeCollab = function(email) {
        // Remove html entry ...
        $('#collabsTable tr[data-collab="' + email + '"]').detach();
    };

    $(function() {
        var loadedCollabs = [
            {email:'paulo1@pveiga.com.ar',role:'viewer'},
            {email:'paulo2@pveiga.com.ar',role:'editor'},
            {email:'paulo3@pveiga.com.ar',role:'editor'}
        ];

        // Init table will all values ...
        for (var i = 0; i < loadedCollabs.length; i++) {
            var collab = loadedCollabs[i];
            addCollaborator(collab.email, collab.role);
        }
    });

    $("#addBtn").click(function(event) {
        var i,email;
        var emailsStr = $("#collabEmails").val();
        var role = $("#shareRole").attr('data-role');

        // Clear previous state ...
        $("#errorMsg").text("").hide();

        // Split emails ...
        var valid = true;
        if (emailsStr.length > 0) {
            var emails = jQuery.grep(emailsStr.split(/[;|,|\s]/), function(val) {
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
                var useExists = jQuery.grep(model,
                        function(val) {
                            return val.email == email;
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
                addCollaborator(email, role);
            }
        } else {

        }
    });

    // Register change event  ...
    $("#shareRole a").click(function() {
        var role = onClickShare(this);
        $(this).parent().attr('data-role', role);

        event.preventDefault();
    });

    function isValidEmailAddress(emailAddress) {
        var pattern = new RegExp(/^((([a-z]|\d|[!#\$%&'\*\+\-\/=\?\^_`{\|}~]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])+(\.([a-z]|\d|[!#\$%&'\*\+\-\/=\?\^_`{\|}~]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])+)*)|((\x22)((((\x20|\x09)*(\x0d\x0a))?(\x20|\x09)+)?(([\x01-\x08\x0b\x0c\x0e-\x1f\x7f]|\x21|[\x23-\x5b]|[\x5d-\x7e]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(\\([\x01-\x09\x0b\x0c\x0d-\x7f]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF]))))*(((\x20|\x09)*(\x0d\x0a))?(\x20|\x09)+)?(\x22)))@((([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])*([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])))\.)+(([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])*([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])))\.?$/i);
        return pattern.test(emailAddress);
    }

    function reportError(msg) {
        $('#errorMsg').show();
        $('#errorMsg').append("<p>" + msg + "</p>");
    }

    function buildCollabModel() {
        return $('#collabsTable tr').map(function() {
            return {
                email: $(this).attr('data-collab'),
                role: $(this).attr('data-role')
            };
        });

    }

    // Hook for interaction with the main parent window ...
    var submitDialogForm = function() {

        console.log(buildCollabModel());

    }
</script>

