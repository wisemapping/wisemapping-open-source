<%@page pageEncoding="UTF-8" %>
<%@include file="/jsp/init.jsp" %>

<!DOCTYPE HTML>

<html lang="en">
<head>
    <base href="${requestScope['site.baseurl']}/">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <title><spring:message code="SITE.TITLE"/> - <spring:message code="MY_WISEMAPS"/></title>
    <link rel="icon" href="images/favicon.ico" type="image/x-icon"/>
    <link rel="shortcut icon" href="images/favicon.ico" type="image/x-icon"/>

    <link rel="stylesheet/less" type="text/css" href="css/mindmapList.less"/>

    <script type="text/javascript" language="javascript" src="js/jquery-2.1.0.min.js"></script>
    <script type="text/javascript" language="javascript" src="bootstrap/js/bootstrap.js"></script>
    <script src="js/less.js" type="text/javascript"></script>

    <!--jQuery DataTables-->
    <script type="text/javascript" language="javascript" src="js/jquery.dataTables.min.js"></script>
    <script type="text/javascript" language="javascript" src="js/mindmapList.js"></script>

    <!-- Update timer plugging -->
    <script type="text/javascript" language="javascript" src="js/jquery.timeago.js"></script>
    <script type="text/javascript" language="javascript" src="js/jquery.timeago.${locale}.js"></script>

    <script type="text/javascript" language="javascript">
        $(function () {
            $('#mindmapListTable').dataTable({
                bProcessing:true,
                sAjaxSource:"c/restful/maps/",
                sAjaxDataProp:'mindmapsInfo',
                fnInitComplete:function () {
                    $('#mindmapListTable tbody').change(updateStatusToolbar);
                    callbackOnTableInit();
                },
                aoColumns:[
                    {
                        sTitle:'<input type="checkbox" id="selectAll"/>',
                        sWidth:"60px",
                        sClass:"select",
                        bSortable:false,
                        bSearchable:false,
                        mDataProp:"starred",
                        bUseRendered:false,
                        fnRender:function (obj) {
                            return '<input type="checkbox"/><span class="' + (obj.aData.starred ? 'starredOn' : 'starredOff') + '"></span>';
                        }
                    },
                    {
                        sTitle:"<spring:message code="NAME"/>",
                        sWidth:"270px",
                        bUseRendered:false,
                        mDataProp:"title",
                        fnRender:function (obj) {
                            return '<a href="c/maps/' + obj.aData.id + '/edit">' + $('<span></span>').text(obj.aData.title).html() + '</a>';
                        }
                    },
                    {
                        sTitle:"<spring:message code="CREATOR"/>",
                        mDataProp:"creator"
                    },
                    {
                        bSearchable:false,
                        sTitle:"<spring:message code="LAST_UPDATE"/>",
                        bUseRendered:false,
                        sType:"date",
                        mDataProp:"lastModificationTime",
                        fnRender:function (obj) {
                            var time = obj.aData.lastModificationTime;
                            return '<abbr class="timeago" title="' + time + '">' + jQuery.timeago(time) + '</abbr>' + ' ' + '<span style="color: #777;font-size: 75%;padding-left: 5px;">' + obj.aData.lastModifierUser + '</span>';
                        }
                    }
                ],
                bAutoWidth:false,
                oLanguage:{
                    "sLengthMenu":"<spring:message code="SHOW_REGISTERS"/>",
                    "sSearch":"",
                    "sZeroRecords":"<spring:message code="NO_MATCHING_FOUND"/>",
                    "sLoadingRecords":"<spring:message code="LOADING"/>",
                    "sInfo":"<spring:message code="TABLE_ROWS"/>",
                    "sEmptyTable":"<spring:message code="NO_SEARCH_RESULT"/>",
                    "sProcessing":"<spring:message code="LOADING"/>"
                },
                bStateSave:true
            });

            // Customize search action ...
            $('#mindmapListTable_filter').appendTo("#tableActions");
            $('#mindmapListTable_filter input').addClass('input-medium search-query form-control');
            $('#mindmapListTable_filter input').attr('placeholder', 'Search');
            $("#mindmapListTable_info").appendTo("#pageInfo");

            // Re-arrange pagination actions ...
            $("#tableFooter").appendTo("#mindmapListTable_wrapper");
            $("#mindmapListTable_length").appendTo("#tableFooter");
            $('#mindmapListTable_length select').attr("style", "width:60px;");


            $('input:checkbox[id="selectAll"]').click(function () {
                $("#mindmapListTable").dataTableExt.selectAllMaps();
            });

            // Hack for changing the pagination buttons ...
            $('#nPageBtn').click(function () {
                $('#mindmapListTable_next').click();
            });
            $('#pPageBtn').click(function () {
                $('#mindmapListTable_previous').click();
            });
        });
    </script>
