import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JScrollPane;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
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

class LoginFrame extends JFrame
{
    private final JTextField usernameField = new JTextField();
    private final JPasswordField passwordField = new JPasswordField();

    public LoginFrame()
    {
        setTitle("HealthFirst Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(350, 180);
        setLocationRelativeTo(null);

        JPanel form = new JPanel(new GridLayout(3, 2, 5, 5));
        JButton loginButton = new JButton("Login");
        form.add(new JLabel("Username:"));
        form.add(usernameField);
        form.add(new JLabel("Password:"));
        form.add(passwordField);
        form.add(new JLabel());
        form.add(loginButton);
        add(form);

        loginButton.addActionListener(event -> login());
    }

    private void login()
    {
        try
        {
            User user = new AuthService().login(
                    usernameField.getText(),
                    new String(passwordField.getPassword()));
            if (user == null)
            {
                javax.swing.JOptionPane.showMessageDialog(this, "Invalid login details.");
            }
            else if (user.getRole() == Role.ADMIN)
            {
                new AdminDashboard(user).setVisible(true);
                dispose();
            }
            else
            {
                new CashierDashboard(user).setVisible(true);
                dispose();
            }
        }
        catch (SQLException exception)
        {
            javax.swing.JOptionPane.showMessageDialog(this,
                    "Unable to log in: " + exception.getMessage());
        }
    }
}

class AdminDashboard extends JFrame
{
    public AdminDashboard(User user)
    {
        setTitle("Admin Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Medicines", new ManageMedicinesPanel());
        tabs.addTab("Suppliers", new ManageSuppliersPanel());
        tabs.addTab("Users", new ManageUsersPanel());
        tabs.addTab("Reports", new ReportsPanel());
        add(tabs);
    }
}

class ManageMedicinesPanel extends JPanel
{
    private final JTextField searchField = new JTextField();
    private final javax.swing.JTextArea output = new javax.swing.JTextArea();

    public ManageMedicinesPanel()
    {
        setLayout(new BorderLayout(5, 5));
        JPanel controls = new JPanel(new BorderLayout(5, 5));
        JButton refresh = new JButton("Refresh");
        JButton search = new JButton("Search");
        controls.add(searchField, BorderLayout.CENTER);
        controls.add(search, BorderLayout.EAST);
        controls.add(refresh, BorderLayout.WEST);
        add(controls, BorderLayout.NORTH);
        output.setEditable(false);
        add(new JScrollPane(output), BorderLayout.CENTER);
        refresh.addActionListener(event -> loadMedicines(null));
        search.addActionListener(event -> loadMedicines(searchField.getText().trim()));
        loadMedicines(null);
    }

    private void loadMedicines(String searchTerm)
    {
        MedicineDAO dao = null;
        try
        {
            dao = new MedicineDAO();
            List<Medicine> medicines = searchTerm == null || searchTerm.isEmpty()
                    ? dao.getAllMedicines() : dao.searchMedicine(searchTerm);
            output.setText("");
            for (Medicine medicine : medicines)
            {
                output.append(medicine + "\n\n");
            }
        }
        catch (SQLException exception)
        {
            output.setText("Unable to load medicines: " + exception.getMessage());
        }
        finally
        {
            if (dao != null)
            {
                dao.closeConnection();
            }
        }
    }
}

class ManageSuppliersPanel extends JPanel
{
    private final javax.swing.JTextArea output = new javax.swing.JTextArea();

    public ManageSuppliersPanel()
    {
        setLayout(new BorderLayout(5, 5));
        JButton refresh = new JButton("Refresh suppliers");
        output.setEditable(false);
        add(refresh, BorderLayout.NORTH);
        add(new JScrollPane(output), BorderLayout.CENTER);
        refresh.addActionListener(event -> loadSuppliers());
        loadSuppliers();
    }

