<!DOCTYPE HTML>
<%@ include file="/jsp/init.jsp" %>

<div>
    <iframe src='${url}' style="border: 0;width: 100%;height:100%" id="dialogContentIframe"></iframe>
    <div style="float: right;margin-right: 25px">
        <input type="button" class="btn-primary" value="Accept" id="submitBtn"/>
        <input type="button" class="btn-secondary" value="Cancel" id="cancelBtn"/>
    </div>
</div>

<script type="text/javascript">
    $('submitBtn').addEvent('click', function() {
        var iframeWindow = $('dialogContentIframe').contentWindow;
        var delay = iframeWindow.submitDialogForm();

        if (MooDialog.Request.active) {
            if (!delay) {
                MooDialog.Request.active.close();
            } else {
                MooDialog.Request.active.close.delay(3000, MooDialog.Request.active);
            }
        }
    });

    $('cancelBtn').addEvent('click', function() {
        if (MooDialog.Request.active) {
            MooDialog.Request.active.close();
        }
    });
</script>