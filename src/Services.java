import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

class WindowIcon
{
    private WindowIcon() {}

    public static void apply(JFrame frame)
    {
        java.net.URL resource = WindowIcon.class.getResource("/protection.png");
        ImageIcon icon = resource == null
                ? new ImageIcon("src/protection.png")
                : new ImageIcon(resource);

        if (icon.getIconWidth() > 0 && icon.getIconHeight() > 0)
        {
            frame.setIconImage(icon.getImage());
        }
    }
}

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
        WindowIcon.apply(this);
        setTitle("HealthFirst Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 180);
        setLocationRelativeTo(null);

        JPanel form = new JPanel(new GridLayout(3, 2, 5, 5));
        form.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
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
        if (usernameField.getText().trim().isEmpty()
                || passwordField.getPassword().length == 0)
        {
            JOptionPane.showMessageDialog(this, "Enter both username and password.");
            return;
        }
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
        WindowIcon.apply(this);
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
        JButton add = new JButton("Add");
        JButton update = new JButton("Update");
        JButton delete = new JButton("Delete");
        controls.add(searchField, BorderLayout.CENTER);
        controls.add(search, BorderLayout.EAST);
        controls.add(refresh, BorderLayout.WEST);
        JPanel actions = new JPanel();
        actions.add(add);
        actions.add(update);
        actions.add(delete);
        controls.add(actions, BorderLayout.SOUTH);
        add(controls, BorderLayout.NORTH);
        output.setEditable(false);
        add(new JScrollPane(output), BorderLayout.CENTER);
        refresh.addActionListener(event -> loadMedicines(null));
        search.addActionListener(event -> loadMedicines(searchField.getText().trim()));
        add.addActionListener(event -> editMedicine(null));
        update.addActionListener(event ->
        {
            Medicine medicine = selectedMedicine();
            if (medicine != null)
            {
                editMedicine(medicine);
            }
        });
        delete.addActionListener(event -> deleteMedicine());
        loadMedicines(null);
    }

    private Medicine selectedMedicine()
    {
        try
        {
            int id = Integer.parseInt(JOptionPane.showInputDialog(this, "Medicine ID:"));
            MedicineDAO dao = new MedicineDAO();
            try
            {
                return dao.getMedicineById(id);
            }
            finally
            {
                dao.closeConnection();
            }
        }
        catch (Exception exception)
        {
            JOptionPane.showMessageDialog(this, "Invalid medicine ID.");
            return null;
        }
    }

    private void editMedicine(Medicine medicine)
    {
        boolean update = medicine != null;
        JTextField name = new JTextField(update ? medicine.getName() : "");
        JTextField company = new JTextField(update ? medicine.getCompany() : "");
        JTextField type = new JTextField(update ? medicine.getMedicineType() : "");
        JTextField price = new JTextField(update ? String.valueOf(medicine.getPrice()) : "");
        JTextField quantity = new JTextField(update ? String.valueOf(medicine.getQuantityInStock()) : "0");
        JTextField reorder = new JTextField(update ? String.valueOf(medicine.getReorderLevel()) : "10");
        JTextField expiry = new JTextField(update ? String.valueOf(medicine.getExpiryDate()) : "");
        JTextField supplier = new JTextField(update ? String.valueOf(medicine.getSupplierId()) : "");
        JPanel form = new JPanel(new GridLayout(0, 2));
        JTextField[] fields = {name, company, type, price, quantity, reorder, expiry, supplier};
        String[] labels = {"Name", "Company", "Type", "Price", "Quantity", "Reorder level", "Expiry (YYYY-MM-DD)", "Supplier ID"};
        for (int i = 0; i < fields.length; i++)
        {
            form.add(new JLabel(labels[i]));
            form.add(fields[i]);
        }
        if (JOptionPane.showConfirmDialog(this, form, update ? "Update medicine" : "Add medicine",
                JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION)
        {
            return;
        }
        Medicine value = update ? medicine : new Medicine();
        try
        {
            value.setName(name.getText().trim());
            value.setCompany(company.getText().trim());
            value.setMedicineType(type.getText().trim());
            value.setPrice(Double.parseDouble(price.getText().trim()));
            value.setQuantityInStock(Integer.parseInt(quantity.getText().trim()));
            value.setReorderLevel(Integer.parseInt(reorder.getText().trim()));
            value.setExpiryDate(LocalDate.parse(expiry.getText().trim()));
            value.setSupplierId(Integer.parseInt(supplier.getText().trim()));
            MedicineDAO dao = new MedicineDAO();
            try
            {
                if (!(update ? dao.updateMedicine(value) : dao.addMedicine(value)))
                {
                    throw new SQLException("No medicine was changed.");
                }
            }
            finally
            {
                dao.closeConnection();
            }
            loadMedicines(null);
        }
        catch (Exception exception)
        {
            JOptionPane.showMessageDialog(this, "Unable to save medicine: " + exception.getMessage());
        }
    }

    private void deleteMedicine()
    {
        String value = JOptionPane.showInputDialog(this, "Medicine ID to delete:");
        try
        {
            int id = Integer.parseInt(value);
            MedicineDAO dao = new MedicineDAO();
            try
            {
                if (!dao.deleteMedicine(id))
                {
                    throw new SQLException("Medicine was not found.");
                }
            }
            finally
            {
                dao.closeConnection();
            }
            loadMedicines(null);
        }
        catch (Exception exception)
        {
            JOptionPane.showMessageDialog(this, "Unable to delete medicine: " + exception.getMessage());
        }
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
        JButton add = new JButton("Add");
        JButton update = new JButton("Update");
        JButton delete = new JButton("Delete");
        JPanel controls = new JPanel();
        controls.add(refresh);
        controls.add(add);
        controls.add(update);
        controls.add(delete);
        output.setEditable(false);
        add(controls, BorderLayout.NORTH);
        add(new JScrollPane(output), BorderLayout.CENTER);
        refresh.addActionListener(event -> loadSuppliers());
        add.addActionListener(event -> editSupplier(null));
        update.addActionListener(event ->
        {
            Supplier supplier = selectedSupplier();
            if (supplier != null)
            {
                editSupplier(supplier);
            }
        });
        delete.addActionListener(event -> deleteSupplier());
        loadSuppliers();
    }

    private Supplier selectedSupplier()
    {
        try
        {
            int id = Integer.parseInt(JOptionPane.showInputDialog(this, "Supplier ID:"));
            SupplierDAO dao = new SupplierDAO();
            try
            {
                for (Supplier supplier : dao.getAllSuppliers())
                {
                    if (supplier.getSupplierId() == id)
                    {
                        return supplier;
                    }
                }
            }
            finally
            {
                dao.closeConnection();
            }
        }
        catch (Exception ignored)
        {
            // The edit dialog reports a missing selection.
        }
        return null;
    }

    private void editSupplier(Supplier supplier)
    {
        boolean update = supplier != null;
        JTextField[] fields = {
                new JTextField(update ? supplier.getName() : ""),
                new JTextField(update ? supplier.getContactPerson() : ""),
                new JTextField(update ? supplier.getPhoneNumber() : ""),
                new JTextField(update ? supplier.getEmail() : ""),
                new JTextField(update ? supplier.getAddress() : "")
        };
        String[] labels = {"Name", "Contact person", "Phone", "Email", "Address"};
        JPanel form = new JPanel(new GridLayout(0, 2));
        for (int i = 0; i < fields.length; i++)
        {
            form.add(new JLabel(labels[i]));
            form.add(fields[i]);
        }
        if (JOptionPane.showConfirmDialog(this, form, update ? "Update supplier" : "Add supplier",
                JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION)
        {
            return;
        }
        Supplier value = update ? supplier : new Supplier();
        value.setName(fields[0].getText().trim());
        value.setContactPerson(fields[1].getText().trim());
        value.setPhoneNumber(fields[2].getText().trim());
        value.setEmail(fields[3].getText().trim());
        value.setAddress(fields[4].getText().trim());
        SupplierDAO dao = null;
        try
        {
            dao = new SupplierDAO();
            if (!(update ? dao.updateSupplier(value) : dao.addSupplier(value)))
            {
                throw new SQLException("No supplier was changed.");
            }
            loadSuppliers();
        }
        catch (SQLException exception)
        {
            JOptionPane.showMessageDialog(this, "Unable to save supplier: " + exception.getMessage());
        }
        finally
        {
            if (dao != null)
            {
                dao.closeConnection();
            }
        }
    }

    private void deleteSupplier()
    {
        try
        {
            int id = Integer.parseInt(JOptionPane.showInputDialog(this, "Supplier ID to delete:"));
            SupplierDAO dao = new SupplierDAO();
            try
            {
                if (!dao.deleteSupplier(id))
                {
                    throw new SQLException("Supplier was not found.");
                }
            }
            finally
            {
                dao.closeConnection();
            }
            loadSuppliers();
        }
        catch (Exception exception)
        {
            JOptionPane.showMessageDialog(this, "Unable to delete supplier: " + exception.getMessage());
        }
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
        JButton add = new JButton("Add cashier");
        JButton delete = new JButton("Delete cashier");
        JPanel controls = new JPanel();
        controls.add(refresh);
        controls.add(add);
        controls.add(delete);
        output.setEditable(false);
        add(controls, BorderLayout.NORTH);
        add(new JScrollPane(output), BorderLayout.CENTER);
        refresh.addActionListener(event -> loadUsers());
        add.addActionListener(event -> addUser());
        delete.addActionListener(event -> deleteUser());
        loadUsers();
    }

    private void addUser()
    {
        JTextField username = new JTextField();
        JPasswordField password = new JPasswordField();
        JTextField fullName = new JTextField();
        JPanel form = new JPanel(new GridLayout(0, 2));
        form.add(new JLabel("Username"));
        form.add(username);
        form.add(new JLabel("Password"));
        form.add(password);
        form.add(new JLabel("Full name"));
        form.add(fullName);
        if (JOptionPane.showConfirmDialog(this, form, "Add cashier",
                JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION)
        {
            return;
        }
        User user = new User();
        user.setUsername(username.getText().trim());
        user.setPassword(new String(password.getPassword()));
        user.setFullName(fullName.getText().trim());
        user.setRole(Role.CASHIER);
        UserDAO dao = null;
        try
        {
            dao = new UserDAO();
            if (!dao.addUser(user))
            {
                throw new SQLException("Cashier was not added.");
            }
            loadUsers();
        }
        catch (SQLException exception)
        {
            JOptionPane.showMessageDialog(this, "Unable to add cashier: " + exception.getMessage());
        }
        finally
        {
            if (dao != null)
            {
                dao.closeConnection();
            }
        }
    }

    private void deleteUser()
    {
        try
        {
            int id = Integer.parseInt(JOptionPane.showInputDialog(this, "Cashier user ID to delete:"));
            UserDAO dao = new UserDAO();
            try
            {
                if (!dao.deleteUser(id))
                {
                    throw new SQLException("Cashier was not found.");
                }
            }
            finally
            {
                dao.closeConnection();
            }
            loadUsers();
        }
        catch (Exception exception)
        {
            JOptionPane.showMessageDialog(this, "Unable to delete cashier: " + exception.getMessage());
        }
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
        WindowIcon.apply(this);
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
        JButton clear = new JButton("Clear cart");
        controls.add(new JLabel("Medicine ID"));
        controls.add(medicineIdField);
        controls.add(new JLabel("Quantity"));
        controls.add(quantityField);
        controls.add(add);
        add(controls, BorderLayout.NORTH);
        cartOutput.setEditable(false);
        add(new JScrollPane(cartOutput), BorderLayout.CENTER);
        add(checkout, BorderLayout.SOUTH);
        add(clear, BorderLayout.WEST);
        add.addActionListener(event -> addItem());
        checkout.addActionListener(event -> checkout());
        clear.addActionListener(event -> clearCart());
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
            new BillWindow(sale, new ArrayList<>(cartItems)).setVisible(true);
            cartItems.clear();
            cartOutput.setText("");
        }
        catch (SQLException | IllegalArgumentException exception)
        {
            JOptionPane.showMessageDialog(this, exception.getMessage());
        }
    }

    private void clearCart()
    {
        cartItems.clear();
        cartOutput.setText("");
    }
}

class BillWindow extends JFrame
{
    public BillWindow(Sale sale, List<SaleItem> items)
    {
        WindowIcon.apply(this);
        setTitle("Sale " + sale.getSaleId());
        setSize(400, 300);
        setLocationRelativeTo(null);
        StringBuilder billText = new StringBuilder(sale.toString()).append("\n\nItems:\n");
        for (SaleItem item : items)
        {
            billText.append("Medicine ").append(item.getMedicineId())
                    .append(" x ").append(item.getQuantitySold())
                    .append(" @ ").append(item.getPriceAtSale()).append("\n");
        }
        JTextArea bill = new JTextArea(billText.toString());
        bill.setEditable(false);
        JButton save = new JButton("Save");
        JButton print = new JButton("Print");
        JPanel actions = new JPanel();
        actions.add(save);
        actions.add(print);
        save.addActionListener(event ->
        {
            try
            {
                JFileChooser chooser = new JFileChooser();
                if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION)
                {
                    Files.writeString(chooser.getSelectedFile().toPath(), bill.getText());
                    JOptionPane.showMessageDialog(this, "Bill saved.");
                }
            }
            catch (Exception exception)
            {
                JOptionPane.showMessageDialog(this, "Unable to save bill: " + exception.getMessage());
            }
        });
        print.addActionListener(event ->
        {
            try
            {
                if (bill.print())
                {
                    JOptionPane.showMessageDialog(this, "Bill sent to printer.");
                }
            }
            catch (Exception exception)
            {
                JOptionPane.showMessageDialog(this, "Unable to print bill: " + exception.getMessage());
            }
        });
        add(new JScrollPane(bill), BorderLayout.CENTER);
        add(actions, BorderLayout.SOUTH);
    }
}

