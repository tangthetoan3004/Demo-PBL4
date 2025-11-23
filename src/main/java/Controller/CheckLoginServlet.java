package Controller;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import Model.BO.CheckLoginBO;

@WebServlet("/CheckLoginServlet")
public class CheckLoginServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private CheckLoginBO checkLoginBO;

    @Override
    public void init() throws ServletException {
        checkLoginBO = new CheckLoginBO();
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String username = request.getParameter("Username");
        String password = request.getParameter("Password");

        try {
            int userId = checkLoginBO.getUserId(username, password);
            if (userId != -1) {
                // Tạo session và set attributes
                HttpSession session = request.getSession();
                session.setAttribute("UserID", userId);
                session.setAttribute("Username", username);
                session.setMaxInactiveInterval(30 * 60); // 30 phút

                // Dùng forward để chuyển đến Main.jsp
                request.getRequestDispatcher("Main.jsp").forward(request, response);
            } else {
                // Lưu error message vào request attribute để hiển thị
                request.setAttribute("errorMessage", "Sai tên đăng nhập hoặc mật khẩu.");
                request.getRequestDispatcher("Login.jsp").forward(request, response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("errorMessage", "Đã xảy ra lỗi. Vui lòng thử lại.");
            request.getRequestDispatcher("Login.jsp").forward(request, response);
        }
    }
}