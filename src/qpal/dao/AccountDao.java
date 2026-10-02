package qpal.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import qpal.model.Account;
import qpal.util.DbConnection;

public class AccountDao {

    // LOGIN
    public Account Login(String email, String password) {

        String sql = "SELECT * FROM accounts WHERE email = ? AND password = ? AND status = 'Active'";

        try (
            Connection connection = DbConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)
        ) {

            statement.setString(1, email);
            statement.setString(2, password);

            ResultSet result = statement.executeQuery();

            if(result.next()) {

                Account account = new Account();

                account.setID(result.getInt("id"));
                account.setName(result.getString("name"));
                account.setEmail(result.getString("email"));
                account.setPassword(result.getString("password"));
                account.setRole(result.getString("role"));
                account.setStatus(result.getString("status"));
                account.setProfileImage(result.getString("profile_image"));

                return account;
            }

        } catch(Exception e) {
            e.printStackTrace();
        }

        return null;
    }


    // CHECK EMAIL
    public boolean CheckEmail(String email) {

        String sql = "SELECT email FROM accounts WHERE email = ?";

        try (
            Connection connection = DbConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)
        ) {

            statement.setString(1, email);

            ResultSet result = statement.executeQuery();

            return result.next();

        } catch(Exception e) {

            e.printStackTrace();
        }

        return false;
    }

    // CHECK INACTIVE ACCOUNT
    public boolean CheckInactive(String email) {

    String sql = "SELECT status FROM accounts WHERE email = ?";

    try (
        Connection connection = DbConnection.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql)
    ) {

        statement.setString(1, email);

        ResultSet result = statement.executeQuery();

        if(result.next()) {

            return result.getString("status").equalsIgnoreCase("Inactive");
        }

    } catch(Exception e) {

        e.printStackTrace();
    }

    return false;
}


    // GET ALL ACCOUNTS
    public List<Account> getAllAccounts() {

        List<Account> accounts = new ArrayList<>();

        String sql = "SELECT id, name, email, role, status, profile_image "
                   + "FROM accounts ORDER BY id";


        try (
            Connection connection = DbConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet result = statement.executeQuery()
        ) {


            while(result.next()) {

                Account account = new Account();

                account.setID(result.getInt("id"));
                account.setName(result.getString("name"));
                account.setEmail(result.getString("email"));
                account.setRole(result.getString("role"));
                account.setStatus(result.getString("status"));
                account.setProfileImage(result.getString("profile_image"));

                accounts.add(account);
            }


        } catch(Exception e) {

            e.printStackTrace();
        }


        return accounts;
    }



    // ADD ACCOUNT
    public boolean addAccount(Account account) {


        String sql = "INSERT INTO accounts "
                   + "(name, email, password, role, status) "
                   + "VALUES (?, ?, ?, ?, ?)";


        try (
            Connection connection = DbConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)
        ) {


            statement.setString(1, account.getName());
            statement.setString(2, account.getEmail());
            statement.setString(3, account.getPassword());
            statement.setString(4, account.getRole());
            statement.setString(5, "Active");


            boolean saved = statement.executeUpdate() > 0;
            if (saved) ActivityLogDao.recordActivity("Manage Accounts", "Create", "Created account " + account.getEmail() + " with role " + account.getRole() + ".");
            return saved;


        } catch(Exception e) {

            e.printStackTrace();
        }


        return false;
    }



    // UPDATE ACCOUNT
    public boolean updateAccount(Account account) {

        String sql = "UPDATE accounts SET "
                   + "name = ?, email = ?, password = ?, role = ?, status = ? "
                   + "WHERE id = ?";

        try (
            Connection connection = DbConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)
        ) {

            statement.setString(1, account.getName());
            statement.setString(2, account.getEmail());
            statement.setString(3, account.getPassword());
            statement.setString(4, account.getRole());
            statement.setString(5, account.getStatus());
            statement.setInt(6, account.getID());

            boolean saved = statement.executeUpdate() > 0;
            if (saved) ActivityLogDao.recordActivity("Manage Accounts", "Update", "Updated account #" + account.getID() + " (" + account.getEmail() + ").");
            return saved;

        } catch(Exception e) {

            e.printStackTrace();
        }

        return false;
    }



    // DELETE ACCOUNT
    public boolean deleteAccount(int id) {


        String sql = "DELETE FROM accounts WHERE id = ?";


        try (
            Connection connection = DbConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)
        ) {


            statement.setInt(1, id);


            boolean saved = statement.executeUpdate() > 0;
            if (saved) ActivityLogDao.recordActivity("Manage Accounts", "Delete", "Deleted account #" + id + ".");
            return saved;


        } catch(Exception e) {

            e.printStackTrace();
        }


        return false;
    }



    // GET ACCOUNT BY ID
    public Account getAccount(int id) {


        String sql = "SELECT * FROM accounts WHERE id = ?";


        try (
            Connection connection = DbConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)
        ) {


            statement.setInt(1, id);


            ResultSet result = statement.executeQuery();


            if(result.next()) {


                Account account = new Account();


                account.setID(result.getInt("id"));
                account.setName(result.getString("name"));
                account.setEmail(result.getString("email"));
                account.setPassword(result.getString("password"));
                account.setRole(result.getString("role"));
                account.setStatus(result.getString("status"));
                account.setProfileImage(result.getString("profile_image"));


                return account;

            }


        } catch(Exception e) {

            e.printStackTrace();
        }

        return null;
    }

    // UPDATE PERSONAL DETAILS WITHOUT CHANGING ROLE OR STATUS
    public boolean updateProfile(int id, String name, String email, String password, String profileImage) {

        String sql = "UPDATE accounts SET name = ?, email = ?, "
                + "password = CASE WHEN ? = '' THEN password ELSE ? END, profile_image = ? WHERE id = ?";

        try (
            Connection connection = DbConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)
        ) {

            statement.setString(1, name);
            statement.setString(2, email);
            statement.setString(3, password);
            statement.setString(4, password);
            statement.setString(5, profileImage == null || profileImage.isEmpty() ? null : profileImage);
            statement.setInt(6, id);

            boolean saved = statement.executeUpdate() > 0;
            if (saved) ActivityLogDao.recordActivity("Profile", "Update", "Updated personal profile for account #" + id + ".");
            return saved;

        } catch(Exception e) {

            e.printStackTrace();
        }

        return false;
    }
}