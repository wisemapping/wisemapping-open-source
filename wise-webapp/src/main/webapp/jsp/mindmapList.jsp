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

<script type="text/javascript" language="javascript" src="js/jquery-1.7.2.min.js"></script>
<script type="text/javascript" language="javascript" src="bootstrap/js/bootstrap.js"></script>


<!--jQuery DataTables-->
<script type="text/javascript" language="javascript" src="js/jquery.dataTables.min.js"></script>
<script type="text/javascript" language="javascript" src="js/jquery.dataTables.plugins.js"></script>

<!-- Update timer plugging -->
<script type="text/javascript" language="javascript" src="js/jquery.timeago.js"></script>

<script type="text/javascript" charset="utf-8">
    $(function() {

        var jQueryDataTable = $('#mindmapListTable').dataTable({
            bProcessing : true,
            sAjaxSource : "../service/maps",
            sAjaxDataProp: 'mindmapsInfo',
            fnInitComplete: function() {
                $('#mindmapListTable tbody').change(updateToolbar);
            },
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
        $("#mindmapListTable_length").appendTo("#actionButtons");

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
        $("#actionButtons .show-tags").button({
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

        $("#actionButtons .share").button({
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

        // Creation buttons actions ...
        $("#newBtn").click(
                function() {
                    $("#new-dialog-modal").dialogForm({
                        modal: true,
                        acceptButtonLabel : "Create",
                        cancelButtonLabel : "Cancel",
                        redirect: "c/map/{header.resourceId}/edit.htm",
                        url :  "../service/maps"
                    });
                });

        $("#importBtn").click(function() {
            window.open('c/map/import.htm');
        });


        $("#duplicateBtn").click(function() {
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
                    modal: true,
                    acceptButtonLabel : "Duplicate",
                    cancelButtonLabel : "Cancel",
                    redirect: "c/map/{header.resourceId}/edit.htm",
                    url :  "../service/maps/" + mapId
                });
            }
        });

        $("#renameBtn").click(function() {
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


        $("#deleteBtn").click(function() {
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

        $("#actionButtons .printMap").button({
            icons: { primary: "ui-icon-print" }
        }).click(function() {
                    var mapIds = $('#mindmapListTable').dataTableExt.getSelectedMapsIds();
                    if (mapIds.length > 0) {
                        window.open('c/map/' + mapIds[0] + '/print.htm');
                    }
                });

        $("#actionButtons .publishMap").button({
            icons: { primary: "ui-icon-print" }
        }).click(function() {
                });

        $("#actionButtons .shareMap").button({
            icons: { primary: "ui-icon-print" }
        }).click(function() {
                });

        $("#actionButtons .tagMap").button({
            icons: { primary: "ui-icon-print" }
        }).click(function() {
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
        <div id="buttonsToolbar" class="btn-toolbar">

            <div class="btn-group">
                <button class="btn" id="newBtn"><i class="icon-file"></i> New</button>
                <button class="btn" id="importBtn"><i class="icon-upload"></i> Import</button>
            </div>

            <div class="btn-group" id="deleteBtn" style="display:none">
                <button class="btn"><i class="icon-trash"></i> Delete</button>
            </div>
            <div class="btn-group" id="actionsBtn" style="display:none">

                <button class="btn dropdown-toggle" data-toggle="dropdown">
                    <i class="icon-asterisk"></i> More
                    <span class="caret"></span>
                </button>
                <ul class="dropdown-menu">
                    <li id="duplicateBtn"><a href="#" onclick="return false"><i class="icon-plus-sign"></i> Duplicate</a></li>
                    <li id="renameBtn"><a href="#" onclick="return false"><i class="icon-edit"></i> Rename</a></li>
                    <li id="printMap"><a href="#" onclick="return false"><i class="icon-print"></i> Print</a></li>
                    <li id="publishMap"><a href="#" onclick="return false"><i class="icon-globe"></i>Publish</a></li>
                    <li id="exportMap"><a href="#" onclick="return false"><i class="icon-download-alt"></i> Export</a>
                    </li>
                    <li id="shareMap"><a href="#" onclick="return false"><i class="icon-share"></i> Share</a></li>
                    <li id="tagMap"><a href="#" onclick="return false"><i class="icon-tags"></i> Tag</a></li>
                </ul>
            </div>
        </div>

        <div>
            <div id="delete-dialog-modal" title="Delete maps" style="display: none">
                <p>Are you sure you want to delete maps <span></span> ?</p>
            </div>

            <!-- New map dialog -->
            <div id="new-dialog-modal" title="Add new map" class="modal fade" style="display:none">
                <div class="modal-header">
                    <button class="close" data-dismiss="modal">x</button>
                    <h3>Create a new map</h3>
                </div>
                <div class="modal-body">
                    <div id="errorMessage"></div>
                    <form class="form-horizontal">
                        <fieldset>
                            <div class="control-group">
                                <label class="control-label" for="newTitle"><spring:message code="NAME"/>:</label>
                                <input class="control" name="title" id="newTitle" type="text" required="true"
                                       placeholder="Name used to identify your map"/>
                            </div>
                            <div class="control-group">
                                <label class="control-label" for="newDec"><spring:message code="DESCRIPTION"/>:</label>
                                <input class="control" name="description" id="newDec" type="text"
                                       placeholder="Some description for your map"/>
                            </div>
                        </fieldset>
                    </form>
                </div>
                <div class="modal-footer">
                    <button class="btn btn-primary btn-accept">Create</button>
                    <button class="btn btn-cancel">Close</button>
                </div>
            </div>

            <!-- Duplicate map dialog -->
            <div id="duplicate-dialog-modal" class="modal fade" style="display: none">
                <div class="modal-header">
                    <button class="close" data-dismiss="modal">X</button>
                    <h3 id="dupDialogTitle"></h3>
                </div>
                <div class="modal-body">
                    <div id="errorMessage"></div>
                    <form class="form-horizontal">
                        <fieldset>
                            <div class="control-group">
                                <label for="title" class="control-label"><spring:message code="NAME"/>: </label>
                                <input name="title" id="title" type="text" required="true" class="control"/>
                            </div>
                            <div class="control-group">
                                <label for="description" class="control-label"><spring:message
                                        code="DESCRIPTION"/>: </label>
                                <input name="description" id="description" type="text"
                                       placeholder="Some description for your map" class="control"/>
                            </div>
                        </fieldset>
                    </form>
                </div>
                <div class="modal-footer">
                    <button class="btn btn-primary btn-accept">Duplicate</button>
                    <button class="btn btn-cancel">Close</button>
                </div>
            </div>

            <!-- Rename map dialog -->
            <div id="rename-dialog-modal" class="modal fade" style="display: none">
                <div class="modal-header">
                    <button class="close" data-dismiss="modal">x</button>
                    <h3 id="renameDialogTitle"></h3>
                </div>
                <div class="modal-body">
                    <div id="errorMessage"></div>
                    <form class="form-horizontal">
                        <fieldset>
                            <div class="control-group">
                                <label for="renTitle" class="control-label"><spring:message code="NAME"/>: </label>
                                <input name="title" id="renTitle" class="control" required="true"/>
                            </div>
                            <div class="control-group">
                                <label for="renDescription" class="control-label"><spring:message
                                        code="DESCRIPTION"/>:</label>
                                <input name="description" class="control" id="renDescription"/>
                            </div>
                        </fieldset>
                    </form>
                </div>
                <div class="modal-footer">
                    <button class="btn btn-primary btn-accept">Rename</button>
                    <button class="btn btn-cancel">Close</button>
                </div>
            </div>
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
