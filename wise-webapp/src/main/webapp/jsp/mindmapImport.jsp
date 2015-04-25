<%@page pageEncoding="UTF-8" %>
<%@include file="/jsp/init.jsp" %>

<div>

    <p class="alert alert-info"><spring:message code="IMPORT_MINDMAP_INFO"/></p>

    <form method="POST" enctype="multipart/form-data" action="#" id="dialogMainForm" class="form-horizontal">
        <div class="errorMessage"></div>
        <fieldset>
            <div class="form-group">
                <label for="mapFile" class="control-label col-md-2 "><spring:message code="MIND_FILE"/>: </label>
                <div class="col-md-10">
                    <input type="file" name="file" id="mapFile" required="required" class="form-control"/>
                </div>
            </div>
            <div class="form-group">
                <label for="title" class="control-label col-md-2 "><spring:message code="NAME"/>: </label>
                <div class="col-md-10">
                   <input type="text" id="title" name="title" required="required" autofocus="autofocus"
                           placeholder="<spring:message code="MAP_NAME_HINT"/>"
                           class="form-control" maxlength="255"/>
                </div>
            </div>
            <div class="form-group">
                <label for="description" class="control-label col-md-2"><spring:message code="DESCRIPTION"/>: </label>
                <div class="col-md-10">
                    <textarea name="description" id="description"
                          placeholder="<spring:message code="MAP_DESCRIPTION_HINT"/>" class="form-control" maxlength="255"></textarea>
                </div>
            </div>

        </fieldset>
    </form>
</div>


<script type="text/javascript">
    // Save status on click ...
    var contentType = null;
    var fileContent = null;

    $('#cancelButton').click(function () {
        window.location = 'c/maps/';
    });

    $('#dialogMainForm').submit(function (event) {
        // Load form parameters ...
        var title = $('#dialogMainForm #title').val();
        title = title == undefined ? "" : title;

        var description = $('#dialogMainForm #description').val();
        description = description == undefined ? "" : description;

        var onSuccess = function (data, textStatus, jqXHR) {
            var resourceId = data.getResponseHeader("ResourceId");
            window.location = "c/maps/" + resourceId + "/edit";
        };

        var onError = function (jqXHR, textStatus, errorThrown) {
            if (jqXHR.status == 400) {
                var errors = JSON.parse(jqXHR.responseText);
                // Mark fields with errors ...
                var fieldErrors = errors.fieldErrors;
                if (fieldErrors) {
                    for (var fieldName in fieldErrors) {
                        // Mark the field with errors ...
                        var message = fieldErrors[fieldName];
                        var inputField = $("#dialogMainForm input[name='" + fieldName + "']");
                        $("#dialogMainForm").find(".errorMessage").text(message).addClass("alert alert-danger");
                        inputField.parent().addClass('error');
                    }
                }
                var globalErrors = errors.globalErrors;
                if (globalErrors) {
                    for (var error in globalErrors) {
                        // Mark the field with errors ...
                        $("#dialogMainForm").find(".errorMessage").text(error).addClass("alert alert-danger");
                        inputField.parent().addClass('error');
                    }
                }
            } else {
                console.log(errorThrown);
                console.log(jqXHR);
                $('#messagesPanel div').text(errorThrown).parent().show();
            }
        };

        // Save status on click ...
        jQuery.ajax("c/restful/maps?title=" + encodeURI(title) + "&description=" + encodeURI(description),
                {
                    async:false,
                    data:fileContent,
                    type:'POST',
                    dataType:'json',
                    contentType:contentType,
                    statusCode: {
                        201: onSuccess,
                        400: onError,
                        default: onError
                    }
                });
        event.preventDefault();
    });

    $('#dialogMainForm #mapFile').change(function (event) {
        var file = event.target.files[0];
        var reader = new FileReader();

        var title = file.name;
        title = title.substring(0, title.lastIndexOf("."));
        $('#dialogMainForm #title').attr('value', jQuery.camelCase(title));

        // Closure to capture the file information.
        reader.onload = (function (event) {
            fileContent = event.target.result;
            contentType = file.name.lastIndexOf(".wxml") != -1 ? "application/xml" : "application/freemind";
        });

        // Read in the image file as a data URL.
        reader.readAsText(file);
    });

    // Hook for interaction with the main parent window ...
    function submitDialogForm() {
        $('#dialogMainForm').submit();
    }

</script>