</head>
<body>
<jsp:include page="header.jsp">
    <jsp:param name="removeSignin" value="false"/>
    <jsp:param name="showLogout" value="true"/>
</jsp:include>

<div class="row hide" id="messagesPanel" style="margin-top: 20px">
    <div class="col-md-2"></div>
    <div class="alert alert-danger alert-block fade in col-md-8">
        <strong><spring:message code="UNEXPECTED_ERROR"/></strong>
        <p><spring:message code="UNEXPECTED_ERROR_SERVER_ERROR"/></p>
        <div></div>
    </div>
</div>

<div class="row" style="min-height: 500px">

    <div class="col-md-1"></div>
    <div class="col-md-2" id="foldersContainer">
        <ul class="nav nav-pills nav-stacked">
            <li data-filter="all" class="active"><a href="#"><i class="glyphicon glyphicon-inbox glyphicon-white"></i> <spring:message
                    code="ALL_MAPS"/></a></li>
            <li data-filter="my_maps"><a href="#"><i class="glyphicon glyphicon-user"></i> <spring:message code="MY_MAPS"/></a>
            </li>
            <li data-filter="shared_with_me"><a href="#"><i class="glyphicon glyphicon-share"></i> <spring:message
                    code="SHARED_WITH_ME"/></a></li>
            <li data-filter="starred"><a href="#"><i class="glyphicon glyphicon-star"></i> <spring:message code="STARRED"/></a></li>
            <li data-filter="public"><a href="#"><i class="glyphicon glyphicon-globe"></i> <spring:message code="PUBLIC_MAPS"/></a>
            </li>
        </ul>
    </div>

    <div class="buttonsToolbar btn-toolbar ${requestScope['google.ads.enabled']?'col-md-7':'col-md-8'}">
        <div id="tableActions">
            <div id="pageInfo"></div>
            <div class="btn-group" id="pageButtons">
                <button class="btn" id="pPageBtn"><strong>&lt;</strong></button>
                <button class="btn" id="nPageBtn"><strong>&gt;</strong></button>
            </div>
        </div>

        <div class="btn-group">
            <button id="newBtn" class="btn btn-primary"><i class="glyphicon glyphicon-file glyphicon-white"></i> <spring:message
                    code="NEW"/></button>
            <button id="importBtn" class="btn btn-primary"><i class="glyphicon glyphicon-upload glyphicon-white"></i>
                <spring:message code="IMPORT"/>
            </button>
        </div>

        <div class="btn-group act-multiple" id="deleteBtn" style="display:none">
            <button class="btn btn-primary"><i class="glyphicon glyphicon-trash glyphicon-white"></i> <spring:message
                    code="DELETE"/></button>
        </div>

        <div id="infoBtn" class="btn-group act-single" style="display:none">
            <button class="btn btn-primary"><i class="glyphicon glyphicon-exclamation-sign glyphicon-white"></i> <spring:message
                    code="INFO"/></button>
        </div>

        <div id="actionsBtn" class="btn-group act-single" style="display:none">
            <button class="btn btn-primary dropdown-toggle" data-toggle="dropdown">
                <i class="glyphicon glyphicon-asterisk glyphicon-white"></i> <spring:message code="MORE"/>
                <span class="caret"></span>
            </button>

            <ul class="dropdown-menu">
                <li id="duplicateBtn"><a href="#" onclick="return false"><i class="glyphicon glyphicon-plus-sign"></i>
                    <spring:message code="DUPLICATE"/></a></li>
                <li id="renameBtn"><a href="#" onclick="return false"><i class="glyphicon glyphicon-edit"></i> <spring:message
                        code="RENAME"/></a></li>
                <li id="publishBtn"><a href="#" onclick="return false"><i class="glyphicon glyphicon-globe"></i>
                    <spring:message code="PUBLISH"/></a>
                </li>
                <li id="shareBtn"><a href="#" onclick="return false"><i class="glyphicon glyphicon-share"></i> <spring:message
                        code="SHARE"/></a></li>
                <li id="exportBtn"><a href="#" onclick="return false"><i class="glyphicon glyphicon-download"></i>
                    <spring:message
                            code="EXPORT"/></a>
                </li>
                <li id="printBtn"><a href="#" onclick="return false"><i class="glyphicon glyphicon-print"></i> <spring:message
                        code="PRINT"/></a></li>
                <li id="historyBtn"><a href="#" onclick="return false"><i class="glyphicon glyphicon-time"></i> <spring:message
                        code="HISTORY"/></a>
                </li>
            </ul>
        </div>
        <div id="map-table">
            <table class="table" id="mindmapListTable"></table>
        </div>
        <div id="tableFooter" class="form-inline"></div>

    </div>
    <div class="col-md-1" style="padding-top:25px">
        <c:if test="${requestScope['google.ads.enabled']}">
            <script type="text/javascript"><!--
            google_ad_client = "ca-pub-7564778578019285";
            /* WiseMapping Mindmap List */
            google_ad_slot = "4071968444";
            google_ad_width = 120;
            google_ad_height = 600;
            //-->
            </script>
            <div style="margin-top:5px;">
                <script type="text/javascript"
                        src="https://pagead2.googlesyndication.com/pagead/show_ads.js">
                </script>
            </div>
        </c:if>
    </div>
