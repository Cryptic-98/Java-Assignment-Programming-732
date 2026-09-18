import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database
{
    private static final String URL = "jdbc:mysql://localhost:3306/healthfirst"
        + "?useSSL=false&serverTimezone=UTC";
    public Connection getConnection() throws SQLException
    {
        String username = System.getenv("HEALTHFIRST_DB_USER");
        if (username == null || username.isBlank())
        {
            username = "root";
        }

        String password = System.getenv("HEALTHFIRST_DB_PASSWORD");
        if (password == null)
        {
            password = "";
        }

        return DriverManager.getConnection(URL, username, password);
    }

    public void database_conn()
    {
        try (Connection conn = getConnection())
        {
            if (conn.isValid(2))
            {
                System.out.println("Connected to the database successfully!");
            }
        } catch (SQLException e)
        {
            System.err.println("Database connection failed: " + e.getMessage());
        }
    }

    public static void main(String[] args)
    {
        new Database().database_conn();
    }
}