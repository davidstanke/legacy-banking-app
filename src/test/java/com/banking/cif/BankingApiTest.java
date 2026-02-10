package com.banking.cif;

import com.banking.cif.action.AccountsController;
import com.banking.cif.action.CustomersController;
import com.banking.cif.action.TransactionsController;
import com.banking.cif.model.Account;
import com.banking.cif.model.Customer;
import com.banking.cif.model.Transaction;
import com.banking.cif.util.DatabaseInitializer;
import org.junit.Before;
import org.junit.Test;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;

import java.math.BigDecimal;
import java.sql.Date;

import static org.junit.Assert.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class BankingApiTest {

    @Before
    public void setUp() throws Exception {
        DatabaseInitializer.initialize();
    }

    // --- Customer Tests ---

    @Test
    public void test01_CreateCustomerSuccess() throws Exception {
        CustomersController action = new CustomersController();
        Customer c = (Customer) action.getModel();
        c.setFirstName("Alice");
        c.setLastName("Smith");
        c.setEmail("alice.api@example.com");
        c.setDateOfBirth(Date.valueOf("1990-01-01"));
        c.setCifNumber("CIF-API-001");
        
        action.create();
        assertEquals(201, action.getStatus());
        assertNotNull(c.getCustomerId());
    }

    @Test
    public void test02_CreateCustomerValidationError() throws Exception {
        CustomersController action = new CustomersController();
        Customer c = (Customer) action.getModel();
        c.setFirstName("Bob");
        c.setLastName("Jones");
        // Missing Email
        c.setCifNumber("CIF-API-002");

        action.create();
        assertEquals(400, action.getStatus());
    }

    // --- Account Tests ---

    @Test
    public void test03_CreateAccountSuccess() throws Exception {
        Integer customerId = createHelperCustomer("Bob", "bob.api@example.com", "CIF-BOB-API");
        
        AccountsController action = new AccountsController();
        Account a = (Account) action.getModel();
        a.setCustomerId(customerId);
        a.setProductCode("CHK-STD");
        a.setCurrencyCode("USD");

        action.create();
        assertEquals(201, action.getStatus());
        Integer accountId = a.getAccountId();
        assertNotNull(accountId);
        assertEquals("ACTIVE", a.getStatus());

        // Test Show
        AccountsController showAction = new AccountsController();
        showAction.setId(String.valueOf(accountId));
        showAction.show();
        Account retrieved = (Account) showAction.getModel();
        assertEquals(accountId, retrieved.getAccountId());
    }
    
    @Test
    public void test04_UpdateAccountStatus() throws Exception {
        Integer customerId = createHelperCustomer("Charlie", "charlie.api@example.com", "CIF-CHARLIE-API");
        Integer accountId = createHelperAccount(customerId);

        AccountsController action = new AccountsController();
        action.setId(String.valueOf(accountId));
        Account a = (Account) action.getModel();
        a.setStatus("FROZEN");

        org.apache.struts2.rest.HttpHeaders headers = action.update();
        assertEquals(200, headers.getStatus()); 
        
        // Reload to verify
        action.show();
        assertEquals("FROZEN", ((Account)action.getModel()).getStatus());
    }

    // --- Transaction Tests ---

    @Test
    public void test05_DepositSuccess() throws Exception {
        Integer customerId = createHelperCustomer("Dave", "dave.api@example.com", "CIF-DAVE-API");
        Integer accountId = createHelperAccount(customerId);

        TransactionsController action = new TransactionsController();
        Transaction t = (Transaction) action.getModel();
        t.setAccountId(accountId);
        t.setTransactionType("DEPOSIT");
        t.setAmount(new BigDecimal("500.00"));
        t.setCurrencyCode("USD");
        
        action.create();
        assertEquals(201, action.getStatus());
        assertEquals(0, new BigDecimal("500.00").compareTo(t.getBalanceAfter()));
    }

    @Test
    public void test06_WithdrawalInsufficient() throws Exception {
        Integer customerId = createHelperCustomer("Eve", "eve.api@example.com", "CIF-EVE-API");
        Integer accountId = createHelperAccount(customerId);
        
        // Deposit 100 via Service
        createHelperTransaction(accountId, "DEPOSIT", 100.00);

        // Withdraw 200 via API
        TransactionsController action = new TransactionsController();
        Transaction t = (Transaction) action.getModel();
        t.setAccountId(accountId);
        t.setTransactionType("WITHDRAWAL");
        t.setAmount(new BigDecimal("200.00"));
        t.setCurrencyCode("USD");

        action.create();
        assertEquals(400, action.getStatus());
    }

    @Test
    public void test07_RetrievePreSeededAccount() throws Exception {
        AccountsController action = new AccountsController();
        action.setId("1");
        action.show();
        
        assertEquals(0, action.getStatus()); // 0 is default if no error set
        Account a = (Account) action.getModel();
        assertEquals(Integer.valueOf(1), a.getAccountId());
        assertEquals(Integer.valueOf(1), a.getCustomerId());
    }
    
    private Integer createHelperCustomer(String name, String email, String cif) throws Exception {
        Customer c = new Customer();
        c.setFirstName(name);
        c.setLastName("Helper");
        c.setEmail(email);
        c.setDateOfBirth(java.sql.Date.valueOf("1990-01-01"));
        c.setCifNumber(cif);
        return new com.banking.cif.service.BankingService().createCustomer(c).getCustomerId();
    }
    
    private Integer createHelperAccount(Integer customerId) throws Exception {
        Account a = new Account();
        a.setCustomerId(customerId);
        a.setProductCode("CHK-STD");
        a.setCurrencyCode("USD");
        return new com.banking.cif.service.BankingService().createAccount(a).getAccountId();
    }
    
    private void createHelperTransaction(Integer accountId, String type, double amount) throws Exception {
        Transaction t = new Transaction();
        t.setAccountId(accountId);
        t.setTransactionType(type);
        t.setAmount(new BigDecimal(amount));
        t.setCurrencyCode("USD");
        new com.banking.cif.service.BankingService().processTransaction(t);
    }
}