    private void loadSuppliers()
    {
        SupplierDAO dao = null;
        try
        {
            dao = new SupplierDAO();
            output.setText("");
            for (Supplier supplier : dao.getAllSuppliers())
            {
                output.append(supplier + "\n\n");
            }
        }
        catch (SQLException exception)
        {
            output.setText("Unable to load suppliers: " + exception.getMessage());
        }
        finally
        {
            if (dao != null)
            {
                dao.closeConnection();
            }
        }
    }
}

class ManageUsersPanel extends JPanel
{
    private final javax.swing.JTextArea output = new javax.swing.JTextArea();

    public ManageUsersPanel()
    {
        setLayout(new BorderLayout(5, 5));
        JButton refresh = new JButton("Refresh cashiers");
        output.setEditable(false);
        add(refresh, BorderLayout.NORTH);
        add(new JScrollPane(output), BorderLayout.CENTER);
        refresh.addActionListener(event -> loadUsers());
        loadUsers();
    }

    private void loadUsers()
    {
        UserDAO dao = null;
        try
        {
            dao = new UserDAO();
            output.setText("");
            for (User user : dao.getAllCashiers())
            {
                output.append(user + "\n\n");
            }
        }
        catch (SQLException exception)
        {
            output.setText("Unable to load users: " + exception.getMessage());
        }
        finally
        {
            if (dao != null)
            {
                dao.closeConnection();
            }
        }
    }
}

class ReportsPanel extends JPanel
{
    private final javax.swing.JTextArea output = new javax.swing.JTextArea();

    public ReportsPanel()
    {
        setLayout(new BorderLayout(5, 5));
        JPanel controls = new JPanel();
        JButton sales = new JButton("Sales");
        JButton items = new JButton("Item-wise");
        JButton lowStock = new JButton("Low stock");
        JButton expiry = new JButton("Expiring");
        controls.add(sales);
        controls.add(items);
        controls.add(lowStock);
        controls.add(expiry);
        output.setEditable(false);
        add(controls, BorderLayout.NORTH);
        add(new JScrollPane(output), BorderLayout.CENTER);
        sales.addActionListener(event -> salesReport());
        items.addActionListener(event -> itemReport());
        lowStock.addActionListener(event -> medicineReport(false));
        expiry.addActionListener(event -> medicineReport(true));
    }

    private void salesReport()
    {
        try
        {
            List<Sale> sales = new ReportService().generateSalesReport(
                    LocalDate.now().minusMonths(1), LocalDate.now());
            output.setText("");
            for (Sale sale : sales)
            {
                output.append(sale + "\n\n");
            }
        }
        catch (SQLException exception)
        {
            output.setText("Unable to load sales: " + exception.getMessage());
        }
    }

    private void itemReport()
    {
        try
        {
            output.setText("");
            for (ItemWiseSalesReport report : new ReportService().generateItemWiseReport())
            {
                output.append(report.getMedicineName() + " | Quantity: "
                        + report.getQuantitySold() + " | Revenue: "
                        + report.getTotalRevenue() + "\n");
            }
        }
        catch (SQLException exception)
        {
            output.setText("Unable to load item report: " + exception.getMessage());
        }
    }

    private void medicineReport(boolean expiring)
    {
        try
        {
            List<Medicine> medicines = expiring
                    ? new ReportService().generateExpiryReport()
                    : new ReportService().generateLowStockReport();
            output.setText("");
            for (Medicine medicine : medicines)
            {
                output.append(medicine + "\n\n");
            }
        }
        catch (SQLException exception)
        {
            output.setText("Unable to load medicine report: " + exception.getMessage());
        }
    }
}

class CashierDashboard extends JFrame
{
    public CashierDashboard(User user)
    {
        setTitle("Cashier Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Point of Sale", new POSPanel(user.getUserId()));
        tabs.addTab("Stock Check", new StockCheckPanel());
        add(tabs);
    }
}

class POSPanel extends JPanel
{
    private final int userId;
    private final JTextField medicineIdField = new JTextField();
    private final JTextField quantityField = new JTextField();
    private final javax.swing.JTextArea cartOutput = new javax.swing.JTextArea();
    private final List<SaleItem> cartItems = new ArrayList<>();

