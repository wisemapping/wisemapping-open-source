<%@ page import="org.apache.log4j.Logger" %>
<%@ page autoFlush="true" buffer="none" %>
<%@ include file="/jsp/init.jsp" %>

<%!
    final Logger logger = Logger.getLogger("com.wisemapping");
%>
<div class="installCFG">
    <spring:message code="INSTALL_CFG_REASON"/>
    <br/><br/>
    <a href="${pageContext.request.contextPath}/c/installCFG?mapId=${mapId}"><spring:message code="INSTALL_CFG_CLICK_HERE"/></a>
</div>

