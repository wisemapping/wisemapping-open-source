<!DOCTYPE HTML>

<%@ include file="/jsp/init.jsp" %>
<html>
<head>
<base href="${pageContext.request.contextPath}/"/>
<title><spring:message code="SITE.TITLE"/></title>
<meta http-equiv="Content-type" content="text/html; charset=utf-8"/>

<link rel="icon" href="${pageContext.request.contextPath}/images/favicon.ico" type="image/x-icon"/>
<link rel="shortcut icon" href="${pageContext.request.contextPath}/images/favicon.ico" type="image/x-icon"/>
<link rel="stylesheet/less" type="text/css" href="css/mymaps.less"/>
<script src="js/less.js" type="text/javascript"></script>


<!--jQuery DataTables-->
<script type="text/javascript" language="javascript" src="js/jquery.js"></script>
<script type="text/javascript" language="javascript" src="js/jquery-ui-1.8.16.custom.min.js"></script>
<link href="css/ui-lightness/jquery-ui-1.8.16.custom.css" rel="stylesheet">
<script type="text/javascript" language="javascript" src="js/jquery.dataTables.js"></script>
<script type="text/javascript" language="javascript" src="js/jquery.dataTables.plugins.js"></script>
<script type="text/javascript" language="javascript" src="js/jquery.timeago.js"></script>

<script type="text/javascript" charset="utf-8">
    $(function() {

        var jQueryDataTable = $('#mindmapListTable').dataTable({
            bProcessing : true,
            sAjaxSource : "../service/maps",
            sAjaxDataProp: 'mindmapsInfo',
            aoColumns: [
                {
                    sTitle : '<input type="checkbox" id="selectAll"/>',
                    sWidth : "15px",
                    sClass : "select center",
                    bSortable : false,
                    bSearchable : false,
                    fnRender : function(obj) {
                        return '<input type="checkbox" id="' + obj.aData.id + '"/>';
                    }
                },
                {
                    sClass : "columName",
                    sTitle : "Name",
                    bUseRendered : false,
                    mDataProp: "title",
                    fnRender : function(obj) {
                        return '<a href="c/map/' + obj.aData.id + '/edit.htm">' + obj.aData.title + '</a>';
                    }
                },
                {
                    sTitle : "Description",
                    mDataProp : "description"
                },
                {
                    sTitle : "Owner",
                    mDataProp :"creator"
                },
                {
                    bSearchable : false,
                    sTitle : "Last Modified",
                    bUseRendered: false,
                    sType: "date",
                    mDataProp: "lastModificationTime",
                    fnRender : function(obj) {
                        var time = obj.aData.lastModificationTime;
                        return '<abbr class="timeago" title="' + time + '">' + jQuery.timeago(time) + '</abbr>' + ' ' + '<span style="color: #777;font-size: 75%;padding-left: 5px;">' + obj.aData.lastModifierUser + '</span>';
                    }
                },
                {
                    sTitle: "Details",
                    sClass: "center",
                    sWidth : "15px",
                    bSortable : false,
                    bSearchable : false,
                    fnRender : function(obj) {
                        return '<a href="c/map/' + obj.aData.id + '/details.htm"><span class="ui-icon ui-icon-circle-triangle-e" style="margin: 0 auto;"></span></a>';
                    }
                }
            ],
            "bAutoWidth" : false,
            "oLanguage" : {
                "sSearch" : "Search",
                "sInfo" : "Page _END_ of _TOTAL_"
            },
            bStateSave:true
        });

        $('#mindmapListTable_filter').appendTo("#toolbar");
        $("#mindmapListTable_length").appendTo("#buttons");

        $('input:checkbox[id="selectAll"]').click(function() {
            $("#mindmapListTable").dataTableExt.selectAllMaps();
        });
    });
</script>

<!--Tags-->
<script type="text/javascript" charset="utf-8">

    var tags = ['ThinkMapping', 'Akostic', 'Clients', 'Favoris'];

    $(function() {
        for (i in tags) {
            var outerDiv = $('<div class="tag">' + tags[i] + '</div>');
            var icon = $('<span class="ui-icon ui-icon-folder-collapsed"></span>');
            outerDiv.append(icon);
            $("#tags-list").append(outerDiv);
        }

        $("#tags-actions button").button({
            icons: { primary: "ui-icon-plusthick" }
        });

        $("#tags-list .tag").each(function(index) {
            $(this).click(function() {
                console.log("ddfsfds");
            })
        })
    });
