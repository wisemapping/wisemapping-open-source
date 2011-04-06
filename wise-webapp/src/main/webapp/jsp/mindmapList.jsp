<!DOCTYPE HTML>

<%@ page import="com.wisemapping.view.MindMapBean" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Collections" %>
<%@ page import="java.util.ArrayList" %>
<%@ include file="/jsp/init.jsp" %>
<html>
<head>
    <title>
        <spring:message code="SITE.TITLE"/>
    </title>
    <meta http-equiv="Content-type" content="text/html; charset=utf-8"/>
    <link rel="stylesheet" type="text/css" href="../css/mymaps.css"/>
    <link rel="icon" href="${pageContext.request.contextPath}/images/favicon.ico" type="image/x-icon"/>
    <link rel="shortcut icon" href="${pageContext.request.contextPath}/images/favicon.ico" type="image/x-icon"/>
    <script type="text/javascript" src="${pageContext.request.contextPath}/js/wiseLibrary.js"></script>
    <script type="text/javascript" src="${pageContext.request.contextPath}/js/wiseListLibrary.js"></script>
    <!--[if lt IE 9]>
    <link rel="stylesheet" type="text/css" href="../css/mymapsOldIE.css"/>
    <script type="text/javascript" src="${pageContext.request.contextPath}/js/shadedborder.js"></script>    
    <script type="text/javascript" src="${pageContext.request.contextPath}/js/transcorners.js"></script>    
    <script type="text/javascript">
        window.onload = function() {
            var boxGenerator = RUZEE.ShadedBorder.create({ corner:16,  border:1 });
            boxGenerator.render('recentFiles');
            boxGenerator.render('recentItems');
            $('mydocs').makeRounded({radius: 16,borderColor: '#a7c6df',backgroundColor: '#c3def5'});
        };

    </script>
    <![endif]-->

</head>
<body>
<div class="content">
<jsp:include page="header.jsp">
    <jsp:param name="removeSignin" value="false"/>
    <jsp:param name="showLogout" value="true"/>
</jsp:include>


<c:url value="mymaps.htm" var="mapDetail">
    <c:param name="action" value="detail"/>
</c:url>
<c:url value="mymaps.htm" var="deleteSelectedMapUrl">
    <c:param name="action" value="deleteAll"/>
    <c:param name="userEmail" value="${pageContext.request.userPrincipal.name}"/>
</c:url>

<div id="mapListContainer">

<div id="myDocsContainer">

<div id="recentFiles" class="sb">
    <div id="recentText">
        <spring:message code="RECENT_FILES"/>
    </div>
    <div id="recentItems" class="sb">
        <div class="recentItemContainer">
            <%
                int MAX_RECENT_MAPS = 6;
                List<MindMapBean> list = (List<MindMapBean>) request.getAttribute("wisemapsList");
                if (list != null && !list.isEmpty()) {
                    List<MindMapBean> recentMaps = new ArrayList<MindMapBean>();
                    recentMaps.addAll(list);
                    Collections.sort(recentMaps, new MindMapBean.MindMapBeanComparator());
                    for (int i = recentMaps.size() - 1; i >= 0 && i >= recentMaps.size() - MAX_RECENT_MAPS; i--) {
            %>

            <div class="recentItemIcon"></div>
            <div class="recentItemTitle">
                <a href="javascript:openMap('<%=recentMaps.get(i).getId()%>')">
                    <%=recentMaps.get(i).getTitle()%>
                </a>
            </div>

            <%
                }
            } else {
            %>
            <div class="recentItemTitle">
                <spring:message code="EMPTY_MINDMAP_TABLE"/>
            </div>
            <%
                }
            %>
        </div>
    </div>
</div>

