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
    updateStatus();
};

jQuery.fn.dataTableExt.getSelectedMapsIds = function() {
    var ids = [];
    $('.select input:checked[id!="selectAll"]').each(function() {
        var id = $(this).attr("id");
        ids.push(id);
    });

    return ids;
};

jQuery.fn.dataTableExt.getSelectedRows = function() {
    return $('.select  input:checked[id!="selectAll"]').parent().parent();
};

jQuery.fn.dataTableExt.removeSelectedRows = function() {
    var trs = this.getSelectedRows();
    trs.each(function() {
        $('#mindmapListTable').dataTable().fnDeleteRow(this);
    });
};


jQuery.fn.dialogForm = function(options) {

    var containerId = this[0].id;
    var url = options.url;

    // Clear previous state ...
    $("#" + containerId + " .errorMessage").text("").removeClass("alert alert-error");
    $("#" + containerId + " .control-group").removeClass('error');
    $("#" + containerId + " input").attr('value', '');


    var acceptBtn = $('#' + containerId + ' .btn-accept');
    acceptBtn.click(function() {
        var formData = {};
        $('#' + containerId + ' input').each(function(index, elem) {
            formData[elem.name] = elem.value;
        });

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
                    $(acceptBtn).button('loading');
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
                            // Mark the field ...
                            var message = fieldErrors[fieldName];
                            var inputField = $("#" + containerId + " input[name='" + fieldName + "']");

                            $("#" + containerId + " .errorMessage").text(message).addClass("alert alert-error");
                            inputField.parent().addClass('error');
                        }

                    }

                } else {
                    alert("Unexpected error removing maps. Refresh before continue.");
                }

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
function updateStatus() {

    // Mark column row selection values ...
    $("#mindmapListTable tbody input:checked").parent().parent().addClass('row-selected');
    $("#mindmapListTable tbody input:not(:checked)").parent().parent().removeClass('row-selected');

    $("#buttonsToolbar .act-multiple").hide();
    $("#buttonsToolbar .act-single").hide();

    var tableElem = $('#mindmapListTable');
    var selectedRows = tableElem.dataTableExt.getSelectedRows();

    if (selectedRows.length > 0) {
        if (selectedRows.length == 1) {
            $("#buttonsToolbar .act-single").show();
            $("#buttonsToolbar .act-multiple").show();

            // Can be executed by the owner ?
            var rowData = tableElem.dataTable().fnGetData(selectedRows[0]);
            if (rowData.ownerEmail != principalEmail) {
                $("#buttonsToolbar #publishBtn").hide();
                $("#buttonsToolbar #shareBtn").hide();
            }
        } else {
            $("#buttonsToolbar .act-multiple").show();
        }
    }
}



