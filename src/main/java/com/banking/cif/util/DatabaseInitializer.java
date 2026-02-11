package com.banking.cif.util;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.util.Random;

public class DatabaseInitializer {

    public static void initialize() {
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            // Simple check to avoid re-initializing if data exists
            try {
                ResultSet rs = stmt.executeQuery("SELECT count(*) FROM customers");
                if (rs.next() && rs.getInt(1) > 0) {
                    System.out.println("Database already contains data. Skipping initialization.");
                    return;
                }
            } catch (SQLException e) {
                // Table might not exist, proceed
            }

            System.out.println("Cleaning and Initializing Database...");

            stmt.execute("DROP TABLE IF EXISTS transactions");
            stmt.execute("DROP TABLE IF EXISTS accounts");
            stmt.execute("DROP TABLE IF EXISTS customers");
            stmt.execute("DROP TABLE IF EXISTS products");

            stmt.execute("CREATE TABLE products (product_code VARCHAR(50) PRIMARY KEY, name VARCHAR(100) NOT NULL, category VARCHAR(50) NOT NULL, interest_rate NUMERIC(5, 4), is_active BOOLEAN DEFAULT TRUE)");
            stmt.execute("CREATE TABLE customers (customer_id SERIAL PRIMARY KEY, cif_number VARCHAR(20) UNIQUE NOT NULL, first_name VARCHAR(100) NOT NULL, last_name VARCHAR(100) NOT NULL, date_of_birth DATE NOT NULL, email VARCHAR(150) UNIQUE NOT NULL, phone_number VARCHAR(20), address_line1 VARCHAR(255), city VARCHAR(100), state VARCHAR(100), postal_code VARCHAR(20), country_code CHAR(2), kyc_status VARCHAR(20) DEFAULT 'PENDING', kyc_documents VARCHAR(4000) DEFAULT '{}', risk_rating VARCHAR(10) DEFAULT 'LOW', created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
            stmt.execute("CREATE TABLE accounts (account_id SERIAL PRIMARY KEY, customer_id INTEGER NOT NULL, product_code VARCHAR(50) NOT NULL, account_number INTEGER UNIQUE NOT NULL, iban VARCHAR(34), balance NUMERIC(15, 2) DEFAULT 0.00, overdraft_limit NUMERIC(15, 2) DEFAULT 0.00, status VARCHAR(20) DEFAULT 'ACTIVE', opened_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, closed_at TIMESTAMP, configurations VARCHAR(4000) DEFAULT '{}', FOREIGN KEY (customer_id) REFERENCES customers(customer_id), FOREIGN KEY (product_code) REFERENCES products(product_code))");
            stmt.execute("CREATE TABLE transactions (transaction_id SERIAL PRIMARY KEY, account_id INTEGER NOT NULL, reference_code VARCHAR(50), transaction_type VARCHAR(20) NOT NULL, amount NUMERIC(15, 2) NOT NULL, description VARCHAR(1000), transaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, balance_after NUMERIC(15, 2), FOREIGN KEY (account_id) REFERENCES accounts(account_id))");

            stmt.execute("INSERT INTO products (product_code, name, category, interest_rate) VALUES ('CHK-STD', 'Standard Checking', 'CHECKING', 0.00), ('SAV-HYS', 'High Yield Savings', 'SAVINGS', 0.045)");

            System.out.println("Seeding Customers...");
            stmt.execute("INSERT INTO customers (customer_id, cif_number, first_name, last_name, date_of_birth, email, kyc_status) VALUES " +
                "(1, 'CIF-100001', 'John', 'Smith', '1980-01-01', 'john.smith@example.com', 'COMPLETED')," +
                "(2, 'CIF-100002', 'Jane', 'Doe', '1982-02-02', 'jane.doe@example.com', 'COMPLETED')," +
                "(3, 'CIF-100003', 'Frodo', 'Baggins', '1984-03-03', 'frodo.baggins@example.com', 'COMPLETED')");

            System.out.println("Seeding Accounts...");
            Random rand = new Random();
            stmt.execute("INSERT INTO accounts (account_id, customer_id, product_code, account_number, balance, status) VALUES " +
                "(1, 1, 'CHK-STD', " + (11111111 + rand.nextInt(88888889)) + ", 1500.00, 'ACTIVE')," +
                "(2, 1, 'SAV-HYS', " + (11111111 + rand.nextInt(88888889)) + ", 5000.00, 'ACTIVE')," +
                "(3, 1, 'CHK-STD', " + (11111111 + rand.nextInt(88888889)) + ", 250.00, 'ACTIVE')," +
                "(4, 2, 'CHK-STD', " + (11111111 + rand.nextInt(88888889)) + ", 3200.00, 'ACTIVE')," +
                "(5, 2, 'SAV-HYS', " + (11111111 + rand.nextInt(88888889)) + ", 12000.00, 'ACTIVE')," +
                "(6, 3, 'CHK-STD', " + (11111111 + rand.nextInt(88888889)) + ", 50.00, 'ACTIVE')," +
                "(7, 3, 'SAV-HYS', " + (11111111 + rand.nextInt(88888889)) + ", 1000.00, 'ACTIVE')," +
                "(8, 3, 'CHK-STD', " + (11111111 + rand.nextInt(88888889)) + ", 75.00, 'ACTIVE')," +
                "(9, 3, 'SAV-HYS', " + (11111111 + rand.nextInt(88888889)) + ", 2500.00, 'ACTIVE')");

            System.out.println("Seeding Transactions (John)...");
            // John Acc 1: 10 txns
            stmt.execute("INSERT INTO transactions (transaction_id, account_id, transaction_type, amount, description, balance_after) VALUES " +
                "(1, 1, 'DEPOSIT', 1000.00, 'Paycheck', 1000.00)," +
                "(2, 1, 'WITHDRAWAL', 50.00, 'ATM', 950.00)," +
                "(3, 1, 'DEPOSIT', 200.00, 'Venmo', 1150.00)," +
                "(4, 1, 'WITHDRAWAL', 100.00, 'Groceries', 1050.00)," +
                "(5, 1, 'DEPOSIT', 500.00, 'Gift', 1550.00)," +
                "(6, 1, 'WITHDRAWAL', 20.00, 'Coffee', 1530.00)," +
                "(7, 1, 'WITHDRAWAL', 40.00, 'Gas', 1490.00)," +
                "(8, 1, 'DEPOSIT', 100.00, 'Refund', 1590.00)," +
                "(9, 1, 'WITHDRAWAL', 60.00, 'Dinner', 1530.00)," +
                "(10, 1, 'WITHDRAWAL', 30.00, 'Movies', 1500.00)");

            // John Acc 2: 10 txns
            stmt.execute("INSERT INTO transactions (transaction_id, account_id, transaction_type, amount, description, balance_after) VALUES " +
                "(11, 2, 'DEPOSIT', 5000.00, 'Transfer from External', 5000.00)," +
                "(12, 2, 'DEPOSIT', 50.00, 'Interest', 5050.00)," +
                "(13, 2, 'WITHDRAWAL', 50.00, 'Transfer to Check', 5000.00)," +
                "(14, 2, 'DEPOSIT', 10.00, 'Promo', 5010.00)," +
                "(15, 2, 'WITHDRAWAL', 10.00, 'Service Fee', 5000.00)," +
                "(16, 2, 'DEPOSIT', 100.00, 'Savings Bonus', 5100.00)," +
                "(17, 2, 'WITHDRAWAL', 100.00, 'Correction', 5000.00)," +
                "(18, 2, 'DEPOSIT', 25.00, 'Round-up', 5025.00)," +
                "(19, 2, 'WITHDRAWAL', 25.00, 'Round-up Reversal', 5000.00)," +
                "(20, 2, 'DEPOSIT', 0.00, 'Zero Txn', 5000.00)");

            // John Acc 3: 10 txns
            stmt.execute("INSERT INTO transactions (transaction_id, account_id, transaction_type, amount, description, balance_after) VALUES " +
                "(21, 3, 'DEPOSIT', 500.00, 'Initial', 500.00)," +
                "(22, 3, 'WITHDRAWAL', 25.00, 'Bus Pass', 475.00)," +
                "(23, 3, 'WITHDRAWAL', 25.00, 'Bus Pass', 450.00)," +
                "(24, 3, 'WITHDRAWAL', 25.00, 'Bus Pass', 425.00)," +
                "(25, 3, 'WITHDRAWAL', 25.00, 'Bus Pass', 400.00)," +
                "(26, 3, 'WITHDRAWAL', 25.00, 'Bus Pass', 375.00)," +
                "(27, 3, 'WITHDRAWAL', 25.00, 'Bus Pass', 350.00)," +
                "(28, 3, 'WITHDRAWAL', 25.00, 'Bus Pass', 325.00)," +
                "(29, 3, 'WITHDRAWAL', 25.00, 'Bus Pass', 300.00)," +
                "(30, 3, 'WITHDRAWAL', 50.00, 'Uber', 250.00)");

            System.out.println("Seeding Transactions (Jane)...");
            // Jane Acc 1: 10 txns
            stmt.execute("INSERT INTO transactions (transaction_id, account_id, transaction_type, amount, description, balance_after) VALUES " +
                "(31, 4, 'DEPOSIT', 4000.00, 'Salary', 4000.00)," +
                "(32, 4, 'WITHDRAWAL', 100.00, 'Target', 3900.00)," +
                "(33, 4, 'WITHDRAWAL', 100.00, 'Target', 3800.00)," +
                "(34, 4, 'WITHDRAWAL', 100.00, 'Target', 3700.00)," +
                "(35, 4, 'WITHDRAWAL', 100.00, 'Target', 3600.00)," +
                "(36, 4, 'WITHDRAWAL', 100.00, 'Target', 3500.00)," +
                "(37, 4, 'WITHDRAWAL', 100.00, 'Target', 3400.00)," +
                "(38, 4, 'WITHDRAWAL', 100.00, 'Target', 3300.00)," +
                "(39, 4, 'WITHDRAWAL', 100.00, 'Target', 3200.00)," +
                "(40, 4, 'WITHDRAWAL', 0.00, 'Void', 3200.00)");

            // Jane Acc 2: 10 txns
            stmt.execute("INSERT INTO transactions (transaction_id, account_id, transaction_type, amount, description, balance_after) VALUES " +
                "(41, 5, 'DEPOSIT', 10000.00, 'Inheritance', 10000.00)," +
                "(42, 5, 'DEPOSIT', 2000.00, 'Transfer', 12000.00)," +
                "(43, 5, 'DEPOSIT', 1.00, 'Micro', 12001.00)," +
                "(44, 5, 'WITHDRAWAL', 1.00, 'Micro', 12000.00)," +
                "(45, 5, 'DEPOSIT', 1.00, 'Micro', 12001.00)," +
                "(46, 5, 'WITHDRAWAL', 1.00, 'Micro', 12000.00)," +
                "(47, 5, 'DEPOSIT', 1.00, 'Micro', 12001.00)," +
                "(48, 5, 'WITHDRAWAL', 1.00, 'Micro', 12000.00)," +
                "(49, 5, 'DEPOSIT', 1.00, 'Micro', 12001.00)," +
                "(50, 5, 'WITHDRAWAL', 1.00, 'Micro', 12000.00)");

            System.out.println("Seeding Transactions (Frodo)...");
            // Frodo Acc 1: 10 txns
            stmt.execute("INSERT INTO transactions (transaction_id, account_id, transaction_type, amount, description, balance_after) VALUES " +
                "(51, 6, 'DEPOSIT', 100.00, 'Found gold', 100.00)," +
                "(52, 6, 'WITHDRAWAL', 5.00, 'Ale', 95.00)," +
                "(53, 6, 'WITHDRAWAL', 5.00, 'Ale', 90.00)," +
                "(54, 6, 'WITHDRAWAL', 5.00, 'Ale', 85.00)," +
                "(55, 6, 'WITHDRAWAL', 5.00, 'Ale', 80.00)," +
                "(56, 6, 'WITHDRAWAL', 5.00, 'Ale', 75.00)," +
                "(57, 6, 'WITHDRAWAL', 5.00, 'Ale', 70.00)," +
                "(58, 6, 'WITHDRAWAL', 5.00, 'Ale', 65.00)," +
                "(59, 6, 'WITHDRAWAL', 5.00, 'Ale', 60.00)," +
                "(60, 6, 'WITHDRAWAL', 10.00, 'Lembas', 50.00)");

            // Frodo Acc 2: 10 txns
            stmt.execute("INSERT INTO transactions (transaction_id, account_id, transaction_type, amount, description, balance_after) VALUES " +
                "(61, 7, 'DEPOSIT', 1000.00, 'Bag End Rent', 1000.00)," +
                "(62, 7, 'DEPOSIT', 0.00, 'Ping', 1000.00)," +
                "(63, 7, 'DEPOSIT', 0.00, 'Ping', 1000.00)," +
                "(64, 7, 'DEPOSIT', 0.00, 'Ping', 1000.00)," +
                "(65, 7, 'DEPOSIT', 0.00, 'Ping', 1000.00)," +
                "(66, 7, 'DEPOSIT', 0.00, 'Ping', 1000.00)," +
                "(67, 7, 'DEPOSIT', 0.00, 'Ping', 1000.00)," +
                "(68, 7, 'DEPOSIT', 0.00, 'Ping', 1000.00)," +
                "(69, 7, 'DEPOSIT', 0.00, 'Ping', 1000.00)," +
                "(70, 7, 'DEPOSIT', 0.00, 'Ping', 1000.00)");

            // Frodo Acc 3: 10 txns
            stmt.execute("INSERT INTO transactions (transaction_id, account_id, transaction_type, amount, description, balance_after) VALUES " +
                "(71, 8, 'DEPOSIT', 500.00, 'Bree gold', 500.00)," +
                "(72, 8, 'WITHDRAWAL', 50.00, 'Pony', 450.00)," +
                "(73, 8, 'WITHDRAWAL', 50.00, 'Pony', 400.00)," +
                "(74, 8, 'WITHDRAWAL', 50.00, 'Pony', 350.00)," +
                "(75, 8, 'WITHDRAWAL', 50.00, 'Pony', 300.00)," +
                "(76, 8, 'WITHDRAWAL', 50.00, 'Pony', 250.00)," +
                "(77, 8, 'WITHDRAWAL', 50.00, 'Pony', 200.00)," +
                "(78, 8, 'WITHDRAWAL', 50.00, 'Pony', 150.00)," +
                "(79, 8, 'WITHDRAWAL', 50.00, 'Pony', 100.00)," +
                "(80, 8, 'WITHDRAWAL', 25.00, 'Feed', 75.00)");

            // Frodo Acc 4: 10 txns
            stmt.execute("INSERT INTO transactions (transaction_id, account_id, transaction_type, amount, description, balance_after) VALUES " +
                "(81, 9, 'DEPOSIT', 2500.00, 'Rivendell treasury', 2500.00)," +
                "(82, 9, 'DEPOSIT', 0.00, 'Marker', 2500.00)," +
                "(83, 9, 'DEPOSIT', 0.00, 'Marker', 2500.00)," +
                "(84, 9, 'DEPOSIT', 0.00, 'Marker', 2500.00)," +
                "(85, 9, 'DEPOSIT', 0.00, 'Marker', 2500.00)," +
                "(86, 9, 'DEPOSIT', 0.00, 'Marker', 2500.00)," +
                "(87, 9, 'DEPOSIT', 0.00, 'Marker', 2500.00)," +
                "(88, 9, 'DEPOSIT', 0.00, 'Marker', 2500.00)," +
                "(89, 9, 'DEPOSIT', 0.00, 'Marker', 2500.00)," +
                "(90, 9, 'DEPOSIT', 0.00, 'Marker', 2500.00)");

            System.out.println("Database Initialized with fully hardcoded SQL literal data.");

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Database initialization failed", e);
        }
    }
}