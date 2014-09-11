BootstrapDialog.Request = new Class({

    Extends: BootstrapDialog,

    initialize: function(url, title, options) {
        this.parent(title, options);
        this.requestOptions = {};
        this.requestOptions.cache = false;
        var me = this;
        this.requestOptions.fail = function(xhr) {
            // Intercept form requests ...
            console.log("Failure:");
            console.log(xhr);
        };

        this.requestOptions.success = function() {
            // Intercept form requests ...
            var forms = me._native.find('form');
            _.each(forms, function(form) {
                $(form).on('submit', function(event) {
                    // Intercept form ...
                    me.requestOptions.url = form.action;
                    me.requestOptions.method = form.method ? form.method : 'post';
                    $.ajax(me.requestOptions);
                    event.stopPropagation();
                    return false;
                });
            });
        };

        this._native.find('.modal-body').load(url, function () {
            me.acceptButton.unbind('click').click(function () {
                submitDialogForm();
            });
            me._native.on('hidden.bs.modal', function () {
                $(this).remove();
            });
            me.show();
        });
    },

    onDialogShown: function() {
        if (typeof(onDialogShown) == "function") {
            onDialogShown();
        }
    }


});
