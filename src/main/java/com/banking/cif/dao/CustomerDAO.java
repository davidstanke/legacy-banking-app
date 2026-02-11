package com.banking.cif.dao;

import com.banking.cif.model.Customer;
import com.banking.cif.util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerDAO {

    public Customer create(Customer customer) throws SQLException {
        String sql = "INSERT INTO customers (cif_number, first_name, last_name, date_of_birth, email, kyc_status) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        
        customer.setKycStatus("PENDING"); // Default

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, customer.getCifNumber());
            pstmt.setString(2, customer.getFirstName());
            pstmt.setString(3, customer.getLastName());
            pstmt.setDate(4, customer.getDateOfBirth());
            pstmt.setString(5, customer.getEmail());
            pstmt.setString(6, customer.getKycStatus());
            
            pstmt.executeUpdate();
            
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    customer.setCustomerId(generatedKeys.getInt(1));
                }
            }
            return customer;
        }
    }

    public Customer findById(Integer id) throws SQLException {
        String sql = "SELECT * FROM customers WHERE customer_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToCustomer(rs);
                }
            }
        }
        return null;
    }

    public List<Customer> findByName(String name) throws SQLException {
        List<Customer> customers = new ArrayList<>();
        String sql = "SELECT * FROM customers WHERE LOWER(first_name) LIKE LOWER(?) OR LOWER(last_name) LIKE LOWER(?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            String queryParam = "%" + name + "%";
            pstmt.setString(1, queryParam);
            pstmt.setString(2, queryParam);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    customers.add(mapResultSetToCustomer(rs));
                }
            }
        }
        return customers;
    }

    public List<Customer> findAllWithAccountCount() throws SQLException {
        List<Customer> customers = new ArrayList<>();
        String sql = "SELECT c.*, (SELECT COUNT(*) FROM accounts a WHERE a.customer_id = c.customer_id) as account_count FROM customers c ORDER BY c.last_name, c.first_name";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                Customer c = mapResultSetToCustomer(rs);
                c.setAccountCount(rs.getInt("account_count"));
                customers.add(c);
            }
        }
        return customers;
    }
    
    public boolean emailExists(String email) throws SQLException {
        String sql = "SELECT 1 FROM customers WHERE email = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    private Customer mapResultSetToCustomer(ResultSet rs) throws SQLException {
        Customer c = new Customer();
        c.setCustomerId(rs.getInt("customer_id"));
        c.setCifNumber(rs.getString("cif_number"));
        c.setFirstName(rs.getString("first_name"));
        c.setLastName(rs.getString("last_name"));
        c.setDateOfBirth(rs.getDate("date_of_birth"));
        c.setEmail(rs.getString("email"));
        c.setKycStatus(rs.getString("kyc_status"));
        c.setAddressLine1(rs.getString("address_line1"));
        c.setCity(rs.getString("city"));
        c.setPhoneNumber(rs.getString("phone_number"));
        c.setState(rs.getString("state"));
        c.setPostalCode(rs.getString("postal_code"));
        c.setCountryCode(rs.getString("country_code"));
        c.setRiskRating(rs.getString("risk_rating"));
        return c;
    }
}
