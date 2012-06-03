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
                <li data-filter="all" class="active"><a href="#"><i class="icon-inbox icon-white"></i> All</a></li>
                <li data-filter="my_maps"><a href="#"><i class="icon-user"></i> My Maps</a></li>
                <li data-filter="shared_with_me"><a href="#"><i class="icon-share"></i> Shared With Me</a></li>
                <li data-filter="starred"><a href="#"><i class="icon-star"></i> Starred</a></li>
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

    <!-- Export Dialog Config -->
    <div id="export-dialog-modal" class="modal fade" style="display: none">
        <div class="modal-header">
            <button class="close" data-dismiss="modal">x</button>
            <h3>Export</h3>
        </div>
        <div class="modal-body">

        </div>
        <div class="modal-footer">
            <button class="btn btn-primary btn-accept" data-loading-text="Exporting...">Export</button>
            <button class="btn btn-cancel" data-dismiss="modal">Cancel</button>
        </div>
    </div>

</div>
</body>
</html>
