package com.banking.cif.action;

import com.banking.cif.dao.AccountDAO;
import com.banking.cif.dao.CustomerDAO;
import com.banking.cif.dao.ProductDAO;
import com.banking.cif.model.Account;
import com.banking.cif.model.Product;
import com.banking.cif.service.BankingService;
import com.opensymphony.xwork2.ModelDriven;
import org.apache.struts2.rest.DefaultHttpHeaders;
import org.apache.struts2.rest.HttpHeaders;

public class AccountsController implements ModelDriven<Object> {
    
    // Ensure DB is initialized
    static {
        com.banking.cif.util.DatabaseInitializer.initialize();
    }

    private String id;
    private Account model = new Account();
    private BankingService service = new BankingService();
    private AccountDAO accountDAO = new AccountDAO();
    private ProductDAO productDAO = new ProductDAO();
    private CustomerDAO customerDAO = new CustomerDAO();

    // Error response fields
    private int status;
    private String error;
    private String message;

    // GET /api/v1/accounts/{id}
    public HttpHeaders show() {
        System.out.println("AccountsController.show() called with id: [" + id + "]");
        if (id == null || id.isEmpty()) {
            status = 400;
            error = "Bad Request";
            message = "Account ID is missing";
            return new DefaultHttpHeaders("show").withStatus(400);
        }
        
        // Clean up ID (strip potential extensions or trailing dots/spaces)
        String cleanId = id.trim();
        if (cleanId.endsWith(".json")) {
            cleanId = cleanId.substring(0, cleanId.length() - 5);
        }
        System.out.println("AccountsController.show() cleaned id: [" + cleanId + "]");

        try {
            Integer intId = Integer.parseInt(cleanId);
            model = service.getAccount(intId);
            System.out.println("AccountsController.show() found account: " + model.getAccountId());
        } catch (NumberFormatException nfe) {
            System.err.println("AccountsController.show() invalid ID format: " + cleanId);
            status = 400;
            error = "Bad Request";
            message = "Invalid Account ID format: " + cleanId;
            return new DefaultHttpHeaders("show").withStatus(400);
        } catch (Exception e) {
            System.err.println("AccountsController.show() account not found: " + e.getMessage());
            status = 404;
            error = "Not Found";
            message = "Account not found for ID: " + cleanId;
            return new DefaultHttpHeaders("show").withStatus(404);
        }
        return new DefaultHttpHeaders("show").disableCaching();
    }

    // POST /api/v1/accounts
    public HttpHeaders create() {
        try {
            // Bad Practice: Business Logic in Controller
            Product p = productDAO.findByCode(model.getProductCode());
            if (p == null) {
                throw new Exception("Invalid Product Code");
            }
            // validate customer exists
            if (customerDAO.findById(model.getCustomerId()) == null) {
                throw new Exception("Customer not found");
            }

            accountDAO.create(model);
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
            Integer intId = Integer.parseInt(id);
            // Check if it's a status update
            if (model.getStatus() != null) {
                // Bad Practice: Direct DAO Call
                accountDAO.updateStatus(intId, model.getStatus());
                // Refresh model to return updated object
                model = accountDAO.findById(intId);
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
        if (message != null) {
            java.util.Map<String, Object> errorResponse = new java.util.HashMap<>();
            errorResponse.put("status", status);
            errorResponse.put("error", error);
            errorResponse.put("message", message);
            return errorResponse;
        }
        return model;
    }

    public int getStatus() { return status; }
    public String getError() { return error; }
    public String getMessage() { return message; }
}
