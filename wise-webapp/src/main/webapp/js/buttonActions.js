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
                    tagMindmaps(data.title, data.color);
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
            var labelList = $("#labelList");

            var defaultValue = labelList.find("li[id=\"createLabelBtn\"]");
            //clear dropdown...
            labelList.find('li').remove();
            //append items to dropdown
            $.each(labels, function(index, value) {
                labelList.append(
                    //aca jay codigo repetido
                    $('<li class="chooseLabel"></li>').attr('value', value.id).attr('color', value.color)
                        .append('<a href="#" onclick="return false">' +
                            "<div class='labelColor' style='background: " +  value.color + "'></div>" +
                            "<div class='labelName'>" + value.title + "</div>" +

                            '</a>'));
            });

            //add the defaultValue
            labelList.append('<li><div class="listSeparator"></div></li>')
            labelList.append(defaultValue);

            var mapIds = $('#mindmapListTable').dataTableExt.getSelectedMapsIds();

            $(document).one('click', '.chooseLabel',
                function () {
                    var labelId = $(this).attr('value');
                    var labelName = $(this).text();
                    var labelColor = $(this).attr('color');
                    if (mapIds.length > 0) {
                        jQuery.ajax("c/restful/labels/maps?ids=" + jQuery.makeArray(mapIds).join(','), {
                            type:'POST',
                            dataType: "json",
                            contentType:"application/json; charset=utf-8",
                            data: JSON.stringify({id: labelId}),
                            success: function() {
                                tagMindmaps(labelName, labelColor);
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

        // Reload the table data ...
        dataTable.fnReloadAjax("c/restful/maps/?q=" + $(this).attr('data-filter'), callbackOnTableInit, true);
        event.preventDefault();
    });

    $("#parentLblCheckbox").click(
        function () {
            if ($(this).is(":checked")) {
                $("#dropdownLabel").prop("disabled", false);
            } else {
                $("#dropdownLabel").prop("disabled", true);
            }
        }
    );

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
                event.preventDefault();

            }
        })
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
