function createLabelItem(data, id) {
    var labelId = data.id || id;
    $("#foldersContainer").find("ul").append(
        $("<li data-filter=\""  + data.title  + "\">").append(
            "<a href=\"#\"> " +
                "<i class=\"icon-tag\"></i>" +
                "<div class='labelColor' style='background: " +  data.color + "'></div>" +
                "<div class='labelName'>" + data.title + "</div>" +
                "<button id='deleteLabelBtn' class='close closeLabel' labelid=\""+ labelId +"\">x</button>" +
            "</a>"
        )
    )
}

function labelTagsAsHtml(labels) {
    var result = "";
    for (var i = 0; i<labels.length; i++) {
        var label = labels[i];
        result+= "<div class='labelTag' style='background-color:" + label.color + "'>"+ label.title + "</div>"
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