</script>

<!--Buttons-->
<script type="text/javascript" charset="utf-8">
    $(function() {
        $("#buttons .show-tags").button({
            icons: { primary: "ui-icon-folder-open" }
        }).click(function() {
                    if ($("#tags").css("opacity") == 0) {
                        $("#tags").css("opacity", 1);
                        $("#mindmapListTable").animate({
                            width: "77%"
                        }, 1000);
                    } else {
                        $("#mindmapListTable").animate({
                            width: "100%"
                        }, 1000, function() {
                            $("#tags").css("opacity", 0);
                        });
                    }
                });

        $("#buttons .share").button({
            icons: { primary: "ui-icon-transferthick-e-w" }
        }).click(function() {
                    var selectedMaps = $('#mindmapListTable').dataTableExt.getSelectedMapsIds();
                    var html2 = $('#share-dialog-modal p span').html(selectedMaps.toString());

                    if (selectedMaps.length > 0) {
                        $("#share-dialog-modal").dialog({
                            height: 140,
                            modal: true,
                            buttons: {
                                "Delete": function() {
                                    $(this).dialog("close");
                                },
                                Cancel: function() {
                                    $(this).dialog("close");
                                }
                            }
                        });
                    }
                });

        $("#buttons .newMap").button({
            icons: { primary: "ui-icon-circle-plus" }
        }).click(function() {
                    $("#new-dialog-modal").dialogForm({
                        modal: true,
                        acceptButtonLabel : "Create",
                        cancelButtonLabel : "Cancel",
                        redirect: "c/editor.htm?action=open",
                        url :  "../service/maps"
                    });
                });


        $("#buttons .duplicateMap").button({
            icons: { primary: "ui-icon-copy" }
        }).click(function() {
                    // Map to be cloned ...
                    var tableElem = $('#mindmapListTable');
                    var rows = tableElem.dataTableExt.getSelectedRows();
                    if (rows.length > 0) {

                        // Obtain map name  ...
                        var rowData = tableElem.dataTable().fnGetData(rows[0]);
                        $('#duplicateMessage').text("Duplicate '" + rowData.title + "'");

                        // Obtains map id ...
                        var mapId = rowData.id;

                        // Initialize dialog ...
                        $("#duplicate-dialog-modal").dialogForm({
                            modal: true,
                            acceptButtonLabel : "Duplicated",
                            cancelButtonLabel : "Cancel",
                            redirect: "c/editor.htm?action=open",
                            url :  "../service/maps/" + mapId
                        });
                    }
                });

        $("#buttons .renameMap").button({
            icons: { primary: "ui-icon-gear" }
        }).click(function() {
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

                        // Initialize dialog ...
                        $("#rename-dialog-modal").dialogForm({
                            modal: true,
                            type: 'PUT',
                            acceptButtonLabel : "Rename",
                            cancelButtonLabel : "Cancel",
                            postUpdate: function(reqBodyData) {
                                // Remove old entry ...
                                dataTable.fnDeleteRow(rowData);

                                // Add a new one...
                                rowData.title = reqBodyData.title;
                                rowData.description = reqBodyData.description;
                                dataTable.fnAddData(rowData);
                            },
                            url :  "../service/maps/" + mapId
                        });
                    }
                });


        $("#buttons .delete").button({
            icons: { primary: "ui-icon-trash" }
        }).click(function() {
                    var mapIds = $('#mindmapListTable').dataTableExt.getSelectedMapsIds();
                    if (mapIds.length > 0) {
                        var html2 = $('#delete-dialog-modal p span');
                        $("#delete-dialog-modal").dialog({
                            height: 140,
                            modal: true,
                            buttons: {
                                "Delete": function() {
                                    $('#mindmapListTable').dataTableExt.removeSelectedRows();
                                    $(this).dialog("close");
                                },
                                Cancel: function() {
                                    $(this).dialog("close");
                                }
                            }
                        });
                    }
                });

        $("#buttons .importMap").button({
            icons: { primary: "ui-icon-trash" }
        });

        $("#buttons .printMap").button({
            icons: { primary: "ui-icon-trash" }
        }).click(function() {
                    var mapIds = $('#mindmapListTable').dataTableExt.getSelectedMapsIds();
                    if (mapIds.length > 0) {
                        window.open('c/map/' + mapIds[0] + '/print.htm');
                    }
        });


        $("#buttons .moreActions").button({
            icons: { primary: "ui-icon-triangle-1-s" }
        });
    });

    // Register time update functions ....
    setTimeout(function() {
        jQuery("abbr.timeago").timeago()
    }, 50000);
