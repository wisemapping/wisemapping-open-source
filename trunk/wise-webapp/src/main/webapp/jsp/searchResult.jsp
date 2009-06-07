<%@ include file="/jsp/init.jsp" %>
<script type="text/javascript">
    window.onload = function() {
        var boxGenerator = RUZEE.ShadedBorder.create({ corner:16,  border:1 });
        boxGenerator.render('searchResult');

        $("broweButton").addEvent('click', function() {
            window.location = "${pageContext.request.contextPath}/c/search.htm?action=showAll";
        });
    };

    function toogleAdvanceSearch() {
        var searchDiv = document.getElementById("searchAdvance");
        var newDisplayValue = "none";
        if (searchDiv.style.display == "none") {
            newDisplayValue = "";
        }
        document.getElementById("searchFormCell").style.display = searchDiv.style.display;
        searchDiv.style.display = newDisplayValue;
    }

</script>
<div class="content">
<c:url value="mymaps.htm" var="mapDetail">
    <c:param name="action" value="detail"/>
</c:url>

<div id="searchResult" class="sb">
<h1 style="margin-right:5px;">
    <spring:message code="SEARCH_PUBLIC"/>
</h1>

<table>
    <tr>
        <td colspan="2">
            <spring:message code="SEARCH_MSG"/>
            <br/><br/></td>
    </tr>
    <tr>
        <td> &nbsp;
        </td>
        <td id="searchFormCell" style="display:${advanceSearch != null ? 'none' : ''}">
            <form method="post" id="searchForm" name="searchForm" action="<c:url value="search.htm"/>">
                <input type="hidden" name="action" value="search"/>
                <input id="titleOrTags" name="titleOrTags" value="${titleOrTags}"/>
                <input type="submit" value="<spring:message code="SEARCH"/>" id="submitButton1" class="btn-primary">
                <input type="button" value="<spring:message code="BROWSE"/>" id="broweButton" class="btn-primary">
                <br/>
                <a href="javascript:toogleAdvanceSearch()" id="toAdvancedSearch">
                    <spring:message code="ADVANCE_SEARCH"/>
                </a>

            </form>
        </td>
    </tr>
</table>

<div id="searchAdvance" style="display:${advanceSearch != null ? '' : 'none'};">
    <form method="post" id="advanceSearchForm" name="searchForm" action="<c:url value="search.htm"/>">
        <input type="hidden" name="action" value="search"/>
        <input type="hidden" name="advanceSearch" value="true"/>
        <table>
            <thead>
                <tr>
                    <th colspan="2">
                        <spring:message code="ADVANCE_SEARCH"/>
                    </th>
                </tr>
            </thead>
            <tbody>
                <tr>
                    <td class="formLabel">
                        <spring:message code="NAME"/>
                        :
                    </td>
                    <td>
                        <input id="name" name="name" value="${name}"/>
                    </td>
                </tr>
                <tr>
                    <td class="formLabel">
                        <spring:message code="DESCRIPTION"/>
                        :
                    </td>
                    <td>
                        <input id="description" name="description" value="${description}"/>
                    </td>
                </tr>
                <tr>
                    <td class="formLabel">
                        <spring:message code="TAGS"/>
                        :
                    </td>
                    <td>
                        <input id="tags" name="tags" value="${tags}"/>
                    </td>
                </tr>
                <tr>
                    <td>&nbsp;</td>
                    <td><input type="submit" value="<spring:message code="SEARCH"/>" id="submitButton2"
                               class="btn-primary">&nbsp;
                        <a href="javascript:toogleAdvanceSearch()" id="toSimpleSearch">
                            <spring:message code="SIMPLE_SEARCH"/>
                        </a>
                    </td>
                </tr>
            </tbody>
        </table>
    </form>
</div>
<div id="searchResultTable">
    <table>
        <colgroup>
            <col width="30%"/>
            <col width="40%"/>
            <col width="30%"/>
        </colgroup>
        <thead>
            <tr>
                <th>
                    <spring:message code="NAME"/>
                </th>
                <th>
                    <spring:message code="DESCRIPTION"/>
                </th>
                <th>
                    <spring:message code="LAST_EDITOR"/>
                </th>
            </tr>
        </thead>
        <tbody>
            <c:if test="${emptyCriteria}">
                <tr>
                    <td colspan="3">
                        <spring:message code="NO_SEARCH_TERM"/>
                        <br>
                        <a href="${pageContext.request.contextPath}/c/search.htm?action=showAll">
                            <spring:message code="VIEW_ALL_RESULTS"/>
                        </a>
                    </td>
                </tr>
            </c:if>
            <c:if test="${!emptyCriteria and empty wisemapsList}">
                <tr>
                    <td colspan="3">
                        <spring:message code="NO_SEARCH_RESULTS"/>
                        <c:if test="${user != null}">
                            <br>
                            <a href="${pageContext.request.contextPath}/c/mymaps.htm">
                                <spring:message code="GO_TO"/>
                            </a>
                        </c:if>
                        <c:if test="${user == null}">
                            <dl>
                                <dt>
                                    <spring:message code="JOIN_NOW"/>
                                </dt>
                            </dl>
                            <br>
                            <a href="${pageContext.request.contextPath}/c/userRegistration.htm">
                                <spring:message code="NOT_READY_A_USER_MESSAGE"/>
                            </a>
                        </c:if>
                    </td>
                </tr>
            </c:if>
            <c:forEach items="${wisemapsList}" var="mindmap">
                <tr>
                    <td>
                        <div>
                            <div class="mapTitle">
                                <a href="${pageContext.request.contextPath}/c/publicView.htm?mapId=${mindmap.id}">
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
                    <td>${mindmap.lastEditDate} by ${mindmap.lastEditor}</td>
                </tr>
            </c:forEach>
        </tbody>
    </table>
</div>
</div>
</div>

