package com.banking.cif.util;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;
import java.sql.ResultSet;

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

            stmt.execute("CREATE TABLE products (product_code VARCHAR(50) PRIMARY KEY, name VARCHAR(100) NOT NULL, category VARCHAR(50) NOT NULL, interest_rate NUMERIC(5, 4), currency_code CHAR(3) DEFAULT 'USD', is_active BOOLEAN DEFAULT TRUE)");
            stmt.execute("CREATE TABLE customers (customer_id SERIAL PRIMARY KEY, cif_number VARCHAR(20) UNIQUE NOT NULL, first_name VARCHAR(100) NOT NULL, last_name VARCHAR(100) NOT NULL, date_of_birth DATE NOT NULL, email VARCHAR(150) UNIQUE NOT NULL, phone_number VARCHAR(20), address_line1 VARCHAR(255), city VARCHAR(100), state VARCHAR(100), postal_code VARCHAR(20), country_code CHAR(2), kyc_status VARCHAR(20) DEFAULT 'PENDING', kyc_documents VARCHAR(4000) DEFAULT '{}', risk_rating VARCHAR(10) DEFAULT 'LOW', created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
            stmt.execute("CREATE TABLE accounts (account_id SERIAL PRIMARY KEY, customer_id INTEGER NOT NULL, product_code VARCHAR(50) NOT NULL, account_number VARCHAR(30) UNIQUE NOT NULL, iban VARCHAR(34), balance NUMERIC(15, 2) DEFAULT 0.00, currency_code CHAR(3) NOT NULL DEFAULT 'USD', overdraft_limit NUMERIC(15, 2) DEFAULT 0.00, status VARCHAR(20) DEFAULT 'ACTIVE', opened_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, closed_at TIMESTAMP, configurations VARCHAR(4000) DEFAULT '{}', FOREIGN KEY (customer_id) REFERENCES customers(customer_id), FOREIGN KEY (product_code) REFERENCES products(product_code))");
            stmt.execute("CREATE TABLE transactions (transaction_id VARCHAR(36) PRIMARY KEY, account_id INTEGER NOT NULL, reference_code VARCHAR(50), transaction_type VARCHAR(20) NOT NULL, amount NUMERIC(15, 2) NOT NULL, currency_code CHAR(3) NOT NULL DEFAULT 'USD', description VARCHAR(1000), transaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, balance_after NUMERIC(15, 2), FOREIGN KEY (account_id) REFERENCES accounts(account_id))");

            stmt.execute("INSERT INTO products (product_code, name, category, interest_rate) VALUES ('CHK-STD', 'Standard Checking', 'CHECKING', 0.00), ('SAV-HYS', 'High Yield Savings', 'SAVINGS', 0.045)");

            System.out.println("Seeding Customers...");
            stmt.execute("INSERT INTO customers (customer_id, cif_number, first_name, last_name, date_of_birth, email, kyc_status) VALUES " +
                "(1, 'CIF-100001', 'John', 'Smith', '1980-01-01', 'john.smith@example.com', 'COMPLETED')," +
                "(2, 'CIF-100002', 'Jane', 'Doe', '1982-02-02', 'jane.doe@example.com', 'COMPLETED')," +
                "(3, 'CIF-100003', 'Frodo', 'Baggins', '1984-03-03', 'frodo.baggins@example.com', 'COMPLETED')");

            System.out.println("Seeding Accounts...");
            stmt.execute("INSERT INTO accounts (account_id, customer_id, product_code, account_number, balance, status) VALUES " +
                "(1, 1, 'CHK-STD', 'ACC-JOHN-1', 1500.00, 'ACTIVE')," +
                "(2, 1, 'SAV-HYS', 'ACC-JOHN-2', 5000.00, 'ACTIVE')," +
                "(3, 1, 'CHK-STD', 'ACC-JOHN-3', 250.00, 'ACTIVE')," +
                "(4, 2, 'CHK-STD', 'ACC-JANE-1', 3200.00, 'ACTIVE')," +
                "(5, 2, 'SAV-HYS', 'ACC-JANE-2', 12000.00, 'ACTIVE')," +
                "(6, 3, 'CHK-STD', 'ACC-FRODO-1', 50.00, 'ACTIVE')," +
                "(7, 3, 'SAV-HYS', 'ACC-FRODO-2', 1000.00, 'ACTIVE')," +
                "(8, 3, 'CHK-STD', 'ACC-FRODO-3', 75.00, 'ACTIVE')," +
                "(9, 3, 'SAV-HYS', 'ACC-FRODO-4', 2500.00, 'ACTIVE')");

            System.out.println("Seeding Transactions (John)...");
            // John Acc 1: 10 txns
            stmt.execute("INSERT INTO transactions (transaction_id, account_id, transaction_type, amount, description, balance_after) VALUES " +
                "('j1-t1', 1, 'DEPOSIT', 1000.00, 'Paycheck', 1000.00)," +
                "('j1-t2', 1, 'WITHDRAWAL', 50.00, 'ATM', 950.00)," +
                "('j1-t3', 1, 'DEPOSIT', 200.00, 'Venmo', 1150.00)," +
                "('j1-t4', 1, 'WITHDRAWAL', 100.00, 'Groceries', 1050.00)," +
                "('j1-t5', 1, 'DEPOSIT', 500.00, 'Gift', 1550.00)," +
                "('j1-t6', 1, 'WITHDRAWAL', 20.00, 'Coffee', 1530.00)," +
                "('j1-t7', 1, 'WITHDRAWAL', 40.00, 'Gas', 1490.00)," +
                "('j1-t8', 1, 'DEPOSIT', 100.00, 'Refund', 1590.00)," +
                "('j1-t9', 1, 'WITHDRAWAL', 60.00, 'Dinner', 1530.00)," +
                "('j1-t10', 1, 'WITHDRAWAL', 30.00, 'Movies', 1500.00)");

            // John Acc 2: 10 txns
            stmt.execute("INSERT INTO transactions (transaction_id, account_id, transaction_type, amount, description, balance_after) VALUES " +
                "('j2-t1', 2, 'DEPOSIT', 5000.00, 'Transfer from External', 5000.00)," +
                "('j2-t2', 2, 'DEPOSIT', 50.00, 'Interest', 5050.00)," +
                "('j2-t3', 2, 'WITHDRAWAL', 50.00, 'Transfer to Check', 5000.00)," +
                "('j2-t4', 2, 'DEPOSIT', 10.00, 'Promo', 5010.00)," +
                "('j2-t5', 2, 'WITHDRAWAL', 10.00, 'Service Fee', 5000.00)," +
                "('j2-t6', 2, 'DEPOSIT', 100.00, 'Savings Bonus', 5100.00)," +
                "('j2-t7', 2, 'WITHDRAWAL', 100.00, 'Correction', 5000.00)," +
                "('j2-t8', 2, 'DEPOSIT', 25.00, 'Round-up', 5025.00)," +
                "('j2-t9', 2, 'WITHDRAWAL', 25.00, 'Round-up Reversal', 5000.00)," +
                "('j2-t10', 2, 'DEPOSIT', 0.00, 'Zero Txn', 5000.00)");

            // John Acc 3: 10 txns
            stmt.execute("INSERT INTO transactions (transaction_id, account_id, transaction_type, amount, description, balance_after) VALUES " +
                "('j3-t1', 3, 'DEPOSIT', 500.00, 'Initial', 500.00)," +
                "('j3-t2', 3, 'WITHDRAWAL', 25.00, 'Bus Pass', 475.00)," +
                "('j3-t3', 3, 'WITHDRAWAL', 25.00, 'Bus Pass', 450.00)," +
                "('j3-t4', 3, 'WITHDRAWAL', 25.00, 'Bus Pass', 425.00)," +
                "('j3-t5', 3, 'WITHDRAWAL', 25.00, 'Bus Pass', 400.00)," +
                "('j3-t6', 3, 'WITHDRAWAL', 25.00, 'Bus Pass', 375.00)," +
                "('j3-t7', 3, 'WITHDRAWAL', 25.00, 'Bus Pass', 350.00)," +
                "('j3-t8', 3, 'WITHDRAWAL', 25.00, 'Bus Pass', 325.00)," +
                "('j3-t9', 3, 'WITHDRAWAL', 25.00, 'Bus Pass', 300.00)," +
                "('j3-t10', 3, 'WITHDRAWAL', 50.00, 'Uber', 250.00)");

            System.out.println("Seeding Transactions (Jane)...");
            // Jane Acc 1: 10 txns
            stmt.execute("INSERT INTO transactions (transaction_id, account_id, transaction_type, amount, description, balance_after) VALUES " +
                "('ja1-t1', 4, 'DEPOSIT', 4000.00, 'Salary', 4000.00)," +
                "('ja1-t2', 4, 'WITHDRAWAL', 100.00, 'Target', 3900.00)," +
                "('ja1-t3', 4, 'WITHDRAWAL', 100.00, 'Target', 3800.00)," +
                "('ja1-t4', 4, 'WITHDRAWAL', 100.00, 'Target', 3700.00)," +
                "('ja1-t5', 4, 'WITHDRAWAL', 100.00, 'Target', 3600.00)," +
                "('ja1-t6', 4, 'WITHDRAWAL', 100.00, 'Target', 3500.00)," +
                "('ja1-t7', 4, 'WITHDRAWAL', 100.00, 'Target', 3400.00)," +
                "('ja1-t8', 4, 'WITHDRAWAL', 100.00, 'Target', 3300.00)," +
                "('ja1-t9', 4, 'WITHDRAWAL', 100.00, 'Target', 3200.00)," +
                "('ja1-t10', 4, 'WITHDRAWAL', 0.00, 'Void', 3200.00)");

            // Jane Acc 2: 10 txns
            stmt.execute("INSERT INTO transactions (transaction_id, account_id, transaction_type, amount, description, balance_after) VALUES " +
                "('ja2-t1', 5, 'DEPOSIT', 10000.00, 'Inheritance', 10000.00)," +
                "('ja2-t2', 5, 'DEPOSIT', 2000.00, 'Transfer', 12000.00)," +
                "('ja2-t3', 5, 'DEPOSIT', 1.00, 'Micro', 12001.00)," +
                "('ja2-t4', 5, 'WITHDRAWAL', 1.00, 'Micro', 12000.00)," +
                "('ja2-t5', 5, 'DEPOSIT', 1.00, 'Micro', 12001.00)," +
                "('ja2-t6', 5, 'WITHDRAWAL', 1.00, 'Micro', 12000.00)," +
                "('ja2-t7', 5, 'DEPOSIT', 1.00, 'Micro', 12001.00)," +
                "('ja2-t8', 5, 'WITHDRAWAL', 1.00, 'Micro', 12000.00)," +
                "('ja2-t9', 5, 'DEPOSIT', 1.00, 'Micro', 12001.00)," +
                "('ja2-t10', 5, 'WITHDRAWAL', 1.00, 'Micro', 12000.00)");

            System.out.println("Seeding Transactions (Frodo)...");
            // Frodo Acc 1: 10 txns
            stmt.execute("INSERT INTO transactions (transaction_id, account_id, transaction_type, amount, description, balance_after) VALUES " +
                "('f1-t1', 6, 'DEPOSIT', 100.00, 'Found gold', 100.00)," +
                "('f1-t2', 6, 'WITHDRAWAL', 5.00, 'Ale', 95.00)," +
                "('f1-t3', 6, 'WITHDRAWAL', 5.00, 'Ale', 90.00)," +
                "('f1-t4', 6, 'WITHDRAWAL', 5.00, 'Ale', 85.00)," +
                "('f1-t5', 6, 'WITHDRAWAL', 5.00, 'Ale', 80.00)," +
                "('f1-t6', 6, 'WITHDRAWAL', 5.00, 'Ale', 75.00)," +
                "('f1-t7', 6, 'WITHDRAWAL', 5.00, 'Ale', 70.00)," +
                "('f1-t8', 6, 'WITHDRAWAL', 5.00, 'Ale', 65.00)," +
                "('f1-t9', 6, 'WITHDRAWAL', 5.00, 'Ale', 60.00)," +
                "('f1-t10', 6, 'WITHDRAWAL', 10.00, 'Lembas', 50.00)");

            // Frodo Acc 2: 10 txns
            stmt.execute("INSERT INTO transactions (transaction_id, account_id, transaction_type, amount, description, balance_after) VALUES " +
                "('f2-t1', 7, 'DEPOSIT', 1000.00, 'Bag End Rent', 1000.00)," +
                "('f2-t2', 7, 'DEPOSIT', 0.00, 'Ping', 1000.00)," +
                "('f2-t3', 7, 'DEPOSIT', 0.00, 'Ping', 1000.00)," +
                "('f2-t4', 7, 'DEPOSIT', 0.00, 'Ping', 1000.00)," +
                "('f2-t5', 7, 'DEPOSIT', 0.00, 'Ping', 1000.00)," +
                "('f2-t6', 7, 'DEPOSIT', 0.00, 'Ping', 1000.00)," +
                "('f2-t7', 7, 'DEPOSIT', 0.00, 'Ping', 1000.00)," +
                "('f2-t8', 7, 'DEPOSIT', 0.00, 'Ping', 1000.00)," +
                "('f2-t9', 7, 'DEPOSIT', 0.00, 'Ping', 1000.00)," +
                "('f2-t10', 7, 'DEPOSIT', 0.00, 'Ping', 1000.00)");

            // Frodo Acc 3: 10 txns
            stmt.execute("INSERT INTO transactions (transaction_id, account_id, transaction_type, amount, description, balance_after) VALUES " +
                "('f3-t1', 8, 'DEPOSIT', 500.00, 'Bree gold', 500.00)," +
                "('f3-t2', 8, 'WITHDRAWAL', 50.00, 'Pony', 450.00)," +
                "('f3-t3', 8, 'WITHDRAWAL', 50.00, 'Pony', 400.00)," +
                "('f3-t4', 8, 'WITHDRAWAL', 50.00, 'Pony', 350.00)," +
                "('f3-t5', 8, 'WITHDRAWAL', 50.00, 'Pony', 300.00)," +
                "('f3-t6', 8, 'WITHDRAWAL', 50.00, 'Pony', 250.00)," +
                "('f3-t7', 8, 'WITHDRAWAL', 50.00, 'Pony', 200.00)," +
                "('f3-t8', 8, 'WITHDRAWAL', 50.00, 'Pony', 150.00)," +
                "('f3-t9', 8, 'WITHDRAWAL', 50.00, 'Pony', 100.00)," +
                "('f3-t10', 8, 'WITHDRAWAL', 25.00, 'Feed', 75.00)");

            // Frodo Acc 4: 10 txns
            stmt.execute("INSERT INTO transactions (transaction_id, account_id, transaction_type, amount, description, balance_after) VALUES " +
                "('f4-t1', 9, 'DEPOSIT', 2500.00, 'Rivendell treasury', 2500.00)," +
                "('f4-t2', 9, 'DEPOSIT', 0.00, 'Marker', 2500.00)," +
                "('f4-t3', 9, 'DEPOSIT', 0.00, 'Marker', 2500.00)," +
                "('f4-t4', 9, 'DEPOSIT', 0.00, 'Marker', 2500.00)," +
                "('f4-t5', 9, 'DEPOSIT', 0.00, 'Marker', 2500.00)," +
                "('f4-t6', 9, 'DEPOSIT', 0.00, 'Marker', 2500.00)," +
                "('f4-t7', 9, 'DEPOSIT', 0.00, 'Marker', 2500.00)," +
                "('f4-t8', 9, 'DEPOSIT', 0.00, 'Marker', 2500.00)," +
                "('f4-t9', 9, 'DEPOSIT', 0.00, 'Marker', 2500.00)," +
                "('f4-t10', 9, 'DEPOSIT', 0.00, 'Marker', 2500.00)");

            System.out.println("Database Initialized with fully hardcoded SQL literal data.");

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Database initialization failed", e);
        }
    }
}