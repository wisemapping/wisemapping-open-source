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
    $("#" + containerId).find('.errorMessage').text("").removeClass("alert alert-danger");
    $("#" + containerId).find('.form-group').removeClass('error');

    // Clear form values ...
    if (options.clearForm == undefined || options.clearForm) {
        //FIXME: icon and color should be handled as exceptions..
        $("#" + containerId).find('input[name!="color"]input[name!="iconName"]').val('');
    }

    // Clear button "Saving..." state ...
    var acceptBtn = $('#' + containerId + ' .btn-accept');
    acceptBtn.button('reset');

    var dialogElem = this;
    acceptBtn.unbind('click').one('click', function (event) {
        var formData = {};
        $('#' + containerId + ' input').each(function (index, elem) {
            formData[elem.name] = elem.value;
        });

        // Success actions ...
        var onSuccess = function (jqXHR, textStatus, data) {
            var resourceId = jqXHR ? jqXHR.getResponseHeader("ResourceId") : undefined;
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
        };

        // On error message
        var onFailure = function (jqXHR, textStatus, data) {
            var errors = JSON.parse(jqXHR.responseText);
            // Mark fields with errors ...
            var fieldErrors = errors.fieldErrors;
            if (fieldErrors) {
                for (var fieldName in fieldErrors) {
                    // Mark the field with errors ...
                    var message = fieldErrors[fieldName];
                    var inputField = $("#" + containerId + " input[name='" + fieldName + "']");

                    $("#" + containerId).find(".errorMessage").text(message).addClass("alert alert-danger");
                    inputField.parent().addClass('error');
                }
            }
            var acceptBtn = $('#' + containerId + ' .btn-accept');
            acceptBtn.button('reset');
        };

        var onError = function (jqXHR, textStatus, errorThrown) {
            console.log(errorThrown);
            console.log(jqXHR);
            dialogElem.modal('hide');
            $('#messagesPanel div div').text(errorThrown);
            $('#messagesPanel').show()
            var acceptBtn = $('#' + containerId + ' .btn-accept');
            acceptBtn.button('reset');
        };

        $(acceptBtn).button('loading');
        jQuery.ajax(url, {
            async: false,
            dataType: 'json',
            data: JSON.stringify(formData),
            type: options.type ? options.type : 'POST',
            contentType: "application/json; charset=utf-8",
            statusCode: {
                200: onSuccess,
                201: onSuccess,
                204: onSuccess,
                400: onFailure,
                444: onError,
                500: onError,
                501: onError
            }
        });
    });

    $('#' + containerId + ' .btn-cancel').click(function () {
        $(this).modal('hide');
    });

    // Register enter input to submit...
    $("input").keypress(function(event) {
        if (event.which == 13) {
            event.preventDefault();
            acceptBtn.trigger('click');
        }
    });

    // Open the modal dialog ...
    this.on('shown.bs.modal', function() {
        $(this).find('input:first').focus();
    });
    this.modal();
    this.first('input').focus();

};

// Update toolbar events ...
function updateStatusToolbar() {

    // Mark column row selection values ...
    $("#mindmapListTable tbody input:checked").parent().parent().addClass('row-selected');
    $("#mindmapListTable tbody input:not(:checked)").parent().parent().removeClass('row-selected');

    $('.buttonsToolbar').find('.act-single').fadeOut('slow').end().find('.act-multiple').fadeOut('slow');

    var tableElem = $('#mindmapListTable');
    var selectedRows = tableElem.dataTableExt.getSelectedRows();

    if (selectedRows.length > 0) {
        if (selectedRows.length == 1) {
            $('.buttonsToolbar').find('.act-single').fadeIn('slow').end().find('.act-multiple').fadeIn('slow');

            // Can be executed by the owner ?
            var rowData = tableElem.dataTable().fnGetData(selectedRows[0]);
            if ('owner' == rowData.role) {
                $(".buttonsToolbar").find('#publishBtn').show().end().find('#shareBtn').show().end().find('#renameBtn').show();
            } else {
                $(".buttonsToolbar").find('#publishBtn').hide().end().find('#shareBtn').hide().end().find('#renameBtn').hide();
            }
        } else {
            $(".buttonsToolbar .act-multiple").fadeIn('slow');
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
        async: false,
        dataType: 'json',
        data: "" + starred,
        type: 'PUT',
        contentType: "text/plain",
        success: function () {
            if (starred) {
                $(spanElem).removeClass('starredOff');
                $(spanElem).addClass('starredOn');
            } else {
                $(spanElem).removeClass('starredOn');
                $(spanElem).addClass('starredOff');
            }
        },
        error: function (jqXHR, textStatus, errorThrown) {
            $('#messagesPanel div').text(errorThrown).parent().show();
        }
    });

    // Finally update st
    rowData.starred = starred;
}

