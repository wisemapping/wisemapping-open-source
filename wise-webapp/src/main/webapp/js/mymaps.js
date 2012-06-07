jQuery.fn.dataTableExt.oSort['es_date-asc'] = function(a, b) {
    var esDatea = a.split('/');
    var esDateb = b.split('/');

    var x = (esDatea[2] + esDatea[1] + esDatea[0]) * 1;
    var y = (esDateb[2] + esDateb[1] + esDateb[0]) * 1;

    return ((x < y) ? -1 : ((x > y) ? 1 : 0));
};

jQuery.fn.dataTableExt.oSort['es_date-desc'] = function(a, b) {
    var esDatea = a.split('/');
    var esDateb = b.split('/');

    var x = (esDatea[2] + esDatea[1] + esDatea[0]) * 1;
    var y = (esDateb[2] + esDateb[1] + esDateb[0]) * 1;

    return ((x < y) ? 1 : ((x > y) ? -1 : 0));
};

$.fn.dataTableExt.oApi.fnReloadAjax = function (oSettings, sNewSource, fnCallback, bStandingRedraw) {
    if (typeof sNewSource != 'undefined' && sNewSource != null) {
        oSettings.sAjaxSource = sNewSource;
    }
    this.oApi._fnProcessingDisplay(oSettings, true);
    var that = this;
    var iStart = oSettings._iDisplayStart;
    var aData = [];

    this.oApi._fnServerParams(oSettings, aData);

    oSettings.fnServerData(oSettings.sAjaxSource, aData, function(json) {
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

jQuery.fn.dataTableExt.selectAllMaps = function() {
    var total = $('.select input:checkbox[id!="selectAll"]').size();
    var selected = $('.select input:checked[id!="selectAll"]').size();
    if (selected < total) {
        $('.select input:!checked[id!="selectAll"]').each(function() {
            $(this).prop("checked", true);
        });
    }
    else {
        $('.select input:!checked[id!="selectAll"]').each(function() {
            $(this).prop("checked", false);
        });
    }
    updateStatusToolbar();
};

jQuery.fn.dataTableExt.getSelectedMapsIds = function() {
    var selectedRows = $('#mindmapListTable').dataTableExt.getSelectedRows();
    var dataTable = $('#mindmapListTable').dataTable();
    return  selectedRows.map(function() {
        return dataTable.fnGetData(this).id;
    });
};

jQuery.fn.dataTableExt.getSelectedRows = function(oSettings) {
    return $('.select  input:checked[id!="selectAll"]').parent().parent();
};

jQuery.fn.dataTableExt.removeSelectedRows = function() {
    var trs = this.getSelectedRows();
    trs.each(function() {
        $('#mindmapListTable').dataTable().fnDeleteRow(this);
    });
    updateStatusToolbar();
};


jQuery.fn.dialogForm = function(options) {

    var containerId = this[0].id;
    var url = options.url;

    // Clear previous state ...
    $("#" + containerId).find('.errorMessage').text("").removeClass("alert alert-error");
    $("#" + containerId).find('.control-group').removeClass('error');

    // Clear form values ...
    if (options.clearForm == undefined || options.clearForm) {
        $("#" + containerId).find('input').attr('value', '');
    }

    // Clear button "Saving..." state ...
    var acceptBtn = $('#' + containerId + ' .btn-accept');
    acceptBtn.button('reset');

    acceptBtn.click(function() {
        var formData = {};
        $('#' + containerId + ' input').each(function(index, elem) {
            formData[elem.name] = elem.value;
        });
        $(acceptBtn).button('loading');
        var dialogElem = this;
        jQuery.ajax(url, {
            async:false,
            dataType: 'json',
            data: JSON.stringify(formData),
            type: options.type ? options.type : 'POST',
            contentType:"application/json; charset=utf-8",
            success : function(data, textStatus, jqXHR) {
                if (options.redirect) {
                    var resourceId = jqXHR.getResponseHeader("ResourceId");
                    var redirectUrl = options.redirect;
                    redirectUrl = redirectUrl.replace("{header.resourceId}", resourceId);
                    window.location = redirectUrl;

                } else if (options.postUpdate) {
                    options.postUpdate(formData);
                }
                dialogElem.modal('hide');
            },
            error: function(jqXHR, textStatus, errorThrown) {
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
                    $('#messagesPanel div').text(errorThrown).parent().show();
                }
                var acceptBtn = $('#' + containerId + ' .btn-accept');
                acceptBtn.button('reset');

            }
        });
    }.bind(this));

    $('#' + containerId + ' .btn-cancel').click(function() {
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

    $('#buttonsToolbar').find('.act-single').hide().end().find('.act-multiple').hide();

    var tableElem = $('#mindmapListTable');
    var selectedRows = tableElem.dataTableExt.getSelectedRows();

    if (selectedRows.length > 0) {
        if (selectedRows.length == 1) {
            $('#buttonsToolbar').find('.act-single').show().end().find('.act-multiple').show();

            // Can be executed by the owner ?
            var rowData = tableElem.dataTable().fnGetData(selectedRows[0]);
            if (rowData.ownerEmail != principalEmail) {
                $("#buttonsToolbar").find('#publishBtn').hide().end().find('#shareBtn').hide();
            }
        } else {
            $("#buttonsToolbar .act-multiple").show();
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

    jQuery.ajax("service/maps/" + mapId + "/starred", {
        async:false,
        dataType: 'json',
        data: "" + starred,
        type: 'PUT',
        contentType:"text/plain",
        success : function() {
            if (starred) {
                $(spanElem).removeClass('starredOff');
                $(spanElem).addClass('starredOn');
            } else {
                $(spanElem).removeClass('starredOn');
                $(spanElem).addClass('starredOff');
            }
        },
        error: function(jqXHR, textStatus, errorThrown) {
            $('#messagesPanel div').text(errorThrown).parent().show();
        }
    });

    // Finally update st
    rowData.starred = starred;
}

function callbackOnTableInit() {
    // Register starred events ...
    $('#mindmapListTable .starredOff, #mindmapListTable .starredOn').click(function() {
        updateStarred(this);
    });
    updateStatusToolbar();
}

$(function() {
    $('#mindmapListTable').dataTable({
        bProcessing : true,
        sAjaxSource : "../service/maps/",
        sAjaxDataProp: 'mindmapsInfo',
        fnInitComplete: function() {
            $('#mindmapListTable tbody').change(updateStatusToolbar);
            callbackOnTableInit();
        },
        aoColumns: [
            {
                sTitle : '<input type="checkbox" id="selectAll"/>',
                sWidth : "55px",
                sClass : "select",
                bSortable : false,
                bSearchable : false,
                mDataProp: "starred",
                bUseRendered : false,
                fnRender : function(obj) {
                    return '<input type="checkbox"/><span class="' + (obj.aData.starred ? 'starredOn' : 'starredOff') + '"></span>';
                }
            },
            {
                sTitle : "Name",
                sWidth:"270px",
                bUseRendered : false,
                mDataProp: "title",
                fnRender : function(obj) {
                    return '<a href="c/maps/' + obj.aData.id + '/edit">' + obj.aData.title + '</a>';
                }
            },
            {
                sTitle : "Owner",
                mDataProp :"owner"
            },
            {
                bSearchable : false,
                sTitle : "Last Modified",
                bUseRendered: false,
                sType: "date",
                mDataProp: "lastModificationTime",
                fnRender : function(obj) {
                    var time = obj.aData.lastModificationTime;
                    return '<abbr class="timeago" title="' + time + '">' + jQuery.timeago(time) + '</abbr>' + ' ' + '<span style="color: #777;font-size: 75%;padding-left: 5px;">' + obj.aData.lastModifierUser + '</span>';
                }
            }
        ],
        bAutoWidth : false,
        oLanguage : {
            "sSearch" : "",
            "sInfo" : "_START_-_END_ of _TOTAL_",
            "sEmptyTable": "No mindmap available for the selected filter criteria."
        },
        bStateSave:true
    });

    // Customize search action ...
    $('#mindmapListTable_filter').appendTo("#tableActions");
    $('#mindmapListTable_filter input').addClass('input-medium search-query');
    $('#mindmapListTable_filter input').attr('placeholder', 'Search');
    $("#mindmapListTable_info").appendTo("#pageInfo");

    // Re-arrange pagination actions ...
    $("#tableFooter").appendTo("#mindmapListTable_wrapper");
    $("#mindmapListTable_length").appendTo("#tableFooter");
    $('#mindmapListTable_length select').addClass('span1');


    $('input:checkbox[id="selectAll"]').click(function() {
        $("#mindmapListTable").dataTableExt.selectAllMaps();
    });

    // Hack for changing the pagination buttons ...
    $('#nPageBtn').click(function() {
        $('#mindmapListTable_next').click();
    });
    $('#pPageBtn').click(function() {
        $('#mindmapListTable_previous').click();
    });
});

$(function() {
    // Creation buttons actions ...
    $("#newBtn").click(
        function() {
            $("#new-dialog-modal").dialogForm({
                redirect: "c/maps/{header.resourceId}/edit",
                url :  "../service/maps"
            });
        });

    $("#duplicateBtn").click(function() {
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
                url :  "../service/maps/" + mapId
            });
        }
    });

    $("#renameBtn").click(function() {
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
                postUpdate: function(reqBodyData) {
                    tableElem.dataTableExt.removeSelectedRows();

                    rowData.title = reqBodyData.title;
                    rowData.description = reqBodyData.description;
                    dataTable.fnAddData(JSON.parse(JSON.stringify(rowData)));
                },
                url :  "../service/maps/" + mapId
            });
        }
    });

    $("#deleteBtn").click(function() {
        var tableUI = $('#mindmapListTable');

        var mapIds = tableUI.dataTableExt.getSelectedMapsIds();

        if (mapIds.length > 0) {
            // Initialize dialog ...
            $("#delete-dialog-modal").dialogForm({
                type: 'DELETE',
                postUpdate: function() {
                    // Remove old entry ...
                    tableUI.dataTableExt.removeSelectedRows();
                },
                url :  "../service/maps/batch?ids=" + jQuery.makeArray(mapIds).join(',')
            });
        }
    });

    $("#printBtn").click(function() {
        var mapIds = $('#mindmapListTable').dataTableExt.getSelectedMapsIds();
        if (mapIds.length > 0) {
            window.open('c/maps/' + mapIds[0] + '/print');
        }
    });

    $("#infoBtn").click(function() {
        showEmbeddedDialog("c/maps/{mapId}/details", 'info-dialog-modal');
    });

    $("#publishBtn").click(function() {
        showEmbeddedDialog("c/maps/{mapId}/publish", "publish-dialog-modal");
    });

    $("#exportBtn").click(function() {
        showEmbeddedDialog("c/maps/{mapId}/export", 'export-dialog-modal');
    });

    $("#importBtn").click(function() {
        showEmbeddedDialog("c/maps/import", 'import-dialog-modal', true);
    });

    $("#shareBtn").click(function() {
        showEmbeddedDialog("c/maps/{mapId}/share", 'share-dialog-modal', true);
    });


    var showEmbeddedDialog = function(urlTemplate, dialogElemId, ignore) {
        var mapIds = $('#mindmapListTable').dataTableExt.getSelectedMapsIds();
        if (mapIds.length > 0 || ignore) {
            var mapId = mapIds[0];
            $('#' + dialogElemId + ' .modal-body').load(urlTemplate.replace("{mapId}", mapId),
                function() {
                    $('#' + dialogElemId + ' .btn-accept').click(function() {
                        submitDialogForm();
                    });
                    $('#' + dialogElemId).modal();
                });
        }
    };


    $("#actionButtons .shareMap").click(function() {
    });


    $('#foldersContainer li').click(function(event) {
        // Deselect previous option ...
        $('#foldersContainer li').removeClass('active');
        $('#foldersContainer i').removeClass('icon-white');

        // Select the new item ...
        var dataTable = $('#mindmapListTable').dataTable();
        $(this).addClass('active');
        $('#foldersContainer .active i').addClass('icon-white');

        // Reload the table data ...
        dataTable.fnReloadAjax("../service/maps/?q=" + $(this).attr('data-filter'), callbackOnTableInit, true);
        event.preventDefault();
    });
});

// Register time update functions ....
setTimeout(function() {
    jQuery("abbr.timeago").timeago()
}, 50000);



