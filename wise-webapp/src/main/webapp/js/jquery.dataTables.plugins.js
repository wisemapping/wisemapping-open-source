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
};

jQuery.fn.dataTableExt.getSelectedMapsIds = function() {
    var ids = [];
    $('.select input:checked[id!="selectAll"]').each(function() {
        var id = $(this).attr("id");
        ids.push(id);
    });

    return ids;
};

jQuery.fn.dataTableExt.removeSelectedRows = function() {
    var mapIds = this.getSelectedMapsIds();
    jQuery.ajax({
        async:false,
        url: "../service/maps/batch?ids=" + mapIds.join(","),
        type:"DELETE",
        success : function(data, textStatus, jqXHR) {
            console.log("delete success");
            var trs = $('.select  input:checked[id!="selectAll"]').parent().parent();
            trs.each(function() {
                $('#mindmapListTable').dataTable().fnDeleteRow(this);
            });
        },
        error: function(){
            alert("Unexpected error removing maps. Refresh before continue.");
        }
    });


};
