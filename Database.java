package ke.sonclinic;

import java.sql.*;
import java.time.LocalDate;

public final class Database {
    private static final String URL = "jdbc:sqlite:son_clinic.db";

    private Database() {}

    public static Connection connect() throws SQLException {
        Connection c = DriverManager.getConnection(URL);
        try (Statement s = c.createStatement()) {
            s.execute("PRAGMA foreign_keys = ON");
            s.execute("PRAGMA busy_timeout = 5000");
        }
        return c;
    }

    public static void initialize() {
        try (Connection c = connect(); Statement s = c.createStatement()) {
            s.executeUpdate("""
                CREATE TABLE IF NOT EXISTS users(
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT NOT NULL UNIQUE,
                    password_hash TEXT NOT NULL,
                    role TEXT NOT NULL DEFAULT 'ADMIN',
                    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
                )
            """);
            s.executeUpdate("""
                CREATE TABLE IF NOT EXISTS patients(
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    patient_no TEXT NOT NULL UNIQUE,
                    full_name TEXT NOT NULL,
                    phone TEXT,
                    gender TEXT,
                    date_of_birth TEXT,
                    notes TEXT,
                    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
                )
            """);
            s.executeUpdate("""
                CREATE TABLE IF NOT EXISTS appointments(
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    patient_id INTEGER NOT NULL,
                    appointment_date TEXT NOT NULL,
                    reason TEXT,
                    status TEXT NOT NULL DEFAULT 'SCHEDULED',
                    FOREIGN KEY(patient_id) REFERENCES patients(id)
                )
            """);
            s.executeUpdate("""
                CREATE TABLE IF NOT EXISTS medicines(
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    medicine_name TEXT NOT NULL,
                    category TEXT,
                    quantity INTEGER NOT NULL DEFAULT 0 CHECK(quantity >= 0),
                    unit_price REAL NOT NULL DEFAULT 0 CHECK(unit_price >= 0),
                    expiry_date TEXT,
                    reorder_level INTEGER NOT NULL DEFAULT 10
                )
            """);
            s.executeUpdate("""
                CREATE TABLE IF NOT EXISTS sales(
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    medicine_id INTEGER NOT NULL,
                    quantity INTEGER NOT NULL CHECK(quantity > 0),
                    unit_price REAL NOT NULL,
                    total REAL NOT NULL,
                    sold_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY(medicine_id) REFERENCES medicines(id)
                )
            """);

            try (PreparedStatement p = c.prepareStatement("SELECT COUNT(*) FROM users WHERE username = ?")) {
                p.setString(1, "admin");
                try (ResultSet rs = p.executeQuery()) {
                    if (rs.next() && rs.getInt(1) == 0) {
                        String hash = org.mindrot.jbcrypt.BCrypt.hashpw(
                                "ChangeMe123!", org.mindrot.jbcrypt.BCrypt.gensalt(12));
                        try (PreparedStatement ins = c.prepareStatement(
                                "INSERT INTO users(username,password_hash,role) VALUES(?,?,?)")) {
                            ins.setString(1, "admin");
                            ins.setString(2, hash);
                            ins.setString(3, "ADMIN");
                            ins.executeUpdate();
                        }
                    }
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Could not initialize database: " + e.getMessage(), e);
        }
    }

    public static int count(String table) {
        if (!table.matches("[a-z_]+")) throw new IllegalArgumentException("Invalid table");
        try (Connection c = connect(); Statement s = c.createStatement();
             ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM " + table)) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            return 0;
        }
    }

    public static double todaySales() {
        try (Connection c = connect(); PreparedStatement p = c.prepareStatement(
                "SELECT COALESCE(SUM(total),0) FROM sales WHERE date(sold_at)=date('now','localtime')");
             ResultSet rs = p.executeQuery()) {
            return rs.next() ? rs.getDouble(1) : 0;
        } catch (SQLException e) {
            return 0;
        }
    }
}
