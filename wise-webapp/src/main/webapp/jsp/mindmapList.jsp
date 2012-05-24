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

<script type="text/javascript" language="javascript" src="js/jquery-1.7.2.min.js"></script>
<script type="text/javascript" language="javascript" src="bootstrap/js/bootstrap.js"></script>
<script src="js/less.js" type="text/javascript"></script>

<!--jQuery DataTables-->
<script type="text/javascript" language="javascript" src="js/jquery.dataTables.min.js"></script>
<script type="text/javascript" language="javascript" src="js/mymaps.js"></script>

<!-- Update timer plugging -->
<script type="text/javascript" language="javascript" src="js/jquery.timeago.js"></script>

<script type="text/javascript" charset="utf-8">
    var principalEmail = '${principal.email}';
    $(function() {
        var jQueryDataTable = $('#mindmapListTable').dataTable({
            bProcessing : true,
            sAjaxSource : "../service/maps",
            sAjaxDataProp: 'mindmapsInfo',
            fnInitComplete: function() {
                $('#mindmapListTable tbody').change(updateStatus);
            },
            aoColumns: [
                {
                    sTitle : '<input type="checkbox" id="selectAll"/>',
                    sWidth : "15px",
                    sClass : "select",
                    bSortable : false,
                    bSearchable : false,
                    fnRender : function(obj) {
                        return '<input type="checkbox" id="' + obj.aData.id + '"/>';
                    }
                },
                {
                    sTitle : "Name",
                    bUseRendered : false,
                    mDataProp: "title",
                    fnRender : function(obj) {
                        return '<a href="c/map/' + obj.aData.id + '/edit.htm">' + obj.aData.title + '</a>';
                    }
                },
                {
                    sTitle : "Owner",
                    mDataProp :"owner"
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
                }
            ],
            bAutoWidth : false,
            oLanguage : {
                "sSearch" : "",
                "sInfo" : "_START_-_END_ of _TOTAL_",
                "sEmptyTable": "No mindmap available for the selected filter criteria."
            },
            bStateSave:true
        });

        // Customize search action ...
        $('#mindmapListTable_filter').appendTo("#tableActions");
        $('#mindmapListTable_filter input').addClass('input-medium search-query');
        $('#mindmapListTable_filter input').attr('placeholder', 'Search');
        $("#mindmapListTable_info").appendTo("#pageInfo");

        // Re-arrange pagination actions ...
        $("#tableFooter").appendTo("#mindmapListTable_wrapper");
        $("#mindmapListTable_length").appendTo("#tableFooter");
        $('#mindmapListTable_length select').addClass('span1');


        $('input:checkbox[id="selectAll"]').click(function() {
            $("#mindmapListTable").dataTableExt.selectAllMaps();
        });

        // Hack for changing the lagination buttons ...
        $('#nPageBtn').click(function() {
            $('#mindmapListTable_next').click();
        });
        $('#pPageBtn').click(function() {
            $('#mindmapListTable_previous').click();
        });
    });
</script>

<!--Buttons-->
<script type="text/javascript" charset="utf-8">
    $(function() {
        // Creation buttons actions ...
        $("#newBtn").click(
                function() {
                    $("#new-dialog-modal").dialogForm({
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
                    type: 'PUT',
                    clearForm: false,
                    postUpdate: function(reqBodyData) {
                        tableElem.dataTableExt.removeSelectedRows();

                        rowData.title = reqBodyData.title;
                        rowData.description = reqBodyData.description;
                        dataTable.fnAddData(JSON.parse(JSON.stringify(rowData)));
                    },
                    url :  "../service/maps/" + mapId
                });
            }
        });

        $("#deleteBtn").click(function() {
            var tableUI = $('#mindmapListTable');

            var mapIds = tableUI.dataTableExt.getSelectedMapsIds();
            if (mapIds.length > 0) {
                // Initialize dialog ...
                $("#delete-dialog-modal").dialogForm({
                    type: 'DELETE',
                    postUpdate: function(reqBodyData) {
                        // Remove old entry ...
                        tableUI.dataTableExt.removeSelectedRows();
                    },
                    url :  "../service/maps/batch?ids=" + mapIds.join(',')
                });
            }
        });

        $("#printBtn").click(function() {
            var mapIds = $('#mindmapListTable').dataTableExt.getSelectedMapsIds();
            if (mapIds.length > 0) {
                window.open('c/map/' + mapIds[0] + '/print.htm');
            }
        });

        $("#infoBtn").click(function() {
            var mapIds = $('#mindmapListTable').dataTableExt.getSelectedMapsIds();
            if (mapIds.length > 0) {
                $('#info-dialog-modal .modal-body').load("c/map/" + mapIds[0] + "/details.htm", function() {
                    $('#info-dialog-modal').modal();
                });

            }
        });

        $("#publishBtn").click(function() {
            var mapIds = $('#mindmapListTable').dataTableExt.getSelectedMapsIds();
            if (mapIds.length > 0) {
                $('#publish-dialog-modal .modal-body').load("c/map/" + mapIds[0] + "/publish.htm",
                        function() {
                            $('#publish-dialog-modal .btn-accept').click(function() {
                                $('#publish-dialog-modal #publishForm').submit();
                            });
                            $('#publish-dialog-modal').modal();
                        });
            }
        });

        $("#actionButtons .shareMap").click(function() {
        });

        $("#actionButtons .tagMap").click(function() {
        });

        $("#actionButtons .tags").click(function() {
        });


        $('#foldersContainer li').click(function(event) {
            // Deselect previous option ...
            $('#foldersContainer li').removeClass('active');
            $('#foldersContainer i').removeClass('icon-white');

            // Select the new item ...
            var dataTable = $('#mindmapListTable').dataTable();
            $(this).addClass('active').filter('i').addClass('icon-white');

            // Reload the table data ...
            dataTable.fnReloadAjax("../service/maps?q=" + $(this).attr('data-filter'));

            event.preventDefault();
        });
    });

    // Register time update functions ....
    setTimeout(function() {
        jQuery("abbr.timeago").timeago()
    }, 50000);

