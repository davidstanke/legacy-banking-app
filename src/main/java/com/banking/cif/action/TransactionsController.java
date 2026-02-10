package com.banking.cif.action;

import com.banking.cif.model.Transaction;
import com.banking.cif.service.BankingService;
import com.opensymphony.xwork2.ModelDriven;
import org.apache.struts2.rest.DefaultHttpHeaders;
import org.apache.struts2.rest.HttpHeaders;

import java.util.Collection;
import java.util.List;

public class TransactionsController implements ModelDriven<Object> {
    
    private String id; // transaction id
    private Transaction model = new Transaction();
    private Collection<Transaction> list = null;
    private BankingService service = new BankingService();
    
    // Parameter for listByAccount
    private String accountId;

    // Error response fields
    private int status;
    private String error;
    private String message;

    // POST /api/v1/transactions
    public HttpHeaders create() {
        try {
            service.processTransaction(model);
            status = 201;
            return new DefaultHttpHeaders("create").withStatus(201);
        } catch (Exception e) {
            status = 400;
            error = "Bad Request";
            message = e.getMessage();
            return new DefaultHttpHeaders("create").withStatus(400);
        }
    }
    
    // Custom method for GET /api/v1/transactions/account/{accountId}
    public String listByAccount() {
        try {
            Integer intAccountId = Integer.parseInt(accountId);
            list = service.getTransactions(intAccountId);
            return "index"; // Maps to JSON result
        } catch (Exception e) {
             status = 400;
             error = "Error";
             message = e.getMessage();
             return "error";
        }
    }

    public void setId(String id) { this.id = id; }
    public String getId() { return id; }

    public void setAccountId(String accountId) { this.accountId = accountId; }
    public String getAccountId() { return accountId; }

    @Override
    public Object getModel() {
        if (message != null) {
            java.util.Map<String, Object> errorResponse = new java.util.HashMap<>();
            errorResponse.put("status", status);
            errorResponse.put("error", error);
            errorResponse.put("message", message);
            return errorResponse;
        }
        return (list != null ? list : model);
    }

    public int getStatus() { return status; }
    public String getError() { return error; }
    public String getMessage() { return message; }
}
