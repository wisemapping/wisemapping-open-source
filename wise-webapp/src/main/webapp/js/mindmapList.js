/*--------------------------------------------- Common actions --------------------------------------------------**/

$.fn.dataTableExt.oApi.fnReloadAjax = function (oSettings, sNewSource, fnCallback, bStandingRedraw) {
    if (typeof sNewSource != 'undefined' && sNewSource != null) {
        oSettings.sAjaxSource = sNewSource;
    }
    this.oApi._fnProcessingDisplay(oSettings, true);
    var that = this;
    var iStart = oSettings._iDisplayStart;
    var aData = [];

    this.oApi._fnServerParams(oSettings, aData);

    oSettings.fnServerData(oSettings.sAjaxSource, aData, function (json) {
        /* Clear the old information from the table */
        that.oApi._fnClearTable(oSettings);

        /* Got the data - add it to the table */
        var aData = (oSettings.sAjaxDataProp !== "") ?
            that.oApi._fnGetObjectDataFn(oSettings.sAjaxDataProp)(json) : json;

        for (var i = 0; i < aData.length; i++) {
            that.oApi._fnAddData(oSettings, aData[i]);
        }

        oSettings.aiDisplay = oSettings.aiDisplayMaster.slice();
        that.fnDraw();

        if (typeof bStandingRedraw != 'undefined' && bStandingRedraw === true) {
            oSettings._iDisplayStart = iStart;
            that.fnDraw(false);
        }

        that.oApi._fnProcessingDisplay(oSettings, false);

        /* Callback user function - for event handlers etc */
        if (typeof fnCallback == 'function' && fnCallback != null) {
            fnCallback(oSettings);
        }
    }, oSettings);
};

jQuery.fn.dataTableExt.selectAllMaps = function () {
    var bool = $("input:checkbox[id='selectAll']").prop('checked');
    $("input:checkbox[id!='selectAll']").prop('checked', bool);
    updateStatusToolbar();
};

jQuery.fn.dataTableExt.getSelectedMapsIds = function () {
    var selectedRows = $('#mindmapListTable').dataTableExt.getSelectedRows();
    var dataTable = $('#mindmapListTable').dataTable();
    return  selectedRows.map(function () {
        return dataTable.fnGetData(this).id;
    });
};

jQuery.fn.dataTableExt.getSelectedRows = function (oSettings) {
    return $('.select  input:checked[id!="selectAll"]').parent().parent();
};

jQuery.fn.dataTableExt.removeSelectedRows = function () {
    var trs = this.getSelectedRows();
    trs.each(function () {
        $('#mindmapListTable').dataTable().fnDeleteRow(this);
    });
    updateStatusToolbar();
};