class StockCheckPanel extends JPanel
{
    private final javax.swing.JTextArea output = new javax.swing.JTextArea();
    private final JTextField medicineId = new JTextField();

    public StockCheckPanel()
    {
        setLayout(new BorderLayout(5, 5));
        JButton refresh = new JButton("Refresh stock warnings");
        JButton check = new JButton("Check medicine");
        JPanel controls = new JPanel(new BorderLayout(5, 5));
        controls.add(medicineId, BorderLayout.CENTER);
        controls.add(check, BorderLayout.EAST);
        controls.add(refresh, BorderLayout.WEST);
        output.setEditable(false);
        add(controls, BorderLayout.NORTH);
        add(new JScrollPane(output), BorderLayout.CENTER);
        refresh.addActionListener(event -> loadWarnings());
        check.addActionListener(event -> checkMedicine());
        loadWarnings();
    }

    private void checkMedicine()
    {
        MedicineDAO dao = null;
        try
        {
            int id = Integer.parseInt(medicineId.getText().trim());
            dao = new MedicineDAO();
            Medicine medicine = dao.getMedicineById(id);
            output.setText(medicine == null ? "Medicine not found." : medicine.toString());
        }
        catch (Exception exception)
        {
            output.setText("Unable to check medicine: " + exception.getMessage());
        }
        finally
        {
            if (dao != null)
            {
                dao.closeConnection();
            }
        }
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
