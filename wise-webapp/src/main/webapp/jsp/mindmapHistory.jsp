<%@page pageEncoding="UTF-8" %>
<%@include file="/jsp/init.jsp" %>

<style type="text/css">
    #historyContainer {
        overflow-y: scroll;
        max-height: 400px;
    }

    #historyContainer table {
        font-size: 100%;
    }
</style>


<div id="historyContainer">
    <table class="table table-condensed" id="historyTable">
        <colgroup>
            <col width="40%"/>
            <col width="40%"/>
            <col width="10%"/>
            <col width="10%"/>
        </colgroup>
    </table>
</div>

<script type="text/javascript">
    var tableElem = $('#historyTable');
    jQuery.ajax("c/restful/maps/${mindmapId}/history", {
        async:false,
        dataType:'json',
        type:'GET',
        contentType:"text/plain",
        success:function (data, textStatus, jqXHR) {
            if (data.changes.length > 0) {
                $(data.changes).each(function () {
                    tableElem.append('\
                  <tr data-history-id="' + this.id + '">\
                   <td>' + this.creator + '</td>\
                   <td><abbr class="timeago" title="' + this.creationTime + '">' + jQuery.timeago(this.creationTime) + '</abbr></td>\
                   <td><a class="view" href="#"><spring:message code="VIEW"/></a></td>\
                   <td><a class="revert" href="#"><spring:message code="REVERT"/></a></td>\
               </tr>');
                });
            } else {
                $('#historyContainer').text("<spring:message code="NO_HISTORY_RESULTS"/>");
            }

            tableElem.find('tr a.view').each(function () {
                $(this).click(function (event) {
                    window.open("c/maps/${mindmapId}/" + $(this).closest("tr").attr("data-history-id") + "/view");
                    event.preventDefault();
                });
            });
            tableElem.find('tr a.revert').each(function () {
                $(this).click(function (event) {
                    var url = "c/restful/maps/${mindmapId}/history/" + $(this).closest("tr").attr("data-history-id");
                    jQuery.post(url, function (data) {
                        window.parent.location = "c/maps/${mindmapId}/edit";
                    });
                    event.preventDefault();
                });
            });

        },
        error:function (jqXHR, textStatus, errorThrown) {
            console.l(textStatus);
        }
    });

    function submitDialogForm() {}
</script>
