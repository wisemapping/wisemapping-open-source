<%@ page import="com.wisemapping.model.User" %>
<%@ page import="com.wisemapping.security.Utils" %>
<%@ page import="com.wisemapping.filter.UserAgent" %>
<%@ page import="com.wisemapping.filter.BrowserSupportInterceptor" %>
<%@ page session="false" contentType="text/html;charset=UTF-8" %>

<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%--<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt" %>--%>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%
    User user = Utils.getUser(request);
    request.setAttribute("principal", user);

    UserAgent userAgent = null;
    final HttpSession session = request.getSession();
    if (session != null) {
        userAgent = (UserAgent) session.getAttribute(BrowserSupportInterceptor.USER_AGENT);
    }
%>