</script>
</head>
<body>
<jsp:include page="header.jsp">
    <jsp:param name="removeSignin" value="false"/>
    <jsp:param name="showLogout" value="true"/>
</jsp:include>

<div style="min-height: 500px">

    <div id="messagesPanel" class="alert alert-error alert-block fade in hide" style="margin-top: 10px">
        <strong><spring:message code="UNEXPECTED_ERROR"/></strong>

        <p><spring:message code="UNEXPECTED_ERROR_SERVER_ERROR"/></p>

        <div></div>
    </div>

    <div id="mindmapListContainer">
        <div id="foldersContainer">
            <ul class="nav nav-list">
                <li class="nav-header">Folders</li>
                <li data-filter="all" class="active"><a href="#"><i class="icon-inbox"></i> All</a></li>
                <li data-filter="my_maps" ><a href="#"><i class="icon-user"></i> My Maps</a></li>
                <li data-filter="shared_with_me"><a href="#"><i class="icon-share"></i> Shared With Me</a></li>
                <li data-filter="public"><a href="#"><i class="icon-globe"></i> Public Maps</a></li>
            </ul>
        </div>

        <div style="width: 78%;float: left;">
            <div id="buttonsToolbar" class="btn-toolbar">

                <div class="btn-group">
                    <button id="newBtn" class="btn btn-primary"><i class="icon-file icon-white"></i> New</button>
                    <button id="importBtn" class="btn btn-primary"><i class="icon-upload icon-white"></i> Import
                    </button>
                </div>

                <div class="btn-group act-multiple" id="deleteBtn" style="display:none">
                    <button class="btn btn-primary"><i class="icon-trash icon-white"></i> Delete</button>
                </div>

                <div id="infoBtn" class="btn-group act-single" style="display:none">
                    <button class="btn btn-primary"><i class="icon-exclamation-sign icon-white"></i> Info</button>
                </div>

                <div id="actionsBtn" class="btn-group act-single" style="display:none">
                    <button class="btn btn-primary dropdown-toggle" data-toggle="dropdown">
                        <i class="icon-asterisk icon-white"></i> More
                        <span class="caret"></span>
                    </button>

                    <ul class="dropdown-menu">
                        <li id="duplicateBtn"><a href="#" onclick="return false"><i class="icon-plus-sign"></i>
                            Duplicate</a></li>
                        <li id="renameBtn"><a href="#" onclick="return false"><i class="icon-edit"></i> Rename</a></li>
                        <li id="printBtn"><a href="#" onclick="return false"><i class="icon-print"></i> Print</a></li>
                        <li id="publishBtn"><a href="#" onclick="return false"><i class="icon-globe"></i>Publish</a>
                        </li>
                        <li id="shareBtn"><a href="#" onclick="return false"><i class="icon-share"></i> Share</a></li>
                        <li id="tagMap"><a href="#" onclick="return false"><i class="icon-tags"></i> Tag</a></li>
                    </ul>
                </div>

                <div id="tableActions" class="btn-toolbar">
                    <div class="btn-group" id="pageButtons">
                        <button class="btn" id="pPageBtn"><strong>&lt;</strong></button>
                        <button class="btn" id="nPageBtn"><strong>&gt;</strong></button>
                    </div>
                    <div id="pageInfo"></div>
                </div>
            </div>
            <div id="map-table">
                <table class="table" id="mindmapListTable"></table>
                <div id="tableFooter" class="form-inline"></div>
            </div>
        </div>
    </div>
