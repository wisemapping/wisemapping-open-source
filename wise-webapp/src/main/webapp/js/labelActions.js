function createLabelItem(data, id) {
    var labelId = data.id || id;
    $("#foldersContainer").find("ul").append(
        $("<li data-filter=\""  + data.title  + "\">").append(
            "<a href=\"#\"> " +
                "<i class=\"icon-tag labelIcon\"></i>" +
                "<div class='labelColor' style='background: " +  data.color + "'></div>" +
                "<div class='labelName labelNameList'>" + data.title + "</div>" +
                "<button id='deleteLabelBtn' class='close closeLabel' labelid=\""+ labelId +"\">x</button>" +
            "</a>"
        )
    )
}

function labelTagsAsHtml(labels) {
    var result = "";
    for (var i = 0; i<labels.length; i++) {
        var label = labels[i];
        result +=
            "<table class='tableTag'>" +
                "<tbody><tr>" +
                    "<td style='cursor: default; background-color:"+ label.color +"'>" +
                        "<div class='labelTag' >" +
                            label.title +
                        '</div>' +
                    "</td>" +
                    //"<td style='padding: 0; background-color: #d8d4d4'></td>" +
                    "<td class='closeTag' style='background-color:"+ label.color +"'>" +
                        "<span style='top: -1px;position: relative;font-size: 11px' title='remove'>x</span>"+
                    "</td>" +
                "</tr></tbody>" +
            "</table>"
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

function tagMindmaps(labelName, labelColor) {
    //tag selected mindmaps...
    var rows = $('#mindmapListTable').dataTableExt.getSelectedRows();
    for (var i = 0; i < rows.length; i++) {
        if ($(rows[i]).find(".labelTag:contains('" + labelName + "')").length == 0) {
            $(rows[i]).find('.mindmapName').parent().append(
                labelTagsAsHtml([{
                    title: labelName,
                    color: labelColor
                }])
            )
        }
    }
}