function callbackOnTableInit() {
    var clickAction = function() {
        $("#mindmapListTable tbody tr").unbind('click').click(
            function (event) {
                var target = $(event.target);
                if (!target.is('.closeTag')) {
                    if (!target.parent().is('.closeTag')) {
                        var baseUrl = window.location.href.substring(0, window.location.href.lastIndexOf("c/maps/"));
                        window.open(baseUrl + 'c/maps/' + $(this).find('.mindmapName').attr('value') + '/edit', '_self');
                    }
                }
            });
    };
    // Register starred events ...
    $('#mindmapListTable .starredOff, #mindmapListTable .starredOn').click(function (event) {
        updateStarred(this);
        event.stopPropagation();
    });

    clickAction();
    $('input:checkbox').click(function(event) {
        event.stopPropagation();
    });
    updateStatusToolbar();
    $("#mindmapListTable").on("draw", clickAction);
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
                redirect: "c/maps/{header.resourceId}/edit",
                url: "c/restful/maps"
            });
        }
    );

    $(document).on('click', '#createLabelBtn',
        function () {
            var mapIds = $('#mindmapListTable').dataTableExt.getSelectedMapsIds();
            $("#new-folder-dialog-modal").dialogForm({
                url: "c/restful/labels",
                postUpdate: function(data, id) {
                    createLabelItem(data, id);
                    if (mapIds.length > 0) {
                        linkLabelToMindmap(mapIds, {id: id, title: data.title, color: data.color, icon: data.icon});
                    }
                }
            });
            // Setting sizes to label icon list
            var dropDownHeight = $(window).height()/3;
            $("#labelIconItems ul").height(dropDownHeight);
            var dropDownWidth = $(window).width()/3;
            $("#labelIconItems ul").width(dropDownWidth);
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
            $('.chooseLabel').one('click' ,
                function () {
                    var mapIds = $('#mindmapListTable').dataTableExt.getSelectedMapsIds();
                    if (mapIds.length > 0) {
                        var labelId = $(this).attr('value');
                        var labelName = $(this).text();
                        var labelColor = $(this).attr('color');
                        linkLabelToMindmap(mapIds, {id: labelId, title: labelName, color: labelColor});
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
                redirect: "c/maps/{header.resourceId}/edit",
                url: "c/restful/maps/" + mapId
            });
        }
    });

    $("#renameBtn").click(function (event) {
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
                type: 'PUT',
                clearForm: false,
                postUpdate: function (reqBodyData) {
                    tableElem.dataTableExt.removeSelectedRows();

                    rowData.title = reqBodyData.title;
                    rowData.description = reqBodyData.description;
                    dataTable.fnAddData(JSON.parse(JSON.stringify(rowData)));
                },
                url: "c/restful/maps/" + mapId
            });
        }
    });

    $("#deleteBtn").click(function () {
        var tableUI = $('#mindmapListTable');

        var mapIds = tableUI.dataTableExt.getSelectedMapsIds();

        if (mapIds.length > 0) {
            // Initialize dialog ...
            $("#delete-dialog-modal").dialogForm({
                type: 'DELETE',
                postUpdate: function () {
                    // Remove old entry ...
                    tableUI.dataTableExt.removeSelectedRows();
                },
                url: "c/restful/maps/batch?ids=" + jQuery.makeArray(mapIds).join(',')
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
            var modalElement = $('#' + dialogElemId);
            modalElement.find('.modal-body').load(urlTemplate.replace("{mapId}", mapId),
                function () {
                    modalElement.find('.btn-accept').unbind('click').click(function () {
                        submitDialogForm();
                    });
                    modalElement.modal();
                    //TODO here we need to make focus on a input tag
                });
        }
    };

    $(document).on('click', '#foldersContainer li', function (event) {
        if (!$(this).is($('#foldersContainer .active'))) {
            $('#foldersContainer .active').animate({left: '-=8px'}, 'fast');
        }

        // Deselect previous option ...
        $('#foldersContainer li').removeClass('active');
        $('#foldersContainer i').removeClass('glyphicon-white');

        // Select the new item ...
        var dataTable = $('#mindmapListTable').dataTable();
        $(this).addClass('active');
        $('#foldersContainer .active i').addClass('glyphicon-white');

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
                $("#foldersContainer li:first").addClass("active");
                $('#foldersContainer .active i').addClass('icon-white');
                $("#foldersContainer li:first").animate({left: '+=8px'}, 'fast');
                dataTable.fnReloadAjax("c/restful/maps/?q=all", callbackOnTableInit, true);
            }
        })
    });

    $(document).on('click', ".closeTag", function(event) {
        var me = $(this);
        var mindmapId = me.parents("td").find(".mindmapName").attr("value");
        var data = {
            id: me.attr("value"),
            title: me.attr("name"),
            color: me.css('background-color')
        };
        jQuery.ajax("c/restful/labels/maps/" + mindmapId, {
            async:false,
            dataType:'json',
            data:JSON.stringify(data),
            type:'DELETE',
            contentType:"application/json; charset=utf-8",
            success: function() {
                var tag = me.closest("table");
                $(tag).fadeOut('fast', function () {
                    $(this).remove();
                });

            }
        });
    });

    $(document).ready(function() {
        // add labels to filter list...
        $("#foldersContainer li").fadeIn('fast');
        fetchLabels({
            postUpdate: function(data) {
                var labels = data.labels;
                for (var i = 0; i < labels.length; i++) {
                    createLabelItem(labels[i], null)
                }
            }
        });

        //setting max heigth to ul filters...
        var maxHeight = $("#map-table").height() - 20;
         $("#foldersContainer ul").css('overflow-y', 'scrollbar');
         $("#foldersContainer ul").css('overflow-x', 'hidden');
         $("#foldersContainer ul").height(maxHeight);

    });

    //init popovers...
    var icons = $(".bs-glyphicons-list li");
    icons.each(function() {
        $(this).popover({
            animation: true,
            placement: "auto",
            trigger: 'hover',
            //FIXME: Which is the best way to use messages.properties here?
            content: ($(this).attr('class').replace('glyphicon glyphicon-',''))
        })
    });

    icons.on("click", function(){
        var defaultIcon = $("#defaultIcon");
        //remove current icon
        defaultIcon.find("i").remove();
        var myClass = $(this).attr("class");
        defaultIcon.prepend("<i class='" + myClass +"'></i>");
        defaultIcon.closest("#iconGroup").find('input').val(myClass);
    });
});