</div>
<jsp:include page="footer.jsp"/>


<div id="dialogsContainer">
    <!-- New map dialog -->
    <div id="new-dialog-modal" title="Add new map" class="modal fade" style="display:none">
        <div class="modal-header">
            <button class="close" data-dismiss="modal">x</button>
            <h3>Create a new map</h3>
        </div>
        <div class="modal-body">
            <div class="errorMessage"></div>
            <form class="form-horizontal">
                <fieldset>
                    <div class="control-group">
                        <label class="control-label" for="newTitle"><spring:message code="NAME"/>:</label>
                        <input class="control" name="title" id="newTitle" type="text" required="true"
                               placeholder="Name of the new map to create" autofocus="autofocus"/>
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
            <button class="btn btn-primary btn-accept" data-loading-text="Saving ...">Create</button>
            <button class="btn btn-cancel" data-dismiss="modal">Cancel</button>
        </div>
    </div>

    <!-- Duplicate map dialog -->
    <div id="duplicate-dialog-modal" class="modal fade" style="display: none">
        <div class="modal-header">
            <button class="close" data-dismiss="modal">X</button>
            <h3 id="dupDialogTitle"></h3>
        </div>
        <div class="modal-body">
            <div class="errorMessage"></div>
            <form class="form-horizontal">
                <fieldset>
                    <div class="control-group">
                        <label for="title" class="control-label"><spring:message code="NAME"/>: </label>
                        <input name="title" id="title" type="text" required="required"
                               placeholder="Name of the new map to create" autofocus="autofocus"
                               class="control"/>
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
            <button class="btn btn-primary btn-accept" data-loading-text="Saving ...">Duplicate</button>
            <button class="btn btn-cancel" data-dismiss="modal">Cancel</button>
        </div>
    </div>

    <!-- Rename map dialog -->
    <div id="rename-dialog-modal" class="modal fade" style="display: none">
        <div class="modal-header">
            <button class="close" data-dismiss="modal">x</button>
            <h3 id="renameDialogTitle"></h3>
        </div>
        <div class="modal-body">
            <div class="errorMessage"></div>
            <form class="form-horizontal">
                <fieldset>
                    <div class="control-group">
                        <label for="renTitle" class="control-label"><spring:message code="NAME"/>: </label>
                        <input name="title" id="renTitle" required="required" autofocus="autofocus"
                               class="control"/>
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
            <button class="btn btn-primary btn-accept" data-loading-text="Saving ...">Rename</button>
            <button class="btn btn-cancel" data-dismiss="modal">Cancel</button>
        </div>
    </div>

    <!-- Delete map dialog -->
    <div id="delete-dialog-modal" class="modal fade" style="display: none">
        <div class="modal-header">
            <button class="close" data-dismiss="modal">x</button>
            <h3>Delete MindMap</h3>
        </div>
        <div class="modal-body">
            <div class="alert alert-block">
                <h4 class="alert-heading">Warning!</h4>Deleted mindmap can not be recovered. Do you want to
                continue ?.
            </div>
        </div>
        <div class="modal-footer">
            <button class="btn btn-primary btn-accept" data-loading-text="Saving ...">Delete</button>
            <button class="btn btn-cancel" data-dismiss="modal">Cancel</button>
        </div>
    </div>

    <!-- Info map dialog -->
    <div id="info-dialog-modal" class="modal fade" style="display: none">
        <div class="modal-header">
            <button class="close" data-dismiss="modal">x</button>
            <h3>Info</h3>
        </div>
        <div class="modal-body">

        </div>
        <div class="modal-footer">
            <button class="btn btn-cancel" data-dismiss="modal">Close</button>
        </div>
    </div>

    <!-- Publish Dialog Config -->
    <div id="publish-dialog-modal" class="modal fade" style="display: none">
        <div class="modal-header">
            <button class="close" data-dismiss="modal">x</button>
            <h3>Publish</h3>
        </div>
        <div class="modal-body">

        </div>
        <div class="modal-footer">
            <button class="btn btn-primary btn-accept" data-loading-text="Saving...">Accept</button>
            <button class="btn btn-cancel" data-dismiss="modal">Cancel</button>
        </div>
    </div>
</body>
</html>
