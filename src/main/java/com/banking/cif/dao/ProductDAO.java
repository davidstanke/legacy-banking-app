package com.banking.cif.dao;

import com.banking.cif.model.Product;
import com.banking.cif.util.DBConnection;
import java.sql.*;

public class ProductDAO {
    public Product findByCode(String code) throws SQLException {
        String sql = "SELECT * FROM products WHERE product_code = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, code);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Product p = new Product();
                    p.setProductCode(rs.getString("product_code"));
                    p.setCategory(rs.getString("category"));
                    return p;
                }
            }
        }
        return null;
    }
}
