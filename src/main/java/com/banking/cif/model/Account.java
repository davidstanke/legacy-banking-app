package com.banking.cif.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class Account {
    private Integer accountId; // Integer ID
    private Integer customerId; // Integer ID
    private String productCode;
    private String accountNumber;
    private String iban;
    private BigDecimal balance;
    private String currencyCode;
    private BigDecimal overdraftLimit;
    private String status;
    private Timestamp openedAt;
    private Timestamp closedAt;
    private String configurations; // JSON

    public Integer getAccountId() { return accountId; }
    public void setAccountId(Integer accountId) { this.accountId = accountId; }

    public Integer getCustomerId() { return customerId; }
    public void setCustomerId(Integer customerId) { this.customerId = customerId; }

    public String getProductCode() { return productCode; }
    public void setProductCode(String productCode) { this.productCode = productCode; }

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    public String getIban() { return iban; }
    public void setIban(String iban) { this.iban = iban; }

    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }

    public String getCurrencyCode() { return currencyCode; }
    public void setCurrencyCode(String currencyCode) { this.currencyCode = currencyCode; }

    public BigDecimal getOverdraftLimit() { return overdraftLimit; }
    public void setOverdraftLimit(BigDecimal overdraftLimit) { this.overdraftLimit = overdraftLimit; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Timestamp getOpenedAt() { return openedAt; }
    public void setOpenedAt(Timestamp openedAt) { this.openedAt = openedAt; }

    public Timestamp getClosedAt() { return closedAt; }
    public void setClosedAt(Timestamp closedAt) { this.closedAt = closedAt; }

    public String getConfigurations() { return configurations; }
    public void setConfigurations(String configurations) { this.configurations = configurations; }
}
