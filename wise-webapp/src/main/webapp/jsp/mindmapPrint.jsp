<%@ page import="java.text.DateFormat" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.util.Calendar" %>

<%@ include file="/jsp/init.jsp" %>

<%
    Calendar calendar = Calendar.getInstance(request.getLocale());
    calendar.setTimeInMillis(System.currentTimeMillis());
    DateFormat dateFormat = SimpleDateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, request.getLocale());
    String todayString = dateFormat.format(calendar.getTime());
%>
<!DOCTYPE HTML>
<html>
<head>
    <!--[if lt IE 9]>
    <meta http-equiv="X-UA-Compatible" content="chrome=1">
    <![endif]-->
    <title><spring:message code="SITE.TITLE"/> - ${mindmap.title} </title>
    <meta http-equiv="Content-type" content="text/html; charset=UTF-8"/>
    <meta http-equiv="Content-type" content="text/html; charset=UTF-8"/>
    <link rel="icon" href="../images/favicon.ico" type="image/x-icon">
    <link rel="shortcut icon" href="../images/favicon.ico" type="image/x-icon">


    <link rel="stylesheet" type="text/css" href="../css/print.css"/>

    <script type='text/javascript' src='../js/mootools-core.js'></script>
    <script type='text/javascript' src='../js/mootools-more.js'></script>
    <script type='text/javascript' src='../js/core.js'></script>


    <script type="text/javascript">
        var mapId = '${mindmap.id}';
        var mapXml = '${mapXml}';
        var mindReady = false;
        $(document).addEvent('loadcomplete', function(resource) {
            mindReady = resource == 'mind' ? true : mindReady;
            if (mindReady) {

                var editorProperties = {zoom:0.85,saveOnLoad:true,collab:'standalone',readOnly:true};
                designer = buildDesigner(editorProperties);

                var domDocument = core.Utils.createDocumentFromText(mapXml);
                var serializer = mindplot.persistence.XMLSerializerFactory.getSerializerFromDocument(domDocument);
                var mindmap = serializer.loadFromDom(domDocument, mapId);

                // Now, load the map ...
                designer.loadMap(mindmap);

                $('zoomIn').addEvent('click', function() {
                    designer.zoomIn();
                });

                $('zoomOut').addEvent('click', function() {
                    designer.zoomOut();
                });
            }
        });
    </script>
</head>
<body onload="setTimeout('print()', 5)">

<div id="waitDialog" style="display:none">
    <div id="waitingContainer">
        <div class="loadingIcon"></div>
        <div class="loadingText">
            Loading ...
        </div>
    </div>
</div>

<div id="errorDialog" style="display:none">
    <div id="errorContainer">
        <div class="loadingIcon"></div>
        <div class="loadingText">
            Unexpected error loading your map :(
        </div>
    </div>
</div>

<div id="printHeader">
    <div id="printLogo"></div>
    <div id="headerTitle">${mindmap.title}<span id="headerSubTitle">&nbsp;(<%=todayString%>)</span></div>
</div>
<center>
    <div id="mindplot"></div>
</center>
<script type="text/javascript" src="../js/editor.js"></script>
</body>
</html>
