package com.banking.cif.dao;

import com.banking.cif.model.Account;
import com.banking.cif.util.DBConnection;
import java.sql.*;
import java.util.UUID;
import java.math.BigDecimal;

public class AccountDAO {

    public Account create(Account account) throws SQLException {
        account.setAccountId(UUID.randomUUID().toString());
        // Generate account number? Demo logic: Time-based or Random
        if (account.getAccountNumber() == null) {
            account.setAccountNumber(String.valueOf(System.currentTimeMillis())); 
        }
        account.setStatus("ACTIVE");
        account.setBalance(BigDecimal.ZERO);

        String sql = "INSERT INTO accounts (account_id, customer_id, product_code, account_number, currency_code, balance, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, account.getAccountId());
            pstmt.setString(2, account.getCustomerId());
            pstmt.setString(3, account.getProductCode());
            pstmt.setString(4, account.getAccountNumber());
            pstmt.setString(5, account.getCurrencyCode());
            pstmt.setBigDecimal(6, account.getBalance());
            pstmt.setString(7, account.getStatus());
            
            pstmt.executeUpdate();
            return account;
        }
    }

    public Account findById(String id) throws SQLException {
        String sql = "SELECT * FROM accounts WHERE account_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Account a = new Account();
                    a.setAccountId(rs.getString("account_id"));
                    a.setCustomerId(rs.getString("customer_id"));
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

    public void updateStatus(String id, String status) throws SQLException {
        String sql = "UPDATE accounts SET status = ? WHERE account_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, status);
            pstmt.setString(2, id);
            pstmt.executeUpdate();
        }
    }
    
    // For transactions - update balance
    public void updateBalance(Connection conn, String accountId, BigDecimal newBalance) throws SQLException {
        String sql = "UPDATE accounts SET balance = ? WHERE account_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setBigDecimal(1, newBalance);
            pstmt.setString(2, accountId);
            pstmt.executeUpdate();
        }
    }
    
    // Helper to get with specific connection (for transaction usage)
    public Account findById(Connection conn, String id) throws SQLException {
        String sql = "SELECT * FROM accounts WHERE account_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Account a = new Account();
                    a.setAccountId(rs.getString("account_id"));
                    a.setBalance(rs.getBigDecimal("balance"));
                    // ... other fields needed for validation
                    return a;
                }
            }
        }
        return null;
    }
}
