package com.banking.cif.action;

import com.banking.cif.model.Account;
import com.banking.cif.service.BankingService;
import com.opensymphony.xwork2.ModelDriven;
import org.apache.struts2.rest.DefaultHttpHeaders;
import org.apache.struts2.rest.HttpHeaders;

public class AccountsController implements ModelDriven<Object> {
    
    private String id;
    private Account model = new Account();
    private BankingService service = new BankingService();

    // Error response fields
    private int status;
    private String error;
    private String message;

    // GET /api/v1/accounts/{id}
    public HttpHeaders show() {
        try {
            model = service.getAccount(id);
        } catch (Exception e) {
            status = 404;
            error = "Not Found";
            message = e.getMessage();
            return new DefaultHttpHeaders("show").withStatus(404);
        }
        return new DefaultHttpHeaders("show").disableCaching();
    }

    // POST /api/v1/accounts
    public HttpHeaders create() {
        try {
            service.createAccount(model);
            status = 201;
            return new DefaultHttpHeaders("create").withStatus(201);
        } catch (Exception e) {
            status = 400;
            error = "Bad Request";
            message = e.getMessage();
            return new DefaultHttpHeaders("create").withStatus(400);
        }
    }

    // PUT/PATCH /api/v1/accounts/{id} (Simulating PATCH /status)
    public HttpHeaders update() {
        try {
            // Check if it's a status update
            if (model.getStatus() != null) {
                service.updateAccountStatus(id, model.getStatus());
                // Refresh model to return updated object
                model = service.getAccount(id);
                return new DefaultHttpHeaders("update").withStatus(200);
            }
            return new DefaultHttpHeaders("update").withStatus(200);
        } catch (Exception e) {
            status = 400;
            error = "Bad Request";
            message = e.getMessage();
            return new DefaultHttpHeaders("update").withStatus(400);
        }
    }

    public void setId(String id) { this.id = id; }
    public String getId() { return id; }

    @Override
    public Object getModel() {
        return model;
    }

    public int getStatus() { return status; }
    public String getError() { return error; }
    public String getMessage() { return message; }
}
