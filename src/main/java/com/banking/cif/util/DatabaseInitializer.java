package com.banking.cif.util;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;

public class DatabaseInitializer {

    public static void initialize() {
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            
            // simple check if tables exist
            try {
                stmt.executeQuery("SELECT 1 FROM customers LIMIT 1");
                return; // Already initialized
            } catch (SQLException e) {
                // Ignore, table doesn't exist, proceed to init
            }

            System.out.println("Initializing Database...");

            String[] sqlStatements = {
                "CREATE TABLE products (" +
                "    product_code VARCHAR(50) PRIMARY KEY," +
                "    name VARCHAR(100) NOT NULL," +
                "    category VARCHAR(50) NOT NULL," +
                "    description VARCHAR(1000)," +
                "    interest_rate NUMERIC(5, 4)," +
                "    currency_code CHAR(3) DEFAULT 'USD'," +
                "    is_active BOOLEAN DEFAULT TRUE" +
                ")",

                "CREATE TABLE customers (" +
                "    customer_id VARCHAR(36) PRIMARY KEY," +
                "    cif_number VARCHAR(20) UNIQUE NOT NULL," +
                "    first_name VARCHAR(100) NOT NULL," +
                "    last_name VARCHAR(100) NOT NULL," +
                "    date_of_birth DATE NOT NULL," +
                "    email VARCHAR(150) UNIQUE NOT NULL," +
                "    phone_number VARCHAR(20)," +
                "    address_line1 VARCHAR(255)," +
                "    city VARCHAR(100)," +
                "    state VARCHAR(100)," +
                "    postal_code VARCHAR(20)," +
                "    country_code CHAR(2)," +
                "    kyc_status VARCHAR(20) DEFAULT 'PENDING'," +
                "    kyc_documents VARCHAR(4000) DEFAULT '{}'," +
                "    risk_rating VARCHAR(10) DEFAULT 'LOW'," +
                "    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")",

                "CREATE TABLE accounts (" +
                "    account_id VARCHAR(36) PRIMARY KEY," +
                "    customer_id VARCHAR(36) NOT NULL," +
                "    product_code VARCHAR(50) NOT NULL," +
                "    account_number VARCHAR(30) UNIQUE NOT NULL," +
                "    iban VARCHAR(34)," +
                "    balance NUMERIC(15, 2) DEFAULT 0.00," +
                "    currency_code CHAR(3) NOT NULL DEFAULT 'USD'," +
                "    overdraft_limit NUMERIC(15, 2) DEFAULT 0.00," +
                "    status VARCHAR(20) DEFAULT 'ACTIVE'," +
                "    opened_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "    closed_at TIMESTAMP," +
                "    configurations VARCHAR(4000) DEFAULT '{}'," +
                "    FOREIGN KEY (customer_id) REFERENCES customers(customer_id)," +
                "    FOREIGN KEY (product_code) REFERENCES products(product_code)" +
                ")",

                "CREATE TABLE transactions (" +
                "    transaction_id VARCHAR(36) PRIMARY KEY," +
                "    account_id VARCHAR(36) NOT NULL," +
                "    reference_code VARCHAR(50)," +
                "    transaction_type VARCHAR(20) NOT NULL," +
                "    amount NUMERIC(15, 2) NOT NULL," +
                "    currency_code CHAR(3) NOT NULL DEFAULT 'USD'," +
                "    description VARCHAR(1000)," +
                "    transaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "    balance_after NUMERIC(15, 2)," +
                "    FOREIGN KEY (account_id) REFERENCES accounts(account_id)" +
                ")",
                
                "INSERT INTO products (product_code, name, category, interest_rate) VALUES " +
                "('CHK-STD', 'Standard Checking', 'CHECKING', 0.00)," +
                "('SAV-HYS', 'High Yield Savings', 'SAVINGS', 0.045)",

                "INSERT INTO customers (customer_id, cif_number, first_name, last_name, date_of_birth, email, phone_number, city, country_code, kyc_status, risk_rating) VALUES " +
                "('c101', 'CIF-100101', 'James', 'Smith', '1980-01-01', 'james.smith@example.com', '555-0101', 'New York', 'US', 'COMPLETED', 'LOW')," +
                "('c102', 'CIF-100102', 'Mary', 'Johnson', '1982-02-02', 'mary.johnson@example.com', '555-0102', 'Los Angeles', 'US', 'COMPLETED', 'LOW')," +
                "('c103', 'CIF-100103', 'Robert', 'Williams', '1984-03-03', 'robert.williams@example.com', '555-0103', 'Chicago', 'US', 'COMPLETED', 'LOW')," +
                "('c104', 'CIF-100104', 'Patricia', 'Brown', '1986-04-04', 'patricia.brown@example.com', '555-0104', 'Houston', 'US', 'COMPLETED', 'LOW')," +
                "('c105', 'CIF-100105', 'John', 'Jones', '1988-05-05', 'john.jones@example.com', '555-0105', 'Phoenix', 'US', 'COMPLETED', 'LOW')," +
                "('c106', 'CIF-100106', 'Jennifer', 'Garcia', '1990-06-06', 'jennifer.garcia@example.com', '555-0106', 'Philadelphia', 'US', 'COMPLETED', 'LOW')," +
                "('c107', 'CIF-100107', 'Michael', 'Miller', '1992-07-07', 'michael.miller@example.com', '555-0107', 'San Antonio', 'US', 'COMPLETED', 'LOW')," +
                "('c108', 'CIF-100108', 'Linda', 'Davis', '1994-08-08', 'linda.davis@example.com', '555-0108', 'San Diego', 'US', 'COMPLETED', 'LOW')," +
                "('c109', 'CIF-100109', 'William', 'Rodriguez', '1996-09-09', 'william.rodriguez@example.com', '555-0109', 'Dallas', 'US', 'COMPLETED', 'LOW')," +
                "('c110', 'CIF-100110', 'Elizabeth', 'Martinez', '1998-10-10', 'elizabeth.martinez@example.com', '555-0110', 'San Jose', 'US', 'PENDING', 'MEDIUM')," +
                "('c111', 'CIF-100111', 'David', 'Hernandez', '1981-11-11', 'david.hernandez@example.com', '555-0111', 'Austin', 'US', 'COMPLETED', 'LOW')," +
                "('c112', 'CIF-100112', 'Barbara', 'Lopez', '1983-12-12', 'barbara.lopez@example.com', '555-0112', 'Jacksonville', 'US', 'COMPLETED', 'LOW')," +
                "('c113', 'CIF-100113', 'Richard', 'Gonzalez', '1985-01-13', 'richard.gonzalez@example.com', '555-0113', 'Fort Worth', 'US', 'COMPLETED', 'LOW')," +
                "('c114', 'CIF-100114', 'Susan', 'Wilson', '1987-02-14', 'susan.wilson@example.com', '555-0114', 'Columbus', 'US', 'COMPLETED', 'LOW')," +
                "('c115', 'CIF-100115', 'Joseph', 'Anderson', '1989-03-15', 'joseph.anderson@example.com', '555-0115', 'San Francisco', 'US', 'COMPLETED', 'LOW')," +
                "('c116', 'CIF-100116', 'Jessica', 'Thomas', '1991-04-16', 'jessica.thomas@example.com', '555-0116', 'Charlotte', 'US', 'COMPLETED', 'LOW')," +
                "('c117', 'CIF-100117', 'Thomas', 'Taylor', '1993-05-17', 'thomas.taylor@example.com', '555-0117', 'Indianapolis', 'US', 'COMPLETED', 'LOW')," +
                "('c118', 'CIF-100118', 'Sarah', 'Moore', '1995-06-18', 'sarah.moore@example.com', '555-0118', 'Seattle', 'US', 'COMPLETED', 'LOW')," +
                "('c119', 'CIF-100119', 'Charles', 'Jackson', '1997-07-19', 'charles.jackson@example.com', '555-0119', 'Denver', 'US', 'COMPLETED', 'LOW')," +
                "('c120', 'CIF-100120', 'Karen', 'Martin', '1999-08-20', 'karen.martin@example.com', '555-0120', 'Washington', 'US', 'PENDING', 'MEDIUM')," +
                "('c121', 'CIF-100121', 'Christopher', 'Lee', '1980-09-21', 'christopher.lee@example.com', '555-0121', 'Boston', 'US', 'COMPLETED', 'LOW')," +
                "('c122', 'CIF-100122', 'Nancy', 'Perez', '1982-10-22', 'nancy.perez@example.com', '555-0122', 'El Paso', 'US', 'COMPLETED', 'LOW')," +
                "('c123', 'CIF-100123', 'Daniel', 'Thompson', '1984-11-23', 'daniel.thompson@example.com', '555-0123', 'Nashville', 'US', 'COMPLETED', 'LOW')," +
                "('c124', 'CIF-100124', 'Lisa', 'White', '1986-12-24', 'lisa.white@example.com', '555-0124', 'Detroit', 'US', 'COMPLETED', 'LOW')," +
                "('c125', 'CIF-100125', 'Matthew', 'Harris', '1988-01-25', 'matthew.harris@example.com', '555-0125', 'Oklahoma City', 'US', 'COMPLETED', 'LOW')," +
                "('c126', 'CIF-100126', 'Betty', 'Sanchez', '1990-02-26', 'betty.sanchez@example.com', '555-0126', 'Portland', 'US', 'COMPLETED', 'LOW')," +
                "('c127', 'CIF-100127', 'Anthony', 'Clark', '1992-03-27', 'anthony.clark@example.com', '555-0127', 'Las Vegas', 'US', 'COMPLETED', 'LOW')," +
                "('c128', 'CIF-100128', 'Margaret', 'Ramirez', '1994-04-28', 'margaret.ramirez@example.com', '555-0128', 'Memphis', 'US', 'COMPLETED', 'LOW')," +
                "('c129', 'CIF-100129', 'Mark', 'Lewis', '1996-05-29', 'mark.lewis@example.com', '555-0129', 'Louisville', 'US', 'COMPLETED', 'LOW')," +
                "('c130', 'CIF-100130', 'Sandra', 'Robinson', '1998-06-30', 'sandra.robinson@example.com', '555-0130', 'Baltimore', 'US', 'PENDING', 'HIGH')",

                "INSERT INTO accounts (account_id, customer_id, product_code, account_number, balance, currency_code, status) VALUES " +
                "('a201', 'c101', 'CHK-STD', 'ACC-100201', 5000.00, 'USD', 'ACTIVE')," +
                "('a202', 'c102', 'SAV-HYS', 'ACC-100202', 12000.00, 'USD', 'ACTIVE')," +
                "('a203', 'c103', 'CHK-STD', 'ACC-100203', 1500.50, 'USD', 'ACTIVE')," +
                "('a204', 'c104', 'SAV-HYS', 'ACC-100204', 25000.00, 'USD', 'ACTIVE')," +
                "('a205', 'c105', 'CHK-STD', 'ACC-100205', 300.75, 'USD', 'ACTIVE')," +
                "('a206', 'c106', 'SAV-HYS', 'ACC-100206', 4500.00, 'USD', 'ACTIVE')," +
                "('a207', 'c107', 'CHK-STD', 'ACC-100207', 8900.00, 'USD', 'ACTIVE')," +
                "('a208', 'c108', 'SAV-HYS', 'ACC-100208', 150.00, 'USD', 'ACTIVE')," +
                "('a209', 'c109', 'CHK-STD', 'ACC-100209', 2200.00, 'USD', 'ACTIVE')," +
                "('a210', 'c110', 'SAV-HYS', 'ACC-100210', 0.00, 'USD', 'PENDING')," +
                "('a211', 'c111', 'CHK-STD', 'ACC-100211', 7500.00, 'USD', 'ACTIVE')," +
                "('a212', 'c112', 'SAV-HYS', 'ACC-100212', 30000.00, 'USD', 'ACTIVE')," +
                "('a213', 'c113', 'CHK-STD', 'ACC-100213', 1200.00, 'USD', 'ACTIVE')," +
                "('a214', 'c114', 'SAV-HYS', 'ACC-100214', 500.00, 'USD', 'ACTIVE')," +
                "('a215', 'c115', 'CHK-STD', 'ACC-100215', 6700.00, 'USD', 'ACTIVE')," +
                "('a216', 'c116', 'SAV-HYS', 'ACC-100216', 15000.00, 'USD', 'ACTIVE')," +
                "('a217', 'c117', 'CHK-STD', 'ACC-100217', 250.00, 'USD', 'ACTIVE')," +
                "('a218', 'c118', 'SAV-HYS', 'ACC-100218', 4200.00, 'USD', 'ACTIVE')," +
                "('a219', 'c119', 'CHK-STD', 'ACC-100219', 9800.00, 'USD', 'ACTIVE')," +
                "('a220', 'c120', 'SAV-HYS', 'ACC-100220', 0.00, 'USD', 'PENDING')," +
                "('a221', 'c121', 'CHK-STD', 'ACC-100221', 11000.00, 'USD', 'ACTIVE')," +
                "('a222', 'c122', 'SAV-HYS', 'ACC-100222', 200.00, 'USD', 'ACTIVE')," +
                "('a223', 'c123', 'CHK-STD', 'ACC-100223', 5400.00, 'USD', 'ACTIVE')," +
                "('a224', 'c124', 'SAV-HYS', 'ACC-100224', 8500.00, 'USD', 'ACTIVE')," +
                "('a225', 'c125', 'CHK-STD', 'ACC-100225', 1300.00, 'USD', 'ACTIVE')," +
                "('a226', 'c126', 'SAV-HYS', 'ACC-100226', 19000.00, 'USD', 'ACTIVE')," +
                "('a227', 'c127', 'CHK-STD', 'ACC-100227', 400.00, 'USD', 'ACTIVE')," +
                "('a228', 'c128', 'SAV-HYS', 'ACC-100228', 7600.00, 'USD', 'ACTIVE')," +
                "('a229', 'c129', 'CHK-STD', 'ACC-100229', 2100.00, 'USD', 'ACTIVE')," +
                "('a230', 'c130', 'SAV-HYS', 'ACC-100230', 0.00, 'USD', 'FROZEN')",

                "INSERT INTO transactions (transaction_id, account_id, transaction_type, amount, currency_code, description, balance_after) VALUES " +
                "('t301', 'a201', 'DEPOSIT', 5000.00, 'USD', 'Initial Deposit', 5000.00)," +
                "('t302', 'a202', 'DEPOSIT', 12000.00, 'USD', 'Opening Balance', 12000.00)," +
                "('t303', 'a203', 'DEPOSIT', 1500.50, 'USD', 'Transfer', 1500.50)," +
                "('t304', 'a204', 'DEPOSIT', 25000.00, 'USD', 'Initial Deposit', 25000.00)," +
                "('t305', 'a205', 'DEPOSIT', 500.00, 'USD', 'Cash Deposit', 500.00)," +
                "('t306', 'a205', 'WITHDRAWAL', 199.25, 'USD', 'ATM', 300.75)," +
                "('t307', 'a206', 'DEPOSIT', 4500.00, 'USD', 'Salary', 4500.00)," +
                "('t308', 'a207', 'DEPOSIT', 8900.00, 'USD', 'Initial Deposit', 8900.00)," +
                "('t309', 'a208', 'DEPOSIT', 150.00, 'USD', 'Opening', 150.00)," +
                "('t310', 'a209', 'DEPOSIT', 2200.00, 'USD', 'Transfer', 2200.00)," +
                "('t311', 'a211', 'DEPOSIT', 7500.00, 'USD', 'Initial Deposit', 7500.00)," +
                "('t312', 'a212', 'DEPOSIT', 30000.00, 'USD', 'Opening Balance', 30000.00)," +
                "('t313', 'a213', 'DEPOSIT', 1200.00, 'USD', 'Transfer', 1200.00)," +
                "('t314', 'a214', 'DEPOSIT', 500.00, 'USD', 'Initial Deposit', 500.00)," +
                "('t315', 'a215', 'DEPOSIT', 6700.00, 'USD', 'Salary', 6700.00)," +
                "('t316', 'a216', 'DEPOSIT', 15000.00, 'USD', 'Opening Balance', 15000.00)," +
                "('t317', 'a217', 'DEPOSIT', 300.00, 'USD', 'Cash', 300.00)," +
                "('t318', 'a217', 'WITHDRAWAL', 50.00, 'USD', 'ATM', 250.00)," +
                "('t319', 'a218', 'DEPOSIT', 4200.00, 'USD', 'Initial Deposit', 4200.00)," +
                "('t320', 'a219', 'DEPOSIT', 9800.00, 'USD', 'Salary', 9800.00)," +
                "('t321', 'a221', 'DEPOSIT', 11000.00, 'USD', 'Initial Deposit', 11000.00)," +
                "('t322', 'a222', 'DEPOSIT', 200.00, 'USD', 'Opening', 200.00)," +
                "('t323', 'a223', 'DEPOSIT', 5400.00, 'USD', 'Salary', 5400.00)," +
                "('t324', 'a224', 'DEPOSIT', 8500.00, 'USD', 'Initial Deposit', 8500.00)," +
                "('t325', 'a225', 'DEPOSIT', 1300.00, 'USD', 'Transfer', 1300.00)," +
                "('t326', 'a226', 'DEPOSIT', 19000.00, 'USD', 'Opening Balance', 19000.00)," +
                "('t327', 'a227', 'DEPOSIT', 400.00, 'USD', 'Initial Deposit', 400.00)," +
                "('t328', 'a228', 'DEPOSIT', 7600.00, 'USD', 'Salary', 7600.00)," +
                "('t329', 'a229', 'DEPOSIT', 2100.00, 'USD', 'Initial Deposit', 2100.00)," +
                "('t330', 'a201', 'WITHDRAWAL', 100.00, 'USD', 'ATM Withdrawal', 4900.00)," +
                "('t331', 'a201', 'DEPOSIT', 100.00, 'USD', 'Cash Deposit', 5000.00)," +
                "('t332', 'a202', 'WITHDRAWAL', 500.00, 'USD', 'Online Payment', 11500.00)," +
                "('t333', 'a202', 'DEPOSIT', 500.00, 'USD', 'Refund', 12000.00)," +
                "('t334', 'a203', 'WITHDRAWAL', 50.50, 'USD', 'Subscription', 1450.00)," +
                "('t335', 'a203', 'DEPOSIT', 50.50, 'USD', 'Reversal', 1500.50)," +
                "('t336', 'a204', 'WITHDRAWAL', 1000.00, 'USD', 'Rent', 24000.00)," +
                "('t337', 'a204', 'DEPOSIT', 1000.00, 'USD', 'Adjustment', 25000.00)," +
                "('t338', 'a205', 'DEPOSIT', 100.00, 'USD', 'Gift', 400.75)," +
                "('t339', 'a205', 'WITHDRAWAL', 100.00, 'USD', 'Dinner', 300.75)," +
                "('t340', 'a206', 'WITHDRAWAL', 200.00, 'USD', 'Grocery', 4300.00)"
            };

            for (String sql : sqlStatements) {
                stmt.execute(sql);
            }
            
            System.out.println("Database Initialized.");

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Database initialization failed", e);
        }
    }
}