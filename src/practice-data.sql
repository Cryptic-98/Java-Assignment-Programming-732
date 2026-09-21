USE HealthFirst;

INSERT INTO users (username, password, role, full_name)
VALUES
    ("CToulise", "TrashB0at54", "Admin", "Christopher Toulise"),
    ("RManamela", "LuckyNo07", "Cashier", "Rudy Manamela"),
    -- not executed ↓
    ("CRaslouw", "ManHunt3r", "Cashier", "Cobus Raslouw"),
    ("TBaloi", "MonaL1sa765", "Admin", "Thapelo Baloi");

INSERT INTO suppliers (name, contact_person, phone_number, email, address)
VALUES
    ('TL Bio Pharmaceuticals', 'Thabo Molefe', '0113456789', 'info@tlbio.co.za', 'Johannesburg, Gauteng'),
    ('PrimeHealth Supplies', 'Sarah Naidoo', '0125678901', 'sales@primehealth.co.za', 'Pretoria, Gauteng'),
    ('O.K. Pharma (Pty) Ltd', 'Kevin Dlamini', '0106789012', 'orders@okpharma.co.za', 'Centurion, Gauteng');

INSERT INTO medicines (name, company, medicine_type, price, quantity_in_stock, reorder_level, expiry_date, supplier_id)
VALUES
    ('Panado 500mg', 'Adcock Ingram', 'Tablet', 32.99, 36, 10, '2028-05-31', 1),
    ('Voltaren 50mg', 'Novartis', 'Tablet', 67.50, 21, 10, '2027-11-30', 1),
    ('Augmentin 625mg', 'GlaxoSmithKline', 'Tablet', 115.99, 48, 15, '2028-02-28', 2),
    ('Allergex 4mg', 'Aspen Pharmacare', 'Tablet', 39.95, 17, 10, '2027-09-30', 2),
    ('Disprin 300mg', 'Reckitt', 'Tablet', 28.50, 44, 10, '2028-01-31', 3),
    ('Losec 20mg', 'AstraZeneca', 'Capsule', 71.99, 29, 10, '2027-12-31', 3),
    ('Clarityne 10mg', 'Bayer', 'Tablet', 59.99, 53, 10, '2028-06-30', 1),
    ('Glucophage 500mg', 'Merck', 'Tablet', 68.75, 13, 10, '2027-10-31', 2),
    ('Calpol 100ml', 'Haleon', 'Syrup', 45.99, 31, 10, '2027-08-31', 3),
    ('Benylin 100ml', 'Johnson & Johnson', 'Syrup', 82.50, 52, 15, '2028-03-31', 1);