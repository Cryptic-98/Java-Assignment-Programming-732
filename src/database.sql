CREATE DATABASE IF NOT EXISTS healthfirst;


USE healthfirst;


CREATE TABLE users (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role ENUM('Admin', 'Cashier') NOT NULL,
    full_name VARCHAR(100) NOT NULL
);

CREATE TABLE suppliers (
    supplier_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    contact_person VARCHAR(100) NOT NULL,
    phone_number VARCHAR(20) NOT NULL,
    email VARCHAR(100) NOT NULL,
    address TEXT
);

CREATE TABLE medicines (
    medicine_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(150),
    company VARCHAR(100),
    medicine_type VARCHAR(50),
    price DECIMAL(10,2),
    quantity_in_stock INT,
    reorder_level INT,
    expiry_date DATE,
    supplier_id INT,
    FOREIGN KEY (supplier_id) REFERENCES suppliers(supplier_id)
);

CREATE TABLE sales (
    sale_id INT PRIMARY KEY AUTO_INCREMENT,
    sale_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    total_amount DECIMAL(10,2),
    user_id INT,
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);

CREATE TABLE sale_items (
    sale_item_id INT PRIMARY KEY AUTO_INCREMENT,
    sale_id INT,
    FOREIGN KEY (sale_id) REFERENCES sales(sale_id),
    medicine_id INT,
    FOREIGN KEY (medicine_id) REFERENCES medicines(medicine_id),
    quantity_sold INT,
    price_at_sale DECIMAL(10,2)
);