</script>
</head>
<body>

<div class="content">
    <jsp:include page="header.jsp">
        <jsp:param name="removeSignin" value="false"/>
        <jsp:param name="showLogout" value="true"/>
    </jsp:include>

    <div id="mindmapListContainer">
        <div id="toolbar" class="toolbar">

        </div>

        <div id="buttons">
            <div id="delete-dialog-modal" title="Delete maps" style="display: none">
                <p>Are you sure you want to delete maps <span></span> ?</p>
            </div>
            <!-- New map dialog -->
            <div id="new-dialog-modal" title="Add new map" style="display: none">
                <div id="errorMessage"></div>

                <table>
                    <tr>
                        <td class="formLabel">
                            <span class="fieldRequired">*</span>
                            <label for="newTitle"><spring:message code="NAME"/>:</label>
                        </td>
                        <td>
                            <input name="title" id="newTitle" type="text" required="true"
                                   placeholder="Name used to identify your map"/>
                        </td>
                    </tr>
                    <tr>
                        <td class="formLabel">
                            <label for="newDec"><spring:message code="DESCRIPTION"/>:</label>
                        </td>
                        <td>
                            <input name="description" id="newDec" type="text"
                                   placeholder="Some description for your map"/>
                        </td>
                    </tr>
                </table>
            </div>

            <!-- Duplicate map dialog -->
            <div id="duplicate-dialog-modal" title="Copy Map" style="display: none">
                <div id="duplicateMessage"></div>
                <div id="errorMessage"></div>
                <table>
                    <tr>
                        <td class="formLabel">
                            <span class="fieldRequired">*</span>
                            <label for="title"><spring:message code="NAME"/>:</label>
                        </td>
                        <td>
                            <input name="title" id="title" type="text" required="true"/>
                        </td>
                    </tr>
                    <tr>
                        <td class="formLabel">
                            <label for="description"><spring:message code="DESCRIPTION"/>:</label>
                        </td>
                        <td>
                            <input name="description" id="description" type="text"
                                   placeholder="Some description for your map"/>
                        </td>
                    </tr>
                </table>
            </div>

            <!-- Duplicate map dialog -->
            <div id="rename-dialog-modal" title="Rename" style="display: none">
                <div id="errorMessage"></div>
                <table>
                    <tr>
                        <td class="formLabel">
                            <span class="fieldRequired">*</span>
                            <label for="renTitle"><spring:message code="NAME"/>:</label>
                        </td>
                        <td>
                            <input name="title" id="renTitle" required="true"/>
                        </td>
                    </tr>
                    <tr>
                        <td class="formLabel">
                            <label for="renDescription"><spring:message code="DESCRIPTION"/>:</label>
                        </td>
                        <td>
                            <input name="description" id="renDescription"/>
                        </td>
                    </tr>
                </table>
            </div>


            <div id="share-dialog-modal" title="Share maps" style="display: none">
                <p>Are you sure you want to share maps <span></span> ?</p>
            </div>
            <button class="newMap">New</button>
            <button class="duplicateMap">Duplicate</button>
            <button class="delete">Delete</button>
            <button class="renameMap">Rename</button>
            <button class="importMap">Import</button>
            <button class="printMap">Print</button>
            <button class="moreActions">More</button>
        </div>

        <div>
            <div id="map-table">
                <table class="display" id="mindmapListTable">

                </table>
            </div>
        </div>
    </div>
</div>


<jsp:include page="footer.jsp"/>
</body>
</html>
