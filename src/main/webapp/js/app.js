$(function() {
    
    // --- Helpers ---
    
    window.showNotification = function(message, type) {
        var $bar = $('#notification-bar');
        $bar.removeClass('notif-success notif-error')
            .addClass(type === 'error' ? 'notif-error' : 'notif-success')
            .text(message)
            .slideDown(200)
            .delay(3000)
            .slideUp(200);
    };

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

    var CustomersCollection = Backbone.Collection.extend({
        model: Customer,
        url: '/api/v1/customers',
        parse: function(response) {
            if (response && response.model && Array.isArray(response.model)) {
                return response.model;
            }
            if (Array.isArray(response)) {
                return response;
            }
            return response;
        }
    });

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
            this.$el.hide().html(this.template()).fadeIn(300);
            return this;
        }
    });

    var CustomerListView = Backbone.View.extend({
        el: '#main-container',
        template: _.template($('#customer-list-template').html()),
        events: {
            'mouseenter tr': 'highlightRow',
            'mouseleave tr': 'unhighlightRow'
        },
        render: function() {
            var self = this;
            this.$el.hide().html(this.template({customers: this.collection.toJSON()})).fadeIn(300);
            return this;
        },
        highlightRow: function(e) {
            $(e.currentTarget).addClass('row-hover');
        },
        unhighlightRow: function(e) {
            $(e.currentTarget).removeClass('row-hover');
        }
    });

    var CustomerView = Backbone.View.extend({
        el: '#main-container',
        template: _.template($('#customer-template').html()),
        events: {
            'click .delete-customer': 'deleteCustomer',
            'submit #inline-create-account-form': 'createAccount'
        },
        render: function() {
            this.$el.hide().html(this.template(this.model.toJSON())).fadeIn(300);
            return this;
        },
        createAccount: function(e) {
            e.preventDefault();
            var $form = $(e.target);
            var $btn = $form.find('button[type="submit"]');
            var originalBtnText = $btn.text();

            var data = {
                customerId: this.$('input[name="customerId"]').val(),
                productCode: this.$('select[name="productCode"]').val(),
                balance: parseFloat(this.$('input[name="balance"]').val())
            };

            $btn.text('Processing...').addClass('btn-loading').prop('disabled', true);
            $form.addClass('form-loading');

            var account = new Account();
            account.save(data, {
                success: function(model, response) {
                    showNotification('Account created successfully', 'success');
                    Backbone.history.loadUrl(Backbone.history.fragment); // Reload
                },
                error: function(model, response) {
                    $btn.text(originalBtnText).removeClass('btn-loading').prop('disabled', false);
                    $form.removeClass('form-loading');
                    showNotification('Error: ' + (response.responseJSON ? response.responseJSON.message : response.statusText), 'error');
                }
            });
        },
        deleteCustomer: function() {
            if(confirm('Are you sure you want to delete this customer?')) {
                this.model.destroy({
                    success: function() {
                        showNotification('Customer deleted', 'success');
                        app.navigate('', {trigger: true});
                    },
                    error: function(model, response) {
                        showNotification('Error: ' + (response.responseJSON ? response.responseJSON.message : response.statusText), 'error');
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
            this.$el.hide().html(this.template()).fadeIn(300);
            return this;
        },
        createCustomer: function(e) {
            e.preventDefault();
            var $form = $(e.target);
            var $btn = $form.find('button[type="submit"]');
            var originalBtnText = $btn.text();

            var data = {
                firstName: this.$('input[name="firstName"]').val(),
                lastName: this.$('input[name="lastName"]').val(),
                email: this.$('input[name="email"]').val(),
                dateOfBirth: this.$('input[name="dateOfBirth"]').val(),
                cifNumber: this.$('input[name="cifNumber"]').val(),
                customerId: this.$('input[name="customerId"]').val() // Optional
            };
            if(!data.customerId) delete data.customerId;

            $btn.text('Registering...').addClass('btn-loading').prop('disabled', true);
            $form.addClass('form-loading');

            var customer = new Customer();
            customer.save(data, {
                success: function(model, response) {
                    showNotification('Customer registered successfully', 'success');
                    app.navigate('customers/' + model.id, {trigger: true});
                },
                error: function(model, response) {
                    $btn.text(originalBtnText).removeClass('btn-loading').prop('disabled', false);
                    $form.removeClass('form-loading');
                    showNotification('Error: ' + (response.responseJSON ? response.responseJSON.message : response.statusText), 'error');
                }
            });
        }
    });

    var AccountView = Backbone.View.extend({
        el: '#main-container',
        template: _.template($('#account-template').html()),
        events: {
            'click .close-account': 'closeAccount',
            'submit #inline-create-transaction-form': 'createTransaction',
            'change #inline-create-transaction-form select[name="transactionType"]': 'toggleTargetAccount'
        },
        initialize: function() {
            this.transactions = new TransactionsCollection([], {accountId: this.model.id});
        },
        render: function() {
            var self = this;
            this.$el.hide().html(this.template(this.model.toJSON())).fadeIn(300);
            
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
        toggleTargetAccount: function(e) {
            var type = $(e.target).val();
            if (type === 'TRANSFER') {
                this.$('#inline-target-account-group').slideDown(200);
            } else {
                this.$('#inline-target-account-group').slideUp(200);
            }
        },
        createTransaction: function(e) {
            e.preventDefault();
            var $form = $(e.target);
            var $btn = $form.find('button[type="submit"]');
            var originalBtnText = $btn.text();

            var type = this.$('#inline-create-transaction-form select[name="transactionType"]').val();
            var data = {
                transactionType: type,
                accountId: this.$('#inline-create-transaction-form input[name="accountId"]').val(),
                amount: parseFloat(this.$('#inline-create-transaction-form input[name="amount"]').val())
            };
            if (type === 'TRANSFER') {
                data.targetAccountId = this.$('#inline-create-transaction-form input[name="targetAccountId"]').val();
            }

            $btn.text('Processing...').addClass('btn-loading').prop('disabled', true);
            $form.addClass('form-loading');

            var txn = new Transaction();
            txn.save(data, {
                success: function(model, response) {
                    showNotification('Transaction completed', 'success');
                    Backbone.history.loadUrl(Backbone.history.fragment); // Reload
                },
                error: function(model, response) {
                    $btn.text(originalBtnText).removeClass('btn-loading').prop('disabled', false);
                    $form.removeClass('form-loading');
                    showNotification('Error: ' + (response.responseJSON ? response.responseJSON.message : response.statusText), 'error');
                }
            });
        },
        closeAccount: function() {
            if(confirm('Are you sure you want to deactivate this account?')) {
                this.model.save({status: 'CLOSED'}, {
                    success: function() {
                        showNotification('Account deactivated', 'success');
                        Backbone.history.loadUrl(Backbone.history.fragment); // Reload
                    },
                    error: function(model, response) {
                        showNotification('Error: ' + (response.responseJSON ? response.responseJSON.message : response.statusText), 'error');
                    }
                });
            }
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
            this.$el.hide().html(this.template()).fadeIn(300);
            return this;
        },
        createAccount: function(e) {
            e.preventDefault();
            var $form = $(e.target);
            var $btn = $form.find('button[type="submit"]');
            var originalBtnText = $btn.text();

            var data = {
                customerId: this.$('input[name="customerId"]').val(),
                productCode: this.$('select[name="productCode"]').val(),
                balance: parseFloat(this.$('input[name="balance"]').val())
            };

            $btn.text('Opening...').addClass('btn-loading').prop('disabled', true);
            $form.addClass('form-loading');

            var account = new Account();
            account.save(data, {
                success: function(model, response) {
                    showNotification('New account opened', 'success');
                    app.navigate('accounts/' + model.id, {trigger: true});
                },
                error: function(model, response) {
                    $btn.text(originalBtnText).removeClass('btn-loading').prop('disabled', false);
                    $form.removeClass('form-loading');
                    showNotification('Error: ' + (response.responseJSON ? response.responseJSON.message : response.statusText), 'error');
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
            this.$el.hide().html(this.template()).fadeIn(300);
            return this;
        },
        toggleTargetAccount: function(e) {
            var type = $(e.target).val();
            if (type === 'TRANSFER') {
                this.$('#target-account-group').slideDown(200);
            } else {
                this.$('#target-account-group').slideUp(200);
            }
        },
        createTransaction: function(e) {
            e.preventDefault();
            var $form = $(e.target);
            var $btn = $form.find('button[type="submit"]');
            var originalBtnText = $btn.text();

            var type = this.$('select[name="transactionType"]').val();
            var data = {
                transactionType: type,
                accountId: this.$('input[name="accountId"]').val(),
                amount: parseFloat(this.$('input[name="amount"]').val())
            };
            if (type === 'TRANSFER') {
                data.targetAccountId = this.$('input[name="targetAccountId"]').val();
            }

            $btn.text('Processing...').addClass('btn-loading').prop('disabled', true);
            $form.addClass('form-loading');

            var txn = new Transaction();
            txn.save(data, {
                success: function(model, response) {
                    showNotification('Transaction completed', 'success');
                    app.navigate('accounts/' + data.accountId, {trigger: true});
                },
                error: function(model, response) {
                    $btn.text(originalBtnText).removeClass('btn-loading').prop('disabled', false);
                    $form.removeClass('form-loading');
                    showNotification('Error: ' + (response.responseJSON ? response.responseJSON.message : response.statusText), 'error');
                }
            });
        }
    });

    var ErrorView = Backbone.View.extend({
        el: '#main-container',
        template: _.template($('#error-template').html()),
        render: function(message) {
            this.$el.hide().html(this.template({message: message})).fadeIn(300);
            return this;
        }
    });

    // --- Router ---

    var AppRouter = Backbone.Router.extend({
        routes: {
            '': 'home',
            'customers': 'listCustomers',
            'customers/new': 'createCustomer',
            'customers/:id': 'viewCustomer',
            'accounts/new': 'createAccount',
            'accounts/:id': 'viewAccount',
            'transactions/new': 'createTransaction'
        },

        home: function() {
            new HomeView().render();
        },

        listCustomers: function() {
            var customers = new CustomersCollection();
            customers.fetch({
                success: function() {
                    new CustomerListView({collection: customers}).render();
                },
                error: function(collection, response) {
                    var msg = (response.responseJSON && response.responseJSON.message) ? response.responseJSON.message : 'Error loading customers';
                    new ErrorView().render(msg);
                }
            });
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
