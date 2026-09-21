import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.nio.file.Files;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

final class UITheme
{
    private UITheme() {}

    static final Color MAIN = new Color(0x25, 0x26, 0x2B);
    static final Color MAIN_DARK = new Color(0x1B, 0x1C, 0x20);
    static final Color MAIN_LIGHT = new Color(0xE5, 0xE5, 0xE7);

    static final Color SECONDARY = new Color(0xA7, 0x8B, 0xFA);
    static final Color SECONDARY_DARK = new Color(0x8B, 0x6F, 0xE8);
    static final Color SECONDARY_LIGHT = new Color(0xF0, 0xEB, 0xFF);

    static final Color ACCENT = new Color(0x22, 0xD3, 0xEE);
    static final Color ACCENT_DARK = new Color(0x0E, 0xAF, 0xC8);
    static final Color ACCENT_LIGHT = new Color(0xD5, 0xF8, 0xFC);

    static final Color BACKGROUND = new Color(0xF6, 0xF8, 0xF7);
    static final Color PANEL = Color.WHITE;
    static final Color BORDER = new Color(0xDC, 0xE3, 0xE0);
    static final Color TEXT_DARK = new Color(0x1C, 0x26, 0x22);
    static final Color TEXT_MUTED = new Color(0x62, 0x70, 0x6B);

