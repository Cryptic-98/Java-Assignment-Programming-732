CREATE DATABASE IF NOT EXISTS healthfirst
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;


USE healthfirst;


CREATE TABLE IF NOT EXISTS users (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role ENUM('Admin', 'Cashier') NOT NULL,
    full_name VARCHAR(100) NOT NULL
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS suppliers (
    supplier_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    contact_person VARCHAR(100) NOT NULL,
    phone_number VARCHAR(20) NOT NULL,
    email VARCHAR(100) NOT NULL,
    address TEXT
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS medicines (
    medicine_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(150) NOT NULL,
    company VARCHAR(100) NOT NULL,
    medicine_type VARCHAR(50) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    quantity_in_stock INT NOT NULL DEFAULT 0,
    reorder_level INT NOT NULL DEFAULT 10,
    expiry_date DATE NOT NULL,
    supplier_id INT NOT NULL,
    CONSTRAINT chk_medicine_price CHECK (price >= 0),
    CONSTRAINT chk_medicine_stock CHECK (quantity_in_stock >= 0),
    CONSTRAINT chk_medicine_reorder_level CHECK (reorder_level >= 0),
    CONSTRAINT fk_medicine_supplier
        FOREIGN KEY (supplier_id) REFERENCES suppliers(supplier_id)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS sales (
    sale_id INT PRIMARY KEY AUTO_INCREMENT,
    sale_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    total_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    user_id INT NOT NULL,
    CONSTRAINT chk_sale_total CHECK (total_amount >= 0),
    CONSTRAINT fk_sale_user
        FOREIGN KEY (user_id) REFERENCES users(user_id)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS sale_items (
    sale_item_id INT PRIMARY KEY AUTO_INCREMENT,
    sale_id INT NOT NULL,
    medicine_id INT NOT NULL,
    quantity_sold INT NOT NULL,
    price_at_sale DECIMAL(10,2) NOT NULL,
    CONSTRAINT chk_sale_item_quantity CHECK (quantity_sold > 0),
    CONSTRAINT chk_sale_item_price CHECK (price_at_sale >= 0),
    CONSTRAINT fk_sale_item_sale
        FOREIGN KEY (sale_id) REFERENCES sales(sale_id),
    CONSTRAINT fk_sale_item_medicine
        FOREIGN KEY (medicine_id) REFERENCES medicines(medicine_id)
) ENGINE = InnoDB;