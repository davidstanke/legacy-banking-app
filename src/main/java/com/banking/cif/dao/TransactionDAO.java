package com.banking.cif.dao;

import com.banking.cif.model.Transaction;
import com.banking.cif.util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAO {

    public Transaction create(Connection conn, Transaction transaction) throws SQLException {
        String sql = "INSERT INTO transactions (account_id, transaction_type, amount, description, balance_after) " +
                     "VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, transaction.getAccountId());
            pstmt.setString(2, transaction.getTransactionType());
            pstmt.setBigDecimal(3, transaction.getAmount());
            pstmt.setString(4, transaction.getDescription());
            pstmt.setBigDecimal(5, transaction.getBalanceAfter());
            
            pstmt.executeUpdate();
            
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    transaction.setTransactionId(generatedKeys.getInt(1));
                }
            }
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
                    t.setTransactionId(rs.getInt("transaction_id"));
                    t.setAccountId(rs.getInt("account_id"));
                    t.setReferenceCode(rs.getString("reference_code"));
                    t.setTransactionType(rs.getString("transaction_type"));
                    t.setAmount(rs.getBigDecimal("amount"));
                    t.setDescription(rs.getString("description"));
                    t.setTransactionDate(rs.getTimestamp("transaction_date"));
                    t.setBalanceAfter(rs.getBigDecimal("balance_after"));
                    list.add(t);
                }
            }
        }
        return list;
    }
}
