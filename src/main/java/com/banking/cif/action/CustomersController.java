package com.banking.cif.action;

import com.banking.cif.dao.CustomerDAO;
import com.banking.cif.model.Customer;
import com.banking.cif.service.BankingService;
import com.banking.cif.util.DatabaseInitializer;
import com.opensymphony.xwork2.ModelDriven;
import org.apache.struts2.rest.DefaultHttpHeaders;
import org.apache.struts2.rest.HttpHeaders;

import java.util.Collection;
import java.util.List;

public class CustomersController implements ModelDriven<Object> {
    
    // Initialize DB on first load (lazy init via static block in DBConnection or here)
    static {
        DatabaseInitializer.initialize();
    }

    private String id;
    private Customer model = new Customer();
    private Collection<Customer> list = null;
    private BankingService service = new BankingService();
    private CustomerDAO customerDAO = new CustomerDAO();

    // Error response fields
    private int status;
    private String error;
    private String message;

    // GET /api/v1/customers
    public HttpHeaders index() {
        try {
            // Bad Practice: Direct DAO access in Controller
            list = customerDAO.findAllWithAccountCount();
        } catch (Exception e) {
            status = 500;
            error = "Internal Server Error";
            message = e.getMessage();
            return new DefaultHttpHeaders("index").withStatus(500);
        }
        return new DefaultHttpHeaders("index").disableCaching();
    }

    // GET /api/v1/customers/{id}
    public HttpHeaders show() {
        if (id == null || id.isEmpty()) {
            status = 400;
            error = "Bad Request";
            message = "Customer ID is missing";
            return new DefaultHttpHeaders("show").withStatus(400);
        }

        // Clean up ID (strip potential extensions or trailing dots/spaces)
        String cleanId = id.trim();
        if (cleanId.endsWith(".json")) {
            cleanId = cleanId.substring(0, cleanId.length() - 5);
        }

        try {
            Integer intId = Integer.parseInt(cleanId);
            model = service.getCustomer(intId);
            if (model != null) {
                model.setAccounts(service.getAccountsByCustomerId(model.getCustomerId()));
            }
        } catch (NumberFormatException nfe) {
            // If ID not found, try Name
            try {
                List<Customer> customers = service.getCustomersByName(cleanId);
                if (customers != null && !customers.isEmpty()) {
                    model = customers.get(0); // Take the first match for lookup
                    model.setAccounts(service.getAccountsByCustomerId(model.getCustomerId()));
                } else {
                    status = 404;
                    error = "Not Found";
                    message = "Customer not found by ID or Name: " + cleanId;
                    return new DefaultHttpHeaders("show").withStatus(404);
                }
            } catch (Exception ex) {
                status = 404;
                error = "Not Found";
                message = ex.getMessage();
                return new DefaultHttpHeaders("show").withStatus(404);
            }
        } catch (Exception e) {
            status = 404;
            error = "Not Found";
            message = e.getMessage();
            return new DefaultHttpHeaders("show").withStatus(404);
        }
        return new DefaultHttpHeaders("show").disableCaching();
    }

    // POST /api/v1/customers
    public HttpHeaders create() {
        try {
            // Bad Practice: Logic in Controller
            if (customerDAO.emailExists(model.getEmail())) {
                throw new Exception("Email already exists");
            }
            
            // Bad Practice: Generation logic in Controller
            if (model.getCifNumber() == null || model.getCifNumber().isEmpty()) {
                 model.setCifNumber("CIF-" + System.currentTimeMillis());
            }

            customerDAO.create(model);
            status = 201;
            return new DefaultHttpHeaders("create").withStatus(201);
        } catch (Exception e) {
            status = 400;
            error = "Bad Request";
            message = e.getMessage();
            return new DefaultHttpHeaders("create").withStatus(400);
        }
    }

    // PUT /api/v1/customers/{id}
    public HttpHeaders update() {
        // Spec: Update Person
        // Implementation: service.updateCustomer(id, model);
        // Using create/save for now as placeholder or need new service method
        // Assuming update updates fields.
        return new DefaultHttpHeaders("update");
    }

    // DELETE /api/v1/customers/{id}
    public HttpHeaders destroy() {
        // service.deleteCustomer(id);
        return new DefaultHttpHeaders("destroy").withStatus(204);
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

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

    // Getters for error response
    public int getStatus() { return status; }
    public String getError() { return error; }
    public String getMessage() { return message; }
}
