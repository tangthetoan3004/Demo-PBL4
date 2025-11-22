package Controller;

import java.io.IOException;
import java.util.List;
import Model.BEAN.information;
import Model.BO.SaveInformationBO;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/HistoryController")
public class HistoryController extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private SaveInformationBO saveInformationBO;

    @Override
    public void init() throws ServletException {
        saveInformationBO = new SaveInformationBO();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        Integer userId = (session != null) ? (Integer) session.getAttribute("UserID") : null;

        if (userId == null) {
            response.sendRedirect("Login.jsp");
            return;
        }
        List<information> fileHistory = saveInformationBO.getFileHistory(userId);
        request.setAttribute("fileHistory", fileHistory);
        request.getRequestDispatcher("History.jsp").forward(request, response);
    }
}