jQuery.fn.dialogForm = function (options) {

    var containerId = this[0].id;
    var url = options.url;

    // Clear previous state ...
    $("#" + containerId).find('.errorMessage').text("").removeClass("alert alert-error");
    $("#" + containerId).find('.control-group').removeClass('error');

    // Clear form values ...
    if (options.clearForm == undefined || options.clearForm) {
        $("#" + containerId).find('input[name!="color"]').val('');
    }

    // Clear button "Saving..." state ...
    var acceptBtn = $('#' + containerId + ' .btn-accept');
    acceptBtn.button('reset');

    acceptBtn.unbind('click').click( function (event) {
        var formData = {};
        $('#' + containerId + ' input').each(function (index, elem) {
            formData[elem.name] = elem.value;
        });

        $(acceptBtn).button('loading');
        var dialogElem = this;
        jQuery.ajax(url, {
            async:false,
            //dataType:'json', comentado momentaneamente, problema con jquery 2.1.0
            data:JSON.stringify(formData),
            type:options.type ? options.type : 'POST',
            contentType:"application/json; charset=utf-8",
            success:function (data, textStatus, jqXHR) {
                var resourceId = jqXHR.getResponseHeader("ResourceId");
                if (options.redirect) {
                    var redirectUrl = options.redirect;
                    redirectUrl = redirectUrl.replace("{header.resourceId}", resourceId);

                    // Hack: IE ignore the base href tag ...
                    var baseUrl = window.location.href.substring(0, window.location.href.lastIndexOf("c/maps/"));
                    window.open(baseUrl + redirectUrl, '_self');

                } else if (options.postUpdate) {
                    options.postUpdate(formData, resourceId);
                }
                dialogElem.modal('hide');
            },
            error:function (jqXHR, textStatus, errorThrown) {
                if (jqXHR.status == 400) {
                    var errors = JSON.parse(jqXHR.responseText);
                    // Mark fields with errors ...
                    var fieldErrors = errors.fieldErrors;
                    if (fieldErrors) {
                        for (var fieldName in fieldErrors) {
                            // Mark the field with errors ...
                            var message = fieldErrors[fieldName];
                            var inputField = $("#" + containerId + " input[name='" + fieldName + "']");

                            $("#" + containerId).find(".errorMessage").text(message).addClass("alert alert-error");
                            inputField.parent().addClass('error');
                        }
                    }

                } else {
                    console.log(errorThrown);
                    console.log(jqXHR);
                    dialogElem.modal('hide');
                    $('#messagesPanel div div').text(errorThrown);
                    $('#messagesPanel').show()
                }
                var acceptBtn = $('#' + containerId + ' .btn-accept');
                acceptBtn.button('reset');

            }
        });
    }.bind(this));

    $('#' + containerId + ' .btn-cancel').click(function () {
        this.modal('hide');
    }.bind(this));

    // Open the modal dialog ...
    this.modal();

};


// Update toolbar events ...
function updateStatusToolbar() {

    // Mark column row selection values ...
    $("#mindmapListTable tbody input:checked").parent().parent().addClass('row-selected');
    $("#mindmapListTable tbody input:not(:checked)").parent().parent().removeClass('row-selected');

    $('.buttonsToolbar').find('.act-single').hide().end().find('.act-multiple').hide();

    var tableElem = $('#mindmapListTable');
    var selectedRows = tableElem.dataTableExt.getSelectedRows();

    if (selectedRows.length > 0) {
        if (selectedRows.length == 1) {
            $('.buttonsToolbar').find('.act-single').show().end().find('.act-multiple').show();

            // Can be executed by the owner ?
            var rowData = tableElem.dataTable().fnGetData(selectedRows[0]);
            if ('owner' == rowData.role) {
                $(".buttonsToolbar").find('#publishBtn').show().end().find('#shareBtn').show().end().find('#renameBtn').show();
            } else {
                $(".buttonsToolbar").find('#publishBtn').hide().end().find('#shareBtn').hide().end().find('#renameBtn').hide();
            }
        } else {
            $(".buttonsToolbar .act-multiple").show();
        }
    }
}

// Update toolbar events ...
function updateStarred(spanElem) {
    $(spanElem).removeClass('starredOff');
    $(spanElem).addClass('starredOn');

    // Retrieve row data ...
    var tableElem = $('#mindmapListTable');
    var trElem = $(spanElem).parent().parent();
    var rowData = tableElem.dataTable().fnGetData(trElem[0]);

    // Update status ...
    var starred = !rowData.starred;
    var mapId = rowData.id;
    if (starred) {
        $(spanElem).removeClass('starredOff');
        $(spanElem).addClass('starredOn');
    } else {
        $(spanElem).removeClass('starredOn');
        $(spanElem).addClass('starredOff');
    }

    jQuery.ajax("c/restful/maps/" + mapId + "/starred", {
        async:false,
        dataType:'json',
        data:"" + starred,
        type:'PUT',
        contentType:"text/plain",
        success:function () {
            if (starred) {
                $(spanElem).removeClass('starredOff');
                $(spanElem).addClass('starredOn');
            } else {
                $(spanElem).removeClass('starredOn');
                $(spanElem).addClass('starredOff');
            }
        },
        error:function (jqXHR, textStatus, errorThrown) {
            $('#messagesPanel div').text(errorThrown).parent().show();
        }
    });

    // Finally update st
    rowData.starred = starred;
}

