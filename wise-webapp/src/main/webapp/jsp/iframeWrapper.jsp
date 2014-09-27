<%@page pageEncoding="UTF-8" %>
<%@include file="/jsp/init.jsp" %>

<!DOCTYPE HTML>

<div>
    <iframe src='${url}' style="border: 0;width: 100%;height:100%;min-height:400px;overflow-y: auto;"
            id="dialogContentIframe"></iframe>
    <div style="float: right;margin-right: 25px">
        <input type="button" class="btn-primary" value="<spring:message code="ACCEPT"/>" id="submitBtn"/>
        <input type="button" class="btn-secondary" value="<spring:message code="CANCEL"/>" id="cancelBtn"/>
    </div>
</div>

<script type="text/javascript">
    $('submitBtn').addEvent('click', function () {
        var iframeWindow = $('dialogContentIframe').contentWindow;
        if (iframeWindow && (typeof iframeWindow.submitDialogForm == 'function')) {
            var context = iframeWindow.submitDialogForm(true);
            if (context) {
                // This is a hack for the export function. If this is not done, the dialog is closed and the export fails.
                var iframeForm = $('iframeExportForm');
                iframeForm.setAttribute('method', context.method);
                iframeForm.setAttribute('action', context.action);

                var svgXml = context.method == "POST" ? window.document.getElementById('workspaceContainer').innerHTML : "";
                $('svgXml').setAttribute('value', svgXml);
                $('download').setAttribute('value', context.formatType);
                $('version').setAttribute('value', context.version);
                iframeForm.submit();
            }
            if (MooDialog.Request.active) {
                MooDialog.Request.active.close();
            }
        }
    });

    $('cancelBtn').addEvent('click', function () {
        if (MooDialog.Request.active) {
            MooDialog.Request.active.close();
        }
    });
</script>
<form method="GET" class="form-horizontal" action="c/restful/maps/${mindmap.id}"
      enctype="application/x-www-form-urlencoded" id="iframeExportForm">
    <input name="svgXml" id="svgXml" value="" type="hidden"/>
    <input name="download" id="download" type="hidden" value="mm"/>
    <input name="version" id="version" type="hidden" value=""/>
</form>