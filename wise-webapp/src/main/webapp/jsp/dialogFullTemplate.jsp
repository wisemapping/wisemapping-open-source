<%@ taglib prefix="tiles" uri="http://struts.apache.org/tags-tiles" %>
<%@ include file="/jsp/init.jsp" %>

<tiles:importAttribute name="title" scope="page"/>
<tiles:importAttribute name="details" scope="page"/>

<html>
<head>
    <base href="${pageContext.request.contextPath}/"/>
    <meta http-equiv="Content-type" content="text/html; charset=utf-8"/>

    <link rel="stylesheet" type="text/css" href="bootstrap/css/bootstrap.min.css"/>
    <script type="text/javascript" language="javascript" src="js/jquery-1.7.2.min.js"></script>
    <script type="text/javascript" language="javascript" src="bootstrap/js/bootstrap.js"></script>
    <link rel="stylesheet" type="text/css" href="bootstrap/css/bootstrap-responsive.min.css"/>


<body>
<div style="padding-top:20px">
    <tiles:insert name="body"/>
</div>
</body>
</html>