    static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 21);
    static final Font SUBTITLE_FONT = new Font("Segoe UI", Font.PLAIN, 12);
    static final Font HEADING_FONT = new Font("Segoe UI", Font.BOLD, 13);
    static final Font BODY_FONT = new Font("Segoe UI", Font.PLAIN, 13);
    static final Font BUTTON_FONT = new Font("Segoe UI", Font.BOLD, 13);
    static final Font MONO_FONT = new Font("Consolas", Font.PLAIN, 13);
    static final Font BADGE_FONT = new Font("Segoe UI", Font.BOLD, 11);

    static void applyGlobalDefaults()
    {
        try
        {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        catch (Exception ignored)
        {
        }
        UIManager.put("Panel.background", BACKGROUND);
        UIManager.put("OptionPane.background", BACKGROUND);
        UIManager.put("OptionPane.messageFont", BODY_FONT);
        UIManager.put("OptionPane.buttonFont", BUTTON_FONT);
        UIManager.put("TextField.font", BODY_FONT);
        UIManager.put("PasswordField.font", BODY_FONT);
        UIManager.put("Label.font", BODY_FONT);
        UIManager.put("TabbedPane.selected", MAIN_LIGHT);
        UIManager.put("TabbedPane.font", HEADING_FONT);
    }

    static JPanel headerBar(String icon, String title, String subtitle)
    {
        return headerBar(new JLabel(icon + "  " + title), title, subtitle);
    }

    static JPanel headerBar(ImageIcon icon, String title, String subtitle)
    {
        JLabel titleLabel = new JLabel(title, icon, JLabel.LEFT);
        return headerBar(titleLabel, title, subtitle);
    }

    private static JPanel headerBar(JLabel titleLabel, String title, String subtitle)
    {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(MAIN);
        header.setBorder(BorderFactory.createEmptyBorder(16, 24, 16, 24));

        titleLabel.setFont(TITLE_FONT);
        titleLabel.setForeground(Color.WHITE);
        header.add(titleLabel, BorderLayout.WEST);

        if (subtitle != null && !subtitle.isEmpty())
        {
            JLabel subtitleLabel = new JLabel(subtitle);
            subtitleLabel.setFont(SUBTITLE_FONT);
            subtitleLabel.setForeground(new Color(255, 255, 255, 215));
            header.add(subtitleLabel, BorderLayout.EAST);
        }
        return header;
    }

    static JLabel badge(String text, Color background)
    {
        JLabel label = new JLabel(text, SwingConstants.CENTER)
        {
            @Override
            protected void paintComponent(Graphics g)
            {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(background);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        label.setFont(BADGE_FONT);
        label.setForeground(Color.WHITE);
        label.setOpaque(false);
        label.setBorder(BorderFactory.createEmptyBorder(4, 14, 4, 14));
        return label;
    }

    static JPanel card(LayoutManager layout)
    {
        JPanel panel = new JPanel(layout)
        {
            @Override
            protected void paintComponent(Graphics g)
            {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(PANEL);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
                g2.setColor(BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        return panel;
    }

    static void styleTabbedPane(JTabbedPane tabs)
    {
        tabs.setFont(HEADING_FONT);
        tabs.setBackground(BACKGROUND);
        tabs.setForeground(TEXT_DARK);
    }

    static void styleTextField(JTextField field)
    {
        field.setFont(BODY_FONT);
        field.setForeground(TEXT_DARK);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1, true),
                BorderFactory.createEmptyBorder(7, 10, 7, 10)));
    }

    static void styleTextArea(JTextArea area)
    {
        area.setFont(MONO_FONT);
        area.setForeground(TEXT_DARK);
        area.setBackground(PANEL);
        area.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    }

    static void styleTextPane(JTextPane pane)
    {
        pane.setFont(MONO_FONT);
        pane.setForeground(TEXT_DARK);
        pane.setBackground(PANEL);
        pane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    }

    static void appendLine(JTextPane pane, String text, Color color, boolean bold)
    {
        StyledDocument doc = pane.getStyledDocument();
        Style style = pane.addStyle("s" + doc.getLength() + "-" + System.nanoTime(), null);
        StyleConstants.setForeground(style, color);
        StyleConstants.setBold(style, bold);
        StyleConstants.setFontFamily(style, MONO_FONT.getFamily());
        StyleConstants.setFontSize(style, MONO_FONT.getSize());
        try
        {
            doc.insertString(doc.getLength(), text + "\n", style);
        }
        catch (BadLocationException ignored)
        {
        }
    }

    static void appendIconLine(JTextPane pane, String iconFile, String text,
                               Color color, boolean bold)
    {
        StyledDocument doc = pane.getStyledDocument();
        Style style = pane.addStyle("i" + doc.getLength() + "-" + System.nanoTime(), null);
        StyleConstants.setForeground(style, color);
        StyleConstants.setBold(style, bold);
        StyleConstants.setFontFamily(style, MONO_FONT.getFamily());
        StyleConstants.setFontSize(style, MONO_FONT.getSize());
        try
        {
            ImageIcon icon = WindowIcon.loadIcon(iconFile);
            doc.insertString(doc.getLength(), " ", style);
            pane.setCaretPosition(doc.getLength());
            pane.insertIcon(icon);
            doc.insertString(doc.getLength(), " " + text + "\n", style);
        }
        catch (BadLocationException ignored)
        {
        }
    }
}

class RoundedButton extends JButton
{
    enum Role { PRIMARY, SECONDARY, ACCENT, GHOST }

    private static final int ARC = 16;
    private final Role role;
    private boolean hover = false;

    RoundedButton(String text, Role role)
    {
        super(text);
        this.role = role;
        setFont(UITheme.BUTTON_FONT);
        setForeground(role == Role.GHOST ? UITheme.TEXT_DARK : Color.WHITE);
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setOpaque(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setBorder(BorderFactory.createEmptyBorder(11, 22, 11, 22));
        addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseEntered(MouseEvent e)
            {
                hover = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e)
            {
                hover = false;
                repaint();
            }
        });
    }

    private Color baseColor()
    {
        switch (role)
        {
            case PRIMARY: return UITheme.MAIN;
            case SECONDARY: return UITheme.SECONDARY;
            case ACCENT: return UITheme.ACCENT;
            default: return UITheme.PANEL;
        }
    }

    private Color hoverColor()
    {
        switch (role)
        {
            case PRIMARY: return UITheme.MAIN_DARK;
            case SECONDARY: return UITheme.SECONDARY_DARK;
            case ACCENT: return UITheme.ACCENT_DARK;
            default: return UITheme.BORDER;
        }
    }

    @Override
    protected void paintComponent(Graphics g)
    {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(hover ? hoverColor() : baseColor());
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), ARC, ARC);
        if (role == Role.GHOST)
        {
            g2.setColor(UITheme.BORDER);
            g2.setStroke(new BasicStroke(1.4f));
            g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, ARC, ARC);
        }
        g2.dispose();
        super.paintComponent(g);
    }
}

class WindowIcon
{
    private WindowIcon() {}

    public static ImageIcon loadIcon(String fileName)
    {
        java.net.URL resource = WindowIcon.class.getResource("/" + fileName);
        ImageIcon icon = resource == null
                ? new ImageIcon("src/" + fileName)
                : new ImageIcon(resource);
        if (icon.getIconWidth() <= 0 || icon.getIconHeight() <= 0)
        {
            return new ImageIcon();
        }
        Image image = icon.getImage().getScaledInstance(28, 28, Image.SCALE_SMOOTH);
        return new ImageIcon(image);
    }

    public static ImageIcon loadIcon()
    {
        return loadIcon("protection.png");
    }

    public static void apply(JFrame frame)
    {
        ImageIcon icon = loadIcon();
        if (icon.getIconWidth() > 0 && icon.getIconHeight() > 0)
        {
            frame.setIconImage(icon.getImage());
        }
    }
}

class LoginFrame extends JFrame
{
    private final JTextField usernameField = new JTextField();
    private final JPasswordField passwordField = new JPasswordField();

