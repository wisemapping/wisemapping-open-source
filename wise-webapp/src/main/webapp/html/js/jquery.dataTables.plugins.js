jQuery.fn.dataTableExt.oSort['es_date-asc']  = function(a,b) {
    var esDatea = a.split('/');
    var esDateb = b.split('/');

    var x = (esDatea[2] + esDatea[1] + esDatea[0]) * 1;
    var y = (esDateb[2] + esDateb[1] + esDateb[0]) * 1;

    return ((x < y) ? -1 : ((x > y) ?  1 : 0));
};

jQuery.fn.dataTableExt.oSort['es_date-desc'] = function(a,b) {
    var esDatea = a.split('/');
    var esDateb = b.split('/');

    var x = (esDatea[2] + esDatea[1] + esDatea[0]) * 1;
    var y = (esDateb[2] + esDateb[1] + esDateb[0]) * 1;

    return ((x < y) ? 1 : ((x > y) ?  -1 : 0));
};

jQuery.fn.dataTableExt.selectAllMaps = function() {
    var total = $('.select input:checkbox[id!="selectAll"]').size();
    var selected = $('.select input:checked[id!="selectAll"]').size();
    if (selected < total) {
        $('.select input:!checked[id!="selectAll"]').each(function(index) {
            $(this).prop("checked", true);
        });
    }
    else {
        $('.select input:!checked[id!="selectAll"]').each(function(index) {
            $(this).prop("checked", false);
        });
    }
}

jQuery.fn.dataTableExt.getSelectedMaps = function() {
    var ids = [];
    $('.select input:checked[id!="selectAll"]').each(function(index) {
        ids.push($(this).attr("id"));
    });

    return ids;
}
