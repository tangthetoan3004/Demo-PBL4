package Controller;

import java.io.IOException;
import Model.BO.RegisterBO;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/RegisterServlet")
public class RegisterServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private RegisterBO registerBO;
    
    @Override
    public void init() throws ServletException {
        registerBO = new RegisterBO();
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String username = request.getParameter("Username");
        String password = request.getParameter("Password");
        String confirmPassword = request.getParameter("confirm_password"); 

        if (password == null || !password.equals(confirmPassword)) {
            request.setAttribute("errorMessage", "Mật khẩu không khớp!");
            request.getRequestDispatcher("Register.jsp").forward(request, response);
            return; 
        }
        
        if (registerBO.registerUser(username, password)) {
            response.sendRedirect("Login.jsp");
        } else {
            request.setAttribute("errorMessage", "Username đã tồn tại.");
            request.getRequestDispatcher("Register.jsp").forward(request, response);  
        }
    }
}