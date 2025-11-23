package Model.BO;

import Model.BEAN.users;
import Model.DAO.CheckLoginDAO;
import org.mindrot.jbcrypt.BCrypt;

public class CheckLoginBO {
    CheckLoginDAO checkLoginDAO = new CheckLoginDAO();

    public int getUserId(String username, String plainPassword) {
        users userFromDB = checkLoginDAO.getUserForLogin(username);

        if (userFromDB == null)
            return -1;

        if (BCrypt.checkpw(plainPassword, userFromDB.getPassword())) {
            return userFromDB.getId();
        }
        return -1;
    }
}
