import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.time.LocalDate;

class DBConnection
{
    private static final String URL = "jdbc:mysql://localhost:3306/healthfirst"
            + "?useSSL=false&serverTimezone=UTC";
    public static Connection getConnection() throws SQLException
    {
        String username = System.getenv("HEALTHFIRST_DB_USER");
        if (username == null || username.isBlank())
        {
            username = "root";
        }

        String password = System.getenv("HEALTHFIRST_DB_PASSWORD");
        if (password == null)
        {
            password = "Mang0D4ngo";
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
}

class UserDAO
{
    private final Connection connection;

    public UserDAO() throws SQLException
    {
        this.connection = DBConnection.getConnection();
    }

    public User validateLogin(String username, String password) throws SQLException
    {
        if (username == null || password == null)
        {
            return null;
        }

        String sql = "SELECT user_id, username, password, role, full_name "
                + "FROM users WHERE username = ? AND password = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql))
        {
            statement.setString(1, username);
            statement.setString(2, password);

            try (ResultSet result = statement.executeQuery())
            {
                return result.next() ? mapUser(result) : null;
            }
        }
    }

    public boolean addUser(User user) throws SQLException
    {
        if (user == null || user.getRole() != Role.CASHIER)
        {
            return false;
        }

        String sql = "INSERT INTO users (username, password, role, full_name) "
                + "VALUES (?, ?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(sql))
        {
            statement.setString(1, user.getUsername());
            statement.setString(2, user.getPassword());
            statement.setString(3, user.getRole().toString());
            statement.setString(4, user.getFullName());
            return statement.executeUpdate() > 0;
        }
    }

    public boolean deleteUser(int userId) throws SQLException
    {
        String sql = "DELETE FROM users WHERE user_id = ? AND role = 'Cashier'";

        try (PreparedStatement statement = connection.prepareStatement(sql))
        {
            statement.setInt(1, userId);
            return statement.executeUpdate() > 0;
        }
    }

    public List<User> getAllCashiers() throws SQLException
    {
        String sql = "SELECT user_id, username, password, role, full_name "
                + "FROM users WHERE role = 'Cashier' ORDER BY user_id";
        List<User> cashiers = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery())
        {
            while (result.next())
            {
                cashiers.add(mapUser(result));
            }
        }

        return cashiers;
    }

    private User mapUser(ResultSet result) throws SQLException
    {
        User user = new User();
        user.setUserId(result.getInt("user_id"));
        user.setUsername(result.getString("username"));
        user.setPassword(result.getString("password"));
        user.setRole(Role.valueOf(result.getString("role").toUpperCase(Locale.ROOT)));
        user.setFullName(result.getString("full_name"));
        return user;
    }

    public void closeConnection()
    {
        try
        {
            if (!connection.isClosed())
            {
                connection.close();
            }
        } catch (SQLException e)
        {
            throw new IllegalStateException("Unable to close database connection.", e);
        }
    }
}

class SupplierDAO
{
    private final Connection connection;

    public SupplierDAO() throws SQLException
    {
        this.connection = DBConnection.getConnection();
    }

    public boolean addSupplier(Supplier supplier) throws SQLException
    {
        String sql = "INSERT INTO suppliers "
                + "(name, contact_person, phone_number, email, address) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql))
        {
            setSupplierParameters(statement, supplier);
            return statement.executeUpdate() > 0;
        }
    }

    public boolean updateSupplier(Supplier supplier) throws SQLException
    {
        String sql = "UPDATE suppliers SET name = ?, contact_person = ?, phone_number = ?, "
                + "email = ?, address = ? WHERE supplier_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql))
        {
            setSupplierParameters(statement, supplier);
            statement.setInt(6, supplier.getSupplierId());
            return statement.executeUpdate() > 0;
        }
    }

    public boolean deleteSupplier(int supplierId) throws SQLException
    {
        String sql = "DELETE FROM suppliers WHERE supplier_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql))
        {
            statement.setInt(1, supplierId);
            return statement.executeUpdate() > 0;
        }
    }

