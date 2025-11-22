package Model.BO;

import Model.DAO.RegisterDAO;
import org.mindrot.jbcrypt.BCrypt;

public class RegisterBO {
	RegisterDAO registerDAO = new RegisterDAO();
	
    public boolean registerUser(String username, String password) {
        if (username == null || password == null || username.isEmpty() || password.length() < 4) {
            return false;
        }
        // HASHING
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        return registerDAO.Register(username, hashedPassword);
    }
}

