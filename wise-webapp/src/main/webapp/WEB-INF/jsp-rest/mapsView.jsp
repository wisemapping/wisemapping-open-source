<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>Mindmaps List</title>
</head>
<body>
<h1>Mindmaps List</h1>
<c:forEach items="${list.mindmaps}" var="map">
    <table border="1" cellspacing="0">
        <tbody>
        <tr>
            <td>Id:</td>
            <td>${map.id}</td>
        </tr>
        <tr>
            <td>Title:</td>
            <td>${map.title}</td>
        </tr>
        <tr>
            <td>Description:</td>
            <td>${map.description}</td>
        </tr>
        <tr>
            <td>Owner:</td>
            <td>${map.owner}</td>
        </tr>
        <tr>
            <td>Xml:</td>
            <td><textarea rows="10" cols="100">${map.xml}</textarea></td>
        </tr>

        <tr>
            <td>Last Modified:</td>
            <td>${map.lastModifierUser}</td>
        </tr>
        <tr>
            <td>Creator:</td>
            <td>${map.creator}</td>
        </tr>
        <tr>
            <td>Public:</td>
            <td>${map.public}</td>
        </tr>
        </tbody>
    </table>
</c:forEach>
<br/>
</body>
</html>