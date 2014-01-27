<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>Mindmap Detail</title>
</head>
<body>
<h1>Details for User with id '${user.id}'</h1>
<table border="1" cellspacing="0">
    <tbody>
    <tr>
        <td>Email:</td>
        <td>${user.email}</td>
    </tr>
    <tr>
        <td>Fist Name:</td>
        <td>${user.firstname}</td>
    </tr>
     <tr>
        <td>Last Name:</td>
        <td>${user.lastname}</td>
    </tr>
     <tr>
        <td>Username:</td>
        <td>${user.username}</td>
    </tr>
         <tr>
        <td>Active:</td>
        <td>${user.active}</td>
    </tr>
    </tbody>
</table>
</body>
</html>