function callbackOnTableInit() {
    // Register starred events ...
    $('#mindmapListTable .starredOff, #mindmapListTable .starredOn').click(function () {
        updateStarred(this);
    });
    updateStatusToolbar();
}

// Register time update functions ....
setTimeout(function () {
    jQuery("abbr.timeago").timeago()
}, 50000);

/*--------------------------------------------- Button actions --------------------------------------------------**/

$(function () {
    // Creation buttons actions ...
    $("#newBtn").click(
        function () {
            $("#new-dialog-modal").dialogForm({
                redirect:"c/maps/{header.resourceId}/edit",
                url:"c/restful/maps"
            });
        }
    );

    $(document).on('click', '#createLabelBtn',
        function () {
            var mapIds = $('#mindmapListTable').dataTableExt.getSelectedMapsIds();
            var url = mapIds.length == 0
                ? "c/restful/labels"
                : "c/restful/labels/maps?ids=" + jQuery.makeArray(mapIds).join(',');

            $("#new-folder-dialog-modal").dialogForm({
                url: url,
                postUpdate: function(data, id) {
                    createLabelItem(data, id);
                    tagMindmaps(data.id || id, data.title, data.color);
                }
            });
        }
    );

    $("#addLabelButton").click( function () {
        var labels;
        fetchLabels({
            postUpdate: function(data) {
                labels = data.labels;
            }
        });

        if (labels) {
            prepareLabelList(labels);

            $(document).one('click', '.chooseLabel',
                function () {
                    var mapIds = $('#mindmapListTable').dataTableExt.getSelectedMapsIds();
                    if (mapIds.length > 0) {
                        var labelId = $(this).attr('value');
                        var labelName = $(this).text();
                        var labelColor = $(this).attr('color');
                        jQuery.ajax("c/restful/labels/maps?ids=" + jQuery.makeArray(mapIds).join(','), {
                            type:'POST',
                            //dataType: "json",
                            contentType:"application/json; charset=utf-8",
                            data: JSON.stringify({id: labelId}),
                            success: function() {
                                tagMindmaps(labelId, labelName, labelColor);
                            }
                        });
                    }
                }
            );
        }
    });

    $("#duplicateBtn").click(function () {
        // Map to be cloned ...
        var tableElem = $('#mindmapListTable');
        var rows = tableElem.dataTableExt.getSelectedRows();
        if (rows.length > 0) {

            // Obtain map name  ...
            var rowData = tableElem.dataTable().fnGetData(rows[0]);
            $('#dupDialogTitle').text("Duplicate '" + rowData.title + "'");

            // Obtains map id ...
            var mapId = rowData.id;

            // Initialize dialog ...
            $("#duplicate-dialog-modal").dialogForm({
                redirect:"c/maps/{header.resourceId}/edit",
                url:"c/restful/maps/" + mapId
            });
        }
    });

    $("#renameBtn").click(function () {
        // Map to be cloned ...
        var tableElem = $('#mindmapListTable');
        var rows = tableElem.dataTableExt.getSelectedRows();
        if (rows.length > 0) {

            // Obtain map name  ...
            var dataTable = tableElem.dataTable();
            var rowData = dataTable.fnGetData(rows[0]);

            // Fill dialog with default values ...
            var mapId = rowData.id;
            $("#rename-dialog-modal input[name='title']").attr('value', rowData.title);
            $("#rename-dialog-modal input[name='description']").attr('value', rowData.description);

            // Set title ...
            $('#renameDialogTitle').text("Rename '" + rowData.title + "'");

            // Initialize dialog ...
            $("#rename-dialog-modal").dialogForm({
                type:'PUT',
                clearForm:false,
                postUpdate:function (reqBodyData) {
                    tableElem.dataTableExt.removeSelectedRows();

                    rowData.title = reqBodyData.title;
                    rowData.description = reqBodyData.description;
                    dataTable.fnAddData(JSON.parse(JSON.stringify(rowData)));
                },
                url:"c/restful/maps/" + mapId
            });
        }
    });

    $("#deleteBtn").click(function () {
        var tableUI = $('#mindmapListTable');

        var mapIds = tableUI.dataTableExt.getSelectedMapsIds();

        if (mapIds.length > 0) {
            // Initialize dialog ...
            $("#delete-dialog-modal").dialogForm({
                type:'DELETE',
                postUpdate:function () {
                    // Remove old entry ...
                    tableUI.dataTableExt.removeSelectedRows();
                },
                url:"c/restful/maps/batch?ids=" + jQuery.makeArray(mapIds).join(',')
            });
        }
    });

    $("#printBtn").click(function () {
        var mapIds = $('#mindmapListTable').dataTableExt.getSelectedMapsIds();
        if (mapIds.length > 0) {
            // Hack: IE ignore the base href tag ...
            var baseUrl = window.location.href.substring(0, window.location.href.lastIndexOf("c/maps/"));
            window.open(baseUrl + 'c/maps/' + mapIds[0] + '/print');
        }
    });

    $("#infoBtn").click(function () {
        showEmbeddedDialog("c/maps/{mapId}/details", 'info-dialog-modal');
    });

    $("#historyBtn").click(function () {
        showEmbeddedDialog("c/maps/{mapId}/history", 'history-dialog-modal');
    });

    $("#publishBtn").click(function () {
        showEmbeddedDialog("c/maps/{mapId}/publish", "publish-dialog-modal");
    });

    $("#exportBtn").click(function () {
        showEmbeddedDialog("c/maps/{mapId}/export", 'export-dialog-modal');
    });

    $("#importBtn").click(function () {
        showEmbeddedDialog("c/maps/import", 'import-dialog-modal', true);
    });

    $("#shareBtn").click(function () {
        showEmbeddedDialog("c/maps/{mapId}/share", 'share-dialog-modal', true);
    });

    var showEmbeddedDialog = function (urlTemplate, dialogElemId, ignore) {
        var mapIds = $('#mindmapListTable').dataTableExt.getSelectedMapsIds();
        if (mapIds.length > 0 || ignore) {
            var mapId = mapIds[0];
            $('#' + dialogElemId + ' .modal-body').load(urlTemplate.replace("{mapId}", mapId),
                function () {
                    $('#' + dialogElemId + ' .btn-accept').unbind('click').click(function () {
                        submitDialogForm();
                    });
                    $('#' + dialogElemId).modal();
                });
        }
    };

    $(document).on('click', '#foldersContainer li', function (event) {
        // Deselect previous option ...
        $('#foldersContainer li').removeClass('active');
        $('#foldersContainer i').removeClass('icon-white');

        // Select the new item ...
        var dataTable = $('#mindmapListTable').dataTable();
        $(this).addClass('active');
        $('#foldersContainer .active i').addClass('icon-white');

        $('input:checkbox').prop('checked', false);
        // Reload the table data ...
        dataTable.fnReloadAjax("c/restful/maps/?q=" + $(this).attr('data-filter'), callbackOnTableInit, true);
        event.preventDefault();
    });

    $(document).on('click', "#deleteLabelBtn", function() {
        var me = $(this);
        $("#delete-label-dialog-modal").dialogForm({
            url: "c/restful/labels/" + me.attr('labelid'),
            type: 'DELETE',
            postUpdate: function() {
                var dataTable = $('#mindmapListTable').dataTable();
                //remove the selected tag...
                $("#foldersContainer li.active").remove();
                //find the second li... (all)
                $("#foldersContainer li:nth-child(2)").addClass("active");
                dataTable.fnReloadAjax("c/restful/maps/?q=all", callbackOnTableInit, true);
            }
        })
    });

    $(document).on('click', ".closeTag", function() {
        var me = $(this);
        var data = {
            mindmapId: me.parents("td").find("a").attr("value"),
            labelId: me.attr("value")
        };
        jQuery.ajax("c/restful/labels/maps", {
            async:false,
            //dataType:'json', comentado momentaneamente, problema con jquery 2.1.0
            data:JSON.stringify(data),
            type:'DELETE',
            contentType:"application/json; charset=utf-8",
            success: function() {
                me.closest("table").remove();
            }
        });
    });

    $(document).ready(function() {
        // add labels to filter list...
        fetchLabels({
            postUpdate: function(data) {
                var labels = data.labels;
                for (var i = 0; i < labels.length; i++) {
                    createLabelItem(labels[i], null)
                }
            }
        });
    })
});