    public LoginFrame()
    {
        UITheme.applyGlobalDefaults();
        WindowIcon.apply(this);
        setTitle("HealthFirst Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(440, 420);
        setLocationRelativeTo(null);
        getContentPane().setBackground(UITheme.BACKGROUND);
        setLayout(new BorderLayout());

        add(UITheme.headerBar(WindowIcon.loadIcon(), "HealthFirst", "Pharmacy Management"),
                BorderLayout.NORTH);

        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setBackground(UITheme.BACKGROUND);
        wrapper.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        JPanel card = UITheme.card(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;

        JLabel heading = new JLabel("Sign in to continue");
        heading.setFont(UITheme.HEADING_FONT);
        heading.setForeground(UITheme.TEXT_DARK);
        c.gridy = 0;
        c.gridwidth = 2;
        card.add(heading, c);

        c.gridwidth = 1;
        c.gridy = 1;
        card.add(new JLabel("Username"), c);
        c.gridy = 2;
        UITheme.styleTextField(usernameField);
        card.add(usernameField, c);

        c.gridy = 3;
        card.add(new JLabel("Password"), c);
        c.gridy = 4;
        UITheme.styleTextField(passwordField);
        card.add(passwordField, c);

        RoundedButton loginButton = new RoundedButton("Log In", RoundedButton.Role.PRIMARY);
        loginButton.setPreferredSize(new Dimension(0, 44));
        c.gridy = 5;
        c.insets = new Insets(18, 6, 6, 6);
        card.add(loginButton, c);

        JLabel hint = new JLabel("Press Enter after typing your password to log in.");
        hint.setFont(UITheme.SUBTITLE_FONT);
        hint.setForeground(UITheme.TEXT_MUTED);
        c.gridy = 6;
        c.insets = new Insets(4, 6, 6, 6);
        card.add(hint, c);

        wrapper.add(card);
        add(wrapper, BorderLayout.CENTER);

        loginButton.addActionListener(event -> login());
        getRootPane().setDefaultButton(loginButton);
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
        setSize(900, 650);
        setLocationRelativeTo(null);
        getContentPane().setBackground(UITheme.BACKGROUND);
        setLayout(new BorderLayout());

        add(UITheme.headerBar(WindowIcon.loadIcon("protection.png"), "Admin Dashboard",
                "Signed in as " + user.getFullName()), BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        UITheme.styleTabbedPane(tabs);
        tabs.addTab("Medicines", WindowIcon.loadIcon("medicine.png"), new ManageMedicinesPanel());
        tabs.addTab("Suppliers", WindowIcon.loadIcon("supplier.png"), new ManageSuppliersPanel());
        tabs.addTab("Users", WindowIcon.loadIcon("users.png"), new ManageUsersPanel());
        tabs.addTab("Reports", WindowIcon.loadIcon("report.png"), new ReportsPanel());
        add(tabs, BorderLayout.CENTER);

        add(statusBar(user), BorderLayout.SOUTH);
    }

    private JPanel statusBar(User user)
    {
        JPanel status = new JPanel(new BorderLayout(12, 8));
        status.setBackground(UITheme.PANEL);
        status.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.BORDER));
        JPanel userInfo = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        userInfo.setOpaque(false);
        JLabel badge = UITheme.badge("ADMIN", UITheme.MAIN);
        userInfo.add(badge);
        JLabel name = new JLabel(user.getFullName());
        name.setFont(UITheme.BODY_FONT);
        name.setForeground(UITheme.TEXT_MUTED);
        userInfo.add(name);
        status.add(userInfo, BorderLayout.WEST);

        RoundedButton logout = new RoundedButton("Log out", RoundedButton.Role.GHOST);
        logout.addActionListener(event ->
        {
            dispose();
            new LoginFrame().setVisible(true);
        });
        status.add(logout, BorderLayout.EAST);
        return status;
    }
}

class ManageMedicinesPanel extends JPanel
{
    private final JTextField searchField = new JTextField();
    private final JTextPane output = new JTextPane();

