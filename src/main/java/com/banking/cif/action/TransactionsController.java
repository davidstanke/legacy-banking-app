package com.banking.cif.action;

import com.banking.cif.model.Transaction;
import com.banking.cif.service.BankingService;
import com.opensymphony.xwork2.ModelDriven;
import org.apache.struts2.rest.DefaultHttpHeaders;
import org.apache.struts2.rest.HttpHeaders;

import java.util.Collection;
import java.util.List;

public class TransactionsController implements ModelDriven<Object> {
    
    private String id; // transaction id or account id in custom route
    private Transaction model = new Transaction();
    private Collection<Transaction> list = null;
    private BankingService service = new BankingService();
    
    // Parameter for listByAccount (legacy, can be removed if show() works)
    private String accountId;

    // Error response fields
    private int status;
    private String error;
    private String message;

    // GET /api/v1/transactions
    public HttpHeaders index() {
        return new DefaultHttpHeaders("index").disableCaching();
    }

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
    
    // GET /api/v1/transactions/account/{id}
    public HttpHeaders show() {
        try {
            Integer intAccountId = Integer.parseInt(id);
            list = service.getTransactions(intAccountId);
            return new DefaultHttpHeaders("show").disableCaching();
        } catch (Exception e) {
             status = 400;
             error = "Error";
             message = e.getMessage();
             return new DefaultHttpHeaders("show").withStatus(400);
        }
    }

    public void setId(String id) { this.id = id; }
    public String getId() { return id; }

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
