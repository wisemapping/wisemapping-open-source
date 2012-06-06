<%@ include file="/jsp/init.jsp" %>

<div id="messagePanel" class="alert alert-error">

</div>

<div>

    <p class="well"><spring:message code="IMPORT_MINDMAP_INFO"/></p>

    <form method="POST" enctype="multipart/form-data" action="#" id="dialogMainForm" class="form-horizontal">
        <fieldset>
            <div class="control-group">
                <label for="mapFile" class="control-label"><spring:message code="MIND_FILE"/>: </label>
                <input type="file" name="mapFile" id="mapFile" required="required" class="control"/>
            </div>
            <div class="control-group">
                <label for="title" class="control-label"><spring:message code="NAME"/>: </label>
                <input type="text" id="title" required="required" placeholder="Name of the new map to create"
                       class="control"/>
            </div>
            <div class="control-group">

                <label for="description" class="control-label"><spring:message code="DESCRIPTION"/>: </label>
                <textarea type="text" name="description" id="description"
                          placeholder="Some description for your map" class="control"></textarea>
            </div>

        </fieldset>
    </form>
</div>


<script type="text/javascript">

    // @Todo: Pending: report errors, manage corrupted mapsmanage case,escape url parameters, import with same title tries to save in post XML, explanation.

    $('#messagePanel').hide();

    // Save status on click ...
    var contentType = null;
    var fileContent = null;

    $('#cancelButton').click(function() {
        window.location = '/c/maps/';
    });

    $('#dialogMainForm').submit(function(event) {
        // Load form parameters ...
        var title = $('#dialogMainForm #title').attr('value');
        title = title == undefined ? "" : title;

        var description = $('#dialogMainForm #description').attr('value');
        description = description == undefined ? "" : description;

        // Save status on click ...
        jQuery.ajax("service/maps?title=" + encodeURI(title) + "&description=" + encodeURI(description),
                {
                    async:false,
                    data: fileContent,
                    type: 'POST',
                    dataType: 'json',
                    contentType:contentType,
                    success : function(data, textStatus, jqXHR) {
                        var resourceId = jqXHR.getResponseHeader("ResourceId");
                        window.location = "c/maps/" + resourceId + "/edit";
                    },
                    error: function(jqXHR, textStatus, errorThrown) {
                        $('#messagePanel').text(textStatus);
                    }
                });
        event.preventDefault();
    });

    $('#dialogMainForm #mapFile').change(function(event) {
        var file = event.target.files[0];
        var reader = new FileReader();

        var title = file.name;
        title = title.substring(0, title.lastIndexOf("."));
        $('#dialogMainForm #title').attr('value', jQuery.camelCase(title));

        // Closure to capture the file information.
        reader.onload = (function(event) {
            fileContent = event.target.result;
            contentType = file.name.lastIndexOf(".wxml") != -1 ? "application/xml" : "application/freemind";
        });

        // Read in the image file as a data URL.
        reader.readAsBinaryString(file);
    });

    // Hook for interaction with the main parent window ...
    var submitDialogForm = function() {
        $('#dialogMainForm').submit();
    }

</script>