package com.banking.cif;

import com.banking.cif.action.AccountsController;
import com.banking.cif.action.CustomersController;
import com.banking.cif.action.TransactionsController;
import com.banking.cif.model.Account;
import com.banking.cif.model.Customer;
import com.banking.cif.model.Transaction;
import com.banking.cif.util.DatabaseInitializer;
import org.apache.struts2.StrutsJUnit4TestCase;
import org.junit.Before;
import org.junit.Test;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import javax.servlet.ServletException;

import static org.junit.Assert.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class BankingApiTest extends StrutsJUnit4TestCase<Object> {

    @Before
    public void setUp() throws Exception {
        super.setUp();
        DatabaseInitializer.initialize();
    }

    // --- Customer Tests ---

    @Test
    public void test01_CreateCustomerSuccess() throws Exception {
        request.setContent("{\"firstName\": \"Alice\", \"lastName\": \"Smith\", \"email\": \"alice.smith@example.com\", \"dateOfBirth\": \"1990-01-01\", \"cifNumber\": \"CIF-2024-001\"}".getBytes("UTF-8"));
        request.setContentType("application/json");
        request.setMethod("POST");
        
        String result = executeAction("/api/v1/customers");
        
        CustomersController action = (CustomersController) getAction();
        assertEquals(201, action.getStatus());
        Customer c = (Customer) action.getModel();
        assertEquals("Alice", c.getFirstName());
        assertNotNull(c.getCustomerId());
    }

    @Test
    public void test02_CreateCustomerValidationError() throws Exception {
        // Missing Email
        request.setContent("{\"firstName\": \"Bob\", \"lastName\": \"Jones\", \"cifNumber\": \"CIF-2024-002\"}".getBytes("UTF-8"));
        request.setContentType("application/json");
        request.setMethod("POST");

        executeAction("/api/v1/customers");
        CustomersController action = (CustomersController) getAction();
        assertEquals(400, action.getStatus());
    }

    // --- Account Tests ---

    @Test
    public void test03_CreateAccountSuccess() throws Exception {
        String customerId = createHelperCustomer("Bob", "bob@example.com", "CIF-BOB");
        
        String json = String.format("{\"customerId\": \"%s\", \"productCode\": \"CHK-STD\", \"currencyCode\": \"USD\"}", customerId);
        request.setContent(json.getBytes("UTF-8"));
        request.setContentType("application/json");
        request.setMethod("POST");

        executeAction("/api/v1/accounts");
        AccountsController action = (AccountsController) getAction();
        assertEquals(201, action.getStatus());
        Account a = (Account) action.getModel();
        assertEquals("ACTIVE", a.getStatus());
    }
    
    @Test
    public void test04_UpdateAccountStatus() throws Exception {
        String customerId = createHelperCustomer("Charlie", "charlie@example.com", "CIF-CHARLIE");
        String accountId = createHelperAccount(customerId);

        request.setContent("{\"status\": \"FROZEN\"}".getBytes("UTF-8"));
        request.setContentType("application/json");
        request.setMethod("PUT"); 

        executeAction("/api/v1/accounts/" + accountId);
        AccountsController action = (AccountsController) getAction();
        assertEquals(200, action.getStatus()); 
        Account a = (Account) action.getModel();
        assertEquals("FROZEN", a.getStatus());
    }

    // --- Transaction Tests ---

    @Test
    public void test05_DepositSuccess() throws Exception {
        String customerId = createHelperCustomer("Dave", "dave@example.com", "CIF-DAVE");
        String accountId = createHelperAccount(customerId);

        String json = String.format("{\"accountId\": \"%s\", \"transactionType\": \"DEPOSIT\", \"amount\": 500.00, \"currencyCode\": \"USD\", \"description\": \"Opening\"}", accountId);
        request.setContent(json.getBytes("UTF-8"));
        request.setContentType("application/json");
        request.setMethod("POST");
        
        executeAction("/api/v1/transactions");
        TransactionsController action = (TransactionsController) getAction();
        assertEquals(201, action.getStatus());
        Transaction t = (Transaction) action.getModel();
        assertEquals(0, new BigDecimal("500.00").compareTo(t.getBalanceAfter()));
    }

    @Test
    public void test06_WithdrawalInsufficient() throws Exception {
        String customerId = createHelperCustomer("Eve", "eve@example.com", "CIF-EVE");
        String accountId = createHelperAccount(customerId);
        
        // Deposit 100 via Service
        createHelperTransaction(accountId, "DEPOSIT", 100.00);

        // Withdraw 200 via API
        String json = String.format("{\"accountId\": \"%s\", \"transactionType\": \"WITHDRAWAL\", \"amount\": 200.00, \"currencyCode\": \"USD\"}", accountId);
        request.setContent(json.getBytes("UTF-8"));
        request.setContentType("application/json");
        request.setMethod("POST");

        executeAction("/api/v1/transactions");
        TransactionsController action = (TransactionsController) getAction();
        assertEquals(400, action.getStatus());
    }
    
    private String createHelperCustomer(String name, String email, String cif) throws Exception {
        Customer c = new Customer();
        c.setFirstName(name);
        c.setLastName("Helper");
        c.setEmail(email);
        c.setDateOfBirth(java.sql.Date.valueOf("1990-01-01"));
        c.setCifNumber(cif);
        return new com.banking.cif.service.BankingService().createCustomer(c).getCustomerId();
    }
    
    private String createHelperAccount(String customerId) throws Exception {
        Account a = new Account();
        a.setCustomerId(customerId);
        a.setProductCode("CHK-STD");
        a.setCurrencyCode("USD");
        return new com.banking.cif.service.BankingService().createAccount(a).getAccountId();
    }
    
    private void createHelperTransaction(String accountId, String type, double amount) throws Exception {
        Transaction t = new Transaction();
        t.setAccountId(accountId);
        t.setTransactionType(type);
        t.setAmount(new BigDecimal(amount));
        t.setCurrencyCode("USD");
        new com.banking.cif.service.BankingService().processTransaction(t);
    }
}