HEALTHFIRST PHARMACY MANAGEMENT SYSTEM
======================================

USER GUIDE

1. SYSTEM REQUIREMENTS
- JDK 8 or higher.
- MySQL Server.
- MySQL Connector/J JDBC driver.
- Configured HealthFirst database.


2. DATABASE CONFIGURATION
 - Initialize MySQL Server.
 - Execute the SQL script from:

   src\database.sql

 - Set up the database configuration if needed:
   HEALTHFIRST_DB_USER
   HEALTHFIRST_DB_PASSWORD

   The default user is root. Connection details as follows:

   jdbc:mysql://localhost:3306/healthfirst

 - Ensure the MySQL JDBC connector is available on the project classpath.


3. STARTING THE APPLICATION
Run the Main class:
   src\Main.java
The application opens the HealthFirst login window.


4. DEFAULT LOGIN CREDENTIALS
The following accounts are available in the database:

User ID | Username | Password      | Role    | Full name
--------+----------+---------------+---------+-------------------
1       | MJackson | password123   | Admin   | Madison Jackson
2       | GGilmore | drowssap321   | Cashier | Gregory Gilmore
3       | JDoe     | yemenFools98  | Cashier | John Doe

Passwords are case-sensitive.


5. ADMINISTRATOR FUNCTIONS
Log in to the Admin Dashboard using the Admin login.

The administrator will be able to:
- Add, view, search, edit, and delete medicines.
- Add, view, edit, and delete suppliers.
- Add and delete cashier users.
- Access sales report.
- Access item-wise sales report.
- Check low stock medicines.
- Check medicines that expire in one month.
- Logout using the Logout button.

While creating a new cashier account, provide the following information:
- Full name, for instance, John Doe.
- Password.

The username will be automatically generated as per the first letter of first name along with full surname:
Example: John Doe -> JDoe

6. CASHIER FUNCTIONS
Use a Cashier account to log in and access the Cashier Dashboard.

A cashier is able to:
- See information about medicines, including the price and stock available.
- Search for medicines based on their names in the Point of Sale interface.
- Choose a medicine from the details table.
- Put a certain quantity into the cart.
- Clear the cart.
- Make a checkout.
- Create a bill for the customer.
- Save the bill as a file.
- Print the bill.
- See the stock of medicines according to the medicine ID.
- See low stock or expiry alerts.
- Log out by clicking the "Log out" button.

Cashiers are not able to add, update, or delete medicines, suppliers, or users.


7. PROCESSING A SALE

1. Navigate to the Point of Sale tab.
2. Search for a medicine or choose Show all.
3. Click on a medicine row.
4. Enter the quantity.
5. Click on Add item.
6. If needed, repeat step 3 and 4 for other medicines.
7. Click on Check out.
8. See the created bill.
9. Print or save the bill.

The system checks the stock before adding the item. In the check out phase,
the stock is reduced, and the sale along with sale items are stored in one
database transaction.


8. STOCK CHECK

Under the Stock Check tab:
- Type in a medicine code and then select Check medicine, or hit Enter.
- The system shows the name of the medicine, its price, stock,
  reorder level, expiry date, and supplier code.
- Click Refresh warnings to check low stock and expiring medicines.


9. LOGGING OUT
Click Log out in the dashboard status bar. The current dashboard closes and
the login window opens again so another user can sign in.


10. TROUBLESHOOTING

Error while logging in or connecting to the database:
- Ensure that the MySQL server is up and running.
- Ensure that the database named "healthfirst" exists.
- Ensure that the login id and password are entered correctly.
- Ensure that the MySQL JDBC driver is present in the classpath.
- If MySQL server is using any port other than 3306, ensure that the
  connection is established using the correct port number.

Medicines and users are not available:
- Ensure that the database script was run successfully.
- Ensure that the data exists in the "healthfirst.users" and
  "healthfirst.medicines" tables.
