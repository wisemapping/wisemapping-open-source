$(function () {
    // Creation buttons actions ...
    $("#newMapBtn").click(
        function () {
            $("#new-dialog-modal").dialogForm({
                redirect:"c/maps/{header.resourceId}/edit",
                url:"c/restful/maps"
            });
        }
    );

    $("#newFolderBtn").click(
        function () {
            $("#new-folder-dialog-modal").dialogForm({
                url:"c/restful/labels",
                postUpdate: createLabelItem
            });
        }
    );

    $("#linkBtn").click( function () {
        var labels;
        fetchLabels({
            postUpdate: function(data) {
                labels = data.labels;
            }
        });

        if (labels) {
            var labelList = $("#labelId");

            //clear dropdown...
            labelList.find("option").remove();

            if (labels.length == 0) {
                window.alert('no hay labels, como resolvemos esto?');
                return;
            }
            //append items to dropdown
            $.each(labels, function(index, value) {
                labelList.append($('<option></option>').val(value.id).html(value.title).attr('color', value.color));
            });

            var mapIds = $('#mindmapListTable').dataTableExt.getSelectedMapsIds();

            $("#add-label-dialog-modal").dialogForm({
                type:'PUT',
                url:"c/restful/labels/maps?ids=" + jQuery.makeArray(mapIds).join(','),
                postUpdate: function() {
                    //tag selected mindmaps...
                    var rows = $('#mindmapListTable').dataTableExt.getSelectedRows();
                    for (var i = 0; i < rows.length; i++) {
                        $(rows[i]).find('.mindmapName').append(
                            labelTagsAsHtml([{
                                title: $(':selected', labelList).text(),
                                color: $(':selected', labelList).attr('color')
                            }])
                        )
                    }
                }
            });
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

    //live method is deprecated?
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

    $(document).ready(fetchLabels({
        postUpdate: function(data) {
            var labels = data.labels;
            for (var i = 0; i < labels.length; i++) {
                createLabelItem(labels[i])
            }
        }
    }))
});
