<%@ include file="/jsp/init.jsp" %>
<style type="text/css">
    #wizardContainer input {
        width: 50px;
        height: 25px;
        display: inline-block;
    }
</style>

<form method="post" id="dialogMainForm" action="#" class="well form-inline">
    <label for="enablePublicView" class="control-label">Enable Sharing:
        <input type="checkbox" id="enablePublicView" name="publicView"
                <c:if test="${mindmap.public}">
                    checked="checked"
                </c:if> />
    </label>
</form>

<p><span class="label label-important">Warning</span> <spring:message code="PUBLISH_DETAILS"/></p>

<div id="publishPanel">

    <ul class="nav nav-tabs">
        <li class="active"><a href="#embedTab" data-toggle="pill">Embed</a></li>
        <li><a href="#publicUrlTab" data-toggle="pill">Public URLs</a></li>
    </ul>

    <div class="tab-content">
        <div class="tab-pane fade active in" id="embedTab">
            <spring:message code="BLOG_INCLUSION"/>
            <div id="wizardContainer">
                <form class="form-inline" action="#">
                    <label for="frameWith">Frame width:</label>
                    <input type="number" id="frameWith" name="frameWith" value="600" class="span2"
                           min="0"/>

                    <label for="frameHeight">Frame height:</label>
                    <input type="number" id="frameHeight" name="frameHeight" value="400" class="span2" min="0"/>

                    <label for="mapZoom">Zoom %:</label>
                    <input type="number" id="mapZoom"
                           name="mapZoom" value="80"
                           class="span2" min="10" max="200" step="10"/>
                </form>
            </div>
            <label><spring:message code="BLOG_SNIPPET"/></label>
                <pre id="embedCode">&lt;iframe style="width:600px;height:400px;border: 1px
solid black" src="http://www.wisemapping.com/c/embeddedView.htm?mapId=${mindmap.id}&zoom=1"&gt; &lt;/iframe&gt;</pre>
        </div>

        <div class="tab-pane fade" id="publicUrlTab">
            <spring:message code="URL"/>:
            <input name="url" value="http://www.wisemapping.com/c/publicView.htm?mapId=${mindmap.id}"
                   style="width:400px"
                   readonly="readonly"/>
        </div>
    </div>
</div>

<script type="text/javascript">
    // Update tabs display status ...
    var checkboxElems = $('#dialogMainForm input:checkbox');
    var updateTabsDisplay = function() {
        var divElem = $('#publishPanel');
        checkboxElems[0].checked ? divElem.show() : divElem.hide();
    };
    checkboxElems.change(updateTabsDisplay);
    updateTabsDisplay();

    // Change snippet code based on the user options ...
    var replaceCode = function(regExpr, strReplace, factor) {
        var preElem = $('#publishPanel #embedCode')[0];
        var fieldValue = this.value;
        if (!isNaN(fieldValue) && fieldValue.length > 0) {
            var textVal = $(preElem).text().replace(regExpr, strReplace.replace('%s', fieldValue * factor));
            $(preElem).text(textVal);
        }
    };

    $('#publishPanel #frameWith').keyup(function() {
        replaceCode.bind(this)(/width:[0-9]+px/g, "width:%spx", 1);
    });

    $('#publishPanel #frameWith').change(function() {
        replaceCode.bind(this)(/width:[0-9]+px/g, "width:%spx", 1);
    });

    $('#publishPanel #frameHeight').keyup(function() {
        replaceCode.bind(this)(/height:[0-9]+px/g, "height:%spx", 1);
    });

    $('#publishPanel #frameHeight').change(function() {
        replaceCode.bind(this)(/height:[0-9]+px/g, "height:%spx", 1);
    });

    $('#publishPanel #mapZoom').keyup(function() {
        replaceCode.bind(this)(/zoom=.+\"/g, "zoom=%s\"", 0.1);
    });

    $('#publishPanel #mapZoom').change(function() {
        replaceCode.bind(this)(/zoom=.+\"/g, "zoom=%s\"", 0.01);
    });


    // Save status on click ...
    $('#dialogMainForm').submit(function(event) {
        jQuery.ajax("service/maps/${mindmap.id}/publish", {
            async:false,
            dataType: 'json',
            data: $('#dialogMainForm #enablePublicView')[0].checked ? 'true' : 'false',
            type: 'PUT',
            contentType:"text/plain",
            success : function(data, textStatus, jqXHR) {
                $('#publish-dialog-modal').modal('hide');
            },
            error: function(jqXHR, textStatus, errorThrown) {
                alert(textStatus);
            }
        });
        event.preventDefault();
    });

    // Hook for interaction with the main parent window ...
    var submitDialogForm = function() {
        $('#dialogMainForm').submit();
    }
</script>

