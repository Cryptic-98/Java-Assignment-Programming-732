import java.time.LocalDate;
import java.util.Scanner;

class User
{
    int userId = 1000;
    String username;
    String password;
    String role;
    String fullName;

    public User() {}

    public void setUserId()
    {
        this.userId++;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public void setRole(String role)
    {
        this.role = role;
    }

    public void setFullName(String full_name)
    {
        this.fullName = full_name;
    }

    public int getUserId()
    {
        return this.userId;
    }

    public String getUsername()
    {
        return this.username;
    }

    public String getPassword()
    {
        return this.password;
    }

    public String getRole()
    {
        return this.role;
    }

    public String getFullName()
    {
        return this.fullName;
    }

    @Override
    public String toString()
    {
        return "User ID: " + this.userId + "\nUsername: " + this.username + "\nRole: " + this.role + "\nFull name: " + this.fullName;
    }
}

class Supplier
{
    int supplierId;
    String name;
    String contactPerson;
    int phoneNumber;
    String email;
    String address;

    public Supplier() {}

    public void setSupplierId(int supplier_id)
    {
        this.supplierId = supplier_id;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public void setContactPerson(String contact_person)
    {
        this.contactPerson = contact_person;
    }

    public void setPhoneNumber(int phone_number)
    {
        this.phoneNumber = phone_number;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    public void setAddress(String address)
    {
        this.address = address;
    }

    public int getSupplierId()
    {
        return this.supplierId;
    }

    public String getContactPerson()
    {
        return this.contactPerson;
    }

    public int getPhoneNumber()
    {
        return this.phoneNumber;
    }

    public String getEmail()
    {
        return this.email;
    }

    public String getAddress()
    {
        return this.address;
    }

    @Override
    public String toString()
    {
        return "Supplier ID: " + this.supplierId + "\nContact person: " + this.contactPerson + "\nPhone number: " + this.phoneNumber + "\nEmail: " + this.email;
    }
}

class Medicine
{
    int medicineId;
    String name;
    String company;
    String medicineType;
    double price;
    int quantityInStock;
    int reorderLevel = 10;
    LocalDate expiryDate;
    int supplierId;

    public Medicine() {}

    public void setMedicineId(int medicine_id)
    {
        this.medicineId = medicine_id;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public void setCompany(String company)
    {
        this.company = company;
    }

    public void setMedicineType(String medicine_type)
    {
        this.medicineType = medicine_type;
    }

    public void setPrice(double price)
    {
        this.price = price;
    }

    public void setQuantityInStock(int quantity_in_stock)
    {
        this.quantityInStock = quantity_in_stock;
    }

    public void setReorderLevel(int reorder_level)
    {
        this.reorderLevel = reorder_level;
    }

    public void setExpiryDate(LocalDate expiry_date)
    {
        this.expiryDate = expiry_date;
    }

    public void setSupplierId(int supplier_id)
    {
        this.supplierId = supplier_id;
    }

    public int getMedicineId()
    {
        return this.medicineId;
    }

    public String getName()
    {
        return this.name;
    }

    public String getCompany()
    {
        return this.company;
    }

    public String getMedicineType()
    {
        return this.medicineType;
    }

    public double getPrice()
    {
        return this.price;
    }

    public int getQuantityInStock()
    {
        return this.quantityInStock;
    }

    public int getReorderLevel()
    {
        return this.reorderLevel;
    }

    public LocalDate getExpiryDate()
    {
        return this.expiryDate;
    }

    public int getSupplierId()
    {
        return this.supplierId;
    }

    public boolean isLowStock()
    {
        return this.quantityInStock <= this.reorderLevel;
    }

    public boolean isExpiringSoon()
    {
        if (this.expiryDate == null)
        {
            return false;
        }

        LocalDate today = LocalDate.now();
        LocalDate warningDate = today.plusDays(14);
        return !this.expiryDate.isBefore(today) && !this.expiryDate.isAfter(warningDate);
    }

    @Override
    public String toString()
    {
        return "Medicine ID: " + this.medicineId + "\nName: " + this.name + "\nCompany: " + this.company + "\nMedicine type: " + this.medicineType + "\nPrice: " + this.price + "\nQuantity in stock: " + this.quantityInStock + "\nExpiry date: " + this.expiryDate + "\nSupplier ID: " + this.supplierId;
    }
}

class Sale
{
    int saleId;
    LocalDate saleDate;
    double totalAmount;
    int userId;

    public Sale() {}

    public void setSaleId(int sale_id)
    {
        this.saleId = sale_id;
    }

    public void setSaleDate(LocalDate sale_date)
    {
        this.saleDate = sale_date;
    }

    public void setTotalAmount(double total_amount)
    {
        this.totalAmount = total_amount;
    }

    public void setUserId(int user_id)
    {
        this.userId = user_id;
    }

    public int getSaleId()
    {
        return this.saleId;
    }

    public LocalDate getSaleDate()
    {
        return this.saleDate;
    }

    public double getTotalAmount()
    {
        return this.totalAmount;
    }

    public int getUserId()
    {
        return this.userId;
    }

    @Override
    public String toString()
    {
        return "Sale ID: " + this.saleId + "\nSale date: " + this.saleDate + "\nTotal amount: " + this.totalAmount + "\nUser ID: " + this.userId;
    }
}

class SaleItem
{
    int saleItemId;
    int saleId;
    int medicineId;
    int quantitySold;
    double priceAtSale;

    public SaleItem() {}

    public void setSaleItemId(int sale_item_id)
    {
        this.saleItemId = sale_item_id;
    }

    public void setSaleId(int sale_id)
    {
        this.saleId = sale_id;
    }

    public void setMedicineId(int medicine_id)
    {
        this.medicineId = medicine_id;
    }

    public void setQuantitySold(int quantity_sold)
    {
        this.quantitySold = quantity_sold;
    }

    public void setPriceAtSale(double price_at_sale)
    {
        this.priceAtSale = price_at_sale;
    }

    public int getSaleItemId()
    {
        return saleItemId;
    }

    public int getSaleId()
    {
        return this.saleId;
    }

    public int getMedicineId()
    {
        return this.medicineId;
    }

    public int getQuantitySold()
    {
        return this.quantitySold;
    }

    public double getPriceAtSale()
    {
        return this.priceAtSale;
    }

    @Override
    public String toString()
    {
        return "Sale item ID: " + this.saleItemId + "\nSale ID: " + this.saleId + "\nMedicine ID: " + this.medicineId + "\nQuantity sold: " + this.quantitySold + "\nPrice at sale: " + this.priceAtSale;
    }
}

class Main
{

    public static void main(String[] args)
    {
        try (Scanner input = new Scanner(System.in))
        {
            System.out.print("Enter first name: ");
            String first_name = input.nextLine().trim();

            System.out.print("Enter last name: ");
            String last_name = input.nextLine().trim();

            String username = first_name.substring(0, 1).toUpperCase() + last_name;
            String full_name = first_name + " " + last_name;

            System.out.print("Enter password: ");
            String password = input.nextLine();

            System.out.print("(A)dmin/(C)ashier: ");
            char role_option;
            do
            {
                String role_input = input.nextLine().trim().toUpperCase();
                role_option = role_input.isEmpty() ? '\0' : role_input.charAt(0);
                if (role_option != 'A' && role_option != 'C')
                {
                    System.out.print("Please enter A for Admin or C for Cashier: ");
                }
            }
            while (role_option != 'A' && role_option != 'C');

            String role = role_option == 'A' ? "Admin" : "Cashier";

            User user = new User();
            user.setUserId();
            user.setUsername(username);
            user.setPassword(password);
            user.setRole(role);
            user.setFullName(full_name);

            System.out.println(user);
        }

    }
}