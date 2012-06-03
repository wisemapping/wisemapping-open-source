<%@ include file="/jsp/init.jsp" %>

<div class="fform">
    <h1>
        <spring:message code="${requestScope.title}"/>
    </h1>

    <p><spring:message code="${requestScope.details}"/></p>

    <form method="post" enctype="multipart/form-data" action="#">
        <fieldset>
            <label for="title"><spring:message code="NAME"/></label>
            <input type="text" id="title" required="required"/>

            <label for="description"><spring:message code="DESCRIPTION"/></label>
            <input type="text" name="description" id="description"/>

            <label><spring:message code="DESCRIPTION"/> </label>
            <input type="radio" name="type" value="mm"/> Freemind (0.9)
            <input type="radio" name="type" value="wxml"/> WiseMapping

            <label for="mapFile"><spring:message code="FREE_MIND_FILE"/></label>
            <input type="file" name="mapFile" id="mapFile"/>
        </fieldset>

        <input type="button" id="acceptButton" value="<spring:message code="IMPORT"/>" class="btn btn-primary"/>
        <input type="button" id="cancelButton" value="<spring:message code="CANCEL"/>" class="btn">
    </form>
</div>


<script type="text/javascript">
    // Save status on click ...
    $('#cancelButton').click(function() {
        window.location = '/c/maps/';
    });


    $('#acceptButton').click(function(event) {

        // http://www.html5rocks.com/en/tutorials/file/dndfiles/
        var content;
        if (window.FileReader) {
            reader = new FileReader();
            reader.onloadend = function (e) {
                content = e.target.result;
            };
            reader.readAsDataURL(file);
        }

        jQuery.ajax("service/maps", {
            async:false,
            dataType: 'application/freemind',
            data: "",
            type: 'PUT',
            contentType:"text/plain",
            success : function(data, textStatus, jqXHR) {
            },
            error: function(jqXHR, textStatus, errorThrown) {
            }
        });
    });

    // Hook for interaction with the main parent window ...
    var submitDialogForm = function() {
        $('#dialogMainForm').submit();
    }
</script>