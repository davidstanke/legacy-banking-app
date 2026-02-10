package com.banking.cif.dao;

import com.banking.cif.model.Transaction;
import com.banking.cif.util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TransactionDAO {

    public Transaction create(Connection conn, Transaction transaction) throws SQLException {
        transaction.setTransactionId(UUID.randomUUID().toString());
        
        String sql = "INSERT INTO transactions (transaction_id, account_id, transaction_type, amount, currency_code, description, balance_after) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, transaction.getTransactionId());
            pstmt.setInt(2, transaction.getAccountId());
            pstmt.setString(3, transaction.getTransactionType());
            pstmt.setBigDecimal(4, transaction.getAmount());
            pstmt.setString(5, transaction.getCurrencyCode());
            pstmt.setString(6, transaction.getDescription());
            pstmt.setBigDecimal(7, transaction.getBalanceAfter());
            
            pstmt.executeUpdate();
            return transaction;
        }
    }

    public List<Transaction> findByAccountId(Integer accountId) throws SQLException {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT * FROM transactions WHERE account_id = ? ORDER BY transaction_date DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, accountId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Transaction t = new Transaction();
                    t.setTransactionId(rs.getString("transaction_id"));
                    t.setAccountId(rs.getInt("account_id"));
                    t.setTransactionType(rs.getString("transaction_type"));
                    t.setAmount(rs.getBigDecimal("amount"));
                    t.setBalanceAfter(rs.getBigDecimal("balance_after"));
                    t.setDescription(rs.getString("description"));
                    // ...
                    list.add(t);
                }
            }
        }
        return list;
    }
}
