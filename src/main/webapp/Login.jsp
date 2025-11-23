<%@ page language="java" contentType="text/html; charset=UTF-8"
pageEncoding="UTF-8"%> <%@ taglib prefix="c"
uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html lang="vi">
  <head>
    <meta charset="UTF-8" />
    <title>Đăng nhập - PDFtoDOC</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <link rel="stylesheet" href="./Login.css" />
  </head>
  <body>
    <div class="box">
      <form action="CheckLoginServlet" method="POST">
        <h1>Đăng nhập</h1>
        <p style="color: #999; font-size: 14px; margin-bottom: 20px">
          Chào mừng quay trở lại!
        </p>

        <c:if test="${not empty errorMessage}">
          <div class="error-message">${errorMessage}</div>
        </c:if>
        <c:if test="${param.logout == 'success'}">
          <div class="success-message">Bạn đã đăng xuất thành công!</div>
        </c:if>

        <label for="username">Tên tài khoản</label>
        <input
          type="text"
          id="username"
          name="Username"
          placeholder="Nhập username..."
          required
        />

        <label for="password">Mật khẩu</label>
        <input
          type="password"
          id="password"
          name="Password"
          placeholder="Nhập mật khẩu..."
          required
        />

        <div class="button">
          <input type="submit" value="Đăng nhập" />
          <input type="reset" value="Làm mới" />
        </div>
      </form>

      <div class="content">
        <h1>Xin chào!</h1>
        <p>
          Hãy nhập thông tin cá nhân của bạn để bắt đầu hành trình chuyển đổi
          tài liệu.
        </p>
        <button onclick="window.location.href='Register.jsp'">
          Đăng ký ngay
        </button>
      </div>
    </div>
  </body>
</html>
