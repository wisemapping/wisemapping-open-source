<!DOCTYPE HTML>

<%@ include file="/jsp/init.jsp" %>
<html>
<head>
<base href="../">
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
                    mDataProp: "title"
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
                    sType: "numeric",
                    mDataProp: "lastModificationDate",
                    fnRender : function(obj) {
                        return obj.aData.lastModificationDate + ", " + obj.aData.lastModifierUser;
                    }
                },
                {
                    sTitle: "Details",
                    sClass: "center",
                    sWidth : "15px",
                    bSortable : false,
                    bSearchable : false,
                    fnRender : function(obj) {
                        return '<span class="ui-icon ui-icon-circle-triangle-e" style="margin: 0 auto;"></span>';
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
                    $("#new-dialog-modal").dialog({
                        modal: true,
                        buttons: {
                            "Create": function() {
                                var formData = {};
                                $('#new-dialog-modal input').each(function(index, elem) {
                                    formData[elem.name] = elem.value;
                                });

                                jQuery.ajax("../service/maps", {
                                    async:false,
                                    dataType: 'json',
                                    data: JSON.stringify(formData),
                                    type: 'POST',
                                    contentType:"application/json; charset=utf-8",
                                    success : function(data, textStatus, jqXHR) {
                                        var location = jqXHR.getResponseHeader("Location");
                                        var mapId = location.substring(location.lastIndexOf('/') + 1, location.length);
                                        window.location = "c/editor.htm?action=open&mapId=" + mapId;
                                    },
                                    error: function() {
                                        alert("Unexpected error removing maps. Refresh before continue.");
                                    }
                                });
                            },
                            Cancel: function() {
                                $(this).dialog("close");
                            }
                        }
                    });
                });

        $("#buttons .importMap").button({
            icons: { primary: "ui-icon-trash" }
        });

        $("#buttons .moreActions").button({
            icons: { primary: "ui-icon-triangle-1-s" }
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
    });
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
            <div id="new-dialog-modal" title="New" style="display: none">
                <table>
                    <tr>
                        <td class="formLabel">
                            <span class="fieldRequired">*</span>
                            <label for="title"><spring:message code="NAME"/>:</label>
                        </td>
                        <td>
                            <input name="title" id="title" tabindex="1" type="text" required="true"/>
                        </td>
                    </tr>
                    <tr>
                        <td class="formLabel">
                            <label for="description"><spring:message code="DESCRIPTION"/>:</label>
                        </td>
                        <td>
                            <input name="description" id="description" tabindex="2"/>
                        </td>
                    </tr>
                </table>
            </div>
            <div id="share-dialog-modal" title="Share maps" style="display: none">
                <p>Are you sure you want to share maps <span></span> ?</p>
            </div>
            <button class="newMap">New</button>
            <button class="delete">Delete</button>
            <button class="importMap">Import</button>
            <button class="moreActions">More</button>
        </div>

        <div>
            <div id="tags">
                <h2>Mes dossiers:</h2>

                <div id="tags-list"></div>
                <div id="tags-actions">
                    <button>Nouveau Dossier</button>
                </div>
            </div>
            <div id="map-table">
                <table cellpadding="0" cellspacing="0" border="0" class="display" id="mindmapListTable">

                </table>
            </div>
        </div>
    </div>
</div>


<jsp:include page="footer.jsp"/>
</body>
</html>
