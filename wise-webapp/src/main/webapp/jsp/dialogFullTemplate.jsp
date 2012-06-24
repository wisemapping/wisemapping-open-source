<!DOCTYPE HTML>

<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ include file="/jsp/init.jsp" %>

<tiles:importAttribute name="title" scope="page"/>
<tiles:importAttribute name="details" scope="page"/>

<html>
<head>
    <base href="${baseURL}/">
    <meta http-equiv="Content-type" content="text/html; charset=utf-8"/>
    <!--[if lt IE 9]>
    <meta http-equiv="X-UA-Compatible" content="chrome=1">
    <![endif]-->
    <script type="text/javascript" language="javascript" src="js/jquery-1.7.2.min.js"></script>
    <script type="text/javascript" language="javascript" src="bootstrap/js/bootstrap.js"></script>
    <link rel="stylesheet" type="text/css" href="bootstrap/css/bootstrap.min.css"/>
    <link rel="stylesheet" type="text/css" href="bootstrap/css/bootstrap-responsive.min.css"/>
    <script type="text/javascript" language="javascript" src="js/jquery.timeago.js"></script>
<body>
<div style="padding-top:20px">
    <tiles:insertAttribute name="body"/>
</div>
</body>
</html>