</div>
<jsp:include page="footer.jsp"/>

<div id="dialogsContainer">
<!-- New map dialog -->
<div id="new-dialog-modal" title="<spring:message code="ADD_NEW_MAP"/>" class="modal fade">
  <div class="modal-dialog">
    <div class="modal-content">

        <div class="modal-header">
            <button class="close" data-dismiss="modal">x</button>
            <h3><spring:message code="NEW_MAP_MSG"/></h3>
        </div>
        <div class="modal-body">
            <div class="errorMessage"></div>
            <form class="form-horizontal">
                <fieldset>
                    <div class="form-group">
                        <label class="col-md-3 control-label" for="newTitle"><spring:message code="NAME"/>:</label>
                        <div class="col-md-8">
                            <input class="form-control" name="title" id="newTitle" type="text" required="required" placeholder="<spring:message code="MAP_NAME_HINT"/>" autofocus="autofocus" maxlength="255"/>
                         </div>
                    </div>
                    <div class="form-group">
                        <label class="col-md-3 control-label" for="newDec"><spring:message code="DESCRIPTION"/>:</label>
                        <div class="col-md-8">
                            <input class="form-control" name="description" id="newDec" type="text" placeholder="<spring:message code="MAP_DESCRIPTION_HINT"/>" maxlength="255"/>
                        </div>
                    </div>
                </fieldset>
            </form>
        </div>
        <div class="modal-footer">
            <button class="btn btn-primary btn-accept" data-loading-text="<spring:message
                    code="SAVING"/>"><spring:message
                    code="CREATE"/></button>
            <button class="btn btn-cancel" data-dismiss="modal"><spring:message code="CANCEL"/></button>
        </div>
      </div>
   </div>