<div id="mydocs">
    <div id="toolbar">
        <input id="selectedMapIds" type="hidden" value=""/>

        <div class="leftMenu">
            <div class="button">
                <a href="newMap.htm" rel="moodalbox 500px 190px" title="<spring:message code="NEW_MAP_MSG"/>">
                    <spring:message code="NEW_MINDMAP"/>
                </a>
            </div>
            <div class="button menuLink" onclick="updateLinks($(this).getParent()); new Windoo.Confirm('<spring:message code="DELETE_SELECTED_CONFIRMATION"/>',
                                                  {
                                                    'window': {theme:Windoo.Themes.wise,
                                                            title:'<spring:message code="DELETE_MAP"/>'
                                                    },
                                                    'onConfirm':function(){
                                                        $(document.toolbarForm.mindmapIds).value=$('selectedMapIds').value;
                                                        $(document.toolbarForm).action='<c:out value="${deleteSelectedMapUrl}" escapeXml="true"/>';
                                                        $(document.toolbarForm).submit();
                                                    }
                                                });">
                <spring:message code="DELETE_SELECTED"/>
            </div>
        </div>
        <div class="button">
            <a href="importMap.htm" rel="moodalbox 500px 250px" title="<spring:message code="IMPORT_MINDMAP_DETAILS"/>">
                <spring:message code="IMPORT_MINDMAP"/>
            </a>
        </div>
    </div>
    <c:url value="mymaps.htm" var="shareMap">
        <c:param name="action" value="collaborator"/>
        <c:param name="userEmail" value="${pageContext.request.userPrincipal.name}"/>
    </c:url>
    <c:url value="mymaps.htm" var="deleteMapUrl">
        <c:param name="action" value="delete"/>
        <c:param name="userEmail" value="${pageContext.request.userPrincipal.name}"/>
    </c:url>
    <c:url value="mymaps.htm" var="changeStatus">
        <c:param name="action" value="changeStatus"/>
        <c:param name="userEmail" value="${pageContext.request.userPrincipal.name}"/>
    </c:url>
    <div id="docTable">
        <table>
            <colgroup>
                <col style="width:3%;"/>
                <col style="width:3%;"/>
                <col style="width:15%;"/>
                <col style="width:30%;"/>
                <col style="width:14%;"/>
                <col style="width:10%;"/>
                <col style="width:20%;"/>
                <col style="width:5%;"/>
            </colgroup>
            <thead>
            <tr>
                <th style="text-align:center">
                    <label for="checkAll"></label><input id="checkAll" type="checkbox" onchange="selectAllMaps(this);">
                </th>
                <th>
                    <spring:message code="FILE"/>
                </th>
                <th>
                    <spring:message code="NAME"/>
                </th>
                <th>
                    <spring:message code="DESCRIPTION"/>
                </th>

                <th>
                    <spring:message code="STATUS"/>
                </th>
                <th>
                    <spring:message code="CREATOR"/>
                </th>
                <th>
                    <spring:message code="LAST_EDITOR"/>
                </th>
                <th>&nbsp;
                </th>
            </tr>
            </thead>
            <tbody>
            <c:forEach items="${wisemapsList}" var="mindmap">
                <tr>
                    <td>
                        <div style="text-align:center;"><input type="checkbox" name="chk" id="chk${mindmap.id}" onclick="addToSelectedMapList(this);"></div>
                    </td>
                    <td>
                        <div class="leftMenu">
                            <img src="../images/icon_list.png" class="button" style="display:block;border:0;">
                            <div class="subMenu2">
                                <a href="javascript:openMap('${mindmap.id}')" title="<spring:message code="OPEN_MSG"/>">
                                    <spring:message code="OPEN"/>
                                </a>
                                <c:if test="${mindmap.owner==requestScope.user}">
                                    <a href="renameMap.htm?mapId=${mindmap.id}" rel="moodalbox 400px 180px wizard" title="<spring:message code="RENAME_DETAILS"/>">
                                        <spring:message code="RENAME"/>
                                    </a>
                                </c:if>
                                <a href="history.htm?action=list&amp;goToMindmapList&amp;mapId=${mindmap.id}" rel="moodalbox 600px 400px wizard" title="<spring:message code="HISTORY_INFO"/>">
                                    <spring:message code="HISTORY"/>
                                </a>

                                <div class="menuButton menuLink subMenu2Sep" onclick="new Windoo.Confirm('<spring:message code="DELETE_CONFIRMATION"/>',
                                                    {
                                                         window: {'theme':Windoo.Themes.wise,
                                                                     title:'<spring:message code="DELETE_MAP"/>'
                                                   },
                                                    'onConfirm':function(){
                                                        var form = new Element('form').setProperties({action: '<c:out value="${deleteMapUrl}" escapeXml="true"/>&amp;mapId=${mindmap.id}', method:'post'}).injectInside(document.body);
                                                        form.submit();
                                                    }
                                                });">
                                    <spring:message code="DELETE"/>
                                </div>
                                <a href="tags.htm?mapId=${mindmap.id}" rel="moodalbox 400px 200px wizard" title="<spring:message code="TAGS_DETAILS"/>">
                                    <spring:message code="TAGS"/>
                                </a>
                                <c:if test="${mindmap.owner==requestScope.user}">
                                    <a href="<c:out value="${shareMap}" escapeXml="true"/>&amp;mapId=${mindmap.id}" rel="moodalbox 780px 530px wizard" title="<spring:message code="SHARE_DETAILS"/>">
                                        <spring:message code="COLLABORATION"/>
                                    </a>
                                    <a class="subMenu2Sep" href="publish.htm?mapId=${mindmap.id}" rel="moodalbox 600px 400px wizard" title="<spring:message code="PUBLISH_MSG"/>">
                                        <spring:message code="PUBLISH"/>
                                    </a>
                                </c:if>
                                <%--<a href="export.htm?mapId=${mindmap.id}"
                                   rel="moodalbox 600px 400px" title="<spring:message code="EXPORT_DETAILS"/>">
                                    <spring:message code="EXPORT"/>
                                </a>--%>
                                <a href="javascript:printMap(${mindmap.id});">
                                    <spring:message code="PRINT"/>
                                </a>
                            </div>
                        </div>
                    </td>
                    <td>
                        <div>
                            <div class="mapTitle">
                                <a href="javascript:openMap('${mindmap.id}')">
                                        ${mindmap.title}
                                </a>
                            </div>
                            <div class="mapTags">
                                    ${mindmap.tags}
                            </div>
                        </div>
                    </td>
                    <td>
                            ${mindmap.description}
                    </td>
                    <td>
                        <c:choose>
                            <c:when test="${not mindmap.public}">
                                <img src="../images/key.png"
                                     title="<spring:message code="PRIVATE"/>: <spring:message code="ONLY_VIEW_PRIVATE"/>"
                                     alt="<spring:message code="PRIVATE"/>"/>
                            </c:when>
                            <c:otherwise>
                                <img src="../images/world2.png" title="<spring:message code="PUBLIC"/>: <spring:message code="ALL_VIEW_PUBLIC"/>" alt="World"
                                <spring:message code="PUBLIC"/>
                            </c:otherwise>
                        </c:choose>
                    </td>
                    <td>${mindmap.creationUser}</td>
                    <td>${mindmap.lastEditDate} by ${mindmap.lastEditor}</td>
                    <td><a href="<c:out value="${mapDetail}" escapeXml="true"/>&amp;mapId=${mindmap.id}">
                        <spring:message code="DETAIL"/>
                    </a>
                    </td>
                </tr>
            </c:forEach>
            </tbody>

        </table>
    </div>
