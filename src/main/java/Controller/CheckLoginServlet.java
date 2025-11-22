package Controller;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.RequestDispatcher;
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

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String username = request.getParameter("Username");
        String password = request.getParameter("Password");
        String destination;

        try {
            int userId = checkLoginBO.getUserId(username, password);
            if (userId != -1) {
                HttpSession session = request.getSession();
                session.setAttribute("UserID", userId);
                session.setAttribute("Username", username);
                destination = "/Main.jsp";
            } else {
                request.setAttribute("errorMessage", "Sai tên đăng nhập hoặc mật khẩu.");
                destination = "/Login.jsp";
            }
        } catch (Exception e) {
            e.printStackTrace();
            destination = "/Login.jsp";
        }
        RequestDispatcher rd = getServletContext().getRequestDispatcher(destination);
        rd.forward(request, response);
    }
}