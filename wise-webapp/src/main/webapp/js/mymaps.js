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
    updateToolbar();
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
    var mapIds = this.getSelectedMapsIds();
    var trs = this.getSelectedRows();
    jQuery.ajax({
        async:false,
        url: "../service/maps/batch?ids=" + mapIds.join(","),
        type:"DELETE",
        success : function(data, textStatus, jqXHR) {
            trs.each(function() {
                $('#mindmapListTable').dataTable().fnDeleteRow(this);
            });
        },
        error: function() {
            alert("Unexpected error removing maps. Refresh before continue.");
        }
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
            },
            error: function(jqXHR, textStatus, errorThrown) {
                if (jqXHR.status == 400) {
                    var errors = JSON.parse(jqXHR.responseText);
                    // Clean previous marks ....
                    $("#" + containerId + ' input').each(function(index, elem) {
                        $(elem).removeClass("ui-state-error");
                    });

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
    this.modal(options);

};


// Update toolbar events ...
function updateToolbar() {

    $("#mindmapListTable tbody input:checked").parent().parent().addClass('row-selected');
    $("#mindmapListTable tbody input:not(:checked)").parent().parent().removeClass('row-selected');

    var inputs = $("#mindmapListTable tbody input:checked");
    var selected = inputs.length > 0;
    if (selected) {
        $("#actionsBtn").show();
        $("#deleteBtn").show();
        $("#infoBtn").show();
    } else {
        $("#actionsBtn").hide();
        $("#deleteBtn").hide();
        $("#infoBtn").hide();


    }
}