</div>

<div id="ds1" class="submenu" style="display:none;">
    <a href="<c:out value="${deleteSelectedMapUrl}" escapeXml="true"/>&amp;mindmapIds=" rel="moodalbox 300px 120px">Delete</a>
    <a href="#">Tag</a>
    <a href="#">Publish</a>
</div>

<div id="ds2" class="submenu" style="position:absolute; display:none;">
    <a href="<c:out value="${shareMap}" escapeXml="true"/>&amp;mapId=" rel="moodalbox 780px 530px" title="Share WiseMap">Share</a>
    <a href="publish.htm?mapId=" rel="moodalbox 600px 400px wizard" title="<spring:message code="PUBLISH_DETAILS"/>">
        <spring:message code="PUBLISH"/>
    </a>
    <a href="<c:out value="${deleteMapUrl}" escapeXml="true"/>&amp;mapId=" rel="moodalbox 300px 120px" title="Delete Confirmation">
        Delete
    </a>
    <a href="export.htm?mapId=" rel="moodalbox 750px 400px" title="<spring:message code="EXPORT_DETAILS"/>">
        <spring:message code="EXPORT"/>
    </a>

</div>
</div>
</div>

<c:url value="editor.htm" var="mindmapEditorUrl"/>
<form name="openForm" action="${mindmapEditorUrl}" method="post">
    <input type="hidden" name="action" value="open"/>
    <input type="hidden" name="mapId" value=""/>
</form>
<form name="toolbarForm" method="post">
    <input type="hidden" name="mindmapIds"/>
</form>
<form method="post" id="printForm" name="printForm" action="<c:url value="export.htm"/>" target="new">
    <input type="hidden" name="action" value="print"/>
    <input type="hidden" name="mapId" value="${mindmap.id}"/>