    public ManageMedicinesPanel()
    {
        setLayout(new BorderLayout(10, 10));
        setBackground(UITheme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        JPanel controlsCard = UITheme.card(new BorderLayout(8, 8));
        RoundedButton refresh = new RoundedButton("Refresh", RoundedButton.Role.SECONDARY);
        RoundedButton search = new RoundedButton("Search", RoundedButton.Role.SECONDARY);
        RoundedButton add = new RoundedButton("Add", RoundedButton.Role.PRIMARY);
        RoundedButton update = new RoundedButton("Update", RoundedButton.Role.PRIMARY);
        RoundedButton delete = new RoundedButton("Delete", RoundedButton.Role.ACCENT);

        UITheme.styleTextField(searchField);
        JPanel searchRow = new JPanel(new BorderLayout(8, 8));
        searchRow.setOpaque(false);
        searchRow.add(searchField, BorderLayout.CENTER);
        searchRow.add(search, BorderLayout.EAST);
        searchRow.add(refresh, BorderLayout.WEST);
        controlsCard.add(searchRow, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actions.setOpaque(false);
        actions.add(add);
        actions.add(update);
        actions.add(delete);
        controlsCard.add(actions, BorderLayout.SOUTH);

        add(controlsCard, BorderLayout.NORTH);

        UITheme.styleTextPane(output);
        output.setEditable(false);
        JPanel outputCard = UITheme.card(new BorderLayout());
        outputCard.add(new JScrollPane(output), BorderLayout.CENTER);
        add(outputCard, BorderLayout.CENTER);

        JLabel legend = new JLabel("Blue rows are at or below their reorder level");
        legend.setFont(UITheme.SUBTITLE_FONT);
        legend.setForeground(UITheme.ACCENT_DARK);
        legend.setBorder(BorderFactory.createEmptyBorder(6, 4, 0, 0));
        add(legend, BorderLayout.SOUTH);

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
        JTextField[] fields = {name, company, type, price, quantity, reorder, expiry, supplier};
        for (JTextField field : fields)
        {
            UITheme.styleTextField(field);
        }
        JPanel form = new JPanel(new GridLayout(0, 2, 6, 6));
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
        output.setText("");
        try
        {
            dao = new MedicineDAO();
            List<Medicine> medicines = searchTerm == null || searchTerm.isEmpty()
                    ? dao.getAllMedicines() : dao.searchMedicine(searchTerm);
            for (Medicine medicine : medicines)
            {
                boolean lowStock = medicine.getQuantityInStock() <= medicine.getReorderLevel();
                UITheme.appendLine(output, medicine.toString(),
                        lowStock ? UITheme.ACCENT_DARK : UITheme.TEXT_DARK, lowStock);
                UITheme.appendLine(output, "", UITheme.TEXT_DARK, false);
            }
        }
        catch (SQLException exception)
        {
            UITheme.appendLine(output, "Unable to load medicines: " + exception.getMessage(), UITheme.ACCENT_DARK, true);
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
    private final JTextArea output = new JTextArea();

    public ManageSuppliersPanel()
    {
        setLayout(new BorderLayout(10, 10));
        setBackground(UITheme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        JPanel controlsCard = UITheme.card(new FlowLayout(FlowLayout.LEFT, 8, 0));
        RoundedButton refresh = new RoundedButton("Refresh suppliers", RoundedButton.Role.SECONDARY);
        RoundedButton add = new RoundedButton("Add", RoundedButton.Role.PRIMARY);
        RoundedButton update = new RoundedButton("Update", RoundedButton.Role.PRIMARY);
        RoundedButton delete = new RoundedButton("Delete", RoundedButton.Role.ACCENT);
        controlsCard.add(refresh);
        controlsCard.add(add);
        controlsCard.add(update);
        controlsCard.add(delete);
        add(controlsCard, BorderLayout.NORTH);

        UITheme.styleTextArea(output);
        output.setEditable(false);
        JPanel outputCard = UITheme.card(new BorderLayout());
        outputCard.add(new JScrollPane(output), BorderLayout.CENTER);
        add(outputCard, BorderLayout.CENTER);

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
        for (JTextField field : fields)
        {
            UITheme.styleTextField(field);
        }
        String[] labels = {"Name", "Contact person", "Phone", "Email", "Address"};
        JPanel form = new JPanel(new GridLayout(0, 2, 6, 6));
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
    private final JTextArea output = new JTextArea();

    public ManageUsersPanel()
    {
        setLayout(new BorderLayout(10, 10));
        setBackground(UITheme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        JPanel controlsCard = UITheme.card(new FlowLayout(FlowLayout.LEFT, 8, 0));
        RoundedButton refresh = new RoundedButton("Refresh cashiers", RoundedButton.Role.SECONDARY);
        RoundedButton add = new RoundedButton("Add cashier", RoundedButton.Role.PRIMARY);
        RoundedButton delete = new RoundedButton("Delete cashier", RoundedButton.Role.ACCENT);
        controlsCard.add(refresh);
        controlsCard.add(add);
        controlsCard.add(delete);
        add(controlsCard, BorderLayout.NORTH);

        UITheme.styleTextArea(output);
        output.setEditable(false);
        JPanel outputCard = UITheme.card(new BorderLayout());
        outputCard.add(new JScrollPane(output), BorderLayout.CENTER);
        add(outputCard, BorderLayout.CENTER);

        refresh.addActionListener(event -> loadUsers());
        add.addActionListener(event -> addUser());
        delete.addActionListener(event -> deleteUser());
        loadUsers();
    }

    private void addUser()
    {
        JPasswordField password = new JPasswordField();
        JTextField fullName = new JTextField();
        UITheme.styleTextField(password);
        UITheme.styleTextField(fullName);
        JPanel form = new JPanel(new GridLayout(0, 2, 6, 6));
        form.add(new JLabel("Full name"));
        form.add(fullName);
        form.add(new JLabel("Password"));
        form.add(password);
        if (JOptionPane.showConfirmDialog(this, form, "Add cashier",
                JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION)
        {
            return;
        }

        String name = fullName.getText().trim().replaceAll("\\s+", " ");
        String[] nameParts = name.split(" ", 2);
        String passwordValue = new String(password.getPassword());
        if (nameParts.length < 2 || nameParts[0].isEmpty() || nameParts[1].isEmpty())
        {
            JOptionPane.showMessageDialog(this, "Enter a first name and surname.");
            return;
        }
        if (passwordValue.length() < 8 || passwordValue.matches(".*\\s.*"))
        {
            JOptionPane.showMessageDialog(this,
                    "Password must contain at least 8 characters and no spaces.");
            return;
        }

        String username = nameParts[0].substring(0, 1).toUpperCase(Locale.ROOT)
                + nameParts[1].replaceAll("\\s+", "");
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordValue);
        user.setFullName(name);
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
    private final JTextPane output = new JTextPane();

    public ReportsPanel()
    {
        setLayout(new BorderLayout(10, 10));
        setBackground(UITheme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        JPanel controlsCard = UITheme.card(new FlowLayout(FlowLayout.LEFT, 8, 0));
        RoundedButton sales = new RoundedButton("Sales", RoundedButton.Role.SECONDARY);
        RoundedButton items = new RoundedButton("Item-wise", RoundedButton.Role.SECONDARY);
        RoundedButton lowStock = new RoundedButton("Low stock", RoundedButton.Role.ACCENT);
        RoundedButton expiry = new RoundedButton("Expiring", RoundedButton.Role.ACCENT);
        controlsCard.add(sales);
        controlsCard.add(items);
        controlsCard.add(lowStock);
        controlsCard.add(expiry);
        add(controlsCard, BorderLayout.NORTH);

        UITheme.styleTextPane(output);
        output.setEditable(false);
        JPanel outputCard = UITheme.card(new BorderLayout());
        outputCard.add(new JScrollPane(output), BorderLayout.CENTER);
        add(outputCard, BorderLayout.CENTER);

        sales.addActionListener(event -> salesReport());
        items.addActionListener(event -> itemReport());
        lowStock.addActionListener(event -> medicineReport(false));
        expiry.addActionListener(event -> medicineReport(true));
    }

    private void salesReport()
    {
        output.setText("");
        try
        {
            List<Sale> sales = new ReportService().generateSalesReport(
                    LocalDate.now().minusMonths(1), LocalDate.now());
            UITheme.appendLine(output, "SALES REPORT - last 30 days", UITheme.SECONDARY_DARK, true);
            UITheme.appendLine(output, "", UITheme.TEXT_DARK, false);
            for (Sale sale : sales)
            {
                UITheme.appendLine(output, sale.toString(), UITheme.TEXT_DARK, false);
                UITheme.appendLine(output, "", UITheme.TEXT_DARK, false);
            }
        }
        catch (SQLException exception)
        {
            UITheme.appendLine(output, "Unable to load sales: " + exception.getMessage(), UITheme.ACCENT_DARK, true);
        }
    }

    private void itemReport()
    {
        output.setText("");
        try
        {
            UITheme.appendLine(output, "ITEM-WISE SALES REPORT", UITheme.SECONDARY_DARK, true);
            UITheme.appendLine(output, "", UITheme.TEXT_DARK, false);
            for (ItemWiseSalesReport report : new ReportService().generateItemWiseReport())
            {
                UITheme.appendLine(output, report.getMedicineName() + " | Quantity: "
                        + report.getQuantitySold() + " | Revenue: "
                        + report.getTotalRevenue(), UITheme.TEXT_DARK, false);
            }
        }
        catch (SQLException exception)
        {
            UITheme.appendLine(output, "Unable to load item report: " + exception.getMessage(), UITheme.ACCENT_DARK, true);
        }
    }

    private void medicineReport(boolean expiring)
    {
        output.setText("");
        try
        {
            List<Medicine> medicines = expiring
                    ? new ReportService().generateExpiryReport()
                    : new ReportService().generateLowStockReport();
            UITheme.appendIconLine(output, "warning.png",
                    expiring ? "EXPIRING SOON" : "LOW STOCK", UITheme.ACCENT_DARK, true);
            UITheme.appendLine(output, "", UITheme.TEXT_DARK, false);
            for (Medicine medicine : medicines)
            {
                UITheme.appendLine(output, medicine.toString(), UITheme.ACCENT_DARK, false);
                UITheme.appendLine(output, "", UITheme.TEXT_DARK, false);
            }
        }
        catch (SQLException exception)
        {
            UITheme.appendLine(output, "Unable to load medicine report: " + exception.getMessage(), UITheme.ACCENT_DARK, true);
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
        setSize(900, 650);
        setLocationRelativeTo(null);
        getContentPane().setBackground(UITheme.BACKGROUND);
        setLayout(new BorderLayout());

        add(UITheme.headerBar(WindowIcon.loadIcon("cashier-dashboard.png"), "Cashier Dashboard",
                "Signed in as " + user.getFullName()), BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        UITheme.styleTabbedPane(tabs);
        tabs.addTab("Point of Sale", WindowIcon.loadIcon("point-of-sale.png"),
                new POSPanel(user.getUserId()));
        tabs.addTab("Stock Check", WindowIcon.loadIcon("medicine.png"), new StockCheckPanel());
        add(tabs, BorderLayout.CENTER);

        add(statusBar(user), BorderLayout.SOUTH);
    }

    private JPanel statusBar(User user)
    {
        JPanel status = new JPanel(new BorderLayout(12, 8));
        status.setBackground(UITheme.PANEL);
        status.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.BORDER));
        JPanel userInfo = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        userInfo.setOpaque(false);
        userInfo.add(UITheme.badge("CASHIER", UITheme.SECONDARY));
        JLabel name = new JLabel(user.getFullName());
        name.setFont(UITheme.BODY_FONT);
        name.setForeground(UITheme.TEXT_MUTED);
        userInfo.add(name);
        status.add(userInfo, BorderLayout.WEST);

        RoundedButton logout = new RoundedButton("Log out", RoundedButton.Role.GHOST);
        logout.addActionListener(event ->
        {
            dispose();
            new LoginFrame().setVisible(true);
        });
        status.add(logout, BorderLayout.EAST);
        return status;
    }
}

class POSPanel extends JPanel
{
    private final int userId;
    private final JTextField medicineIdField = new JTextField();
    private final JTextField quantityField = new JTextField();
    private final JTextField medicineSearchField = new JTextField();
    private final JTextArea cartOutput = new JTextArea();
    private final DefaultTableModel medicineTableModel = new DefaultTableModel(
            new Object[]{"ID", "Medicine", "Company", "Type", "Price", "Stock", "Expiry"}, 0)
    {
        @Override
        public boolean isCellEditable(int row, int column)
        {
            return false;
        }
    };
    private final JTable medicineTable = new JTable(medicineTableModel);
    private final List<SaleItem> cartItems = new ArrayList<>();

    public POSPanel(int user_id)
    {
        this.userId = user_id;
        setLayout(new BorderLayout(10, 10));
        setBackground(UITheme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        JPanel controlsCard = UITheme.card(new BorderLayout(8, 8));
        JPanel fields = new JPanel(new GridLayout(1, 4, 8, 8));
        fields.setOpaque(false);
        UITheme.styleTextField(medicineIdField);
        UITheme.styleTextField(quantityField);
        fields.add(new JLabel("Medicine ID"));
        fields.add(medicineIdField);
        fields.add(new JLabel("Quantity"));
        fields.add(quantityField);
        RoundedButton add = new RoundedButton("Add item", RoundedButton.Role.PRIMARY);
        controlsCard.add(fields, BorderLayout.CENTER);
        controlsCard.add(add, BorderLayout.EAST);
        add(controlsCard, BorderLayout.NORTH);

        JPanel detailsCard = UITheme.card(new BorderLayout(8, 8));
        JPanel detailsControls = new JPanel(new BorderLayout(8, 8));
        detailsControls.setOpaque(false);
        UITheme.styleTextField(medicineSearchField);
        detailsControls.add(new JLabel("Search medicines"), BorderLayout.WEST);
        detailsControls.add(medicineSearchField, BorderLayout.CENTER);
        RoundedButton search = new RoundedButton("Search", RoundedButton.Role.SECONDARY);
        RoundedButton refresh = new RoundedButton("Show all", RoundedButton.Role.GHOST);
        JPanel searchActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        searchActions.setOpaque(false);
        searchActions.add(search);
        searchActions.add(refresh);
        detailsControls.add(searchActions, BorderLayout.EAST);
        detailsCard.add(detailsControls, BorderLayout.NORTH);
        medicineTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        medicineTable.setRowHeight(28);
        medicineTable.setAutoCreateRowSorter(true);
        detailsCard.add(new JScrollPane(medicineTable), BorderLayout.CENTER);

        UITheme.styleTextArea(cartOutput);
        cartOutput.setEditable(false);
        JPanel cartCard = UITheme.card(new BorderLayout());
        cartCard.add(new JScrollPane(cartOutput), BorderLayout.CENTER);
        JSplitPane content = new JSplitPane(JSplitPane.VERTICAL_SPLIT, detailsCard, cartCard);
        content.setResizeWeight(0.62);
        content.setBorder(null);
        add(content, BorderLayout.CENTER);

        RoundedButton checkout = new RoundedButton("Checkout", RoundedButton.Role.PRIMARY);
        checkout.setPreferredSize(new Dimension(0, 46));
        RoundedButton clear = new RoundedButton("Clear cart", RoundedButton.Role.ACCENT);
        JPanel bottom = new JPanel(new BorderLayout(8, 8));
        bottom.setOpaque(false);
        bottom.add(clear, BorderLayout.WEST);
        bottom.add(checkout, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);

        add.addActionListener(event -> addItem());
        checkout.addActionListener(event -> checkout());
        clear.addActionListener(event -> clearCart());
        search.addActionListener(event -> loadMedicineDetails(medicineSearchField.getText().trim()));
        medicineSearchField.addActionListener(event ->
                loadMedicineDetails(medicineSearchField.getText().trim()));
        refresh.addActionListener(event -> loadMedicineDetails(""));
        medicineTable.getSelectionModel().addListSelectionListener(event ->
        {
            if (!event.getValueIsAdjusting())
            {
                selectMedicine();
            }
        });
        loadMedicineDetails("");
    }

    private void loadMedicineDetails(String searchTerm)
    {
        MedicineDAO dao = null;
        try
        {
            dao = new MedicineDAO();
            List<Medicine> medicines = searchTerm.isEmpty()
                    ? dao.getAllMedicines() : dao.searchMedicine(searchTerm);
            medicineTableModel.setRowCount(0);
            for (Medicine medicine : medicines)
            {
                medicineTableModel.addRow(new Object[]{
                        medicine.getMedicineId(),
                        medicine.getName(),
                        medicine.getCompany(),
                        medicine.getMedicineType(),
                        String.format("%.2f", medicine.getPrice()),
                        medicine.getQuantityInStock(),
                        medicine.getExpiryDate()
                });
            }
        }
        catch (SQLException exception)
        {
            JOptionPane.showMessageDialog(this,
                    "Unable to load medicine details: " + exception.getMessage());
        }
        finally
        {
            if (dao != null)
            {
                dao.closeConnection();
            }
        }
    }

    private void selectMedicine()
    {
        int selectedRow = medicineTable.getSelectedRow();
        if (selectedRow >= 0)
        {
            int modelRow = medicineTable.convertRowIndexToModel(selectedRow);
            medicineIdField.setText(String.valueOf(medicineTableModel.getValueAt(modelRow, 0)));
            if (quantityField.getText().trim().isEmpty())
            {
                quantityField.setText("1");
            }
        }
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
        setSize(420, 340);
        setLocationRelativeTo(null);
        getContentPane().setBackground(UITheme.BACKGROUND);
        setLayout(new BorderLayout(10, 10));

        add(UITheme.headerBar(WindowIcon.loadIcon("receipt.png"), "Receipt",
                "Sale #" + sale.getSaleId()), BorderLayout.NORTH);

        StringBuilder billText = new StringBuilder(sale.toString()).append("\n\nItems:\n");
        for (SaleItem item : items)
        {
            billText.append("Medicine ").append(item.getMedicineId())
                    .append(" x ").append(item.getQuantitySold())
                    .append(" @ ").append(item.getPriceAtSale()).append("\n");
        }
        JTextArea bill = new JTextArea(billText.toString());
        UITheme.styleTextArea(bill);
        bill.setEditable(false);
        JPanel billCard = UITheme.card(new BorderLayout());
        billCard.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        billCard.add(new JScrollPane(bill), BorderLayout.CENTER);
        add(billCard, BorderLayout.CENTER);

        RoundedButton save = new RoundedButton("Save", RoundedButton.Role.SECONDARY);
        RoundedButton print = new RoundedButton("Print", RoundedButton.Role.PRIMARY);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        actions.setOpaque(false);
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
        add(actions, BorderLayout.SOUTH);
    }
}

class StockCheckPanel extends JPanel
{
    private final JTextPane output = new JTextPane();
    private final JTextField medicineId = new JTextField();

    public StockCheckPanel()
    {
        setLayout(new BorderLayout(10, 10));
        setBackground(UITheme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        JPanel controlsCard = UITheme.card(new BorderLayout(8, 8));
        RoundedButton refresh = new RoundedButton("Refresh warnings", RoundedButton.Role.SECONDARY);
        RoundedButton check = new RoundedButton("Check medicine", RoundedButton.Role.PRIMARY);
        UITheme.styleTextField(medicineId);
        JPanel row = new JPanel(new BorderLayout(8, 8));
        row.setOpaque(false);
        row.add(new JLabel("Medicine ID"), BorderLayout.WEST);
        row.add(medicineId, BorderLayout.CENTER);
        row.add(check, BorderLayout.EAST);
        row.add(refresh, BorderLayout.WEST);
        controlsCard.add(row, BorderLayout.CENTER);
        add(controlsCard, BorderLayout.NORTH);

        UITheme.styleTextPane(output);
        output.setEditable(false);
        JPanel outputCard = UITheme.card(new BorderLayout());
        outputCard.add(new JScrollPane(output), BorderLayout.CENTER);
        add(outputCard, BorderLayout.CENTER);

        refresh.addActionListener(event -> loadWarnings());
        check.addActionListener(event -> checkMedicine());
        medicineId.addActionListener(event -> checkMedicine());
        loadWarnings();
    }

    private void checkMedicine()
    {
        MedicineDAO dao = null;
        output.setText("");
        try
        {
            String idText = medicineId.getText().trim();
            if (idText.isEmpty())
            {
                throw new IllegalArgumentException("Enter a medicine ID.");
            }
            int id = Integer.parseInt(idText);
            dao = new MedicineDAO();
            Medicine medicine = dao.getMedicineById(id);
            if (medicine == null)
            {
                UITheme.appendLine(output, "Medicine not found.", UITheme.ACCENT_DARK, true);
            }
            else
            {
                boolean lowStock = medicine.getQuantityInStock() <= medicine.getReorderLevel();
                UITheme.appendLine(output, "Medicine: " + medicine.getName(),
                        UITheme.TEXT_DARK, true);
                UITheme.appendLine(output, "Price: " + String.format("%.2f", medicine.getPrice()),
                        UITheme.TEXT_DARK, false);
                UITheme.appendLine(output, "Available stock: " + medicine.getQuantityInStock(),
                        lowStock ? UITheme.ACCENT_DARK : UITheme.TEXT_DARK, lowStock);
                UITheme.appendLine(output, "Reorder level: " + medicine.getReorderLevel(),
                        UITheme.TEXT_DARK, false);
                UITheme.appendLine(output, "Expiry date: " + medicine.getExpiryDate(),
                        medicine.isExpiringSoon() ? UITheme.ACCENT_DARK : UITheme.TEXT_DARK,
                        medicine.isExpiringSoon());
                UITheme.appendLine(output, "Supplier ID: " + medicine.getSupplierId(),
                        UITheme.TEXT_DARK, false);
            }
        }
        catch (Exception exception)
        {
            UITheme.appendLine(output, "Unable to check medicine: " + exception.getMessage(), UITheme.ACCENT_DARK, true);
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
        output.setText("");
        try
        {
            ReportService reports = new ReportService();
            UITheme.appendIconLine(output, "warning.png", "LOW STOCK", UITheme.ACCENT_DARK, true);
            List<Medicine> lowStock = reports.generateLowStockReport();
            if (lowStock.isEmpty())
            {
                UITheme.appendLine(output, "No low-stock medicines.", UITheme.TEXT_DARK, false);
            }
            for (Medicine medicine : lowStock)
            {
                UITheme.appendLine(output, medicine.getName() + " (" + medicine.getQuantityInStock()
                        + " remaining)", UITheme.ACCENT_DARK, false);
            }
            UITheme.appendLine(output, "", UITheme.TEXT_DARK, false);
            UITheme.appendIconLine(output, "warning.png",
                    "EXPIRING SOON", UITheme.ACCENT_DARK, true);
            List<Medicine> expiring = reports.generateExpiryReport();
            if (expiring.isEmpty())
            {
                UITheme.appendLine(output, "No medicines expiring within one month.",
                        UITheme.TEXT_DARK, false);
            }
            for (Medicine medicine : expiring)
            {
                UITheme.appendLine(output, medicine.getName() + " (" + medicine.getExpiryDate() + ")",
                        UITheme.ACCENT_DARK, false);
            }
        }
        catch (SQLException exception)
        {
            UITheme.appendLine(output, "Unable to load stock warnings: " + exception.getMessage(), UITheme.ACCENT_DARK, true);
        }
    }
}