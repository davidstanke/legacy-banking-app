package com.banking.cif.service;

import com.banking.cif.dao.AccountDAO;
import com.banking.cif.dao.CustomerDAO;
import com.banking.cif.dao.ProductDAO;
import com.banking.cif.dao.TransactionDAO;
import com.banking.cif.model.Account;
import com.banking.cif.model.Customer;
import com.banking.cif.model.Product;
import com.banking.cif.model.Transaction;
import com.banking.cif.util.DBConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class BankingService {

    private CustomerDAO customerDAO = new CustomerDAO();
    private AccountDAO accountDAO = new AccountDAO();
    private ProductDAO productDAO = new ProductDAO();
    private TransactionDAO transactionDAO = new TransactionDAO();

    // --- Customer Operations ---

    public Customer createCustomer(Customer customer) throws Exception {
        if (customerDAO.emailExists(customer.getEmail())) {
            throw new Exception("Email already exists");
        }
        // Basic validation
        if (customer.getCifNumber() == null || customer.getCifNumber().isEmpty()) {
             // Generate CIF if missing or throw error? Spec says provided in body for create.
             // Test says "CIF-2024-001".
        }
        return customerDAO.create(customer);
    }

    public Customer getCustomer(Integer id) throws Exception {
        Customer c = customerDAO.findById(id);
        if (c == null) throw new Exception("Customer not found");
        return c;
    }

    public List<Customer> getCustomersByName(String name) throws Exception {
        return customerDAO.findByName(name);
    }

    // --- Account Operations ---

    public Account createAccount(Account account) throws Exception {
        Product p = productDAO.findByCode(account.getProductCode());
        if (p == null) {
            throw new Exception("Invalid Product Code");
        }
        // validate customer exists
        if (customerDAO.findById(account.getCustomerId()) == null) {
            throw new Exception("Customer not found");
        }
        
        return accountDAO.create(account);
    }

    public Account getAccount(Integer id) throws Exception {
        Account a = accountDAO.findById(id);
        if (a == null) throw new Exception("Account not found");
        return a;
    }

    public List<Account> getAccountsByCustomerId(Integer customerId) throws Exception {
        return accountDAO.findByCustomerId(customerId);
    }

    public void updateAccountStatus(Integer id, String status) throws Exception {
        if (accountDAO.findById(id) == null) throw new Exception("Account not found");
        accountDAO.updateStatus(id, status);
    }

    // --- Transaction Operations ---

    public Transaction processTransaction(Transaction transaction) throws Exception {
        // Atomic transaction
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                Account account = accountDAO.findById(conn, transaction.getAccountId());
                if (account == null) {
                    throw new Exception("Account not found");
                }

                BigDecimal newBalance = account.getBalance();
                if ("DEPOSIT".equals(transaction.getTransactionType())) {
                    newBalance = newBalance.add(transaction.getAmount());
                } else if ("WITHDRAWAL".equals(transaction.getTransactionType()) || "WITHDRAW".equals(transaction.getTransactionType())) {
                    if (newBalance.compareTo(transaction.getAmount()) < 0) {
                        throw new Exception("Insufficient funds");
                    }
                    newBalance = newBalance.subtract(transaction.getAmount());
                } else {
                     // Handle other types or ignore
                }

                // Update account
                accountDAO.updateBalance(conn, account.getAccountId(), newBalance);

                // Create transaction record
                transaction.setBalanceAfter(newBalance);
                Transaction created = transactionDAO.create(conn, transaction);

                conn.commit();
                return created;

            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public List<Transaction> getTransactions(Integer accountId) throws Exception {
        return transactionDAO.findByAccountId(accountId);
    }
}
