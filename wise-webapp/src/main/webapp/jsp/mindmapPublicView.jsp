<%@ include file="/jsp/init.jsp" %>
<h1>
    <spring:message code="PUBLIC_VIEW_TITLE" arguments="'${mindmap.title}'"/>
</h1>


<c:url value="embeddedView?mapId=${mindmap.id}&fullView=true"
       var="embeddedUrl"/>


<div id="publicViewContent">
    <iframe style="border:0;width:800px;height:420px;border: 1px solid black;" src="${embeddedUrl}">

    </iframe>

</div>