import java.sql.*;
import qpal.dao.AccountDao;
import qpal.util.DbConnection;

public class ProfilePasswordTest {
    static void check(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }

    public static void main(String[] args) throws Exception {
        String schema = "qpal_password_test_" + Long.toUnsignedString(System.nanoTime());
        String oldUrl = System.getProperty("qpal.db.url");
        try (Connection setup = DbConnection.getConnection();
                Statement sql = setup.createStatement()) {
            sql.executeUpdate("CREATE DATABASE " + schema);
            try {
                sql.executeUpdate("CREATE TABLE " + schema + ".accounts LIKE qpal.accounts");
                sql.executeUpdate(
                        "CREATE TABLE " + schema + ".activity_logs LIKE qpal.activity_logs");
                sql.executeUpdate(
                        "INSERT INTO "
                                + schema
                                + ".accounts(id,name,email,password,role,status)"
                                + " VALUES(1,'Test','password@example.test',' OldPass"
                                + " ','Employee','Active')");
                System.setProperty("qpal.db.url", "jdbc:mysql://localhost:3306/" + schema);
                AccountDao dao = new AccountDao();
                check(dao.verifyCurrentPassword(1, " OldPass "), "Exact password accepted");
                check(!dao.verifyCurrentPassword(1, "OldPass"), "Spaces must match");
                check(!dao.verifyCurrentPassword(1, " oldpass "), "Case must match");
                check(
                        !dao.updateProfile(
                                1, "Changed", "password@example.test", "NewPass", null, "wrong"),
                        "Wrong password rejected");
                check(
                        !dao.updateProfile(
                                1,
                                "Changed",
                                "password@example.test",
                                " OldPass ",
                                null,
                                " OldPass "),
                        "Unchanged password rejected");
                check(
                        dao.updateProfile(
                                1,
                                "Changed",
                                "password@example.test",
                                " NewPass ",
                                null,
                                " OldPass "),
                        "Verified change saved");
                check(dao.verifyCurrentPassword(1, " NewPass "), "New password preserves spaces");
                check(
                        !dao.updateProfile(
                                1,
                                "Stale",
                                "password@example.test",
                                "OtherPass",
                                null,
                                " OldPass "),
                        "Stale password rejected at save");
                check(
                        dao.updateProfile(1, "Profile only", "password@example.test", "", null, ""),
                        "Profile-only edit allowed");
                check(
                        dao.verifyCurrentPassword(1, " NewPass "),
                        "Profile-only edit preserves password");
                System.out.println(
                        "PASS: current password, case/space matching, wrong/same/stale password"
                            + " rejection, successful update and profile-only edit");
            } finally {
                if (oldUrl == null) System.clearProperty("qpal.db.url");
                else System.setProperty("qpal.db.url", oldUrl);
                sql.executeUpdate("DROP DATABASE " + schema);
            }
        }
    }
}