</form>
</div>
<script type="text/javascript">
    function openMap(mapId) {
        document.openForm.mapId.value = mapId;
        document.openForm.submit();
    }
    function deleteOkButton(url)
    {
        var form = document.createElement('form');
        form.method = 'post';
        form.action = url;
        document.body.appendChild(form);
        form.submit();
    }

    Window.onDomReady(initDropDowns);
    function initDropDowns()
    {
        $ES('li[rel="submenu"]', $(document.body)).each(function(el) {
            var items = $E('ul', el);
            el.addEvent('click', showMenu.bind(items));
        }, this);
    }

    function showMenu(evt)
    {
        if ($(document).onclick)
        {
            $(document).fireEvent('click', 0);
            showMenu.delay(110, this, evt);
        }
        else
        {
            this.myEffect = $(this).effects({duration:100, transition: Fx.Transitions.linear});
            this.myEffect.start({'opacity':[0,1]});
            $(document).onclick = hide.bind(this);
            var event = new Event(evt);
            event.stop();
        }
    }

    function hide()
    {
        this.myEffect.start({'opacity':[1,0]});
        $(document).onclick = '';
    }

    function updateLinks(el)
    {
        $ES('a', el).each(function(link) {
            if (!link.ohref)
            {
                link.ohref = link.getProperty('ohref');
            }
            link.href = link.ohref + $('selectedMapIds').value;
        });
    }

    function openWizard(href, title, rel)
    {
        href = href + $('selectedMapIds').value;
        MOOdalBox.open(href, title, rel);
    }

    function addToSelectedMapList(el)
    {
        var ids = $('selectedMapIds');
        var id = el.id.replace(/[^\d]/g, '');

        var value = ids.getProperty("value");

        if (value != "")
        {
            var allIds = $A(ids.value.split(','));
            var changed = false;
            if (allIds.contains(id) && !el.checked)
            {
                allIds.remove(id);
                changed = true;
            }
            else if (!allIds.contains(id) && el.checked)
            {
                allIds.extend([id]);
                changed = true;
            }
            if (changed)
            {
                var finalIds = "";
                $each(allIds, function(el) {
                    if (!finalIds == "")
                    {
                        el = "," + el;
                    }
                    finalIds = finalIds + el;
                });
                value = finalIds;
            }
        }
        else
        {
            if (el.checked)
            {
                value = id;
            }
        }
        ids.setProperty("value", value);
    }


    function selectAllMaps(elem)
    {
        var value = elem.checked;
        var ids = "";
        $ES('input[type="checkbox"]', $('docTable')).each(function(el) {
            if (el.name.contains('chk'))
            {
                el.checked = value;
                var id = el.id.replace(/[^\d]/g, '');
                if (!ids == "")
                {
                    id = "," + id;
                }
                ids = ids + id;
            }
        });
        if (!value)
        {
            $('selectedMapIds').setProperty('value', '');
        }
        else
        {
            $('selectedMapIds').setProperty('value', ids);
        }
        /*var allElems = document.getElementsByName("chk");
         for (var i = 0; i < allElems.length; i++)
         {
         allElems[i].checked = value;
         }*/
    }

    function printMap(mapId) {
        document.printForm.mapId.value = mapId;
        document.printForm.submit();
    }

</script>
<script type="text/javascript">

    MOOdalBox.reloadRequered = true;

    function removeCollaborator(collaboratorEmail)
    {
        document.removeCollaboratorForm.colaboratorId.value = collaboratorEmail;
        document.removeCollaboratorForm.submit();
        //submitDialog('removeCollaboratorForm');
    }
    function addFriendsEmails()
    {
        var newUsers = "";
        var ob = $('friendList');
        while (ob.selectedIndex != -1)
        {
            newUsers = newUsers + ", " + ob.options[ob.selectedIndex].value;
            ob.options[ob.selectedIndex].selected = false;
        }
        var dest = $('emailList');
        if (dest.value == "")
            newUsers = newUsers.substr(2, newUsers.length);
        dest.value = dest.value + newUsers;
    }
    function changeStatus(collaboratorEmail)
    {
        document.removeCollaboratorForm.userEmail.value = collaboratorEmail;
        submitDialog('removeCollaboratorForm');
    }

</script>
<jsp:include page="footer.jsp"/>
</body>
</html>