    public List<Supplier> getAllSuppliers() throws SQLException
    {
        List<Supplier> suppliers = new ArrayList<>();
        String sql = "SELECT supplier_id, name, contact_person, phone_number, email, address "
                + "FROM suppliers ORDER BY supplier_id";
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery())
        {
            while (result.next())
            {
                suppliers.add(mapSupplier(result));
            }
        }
        return suppliers;
    }

    private void setSupplierParameters(PreparedStatement statement, Supplier supplier)
            throws SQLException
    {
        statement.setString(1, supplier.getName());
        statement.setString(2, supplier.getContactPerson());
        statement.setString(3, supplier.getPhoneNumber());
        statement.setString(4, supplier.getEmail());
        statement.setString(5, supplier.getAddress());
    }

    private Supplier mapSupplier(ResultSet result) throws SQLException
    {
        Supplier supplier = new Supplier();
        supplier.setSupplierId(result.getInt("supplier_id"));
        supplier.setName(result.getString("name"));
        supplier.setContactPerson(result.getString("contact_person"));
        supplier.setPhoneNumber(result.getString("phone_number"));
        supplier.setEmail(result.getString("email"));
        supplier.setAddress(result.getString("address"));
        return supplier;
    }

    public void closeConnection()
    {
        try
        {
            if (!connection.isClosed())
            {
                connection.close();
            }
        } catch (SQLException e)
        {
            throw new IllegalStateException("Unable to close database connection.", e);
        }
    }
}

class MedicineDAO
{
    private final Connection connection;

    public MedicineDAO() throws SQLException
    {
        this.connection = DBConnection.getConnection();
    }

    public boolean addMedicine(Medicine medicine) throws SQLException
    {
        String sql = "INSERT INTO medicines "
                + "(name, company, medicine_type, price, quantity_in_stock, reorder_level, "
                + "expiry_date, supplier_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql))
        {
            setMedicineParameters(statement, medicine);
            return statement.executeUpdate() > 0;
        }
    }

    public boolean updateMedicine(Medicine medicine) throws SQLException
    {
        String sql = "UPDATE medicines SET name = ?, company = ?, medicine_type = ?, price = ?, "
                + "quantity_in_stock = ?, reorder_level = ?, expiry_date = ?, supplier_id = ? "
                + "WHERE medicine_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql))
        {
            setMedicineParameters(statement, medicine);
            statement.setInt(9, medicine.getMedicineId());
            return statement.executeUpdate() > 0;
        }
    }

    public boolean deleteMedicine(int medicineId) throws SQLException
    {
        String sql = "DELETE FROM medicines WHERE medicine_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql))
        {
            statement.setInt(1, medicineId);
            return statement.executeUpdate() > 0;
        }
    }

    public List<Medicine> getAllMedicines() throws SQLException
    {
        return findMedicines("SELECT * FROM medicines ORDER BY medicine_id");
    }

    public Medicine getMedicineById(int medicineId) throws SQLException
    {
        String sql = "SELECT * FROM medicines WHERE medicine_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql))
        {
            statement.setInt(1, medicineId);
            try (ResultSet result = statement.executeQuery())
            {
                return result.next() ? mapMedicine(result) : null;
            }
        }
    }

    public List<Medicine> searchMedicine(String name) throws SQLException
    {
        String sql = "SELECT * FROM medicines WHERE name LIKE ? ORDER BY name";
        try (PreparedStatement statement = connection.prepareStatement(sql))
        {
            statement.setString(1, "%" + name + "%");
            return readMedicines(statement);
        }
    }

    public List<Medicine> getLowStockMedicines() throws SQLException
    {
        return findMedicines("SELECT * FROM medicines "
                + "WHERE quantity_in_stock <= reorder_level ORDER BY name");
    }

