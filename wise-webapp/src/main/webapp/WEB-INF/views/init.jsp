<%@taglib uri="jakarta.tags.functions" prefix="fn" %>
<%@taglib uri="jakarta.tags.core" prefix="c"%>
<%

    request.setAttribute("principal", com.wisemapping.security.Utils.getUser());
%>

