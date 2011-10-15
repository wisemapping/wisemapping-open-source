<%@ taglib prefix="tiles" uri="http://struts.apache.org/tags-tiles" %>

<%@ include file="/jsp/init.jsp" %>

<tiles:importAttribute name="title" scope="page"/>
<tiles:importAttribute name="details" scope="page"/>

<div class="modalDialog">
    <!--  Header can be customized -->
    <tiles:insert name="body"/>
</div>