    public List<Medicine> getExpiringMedicines() throws SQLException
    {
        return findMedicines("SELECT * FROM medicines "
                + "WHERE expiry_date BETWEEN CURRENT_DATE AND DATE_ADD(CURRENT_DATE, INTERVAL 1 MONTH) "
                + "ORDER BY expiry_date");
    }

    public boolean updateStockQuantity(int medicineId, int newQty) throws SQLException
    {
        if (newQty < 0)
        {
            return false;
        }
        String sql = "UPDATE medicines SET quantity_in_stock = ? WHERE medicine_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql))
        {
            statement.setInt(1, newQty);
            statement.setInt(2, medicineId);
            return statement.executeUpdate() > 0;
        }
    }

    private List<Medicine> findMedicines(String sql) throws SQLException
    {
        try (PreparedStatement statement = connection.prepareStatement(sql))
        {
            return readMedicines(statement);
        }
    }

    private List<Medicine> readMedicines(PreparedStatement statement) throws SQLException
    {
        List<Medicine> medicines = new ArrayList<>();
        try (ResultSet result = statement.executeQuery())
        {
            while (result.next())
            {
                medicines.add(mapMedicine(result));
            }
        }
        return medicines;
    }

    private Medicine mapMedicine(ResultSet result) throws SQLException
    {
        Medicine medicine = new Medicine();
        medicine.setMedicineId(result.getInt("medicine_id"));
        medicine.setName(result.getString("name"));
        medicine.setCompany(result.getString("company"));
        medicine.setMedicineType(result.getString("medicine_type"));
        medicine.setPrice(result.getDouble("price"));
        medicine.setQuantityInStock(result.getInt("quantity_in_stock"));
        medicine.setReorderLevel(result.getInt("reorder_level"));
        medicine.setExpiryDate(result.getDate("expiry_date").toLocalDate());
        medicine.setSupplierId(result.getInt("supplier_id"));
        return medicine;
    }

    private void setMedicineParameters(PreparedStatement statement, Medicine medicine)
            throws SQLException
    {
        statement.setString(1, medicine.getName());
        statement.setString(2, medicine.getCompany());
        statement.setString(3, medicine.getMedicineType());
        statement.setDouble(4, medicine.getPrice());
        statement.setInt(5, medicine.getQuantityInStock());
        statement.setInt(6, medicine.getReorderLevel());
        statement.setDate(7, Date.valueOf(medicine.getExpiryDate()));
        statement.setInt(8, medicine.getSupplierId());
    }

    public void closeConnection()
    {
        try
        {
            if (!connection.isClosed())
            {
                connection.close();
            }
        } catch (SQLException e)
        {
            throw new IllegalStateException("Unable to close database connection.", e);
        }
    }
}

class SaleDAO
{
    private final Connection connection;

    public SaleDAO() throws SQLException
    {
        this.connection = DBConnection.getConnection();
    }

    public int createSale(Sale sale) throws SQLException
    {
        String sql = "INSERT INTO sales (sale_date, total_amount, user_id) VALUES (?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS))
        {
            statement.setDate(1, Date.valueOf(sale.getSaleDate()));
            statement.setDouble(2, sale.getTotalAmount());
            statement.setInt(3, sale.getUserId());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys())
            {
                if (keys.next())
                {
                    return keys.getInt(1);
                }
            }
        }
        return -1;
    }

    public List<Sale> getSalesByDateRange(LocalDate start, LocalDate end) throws SQLException
    {
        List<Sale> sales = new ArrayList<>();
        String sql = "SELECT sale_id, sale_date, total_amount, user_id FROM sales "
                + "WHERE sale_date >= ? AND sale_date < DATE_ADD(?, INTERVAL 1 DAY) "
                + "ORDER BY sale_date";
        try (PreparedStatement statement = connection.prepareStatement(sql))
        {
            statement.setDate(1, Date.valueOf(start));
            statement.setDate(2, Date.valueOf(end));
            try (ResultSet result = statement.executeQuery())
            {
                while (result.next())
                {
                    Sale sale = new Sale();
                    sale.setSaleId(result.getInt("sale_id"));
                    sale.setSaleDate(result.getDate("sale_date").toLocalDate());
                    sale.setTotalAmount(result.getDouble("total_amount"));
                    sale.setUserId(result.getInt("user_id"));
                    sales.add(sale);
                }
            }
        }
        return sales;
    }

    public void closeConnection()
    {
        try
        {
            if (!connection.isClosed())
            {
                connection.close();
            }
        } catch (SQLException e)
        {
            throw new IllegalStateException("Unable to close database connection.", e);
        }
    }
}