/*--------------------------------------------- Label actions --------------------------------------------------**/
function createLabelItem(data, id) {
    var labelId = data.id || id;
    $("#foldersContainer").find("ul").append(
        $("<li data-filter=\""  + data.title  + "\">").append(
            "<a href=\"#\"> " +
                "<i class=\"icon-tag labelIcon\"></i>" +
                "<div class='labelColor' style='background: " +  data.color + "'></div>" +
                "<div class='labelName labelNameList'>" + data.title + "</div>" +
                "<button id='deleteLabelBtn' class='close closeLabel' labelid=\""+ labelId +"\">x</button>" +
                "</a>"
        )
    )
}

function labelTagsAsHtml(labels) {
    var result = "";
    for (var i = 0; i<labels.length; i++) {
        var label = labels[i];
        result +=
            "<table class='tableTag'>" +
                "<tbody><tr>" +
                "<td style='cursor: default; background-color:"+ label.color +"'>" +
                "<div class='labelTag' >" +
                label.title +
                '</div>' +
                "</td>" +
                //"<td style='padding: 0; background-color: #d8d4d4'></td>" +
                "<td class='closeTag' style='background-color:" + label.color +"' value='" + label.id + "'    >" +
                "<span style='top: -1px;position: relative;font-size: 11px' title='delete label'>x</span>"+
                "</td>" +
                "</tr></tbody>" +
                "</table>"
    }
    return result;
}

