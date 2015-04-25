<%@page pageEncoding="UTF-8" %>
<%@include file="/jsp/init.jsp" %>

<style type="text/css">
    #wizardContainer input {
        width: 80px;
        display: inline-block;
    }
</style>

<form method="post" id="dialogMainForm" action="#" class="well form-inline">
    <label for="enablePublicView" class="control-label"><spring:message code="ENABLE_PUBLISHING"/>:
        <input type="checkbox" id="enablePublicView" name="publicView"
                <c:if test="${mindmap.public}">
                    checked="checked"
                </c:if> />
    </label>
</form>

<p><span class="label label-danger"> <spring:message code="WARNING"/></span> <spring:message code="PUBLISH_DETAILS"/>
</p>

<div id="publishPanel">

    <ul class="nav nav-tabs">
        <li class="active"><a href="#embedTab" data-toggle="pill"><spring:message code="EMBED"/></a></li>
        <li><a href="#publicUrlTab" data-toggle="pill"><spring:message code="PUBLIC_URL"/></a></li>
    </ul>
    <br/>
    <div class="tab-content">
        <div class="tab-pane fade active in" id="embedTab">
            <spring:message code="BLOG_INCLUSION"/>
            <div id="wizardContainer">
                <form class="form-horizontal" action="#" style="padding-top: 1em">
                    <div class="form-group">
                        <label for="frameWidth" class="col-sm-4 control-label"><spring:message code="FRAME_WIDTH"/>:</label>
                        <div class="col-sm-4">
                            <input type="number" id="frameWidth" name="frameWidth" value="600" class="form-control" min="0"/>
                        </div>
                    </div>
                    <div class="form-group">
                        <label for="frameHeight" class="col-sm-4 control-label"><spring:message code="FRAME_HEIGHT"/>:</label>
                        <div class="col-sm-4">
                            <input type="number" id="frameHeight" name="frameHeight" value="400" class="form-control" min="0"/>
                        </div>
                    </div>
                    <div class="form-group">
                        <label for="mapZoom" class="col-sm-4 control-label"><spring:message code="ZOOM"/> %:</label>
                        <div class="col-sm-4">
                            <input type="number" id="mapZoom" name="mapZoom" value="80" class="form-control" min="10" max="200" step="10"/>
                        </div>
                    </div>
                </form>
            </div>
            <label><spring:message code="BLOG_SNIPPET"/></label>
                <textarea disabled style="cursor: text; font-family: monospace;resize: none" class="form-control" id="embedCode">&lt;iframe style="width:600px;height:400px;border: 1px
solid black" src="${baseUrl}/c/maps/${mindmap.id}/embed?zoom=1"&gt; &lt;/iframe&gt;</textarea>
        </div>

        <div class="tab-pane fade" id="publicUrlTab">
            <p><spring:message code="DIRECT_LINK_EXPLANATION"/></p>
            <input name="url" value="${baseUrl}/c/maps/${mindmap.id}/public"
                   style="width:400px;cursor: text"
                   readonly="readonly" class="form-control"/>
        </div>
    </div>
</div>

<script type="text/javascript">
    // Update tabs display status ...
    var checkboxElems = $('#dialogMainForm input:checkbox');
    var updateTabsDisplay = function () {
        var divElem = $('#publishPanel');
        checkboxElems[0].checked ? divElem.show() : divElem.hide();
    };
    checkboxElems.change(updateTabsDisplay);
    updateTabsDisplay();

    // Change snippet code based on the user options ...
    var replaceCode = function (regExpr, strReplace, factor) {
        var preElem = $('#publishPanel #embedCode')[0];
        var fieldValue = this.value;
        if (!isNaN(fieldValue) && fieldValue.length > 0) {
            var textVal = $(preElem).text().replace(regExpr, strReplace.replace('%s', fieldValue * factor));
            $(preElem).text(textVal);
        }
    };

    $('#publishPanel #frameWidth').keyup(function () {
        replaceCode.bind(this)(/width:[0-9]+px/g, "width:%spx", 1);
    });

    $('#publishPanel #frameWidth').change(function () {
        replaceCode.bind(this)(/width:[0-9]+px/g, "width:%spx", 1);
    });

    $('#publishPanel #frameHeight').keyup(function () {
        replaceCode.bind(this)(/height:[0-9]+px/g, "height:%spx", 1);
    });

    $('#publishPanel #frameHeight').change(function () {
        replaceCode.bind(this)(/height:[0-9]+px/g, "height:%spx", 1);
    });

    $('#publishPanel #mapZoom').keyup(function () {
        replaceCode.bind(this)(/zoom=.+\"/g, "zoom=%s\"", 0.1);
    });

    $('#publishPanel #mapZoom').change(function () {
        replaceCode.bind(this)(/zoom=.+\"/g, "zoom=%s\"", 0.01);
    });


    // Save status on click ...
    $('#dialogMainForm').submit(function (event) {
        jQuery.ajax("c/restful/maps/${mindmap.id}/publish", {
            async:false,
            dataType:'json',
            data:$('#dialogMainForm #enablePublicView')[0].checked ? 'true' : 'false',
            type:'PUT',
            contentType:"text/plain",
            success:function (data, textStatus, jqXHR) {
                $('#publish-dialog-modal').modal('hide');
            },
            error:function (jqXHR, textStatus, errorThrown) {
                alert(textStatus);
            }
        });
        event.preventDefault();
    });

    // Hook for interaction with the main parent window ...
    function submitDialogForm() {
        $('#dialogMainForm').submit();
    }
</script>

