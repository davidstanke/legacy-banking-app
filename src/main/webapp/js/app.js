$(function() {
    
    // --- Models ---
    
    var Customer = Backbone.Model.extend({
        urlRoot: '/api/v1/customers',
        idAttribute: 'customerId',
        defaults: {
            firstName: '',
            lastName: '',
            email: '',
            cifNumber: '',
            dateOfBirth: ''
        },
        parse: function(response) {
            // Struts actions often wrap the result in 'model' or return the action itself
            if (response && response.model) {
                return response.model;
            }
            return response;
        }
    });

    var Account = Backbone.Model.extend({
        urlRoot: '/api/v1/accounts',
        idAttribute: 'accountId',
        defaults: {
            productCode: 'CHK-STD',
            balance: 0.0,
            status: 'OPEN'
        },
        parse: function(response) {
            if (response && response.model) {
                return response.model;
            }
            return response;
        }
    });

    var Transaction = Backbone.Model.extend({
        urlRoot: '/api/v1/transactions',
        idAttribute: 'transactionId',
        defaults: {
            transactionType: 'DEPOSIT',
            amount: 0.0
        },
        parse: function(response) {
             if (response && response.model) {
                return response.model;
            }
            return response;
        }
    });

    // --- Collections ---

    var TransactionsCollection = Backbone.Collection.extend({
        model: Transaction,
        initialize: function(models, options) {
            this.accountId = options.accountId;
        },
        url: function() {
            return '/api/v1/transactions/' + this.accountId;
        },
        parse: function(response) {
             // The custom route might return the list directly or wrapped
             if (response && response.model && Array.isArray(response.model)) {
                 return response.model;
             }
             if (Array.isArray(response)) {
                 return response;
             }
             return response; // Fallback
        }
    });

    // --- Views ---

    var HomeView = Backbone.View.extend({
        el: '#main-container',
        template: _.template($('#home-template').html()),
        render: function() {
            this.$el.html(this.template());
            return this;
        }
    });

    var CustomerView = Backbone.View.extend({
        el: '#main-container',
        template: _.template($('#customer-template').html()),
        events: {
            'click .delete-customer': 'deleteCustomer'
        },
        render: function() {
            this.$el.html(this.template(this.model.toJSON()));
            return this;
        },
        deleteCustomer: function() {
            if(confirm('Are you sure?')) {
                this.model.destroy({
                    success: function() {
                        alert('Customer deleted');
                        app.navigate('', {trigger: true});
                    },
                    error: function(model, response) {
                        alert('Error deleting customer: ' + (response.responseJSON ? response.responseJSON.message : response.statusText));
                    }
                });
            }
        }
    });

    var CreateCustomerView = Backbone.View.extend({
        el: '#main-container',
        template: _.template($('#create-customer-template').html()),
        events: {
            'submit #create-customer-form': 'createCustomer'
        },
        render: function() {
            this.$el.html(this.template());
            return this;
        },
        createCustomer: function(e) {
            e.preventDefault();
            var data = {
                firstName: this.$('input[name="firstName"]').val(),
                lastName: this.$('input[name="lastName"]').val(),
                email: this.$('input[name="email"]').val(),
                dateOfBirth: this.$('input[name="dateOfBirth"]').val(),
                cifNumber: this.$('input[name="cifNumber"]').val(),
                customerId: this.$('input[name="customerId"]').val() // Optional
            };
            if(!data.customerId) delete data.customerId;

            var customer = new Customer();
            customer.save(data, {
                success: function(model, response) {
                    alert('Customer created with ID: ' + model.id); // model.id maps to customerId
                    app.navigate('customers/' + model.id, {trigger: true});
                },
                error: function(model, response) {
                    alert('Error creating customer: ' + (response.responseJSON ? response.responseJSON.message : response.statusText));
                }
            });
        }
    });

    var AccountView = Backbone.View.extend({
        el: '#main-container',
        template: _.template($('#account-template').html()),
        events: {
            'click .close-account': 'closeAccount'
        },
        initialize: function() {
            this.transactions = new TransactionsCollection([], {accountId: this.model.id});
        },
        render: function() {
            var self = this;
            this.$el.html(this.template(this.model.toJSON()));
            
            // Fetch transactions
            this.transactions.fetch({
                success: function(collection) {
                    var tbody = self.$('#transactions-list');
                    tbody.empty();
                    collection.each(function(txn) {
                        var row = new TransactionRowView({model: txn});
                        tbody.append(row.render().el);
                    });
                },
                error: function() {
                    console.log("Error fetching transactions");
                }
            });
            return this;
        },
        closeAccount: function() {
            // Simulate closing by updating status (if API supported it fully)
            // The API supports updating status via PUT/UPDATE
            this.model.save({status: 'CLOSED'}, {
                success: function() {
                    alert('Account closed');
                    Backbone.history.loadUrl(Backbone.history.fragment); // Reload
                },
                error: function(model, response) {
                    alert('Error closing account: ' + (response.responseJSON ? response.responseJSON.message : response.statusText));
                }
            });
        }
    });

    var TransactionRowView = Backbone.View.extend({
        tagName: 'tr',
        template: _.template($('#transaction-row-template').html()),
        render: function() {
            this.$el.html(this.template(this.model.toJSON()));
            return this;
        }
    });

    var CreateAccountView = Backbone.View.extend({
        el: '#main-container',
        template: _.template($('#create-account-template').html()),
        events: {
            'submit #create-account-form': 'createAccount'
        },
        render: function() {
            this.$el.html(this.template());
            return this;
        },
        createAccount: function(e) {
            e.preventDefault();
            var data = {
                customerId: this.$('input[name="customerId"]').val(),
                productCode: this.$('select[name="productCode"]').val(),
                balance: parseFloat(this.$('input[name="balance"]').val())
            };

            var account = new Account();
            account.save(data, {
                success: function(model, response) {
                    alert('Account created');
                    app.navigate('accounts/' + model.id, {trigger: true});
                },
                error: function(model, response) {
                    alert('Error creating account: ' + (response.responseJSON ? response.responseJSON.message : response.statusText));
                }
            });
        }
    });

    var CreateTransactionView = Backbone.View.extend({
        el: '#main-container',
        template: _.template($('#create-transaction-template').html()),
        events: {
            'submit #create-transaction-form': 'createTransaction',
            'change select[name="transactionType"]': 'toggleTargetAccount'
        },
        render: function() {
            this.$el.html(this.template());
            return this;
        },
        toggleTargetAccount: function(e) {
            var type = $(e.target).val();
            if (type === 'TRANSFER') {
                this.$('#target-account-group').show();
            } else {
                this.$('#target-account-group').hide();
            }
        },
        createTransaction: function(e) {
            e.preventDefault();
            var type = this.$('select[name="transactionType"]').val();
            var data = {
                transactionType: type,
                accountId: this.$('input[name="accountId"]').val(),
                amount: parseFloat(this.$('input[name="amount"]').val())
            };
            if (type === 'TRANSFER') {
                data.targetAccountId = this.$('input[name="targetAccountId"]').val();
            }

            var txn = new Transaction();
            txn.save(data, {
                success: function(model, response) {
                    alert('Transaction processed');
                    app.navigate('accounts/' + data.accountId, {trigger: true});
                },
                error: function(model, response) {
                     alert('Error processing transaction: ' + (response.responseJSON ? response.responseJSON.message : response.statusText));
                }
            });
        }
    });

    var ErrorView = Backbone.View.extend({
        el: '#main-container',
        template: _.template($('#error-template').html()),
        render: function(message) {
            this.$el.html(this.template({message: message}));
            return this;
        }
    });

    // --- Router ---

    var AppRouter = Backbone.Router.extend({
        routes: {
            '': 'home',
            'customers/new': 'createCustomer',
            'customers/:id': 'viewCustomer',
            'accounts/new': 'createAccount',
            'accounts/:id': 'viewAccount',
            'transactions/new': 'createTransaction'
        },

        home: function() {
            new HomeView().render();
        },

        createCustomer: function() {
            new CreateCustomerView().render();
        },

        viewCustomer: function(id) {
            var customer = new Customer();
            // Use a custom URL for the fetch to handle name/id lookup
            customer.url = '/api/v1/customers/' + encodeURIComponent(id);
            customer.fetch({
                success: function() {
                    // Reset url to default for future operations
                    delete customer.url;
                    new CustomerView({model: customer}).render();
                },
                error: function(model, response) {
                    var msg = (response.responseJSON && response.responseJSON.message) ? response.responseJSON.message : 'Customer not found';
                    new ErrorView().render(msg);
                }
            });
        },

        createAccount: function() {
            new CreateAccountView().render();
        },

        viewAccount: function(id) {
            var account = new Account({accountId: id});
            account.fetch({
                success: function() {
                    new AccountView({model: account}).render();
                },
                error: function(model, response) {
                     var msg = (response.responseJSON && response.responseJSON.message) ? response.responseJSON.message : 'Account not found';
                     new ErrorView().render(msg);
                }
            });
        },

        createTransaction: function() {
            new CreateTransactionView().render();
        }
    });

    // --- Initialization ---
    
    var app = new AppRouter();
    Backbone.history.start();

    // Global Navigation Handlers
    $('#lookup-customer-form').submit(function(e) {
        e.preventDefault();
        var id = $('#nav-customer-id').val();
        if(id) app.navigate('customers/' + id, {trigger: true});
    });

    $('#lookup-account-form').submit(function(e) {
        e.preventDefault();
        var id = $('#nav-account-id').val();
        if(id) app.navigate('accounts/' + id, {trigger: true});
    });

});