</div>

<!-- Duplicate map dialog -->
<div id="duplicate-dialog-modal" class="modal fade">
  <div class="modal-dialog">
    <div class="modal-content">

        <div class="modal-header">
            <button class="close" data-dismiss="modal">x</button>
            <h3 id="dupDialogTitle"></h3>
        </div>
        <div class="modal-body">
            <div class="errorMessage"></div>
            <form class="form-horizontal">
                <fieldset>
                    <div class="form-group">
                        <label for="title" class="col-md-3 control-label"><spring:message code="NAME"/>: </label>
                        <div class="col-md-8">
                            <input name="title" id="title" type="text" required="required"
                                   placeholder="<spring:message code="MAP_DESCRIPTION_HINT"/>" autofocus="autofocus"
                                   class="form-control" maxlength="255"/>
                         </div>
                    </div>
                    <div class="form-group">
                        <label for="description" class="col-md-3 control-label"><spring:message
                                code="DESCRIPTION"/>: </label>
                        <div class="col-md-8">
                            <input name="description" id="description" type="text"
                                   placeholder="<spring:message code="MAP_DESCRIPTION_HINT"/>" class="form-control" maxlength="255"/>
                        </div>
                    </div>
                </fieldset>
            </form>
        </div>
        <div class="modal-footer">
            <button class="btn btn-primary btn-accept" data-loading-text="<spring:message code="SAVING"/>">
                <spring:message code="DUPLICATE"/></button>
            <button class="btn btn-cancel" data-dismiss="modal"><spring:message code="CANCEL"/></button>
        </div>
      </div>
    </div>
</div>

<!-- Rename map dialog -->
<div id="rename-dialog-modal" class="modal fade">
  <div class="modal-dialog">
    <div class="modal-content">
        <div class="modal-header">
            <button class="close" data-dismiss="modal">x</button>
            <h3 id="renameDialogTitle"><spring:message code="RENAME"/></h3>
        </div>
        <div class="modal-body">
            <div class="errorMessage"></div>
            <form class="form-horizontal">
                <fieldset>
                    <div class="form-group">
                        <label for="renTitle" class="col-md-3 control-label"><spring:message code="NAME"/>: </label>
                        <div class="col-md-8">
                            <input name="title" id="renTitle" required="required" autofocus="autofocus"
                                   class="form-control" maxlength="255"/>
                        </div>
                    </div>
                    <div class="form-group">
                        <label for="renDescription" class="col-md-3 control-label"><spring:message
                                code="DESCRIPTION"/>:</label>
                        <div class="col-md-8">
                            <input name="description" class="form-control" id="renDescription" maxlength="255"/>
                        </div>
                    </div>
                </fieldset>
            </form>
        </div>
        <div class="modal-footer">
            <button class="btn btn-primary btn-accept" data-loading-text="<spring:message code="SAVING"/>"><spring:message
                    code="RENAME"/></button>
            <button class="btn btn-cancel" data-dismiss="modal"><spring:message code="CANCEL"/></button>
        </div>
       </div>
    </div>
</div>

<!-- Delete map dialog -->
<div id="delete-dialog-modal" class="modal fade">
  <div class="modal-dialog">
    <div class="modal-content">
        <div class="modal-header">
            <button class="close" data-dismiss="modal">x</button>
            <h3><spring:message code="DELETE_MINDMAP"/></h3>
        </div>
        <div class="modal-body">
            <div class="alert alert-block">
                <h4 class="alert-heading"><spring:message code="WARNING"/>!</h4><spring:message code="DELETE_MAPS_WARNING"/>
            </div>
        </div>
        <div class="modal-footer">
            <button class="btn btn-primary btn-accept" data-loading-text="<spring:message
                    code="SAVING"/> ..."><spring:message
                    code="DELETE"/></button>
            <button class="btn btn-cancel" data-dismiss="modal"><spring:message code="CANCEL"/></button>
        </div>
       </div>
    </div>