/*--------------------------------------------- Label actions --------------------------------------------------**/
function createLabelItem(data, id) {
    var labelId = data.id || id;
    var labelItem = $("<li data-filter=\""  + data.title  + "\">");
    labelItem.append(
        "<a href=\"#\"> " +
            "<i class=\"" + data.iconName + " labelIcon\"></i>" +
            "<div class='labelColor' style='background: " +  data.color + "'></div>" +
            "<div class='labelName labelNameList'>" + data.title + "</div>" +
            "<button id='deleteLabelBtn' class='close closeLabel' labelid=\""+ labelId +"\">x</button>" +
        "</a>"
    );
    labelItem.hide().appendTo($("#foldersContainer").find("ul"));
    labelItem.fadeIn('fast');
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
                "<td class='closeTag' style='background-color:" + label.color +"' name='" + label.title +"'value='" + label.id + "'    >" +
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

function tagMindmaps(label) {
    //tag selected mindmaps...
    var rows = $('#mindmapListTable').dataTableExt.getSelectedRows();
    for (var i = 0; i < rows.length; i++) {
        var row = $(rows[i]);
        if (row.find(".labelTag:contains('" + label.title + "')").length == 0) {
            var tag = $(labelTagsAsHtml([label]));
            tag.hide().appendTo(row.find('.mindmapName').parent());
            tag.fadeIn('fast');
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
            $('<li class="chooseLabel"></li>').attr('value', value.id).attr('color', value.color).attr('icon', value.icon)
                .append(
                    '<a href="#" onclick="return false">' +
                        "<div class='labelIcon " + value.iconName + "'></div>" +
                        "<div class='labelColor' style='background: " +  value.color + "'></div>" +
                        "<div class='labelName'>" + value.title + "</div>" +
                        '</a>')
        );
    });

    //add the defaultValue
    if (labels.length > 0) {
        labelList.append('<li><div class="listSeparator"></div></li>')
    }
    labelList.append(defaultValue);
}

function linkLabelToMindmap(mapIds, label) {
    var onSuccess = function () {
        tagMindmaps(label);
    };
    jQuery.ajax("c/restful/labels/maps?ids=" + jQuery.makeArray(mapIds).join(','), {
        type: 'POST',
        dataType: "json",
        contentType: "application/json; charset=utf-8",
        data: JSON.stringify({
            id: label.id,
            title: label.title,
            color: label.color
        }),
        statusCode: {
            200: onSuccess
        }
    });
}

//animations...
$(document).on('click', '#foldersContainer li[class!="nav-header"]', function (event) {
    if ($(this).attr('class') != 'active') {
        $(this).animate({left: '+=8px'}, 'fast');
    }
});