function fetchLabels(options) {
    jQuery.ajax("c/restful/labels/", {
        async:false,
        dataType:'json',
        type:'GET',
        success:function (data) {
            if (options.postUpdate) {
                options.postUpdate(data)
            }
        },
        error:function (jqXHR, textStatus, errorThrown) {
            $('#messagesPanel div').text(errorThrown).parent().show();
        }
    });
}

function tagMindmaps(id, labelName, labelColor) {
    //tag selected mindmaps...
    var rows = $('#mindmapListTable').dataTableExt.getSelectedRows();
    for (var i = 0; i < rows.length; i++) {
        if ($(rows[i]).find(".labelTag:contains('" + labelName + "')").length == 0) {
            $(rows[i]).find('.mindmapName').parent().append(
                labelTagsAsHtml([{
                    id: id,
                    title: labelName,
                    color: labelColor
                }])
            )
        }
    }
}

function prepareLabelList(labels) {
    var labelList = $("#labelList");
    var defaultValue = labelList.find("li[id=\"createLabelBtn\"]");

    //clear dropdown...
    labelList.find('li').remove();

    //append items to dropdown
    $.each(labels, function(index, value) {
        labelList.append(
            $('<li class="chooseLabel"></li>').attr('value', value.id).attr('color', value.color)
                .append(
                    '<a href="#" onclick="return false">' +
                        "<div class='labelColor' style='background: " +  value.color + "'></div>" +
                        "<div class='labelName'>" + value.title + "</div>" +
                        '</a>')
        );
    });

    //add the defaultValue
    labelList.append('<li><div class="listSeparator"></div></li>')
    labelList.append(defaultValue);
}
