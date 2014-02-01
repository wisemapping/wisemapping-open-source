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
            $("#new-folder-dialog-modal").dialogForm({
                url:"c/restful/labels",
                postUpdate: createLabelItem
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
                    $('<li></li>')
                        .append('<a href="#" onclick="return false">' +
                                    '<i class="icon-tag"></i>' +
                                    '<span style="margin-left: 5px">'+ value.title +
                                    '</span>' +
                                '</a>'));
            });

            //add the defaultValue
            labelList.append('<li><div style="height: 1px; background-color: #d5d3d4"></div></li>')
            labelList.append(defaultValue);

            var mapIds = $('#mindmapListTable').dataTableExt.getSelectedMapsIds();

            $("#add-label-dialog-modal").dialogForm({
                type:'PUT',
                url:"c/restful/labels/maps?ids=" + jQuery.makeArray(mapIds).join(','),
                postUpdate: function() {
                    //tag selected mindmaps...
                    var rows = $('#mindmapListTable').dataTableExt.getSelectedRows();
                    for (var i = 0; i < rows.length; i++) {
                        var labelName = $(':selected', labelList).text();
                        if ($(rows[i]).find('\'.labelTag:contains("' + labelName + '")\'').length == 0) {
                            $(rows[i]).find('.mindmapName').append(
                                labelTagsAsHtml([{
                                    title: labelName,
                                    color: $(':selected', labelList).attr('color')
                                }])
                            )
                        }
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

function reloadTable() {
    // Reload the table data ...
    var dataTable = $('#mindmapListTable').dataTable();
    dataTable.fnReloadAjax("c/restful/maps/?q=" + $(this).attr('data-filter'), callbackOnTableInit, true);
    event.preventDefault();
}
