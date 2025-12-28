<%@page contentType="text/html; charset=UTF-8"%>
<%@ page import="java.util.Date" %>
<html>
<head><title>JSPのサンプル</title></head>
<link rel="stylesheet" type="text/css" href="style.css">
<body>

<h1>現在時刻</h1>

<div class="time"><% out.println(new Date()); %></div>

<img src="kappa.png" width="148" height="199">
</body>
</html>