class ItemWiseSalesReport
{
    private final int medicineId;
    private final String medicineName;
    private final int quantitySold;
    private final double totalRevenue;

    public ItemWiseSalesReport(int medicine_id, String medicine_name, int quantity_sold,
                               double total_revenue)
    {
        this.medicineId = medicine_id;
        this.medicineName = medicine_name;
        this.quantitySold = quantity_sold;
        this.totalRevenue = total_revenue;
    }

    public int getMedicineId() { return this.medicineId; }
    public String getMedicineName() { return this.medicineName; }
    public int getQuantitySold() { return this.quantitySold; }
    public double getTotalRevenue() { return this.totalRevenue; }
}

class SaleItemDAO
{
    private final Connection connection;

    public SaleItemDAO() throws SQLException
    {
        this.connection = DBConnection.getConnection();
    }

    public boolean addSaleItem(SaleItem item) throws SQLException
    {
        String sql = "INSERT INTO sale_items "
                + "(sale_id, medicine_id, quantity_sold, price_at_sale) VALUES (?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql))
        {
            statement.setInt(1, item.getSaleId());
            statement.setInt(2, item.getMedicineId());
            statement.setInt(3, item.getQuantitySold());
            statement.setDouble(4, item.getPriceAtSale());
            return statement.executeUpdate() > 0;
        }
    }

    public List<SaleItem> getItemsBySaleId(int saleId) throws SQLException
    {
        List<SaleItem> items = new ArrayList<>();
        String sql = "SELECT * FROM sale_items WHERE sale_id = ? ORDER BY sale_item_id";
        try (PreparedStatement statement = connection.prepareStatement(sql))
        {
            statement.setInt(1, saleId);
            try (ResultSet result = statement.executeQuery())
            {
                while (result.next())
                {
                    SaleItem item = new SaleItem();
                    item.setSaleItemId(result.getInt("sale_item_id"));
                    item.setSaleId(result.getInt("sale_id"));
                    item.setMedicineId(result.getInt("medicine_id"));
                    item.setQuantitySold(result.getInt("quantity_sold"));
                    item.setPriceAtSale(result.getDouble("price_at_sale"));
                    items.add(item);
                }
            }
        }
        return items;
    }

    public List<ItemWiseSalesReport> getItemWiseSalesReport() throws SQLException
    {
        List<ItemWiseSalesReport> report = new ArrayList<>();
        String sql = "SELECT m.medicine_id, m.name, SUM(si.quantity_sold) AS quantity_sold, "
                + "SUM(si.quantity_sold * si.price_at_sale) AS total_revenue "
                + "FROM sale_items si JOIN medicines m ON m.medicine_id = si.medicine_id "
                + "GROUP BY m.medicine_id, m.name ORDER BY total_revenue DESC";
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery())
        {
            while (result.next())
            {
                report.add(new ItemWiseSalesReport(
                        result.getInt("medicine_id"),
                        result.getString("name"),
                        result.getInt("quantity_sold"),
                        result.getDouble("total_revenue")));
            }
        }
        return report;
    }

    public void closeConnection()
    {
        try
        {
            if (!connection.isClosed())
            {
                connection.close();
            }
        } catch (SQLException e)
        {
            throw new IllegalStateException("Unable to close database connection.", e);
        }
    }
}

public class Database
{
    public static void main(String[] args)
    {
        new DBConnection().database_conn();
    }
}