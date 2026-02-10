package com.banking.cif.dao;

import com.banking.cif.model.Account;
import com.banking.cif.util.DBConnection;
import java.sql.*;
import java.math.BigDecimal;

public class AccountDAO {

    public Account create(Account account) throws SQLException {
        // Generate account number? Demo logic: Time-based or Random
        if (account.getAccountNumber() == null) {
            account.setAccountNumber(String.valueOf(System.currentTimeMillis())); 
        }
        account.setStatus("ACTIVE");
        account.setBalance(BigDecimal.ZERO);

        String sql = "INSERT INTO accounts (customer_id, product_code, account_number, balance, status) " +
                     "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setInt(1, account.getCustomerId());
            pstmt.setString(2, account.getProductCode());
            pstmt.setString(3, account.getAccountNumber());
            pstmt.setBigDecimal(4, account.getBalance());
            pstmt.setString(5, account.getStatus());
            
            pstmt.executeUpdate();
            
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    account.setAccountId(generatedKeys.getInt(1));
                }
            }
            return account;
        }
    }

    public Account findById(Integer id) throws SQLException {
        String sql = "SELECT * FROM accounts WHERE account_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Account a = new Account();
                    a.setAccountId(rs.getInt("account_id"));
                    a.setCustomerId(rs.getInt("customer_id"));
                    a.setProductCode(rs.getString("product_code"));
                    a.setAccountNumber(rs.getString("account_number"));
                    a.setBalance(rs.getBigDecimal("balance"));
                    a.setCurrencyCode(rs.getString("currency_code"));
                    a.setStatus(rs.getString("status"));
                    return a;
                }
            }
        }
        return null;
    }

    public void updateStatus(Integer id, String status) throws SQLException {
        String sql = "UPDATE accounts SET status = ? WHERE account_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, status);
            pstmt.setInt(2, id);
            pstmt.executeUpdate();
        }
    }

    public java.util.List<Account> findByCustomerId(Integer customerId) throws SQLException {
        java.util.List<Account> accounts = new java.util.ArrayList<>();
        String sql = "SELECT * FROM accounts WHERE customer_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, customerId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Account a = new Account();
                    a.setAccountId(rs.getInt("account_id"));
                    a.setCustomerId(rs.getInt("customer_id"));
                    a.setProductCode(rs.getString("product_code"));
                    a.setAccountNumber(rs.getString("account_number"));
                    a.setBalance(rs.getBigDecimal("balance"));
                    a.setCurrencyCode(rs.getString("currency_code"));
                    a.setStatus(rs.getString("status"));
                    accounts.add(a);
                }
            }
        }
        return accounts;
    }
    
    // For transactions - update balance
    public void updateBalance(Connection conn, Integer accountId, BigDecimal newBalance) throws SQLException {
        String sql = "UPDATE accounts SET balance = ? WHERE account_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setBigDecimal(1, newBalance);
            pstmt.setInt(2, accountId);
            pstmt.executeUpdate();
        }
    }
    
    // Helper to get with specific connection (for transaction usage)
    public Account findById(Connection conn, Integer id) throws SQLException {
        String sql = "SELECT * FROM accounts WHERE account_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Account a = new Account();
                    a.setAccountId(rs.getInt("account_id"));
                    a.setBalance(rs.getBigDecimal("balance"));
                    // ... other fields needed for validation
                    return a;
                }
            }
        }
        return null;
    }
}
