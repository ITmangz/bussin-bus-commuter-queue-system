package qpal.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import qpal.model.Account;
import qpal.model.ActivityLog;
import qpal.util.DbConnection;

public class ActivityLogDao {

    private static volatile Account currentAccount;
    private static volatile boolean saveFailed;

    public static void setCurrentAccount(Account account) {
        currentAccount = account;
    }

    public static Account getCurrentAccount() {
        return currentAccount;
    }

    public static boolean hasSaveFailed() {
        return saveFailed;
    }

    // RECORD A SUCCESSFUL ACTION USING THE SIGNED-IN ACCOUNT
    public static void recordActivity(String module, String action, String description) {
        Account account = currentAccount;
        if (account == null) {
            return;
        }
        new ActivityLogDao().addActivity(account, module, action, description);
    }

    // ADD ACTIVITY
    public boolean addActivity(Account account, String module, String action, String description) {
        String sql =
                "INSERT INTO activity_logs "
                        + "(account_id, user_name, email, role, module, action, description) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = DbConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, account.getID());
            statement.setString(2, account.getName());
            statement.setString(3, account.getEmail());
            statement.setString(4, account.getRole());
            statement.setString(5, module);
            statement.setString(6, action);
            statement.setString(7, description);
            return statement.executeUpdate() > 0;
        } catch (Exception e) {
            saveFailed = true;
            e.printStackTrace();
        }
        return false;
    }

    // GET ACTIVITY LOGS; EMPLOYEES CAN ONLY READ THEIR OWN RECORDS
    public List<ActivityLog> getAllActivities(Account account) throws SQLException {
        List<ActivityLog> activities = new ArrayList<>();
        if (account == null) {
            throw new SQLException("Sign in to view activity logs.");
        }
        String sql = "SELECT * FROM activity_logs";
        boolean admin = "Admin".equalsIgnoreCase(account.getRole());
        if (!admin) {
            sql += " WHERE account_id = ?";
        }
        sql += " ORDER BY created_at DESC, log_id DESC";

        try (Connection connection = DbConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            if (!admin) {
                statement.setInt(1, account.getID());
            }
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    ActivityLog activity = new ActivityLog();
                    activity.setId(result.getInt("log_id"));
                    activity.setTimestamp(result.getTimestamp("created_at"));
                    activity.setUserName(result.getString("user_name"));
                    activity.setEmail(result.getString("email"));
                    activity.setRole(result.getString("role"));
                    activity.setModule(result.getString("module"));
                    activity.setAction(result.getString("action"));
                    activity.setDescription(result.getString("description"));
                    activities.add(activity);
                }
            }
        }
        return activities;
    }
}