    public POSPanel(int user_id)
    {
        this.userId = user_id;
        setLayout(new BorderLayout());
        JPanel controls = new JPanel(new GridLayout(1, 5, 5, 5));
        JButton add = new JButton("Add item");
        JButton checkout = new JButton("Checkout");
        controls.add(new JLabel("Medicine ID"));
        controls.add(medicineIdField);
        controls.add(new JLabel("Quantity"));
        controls.add(quantityField);
        controls.add(add);
        add(controls, BorderLayout.NORTH);
        cartOutput.setEditable(false);
        add(new JScrollPane(cartOutput), BorderLayout.CENTER);
        add(checkout, BorderLayout.SOUTH);
        add.addActionListener(event -> addItem());
        checkout.addActionListener(event -> checkout());
    }

    private void addItem()
    {
        MedicineDAO dao = null;
        try
        {
            int medicineId = Integer.parseInt(medicineIdField.getText().trim());
            int quantity = Integer.parseInt(quantityField.getText().trim());
            dao = new MedicineDAO();
            Medicine medicine = dao.getMedicineById(medicineId);
            if (medicine == null || quantity <= 0 || medicine.getQuantityInStock() < quantity)
            {
                throw new IllegalArgumentException("Medicine not found or insufficient stock.");
            }
            SaleItem item = new SaleItem();
            item.setMedicineId(medicineId);
            item.setQuantitySold(quantity);
            item.setPriceAtSale(medicine.getPrice());
            cartItems.add(item);
            cartOutput.append(medicine.getName() + " x " + quantity + "\n");
        }
        catch (IllegalArgumentException | SQLException exception)
        {
            JOptionPane.showMessageDialog(this, exception.getMessage());
        }
        finally
        {
            if (dao != null)
            {
                dao.closeConnection();
            }
        }
    }

    private void checkout()
    {
        try
        {
            Sale sale = new SalesService().processSale(cartItems, userId);
            new BillWindow(sale).setVisible(true);
            cartItems.clear();
            cartOutput.setText("");
        }
        catch (SQLException | IllegalArgumentException exception)
        {
            JOptionPane.showMessageDialog(this, exception.getMessage());
        }
    }
}

class BillWindow extends JFrame
{
    public BillWindow(Sale sale)
    {
        setTitle("Sale " + sale.getSaleId());
        setSize(400, 300);
        setLocationRelativeTo(null);
        add(new JLabel(sale.toString()));
    }
}

class StockCheckPanel extends JPanel
{
    private final javax.swing.JTextArea output = new javax.swing.JTextArea();

    public StockCheckPanel()
    {
        setLayout(new BorderLayout(5, 5));
        JButton refresh = new JButton("Refresh stock warnings");
        output.setEditable(false);
        add(refresh, BorderLayout.NORTH);
        add(new JScrollPane(output), BorderLayout.CENTER);
        refresh.addActionListener(event -> loadWarnings());
        loadWarnings();
    }

    private void loadWarnings()
    {
        try
        {
            ReportService reports = new ReportService();
            output.setText("LOW STOCK\n=========\n");
            for (Medicine medicine : reports.generateLowStockReport())
            {
                output.append(medicine.getName() + " (" + medicine.getQuantityInStock()
                        + " remaining)\n");
            }
            output.append("\nEXPIRING SOON\n=============\n");
            for (Medicine medicine : reports.generateExpiryReport())
            {
                output.append(medicine.getName() + " (" + medicine.getExpiryDate() + ")\n");
            }
        }
        catch (SQLException exception)
        {
            output.setText("Unable to load stock warnings: " + exception.getMessage());
        }
    }
}
