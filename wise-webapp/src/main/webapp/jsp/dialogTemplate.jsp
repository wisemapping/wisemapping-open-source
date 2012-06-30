<%@page pageEncoding="UTF-8" %>
<%@include file="/jsp/init.jsp" %>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>

<!DOCTYPE HTML>

<tiles:importAttribute name="title" scope="page"/>
<tiles:importAttribute name="details" scope="page"/>
<html>
<head>
</head>
<body>
<div>
    <tiles:insertAttribute name="body"/>
</div>
</body>
</html>