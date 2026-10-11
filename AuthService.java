package ke.sonclinic;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public final class AuthService {
    private AuthService() {}

    public static boolean authenticate(String username, String password) {
        if (username == null || password == null) return false;
        String sql = "SELECT password_hash FROM users WHERE username = ?";
        try (Connection c = Database.connect(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, username.trim());
            try (ResultSet rs = p.executeQuery()) {
                return rs.next() && org.mindrot.jbcrypt.BCrypt.checkpw(password, rs.getString("password_hash"));
            }
        } catch (Exception e) {
            return false;
        }
    }

    public static void changePassword(String username, String newPassword) throws Exception {
        if (newPassword == null || newPassword.length() < 10) {
            throw new IllegalArgumentException("Use a password with at least 10 characters.");
        }
        String hash = org.mindrot.jbcrypt.BCrypt.hashpw(newPassword, org.mindrot.jbcrypt.BCrypt.gensalt(12));
        try (Connection c = Database.connect(); PreparedStatement p =
                c.prepareStatement("UPDATE users SET password_hash=? WHERE username=?")) {
            p.setString(1, hash);
            p.setString(2, username);
            p.executeUpdate();
        }
    }
}
