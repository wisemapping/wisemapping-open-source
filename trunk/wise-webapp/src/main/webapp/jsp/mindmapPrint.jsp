<%@ page import="java.text.DateFormat" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.util.Calendar" %>
<%
    Calendar calendar = Calendar.getInstance(request.getLocale());
    calendar.setTimeInMillis(System.currentTimeMillis());
    DateFormat dateFormat = SimpleDateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, request.getLocale());
    String todayString = dateFormat.format(calendar.getTime());
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<%@ include file="/jsp/init.jsp" %>
<c:url value="export.htm" var="exportUrl">
    <c:param name="action" value="image"/>
    <c:param name="mapId" value="${mindmap.id}"/>
</c:url>
<html>
<head>
    <title>
        <spring:message code="PRINT"/>
        - ${mindmap.title}</title>
    <link rel="stylesheet" type="text/css" href="../css/common.css">
</head>
<body onload="setTimeout('print()', 5)">
    <div id="printHeader">
        <div id="printLogo"></div>
        <div id="headerTitle">${mindmap.title}<span id="headerSubTitle">&nbsp;(<%=todayString%>)</span></div>
    </div>
    <center>
        <img src="${exportUrl}" alt="${mindmap.title}"/>
    </center>
</body>
</html>