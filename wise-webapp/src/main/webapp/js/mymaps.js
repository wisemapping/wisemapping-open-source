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
    updateStatus();
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



