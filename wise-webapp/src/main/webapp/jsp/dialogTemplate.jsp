<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>

<%@ include file="/jsp/init.jsp" %>

<tiles:importAttribute name="title" scope="page"/>
<tiles:importAttribute name="details" scope="page"/>

<div>
    <tiles:insertAttribute name="body"/>
</div>