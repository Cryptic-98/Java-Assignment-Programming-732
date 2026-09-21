import java.sql.*;
import java.time.LocalDate;
import java.util.List;

class AuthService
{
    public User login(String username, String password) throws SQLException
    {
        UserDAO userDAO = new UserDAO();
        try
        {
            return userDAO.validateLogin(username, password);
        }
        finally
        {
            userDAO.closeConnection();
        }
    }
}

class SalesService
{
    public Sale processSale(List<SaleItem> cartItems, int userId) throws SQLException
    {
        if (cartItems == null || cartItems.isEmpty())
        {
            throw new IllegalArgumentException("A sale must contain at least one item.");
        }

        try (Connection connection = DBConnection.getConnection())
        {
            connection.setAutoCommit(false);
            try
            {
                double total = 0;
                for (SaleItem item : cartItems)
                {
                    if (item == null || item.getQuantitySold() <= 0 || item.getPriceAtSale() < 0)
                    {
                        throw new IllegalArgumentException("Sale items must have valid quantities and prices.");
                    }
                    total += item.getQuantitySold() * item.getPriceAtSale();
                    deductStock(connection, item);
                }

                Sale sale = new Sale();
                sale.setSaleDate(LocalDate.now());
                sale.setTotalAmount(total);
                sale.setUserId(userId);
                int saleId = insertSale(connection, sale);
                sale.setSaleId(saleId);

                for (SaleItem item : cartItems)
                {
                    insertSaleItem(connection, item, saleId);
                }

                connection.commit();
                return sale;
            }
            catch (SQLException | RuntimeException e)
            {
                connection.rollback();
                throw e;
            }
            finally
            {
                connection.setAutoCommit(true);
            }
        }
    }

    private void deductStock(Connection connection, SaleItem item) throws SQLException
    {
        String sql = "UPDATE medicines SET quantity_in_stock = quantity_in_stock - ? "
                + "WHERE medicine_id = ? AND quantity_in_stock >= ?";
        try (PreparedStatement statement = connection.prepareStatement(sql))
        {
            statement.setInt(1, item.getQuantitySold());
            statement.setInt(2, item.getMedicineId());
            statement.setInt(3, item.getQuantitySold());
            if (statement.executeUpdate() != 1)
            {
                throw new SQLException("Insufficient stock or medicine not found: "
                        + item.getMedicineId());
            }
        }
    }

    private int insertSale(Connection connection, Sale sale) throws SQLException
    {
        String sql = "INSERT INTO sales (sale_date, total_amount, user_id) VALUES (?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(
                sql, java.sql.Statement.RETURN_GENERATED_KEYS))
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
        throw new SQLException("The sale ID was not generated.");
    }

    private void insertSaleItem(Connection connection, SaleItem item, int saleId)
            throws SQLException
    {
        String sql = "INSERT INTO sale_items "
                + "(sale_id, medicine_id, quantity_sold, price_at_sale) VALUES (?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql))
        {
            statement.setInt(1, saleId);
            statement.setInt(2, item.getMedicineId());
            statement.setInt(3, item.getQuantitySold());
            statement.setDouble(4, item.getPriceAtSale());
            statement.executeUpdate();
        }
    }
}

class ReportService
{
    public List<Sale> generateSalesReport(LocalDate start, LocalDate end) throws SQLException
    {
        SaleDAO saleDAO = new SaleDAO();
        try
        {
            return saleDAO.getSalesByDateRange(start, end);
        }
        finally
        {
            saleDAO.closeConnection();
        }
    }

    public List<ItemWiseSalesReport> generateItemWiseReport() throws SQLException
    {
        SaleItemDAO saleItemDAO = new SaleItemDAO();
        try
        {
            return saleItemDAO.getItemWiseSalesReport();
        }
        finally
        {
            saleItemDAO.closeConnection();
        }
    }

    public List<Medicine> generateLowStockReport() throws SQLException
    {
        MedicineDAO medicineDAO = new MedicineDAO();
        try
        {
            return medicineDAO.getLowStockMedicines();
        }
        finally
        {
            medicineDAO.closeConnection();
        }
    }

    public List<Medicine> generateExpiryReport() throws SQLException
    {
        MedicineDAO medicineDAO = new MedicineDAO();
        try
        {
            return medicineDAO.getExpiringMedicines();
        }
        finally
        {
            medicineDAO.closeConnection();
        }
    }
}

