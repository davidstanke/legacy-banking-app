package com.banking.cif;

import com.banking.cif.model.Account;
import com.banking.cif.model.Customer;
import com.banking.cif.model.Transaction;
import com.banking.cif.service.BankingService;
import com.banking.cif.util.DatabaseInitializer;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.sql.Date;

import static org.junit.Assert.*;

public class BankingServiceTest {

    private BankingService service;

    @Before
    public void setUp() {
        DatabaseInitializer.initialize();
        service = new BankingService();
    }

    @Test
    public void testCreateCustomer() throws Exception {
        Customer c = new Customer();
        c.setFirstName("Alice");
        c.setLastName("Smith");
        c.setEmail("alice.service@example.com");
        c.setDateOfBirth(Date.valueOf("1990-01-01"));
        c.setCifNumber("CIF-SERVICE-001");
        
        Customer created = service.createCustomer(c);
        assertNotNull(created.getCustomerId());
        assertEquals("Alice", created.getFirstName());
    }

    @Test
    public void testCreateAccount() throws Exception {
        Customer c = new Customer();
        c.setFirstName("Bob");
        c.setLastName("Jones");
        c.setEmail("bob.service@example.com");
        c.setDateOfBirth(Date.valueOf("1990-01-01"));
        c.setCifNumber("CIF-SERVICE-002");
        Integer customerId = service.createCustomer(c).getCustomerId();

        Account a = new Account();
        a.setCustomerId(customerId);
        a.setProductCode("CHK-STD");
        
        Account created = service.createAccount(a);
        assertNotNull(created.getAccountId());
        assertEquals("ACTIVE", created.getStatus());
    }

    @Test
    public void testTransaction() throws Exception {
        Customer c = new Customer();
        c.setFirstName("Charlie");
        c.setLastName("Brown");
        c.setEmail("charlie.service@example.com");
        c.setDateOfBirth(Date.valueOf("1990-01-01"));
        c.setCifNumber("CIF-SERVICE-003");
        Integer customerId = service.createCustomer(c).getCustomerId();

        Account a = new Account();
        a.setCustomerId(customerId);
        a.setProductCode("CHK-STD");
        Integer accountId = service.createAccount(a).getAccountId();

        // Deposit
        Transaction t1 = new Transaction();
        t1.setAccountId(accountId);
        t1.setTransactionType("DEPOSIT");
        t1.setAmount(new BigDecimal("100.00"));
        
        Transaction result1 = service.processTransaction(t1);
        assertEquals(0, new BigDecimal("100.00").compareTo(result1.getBalanceAfter()));

        // Withdrawal
        Transaction t2 = new Transaction();
        t2.setAccountId(accountId);
        t2.setTransactionType("WITHDRAWAL");
        t2.setAmount(new BigDecimal("40.00"));
        
        Transaction result2 = service.processTransaction(t2);
        assertEquals(0, new BigDecimal("60.00").compareTo(result2.getBalanceAfter()));
    }

    @Test
    public void testGetAccountsByCustomerId() throws Exception {
        Customer c = new Customer();
        c.setFirstName("Eve");
        c.setLastName("Online");
        c.setEmail("eve.service@example.com");
        c.setDateOfBirth(Date.valueOf("1990-01-01"));
        c.setCifNumber("CIF-SERVICE-005");
        Integer customerId = service.createCustomer(c).getCustomerId();

        Account a1 = new Account();
        a1.setCustomerId(customerId);
        a1.setProductCode("CHK-STD");
        service.createAccount(a1);

        Account a2 = new Account();
        a2.setCustomerId(customerId);
        a2.setProductCode("SAV-HYS");
        service.createAccount(a2);

        java.util.List<Account> accounts = service.getAccountsByCustomerId(customerId);
        assertEquals(2, accounts.size());
    }

    @Test(expected = Exception.class)
    public void testWithdrawalInsufficient() throws Exception {
        Customer c = new Customer();
        c.setFirstName("Dave");
        c.setLastName("Miller");
        c.setEmail("dave.service@example.com");
        c.setDateOfBirth(Date.valueOf("1990-01-01"));
        c.setCifNumber("CIF-SERVICE-004");
        Integer customerId = service.createCustomer(c).getCustomerId();

        Account a = new Account();
        a.setCustomerId(customerId);
        a.setProductCode("CHK-STD");
        Integer accountId = service.createAccount(a).getAccountId();

        Transaction t = new Transaction();
        t.setAccountId(accountId);
        t.setTransactionType("WITHDRAWAL");
        t.setAmount(new BigDecimal("100.00"));
        
        service.processTransaction(t); // Should throw Exception
    }
}
