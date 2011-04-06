<%@ page import="java.text.DateFormat" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.util.Calendar" %>
<%
    Calendar calendar = Calendar.getInstance(request.getLocale());
    calendar.setTimeInMillis(System.currentTimeMillis());
    DateFormat dateFormat = SimpleDateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, request.getLocale());
    String todayString = dateFormat.format(calendar.getTime());
%>
<!DOCTYPE HTML PUBLIC>
<%@ include file="/jsp/init.jsp" %>
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
        <img src="${mapSvg}" alt="${mindmap.title}"/>
    </center>
</body>
</html>