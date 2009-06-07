<%@ taglib prefix="tiles" uri="http://struts.apache.org/tags-tiles" %>

<%@ include file="/jsp/init.jsp" %>

<tiles:importAttribute name="title" scope="page"/>
<tiles:importAttribute name="details" scope="page"/>

<div class="modalDialog">

    <!--  Header can be customized -->

    <c:if test="${(not empty pageScope.title)}">
        <h1>
            <spring:message code="${pageScope.title}"/>
        </h1>

        <c:if test="${(not empty pageScope.details)}">
            <h2>
                <spring:message code="${pageScope.details}"/>
            </h2>
        </c:if>
    </c:if>

    <tiles:insert name="body"/>
</div>