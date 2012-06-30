<%@page pageEncoding="UTF-8" %>
<%@include file="/jsp/init.jsp" %>

<!DOCTYPE HTML>

<html>
<head>
    <base href="${baseURL}">
    <title><spring:message code="SITE.TITLE"/></title>
    <!--[if lt IE 9]>
    <meta http-equiv="X-UA-Compatible" content="chrome=1">
    <![endif]-->

    <link rel="icon" href="images/favicon.ico" type="image/x-icon"/>
    <link rel="shortcut icon" href="images/favicon.ico" type="image/x-icon"/>

    <link rel="stylesheet/less" type="text/css" href="css/mindmapList.less"/>

    <script type="text/javascript" language="javascript" src="js/jquery-1.7.2.min.js"></script>
    <script type="text/javascript" language="javascript" src="bootstrap/js/bootstrap.js"></script>
    <script src="js/less.js" type="text/javascript"></script>

    <!--jQuery DataTables-->
    <script type="text/javascript" language="javascript" src="js/jquery.dataTables.min.js"></script>
    <script type="text/javascript" language="javascript" src="js/mindmapList.js"></script>

    <!-- Update timer plugging -->
    <script type="text/javascript" language="javascript" src="js/jquery.timeago.js"></script>


    <script type="text/javascript" language="javascript">
        var principalEmail = '${principal.email}';
    </script>

</head>
<body>
<jsp:include page="header.jsp">
    <jsp:param name="removeSignin" value="false"/>
    <jsp:param name="showLogout" value="true"/>
</jsp:include>

<div style="min-height: 500px">

    <div id="mindmapListContainer">
        <div id="messagesPanel" class="alert alert-error alert-block fade in hide" style="margin-top: 10px">
            <strong><spring:message code="UNEXPECTED_ERROR"/></strong>

            <p><spring:message code="UNEXPECTED_ERROR_SERVER_ERROR"/></p>

            <div></div>
        </div>

        <div id="foldersContainer">
            <ul class="nav nav-list">
                <li class="nav-header">Filters</li>
                <li data-filter="all" class="active"><a href="#"><i class="icon-inbox icon-white"></i> <spring:message
                        code="ALL_MAPS"/></a></li>
                <li data-filter="my_maps"><a href="#"><i class="icon-user"></i> <spring:message code="MY_MAPS"/></a>
                </li>
                <li data-filter="shared_with_me"><a href="#"><i class="icon-share"></i> <spring:message
                        code="SHARED_WITH_ME"/></a></li>
                <li data-filter="starred"><a href="#"><i class="icon-star"></i> <spring:message code="STARRED"/></a>
                </li>
                <li data-filter="public"><a href="#"><i class="icon-globe"></i> <spring:message code="PUBLIC_MAPS"/></a>
                </li>
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
                        <li id="publishBtn"><a href="#" onclick="return false"><i class="icon-globe"></i> Publish</a>
                        </li>
                        <li id="shareBtn"><a href="#" onclick="return false"><i class="icon-share"></i> Share</a></li>
                        <li id="exportBtn"><a href="#" onclick="return false"><i class="icon-download"></i> Export</a>
                        </li>
                        <li id="printBtn"><a href="#" onclick="return false"><i class="icon-print"></i> Print</a></li>
                        <li id="historyBtn"><a href="#" onclick="return false"><i class="icon-time"></i> History</a>
                        </li>
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
    <div id="new-dialog-modal" title="Add new map" class="modal fade">
        <div class="modal-header">
            <button class="close" data-dismiss="modal">x</button>
            <h3><spring:message code="NEW_MAP_MSG"/></h3>
        </div>
        <div class="modal-body">
            <div class="errorMessage"></div>
            <form class="form-horizontal">
                <fieldset>
                    <div class="control-group">
                        <label class="control-label" for="newTitle"><spring:message code="NAME"/>:</label>
                        <input class="control" name="title" id="newTitle" type="text" required="required"
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
            <button class="btn btn-primary btn-accept" data-loading-text="Saving ..."><spring:message
                    code="CREATE"/></button>
            <button class="btn btn-cancel" data-dismiss="modal"><spring:message code="CANCEL"/></button>
        </div>
    </div>

    <!-- Duplicate map dialog -->
    <div id="duplicate-dialog-modal" class="modal fade">
        <div class="modal-header">
            <button class="close" data-dismiss="modal">x</button>
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
            <button class="btn btn-primary btn-accept" data-loading-text="<spring:message code="SAVING"/> ...">
                <spring:message code="DUPLICATE"/></button>
            <button class="btn btn-cancel" data-dismiss="modal"><spring:message code="CANCEL"/></button>
        </div>
    </div>

    <!-- Rename map dialog -->
    <div id="rename-dialog-modal" class="modal fade">
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
            <button class="btn btn-primary btn-accept" data-loading-text="Saving ..."><spring:message
                    code="RENAME"/></button>
            <button class="btn btn-cancel" data-dismiss="modal"><spring:message code="CANCEL"/></button>
        </div>
    </div>

    <!-- Delete map dialog -->
    <div id="delete-dialog-modal" class="modal fade">
        <div class="modal-header">
            <button class="close" data-dismiss="modal">x</button>
            <h3><spring:message code="DELETE_MINDMAP"/></h3>
        </div>
        <div class="modal-body">
            <div class="alert alert-block">
                <h4 class="alert-heading">Warning!</h4>Deleted mindmap can not be recovered. Do you want to
                continue ?.
            </div>
        </div>
        <div class="modal-footer">
            <button class="btn btn-primary btn-accept" data-loading-text="Saving ..."><spring:message
                    code="DELETE"/></button>
            <button class="btn btn-cancel" data-dismiss="modal"><spring:message code="CANCEL"/></button>
        </div>
    </div>

    <!-- Info map dialog -->
    <div id="info-dialog-modal" class="modal fade">
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

    <!-- Publish Dialog Config -->
    <div id="publish-dialog-modal" class="modal fade">
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

    <!-- Export Dialog Config -->
    <div id="export-dialog-modal" class="modal fade">
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

    <!-- Import Dialog Config -->
    <div id="import-dialog-modal" class="modal fade">
        <div class="modal-header">
            <button class="close" data-dismiss="modal">x</button>
            <h3><spring:message code="IMPORT"/></h3>
        </div>
        <div class="modal-body">

        </div>
        <div class="modal-footer">
            <button class="btn btn-primary btn-accept" data-loading-text="Importing..."><spring:message
                    code="IMPORT"/></button>
            <button class="btn btn-cancel" data-dismiss="modal"><spring:message code="CANCEL"/></button>
        </div>
    </div>

    <!-- Share Dialog Config -->
    <div id="share-dialog-modal" class="modal fade">
        <div class="modal-header">
            <button class="close" data-dismiss="modal">x</button>
            <h3><spring:message code="SHARE"/></h3>
        </div>
        <div class="modal-body">

        </div>
        <div class="modal-footer">
            <button class="btn btn-primary btn-accept" data-loading-text="<spring:message code="SAVING"/> ...">
                <spring:message code="ACCEPT"/></button>
            <button class="btn btn-cancel" data-dismiss="modal"><spring:message code="CANCEL"/></button>
        </div>
    </div>

    <!-- History Dialog Config -->
    <div id="history-dialog-modal" class="modal fade">
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
</body>
</html>
