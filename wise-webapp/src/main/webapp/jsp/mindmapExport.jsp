<%@page pageEncoding="UTF-8" %>
<%@include file="/jsp/init.jsp" %>


<div style="height: 400px; overflow-y: auto">
    <div class="alert alert-info" role="alert">
        <spring:message code="EXPORT_DETAILS"/>
    </div>
    <form method="GET" class="form-horizontal" action="c/restful/maps/${mindmap.id}"
          enctype="application/x-www-form-urlencoded" id="dialogMainForm">
        <input name="svgXml" id="svgXml" value="" type="hidden"/>
        <input name="download" type="hidden" value="mm"/>
        <fieldset>
            <label for="freemind">
                <input type="radio" id="freemind" name="exportFormat" value="mm" checked="checked"/>
                <strong><spring:message code="FREEMIND_EXPORT_FORMAT"/></strong><br/>
                <spring:message code="FREEMIND_EXPORT_FORMAT_DETAILS"/>
            </label>

            <label for="mmap">
                <input type="radio" name="exportFormat" value="mmap" id="mmap"/>
                <strong><spring:message code="MINDJET_EXPORT_FORMAT"/></strong><br/>
                <spring:message code="MINDJET_EXPORT_FORMAT_DETAILS"/>
            </label>

            <label for="wisemapping">
                <input type="radio" id="wisemapping" name="exportFormat" value="wxml"/>
                <strong><spring:message code="WISEMAPPING_EXPORT_FORMAT"/></strong><br/>
                <spring:message code="WISEMAPPING_EXPORT_FORMAT_DETAILS"/>
            </label>

            <label for="svg">
                <input type="radio" id="svg" name="exportFormat" value="svg"/>
                <strong><spring:message code="SVG_EXPORT_FORMAT"/></strong><br/>
                <spring:message code="SVG_EXPORT_FORMAT_DETAILS"/><br/>
            </label>

            <label for="pdf">
                <input type="radio" name="exportFormat" value="pdf" id="pdf"/>
                <strong><spring:message code="PDF_EXPORT_FORMAT"/></strong><br/>
                <spring:message code="PDF_EXPORT_FORMAT_DETAILS"/>
            </label>

            <label for="img">
                <input type="radio" name="exportFormat" id="img" value="image"/>
                <strong><spring:message code="IMG_EXPORT_FORMAT"/></strong><br/>
                <spring:message code="IMG_EXPORT_FORMAT_DETAILS"/>

                <select name="imgFormat" id="imgFormat" style="display:none">
                    <option value='png'>PNG</option>
                    <option value='jpg'>JPEG</option>
                </select>
            </label>

            <label for="txt">
                <input type="radio" name="exportFormat" value="txt" id="txt"/>
                <strong><spring:message code="TXT_EXPORT_FORMAT"/></strong><br/>
                <spring:message code="TXT_EXPORT_FORMAT_DETAILS"/>
            </label>

            <label for="xls">
                <input type="radio" name="exportFormat" value="xls" id="xls"/>
                <strong><spring:message code="XLS_EXPORT_FORMAT"/></strong><br/>
                <spring:message code="XLS_EXPORT_FORMAT_DETAILS"/>
            </label>

            <label for="odt">
                <input type="radio" name="exportFormat" value="odt" id="odt"/>
                <strong><spring:message code="OPEN_OFFICE_EXPORT_FORMAT"/></strong><br/>
                <spring:message code="OPEN_OFFICE_EXPORT_FORMAT_DETAILS"/>
            </label>
        </fieldset>
    </form>
    <div id="exportInfo">
        <span class="label label-danger">Warning</span> <spring:message code="EXPORT_FORMAT_RESTRICTIONS"/>
    </div>

</div>


<style>
    h2 {
        font-size: 160%;
        color: #8e9181;
    }
</style>
<script type="text/javascript">

    // No way to obtain map svg. Hide panels..
    if (window.location.pathname.match(/\/[0-9]+\/edit/)) {
        $('#exportInfo').hide();
        $('#freemind,#pdf,#svg,#odt,#txt,#xls,#mmap').click('click', function (event) {
            $('#imgFormat').hide();
        });

        $('#img').click('click', function (event) {
            $('#imgFormat').show();
        });
        $('#exportInfo').hide();
    } else {
        $('#pdf,#svg,#img').parent().hide();
    }

    function submitDialogForm(differ) {
        // If the map is opened, use the latest model ...
        var formatType = $('#dialogMainForm input:checked').attr('value');
        var form = $('#dialogMainForm');

        // Restore default ..
        form.attr('action', 'c/restful/maps/${mindmap.id}.' + formatType);

        if (formatType == 'image' || formatType == 'svg' || formatType == 'pdf') {

            // Look for the selected format and append export suffix...
            if (formatType == 'image') {
                formatType = $('#dialogMainForm option:selected').attr('value');
            }
            // Change to transform url ...
            form.attr('method', "POST");
            form.attr('action', 'c/restful/transform.' + formatType);

            // Load page SVG ...
            var svgXml = window.parent.document.getElementById('workspaceContainer').innerHTML;
            $('#svgXml').attr('value', svgXml);

        }

        $('#dialogMainForm input[name=download]').attr('value', formatType);
        form.submit();

        // Close dialog ...
        $('#export-dialog-modal').modal('hide');
    }

</script>