<%@ page import="com.wisemapping.model.User" %>
<%@ page import="com.wisemapping.security.Utils" %>
<%@ page import="com.wisemapping.filter.UserAgent" %>
<%@ page import="com.wisemapping.filter.BrowserSupportInterceptor" %>
<%@ page session="false" contentType="text/html;charset=UTF-8" %>

<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%
    User user = Utils.getUser(false);
    request.setAttribute("principal", user);
%>
