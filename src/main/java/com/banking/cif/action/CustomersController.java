package com.banking.cif.action;

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

    // Error response fields
    private int status;
    private String error;
    private String message;

    // GET /api/v1/customers
    public HttpHeaders index() {
        // Not strictly in spec, but good for debug
        // list = service.getAllCustomers(); 
        return new DefaultHttpHeaders("index").disableCaching();
    }

    // GET /api/v1/customers/{id}
    public HttpHeaders show() {
        try {
            model = service.getCustomer(id);
        } catch (Exception e) {
            // If ID not found, try Name
            try {
                List<Customer> customers = service.getCustomersByName(id);
                if (customers != null && !customers.isEmpty()) {
                    model = customers.get(0); // Take the first match for lookup
                } else {
                    status = 404;
                    error = "Not Found";
                    message = "Customer not found by ID or Name: " + id;
                    return new DefaultHttpHeaders("show").withStatus(404);
                }
            } catch (Exception ex) {
                status = 404;
                error = "Not Found";
                message = e.getMessage();
                return new DefaultHttpHeaders("show").withStatus(404);
            }
        }
        return new DefaultHttpHeaders("show").disableCaching();
    }

    // POST /api/v1/customers
    public HttpHeaders create() {
        try {
            service.createCustomer(model);
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
        return (list != null ? list : model);
    }

    // Getters for error response
    public int getStatus() { return status; }
    public String getError() { return error; }
    public String getMessage() { return message; }
}