</div>

<!-- Info map dialog -->
<div id="info-dialog-modal" class="modal fade">
  <div class="modal-dialog">
    <div class="modal-content">
        <div class="modal-header">
            <button class="close" data-dismiss="modal">x</button>
            <h3><spring:message code="INFO"/></h3>
        </div>
        <div class="modal-body">

        </div>
        <div class="modal-footer">
            <button class="btn btn-cancel" data-dismiss="modal"><spring:message code="CLOSE"/></button>
        </div>
      </div>
    </div>
</div>

<!-- Publish Dialog Config -->
<div id="publish-dialog-modal" class="modal fade">
  <div class="modal-dialog">
    <div class="modal-content">
        <div class="modal-header">
            <button class="close" data-dismiss="modal">x</button>
            <h3><spring:message code="PUBLISH"/></h3>
        </div>
        <div class="modal-body">

        </div>
        <div class="modal-footer">
            <button class="btn btn-primary btn-accept" data-loading-text="<spring:message code="SAVING"/>...">
                <spring:message code="ACCEPT"/></button>
            <button class="btn btn-cancel" data-dismiss="modal"><spring:message code="CANCEL"/></button>
        </div>
     </div>
   </div>
</div>

<!-- Export Dialog Config -->
<div id="export-dialog-modal" class="modal fade">
  <div class="modal-dialog">
    <div class="modal-content">
        <div class="modal-header">
            <button class="close" data-dismiss="modal">x</button>
            <h3><spring:message code="EXPORT"/></h3>
        </div>
        <div class="modal-body">

        </div>
        <div class="modal-footer">
            <button class="btn btn-primary btn-accept" data-loading-text="Exporting..."><spring:message
                    code="EXPORT"/></button>
            <button class="btn btn-cancel" data-dismiss="modal"><spring:message code="CANCEL"/></button>
        </div>
     </div>
   </div>
</div>

<!-- Import Dialog Config -->
<div id="import-dialog-modal" class="modal fade">
  <div class="modal-dialog">
    <div class="modal-content">
    <div class="modal-header">
            <button class="close" data-dismiss="modal">x</button>
            <h3><spring:message code="IMPORT"/></h3>
        </div>
        <div class="modal-body">

        </div>
        <div class="modal-footer">
            <button class="btn btn-primary btn-accept" data-loading-text="<spring:message
                        code="IMPORTING"/>"><spring:message
                    code="IMPORT"/></button>
            <button class="btn btn-cancel" data-dismiss="modal"><spring:message code="CANCEL"/></button>
        </div>
     </div>
   </div>
</div>

<!-- Share Dialog Config -->
<div id="share-dialog-modal" class="modal fade">
  <div class="modal-dialog">
    <div class="modal-content">
        <div class="modal-header">
            <button class="close" data-dismiss="modal">x</button>
            <h3><spring:message code="SHARE"/></h3>
        </div>
        <div class="modal-body">

        </div>
        <div class="modal-footer">
            <button class="btn btn-primary btn-accept" data-loading-text="<spring:message code="SAVING"/>">
                <spring:message code="ACCEPT"/></button>
            <button class="btn btn-cancel" data-dismiss="modal"><spring:message code="CANCEL"/></button>
        </div>
     </div>
   </div>
</div>

<!-- History Dialog Config -->
<div id="history-dialog-modal" class="modal fade">
  <div class="modal-dialog">
    <div class="modal-content">
        <div class="modal-header">
            <button class="close" data-dismiss="modal">x</button>
            <h3><spring:message code="HISTORY"/></h3>
        </div>
        <div class="modal-body">

        </div>
        <div class="modal-footer">
            <button class="btn btn-cancel" data-dismiss="modal"><spring:message code="CLOSE"/></button>
        </div>
     </div>
   </div>
</div>

</div>
</body>
</html>
