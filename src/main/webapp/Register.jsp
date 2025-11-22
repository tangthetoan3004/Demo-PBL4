<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
   <meta charset="UTF-8">
   <title>Đăng ký</title>
   <link href="https://fonts.googleapis.com/css2?family=Roboto:wght@400;500;700&display=swap" rel="stylesheet">
   <link rel="stylesheet" href="./Login.css">
</head>
<body>
    <div class="box">
        <form action="RegisterServlet" method="post">
            <h1>Đăng ký</h1>
            <c:if test="${not empty errorMessage}">
                <p class="error-message">${errorMessage}</p>
            </c:if>
            Username <input type="text" name="Username" required>
            Password <input type="password" name="Password" required>
            Confirm Password <input type="password" name="confirm_password" required>
            <div class="button">
                <input type="submit" value="Sign Up">
                <input type="reset" value="Reset">
            </div>
        </form>
        <div class="content">
            <h1>Tạo tài khoản</h1>
            <button onclick="window.location.href='Login.jsp'">Đăng nhập</button>
        </div>
    </div>
</body>
</html>