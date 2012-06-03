<%@ include file="/jsp/init.jsp" %>

<div class="fform">
    <h1>
        <spring:message code="${requestScope.title}"/>
    </h1>

    <p><spring:message code="${requestScope.details}"/></p>
    <form:form method="post" commandName="importMap" enctype="multipart/form-data">
        <fieldset>
            <label for="title"><spring:message code="NAME"/></label>
            <form:input path="title" id="title" tabindex="1" required="required"/>
            <form:errors path="title" cssClass="errorMsg"/>

            <label for="title"><spring:message code="DESCRIPTION"/></label>
            <form:input path="description" id="description" tabindex="2"/>
            <form:errors path="description" cssClass="errorMsg"/>

            <label for="mapFile"><spring:message code="FREE_MIND_FILE"/></label>
            <input type="file" name="mapFile" id="mapFile"/>
            <form:errors path="mapFile" cssClass="errorMsg"/>
        </fieldset>

        <input type="submit" value="<spring:message code="IMPORT"/>" class="btn btn-primary"/>
        <input type="button" value="<spring:message code="CANCEL"/>" class="btn"
               onclick="window.location='/c/maps/'">

    </form:form>
</div>
