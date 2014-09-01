BootstrapDialog.Request = new Class({

    Extends: BootstrapDialog,

    initialize: function(url, requestOptions, options) {
        //this.parent(options);
        this.requestOptions = requestOptions || {};
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

        var request = $('<div></div>');
        request.load(url, function() {
            me._native = $(this).find('.modal');
            if (!me._native) {
                throw new Error('modal not found');
            } else {
                $(document.body).append(me._native);
                me.show();
            }
        });

        this._native.on('hidden.bs.modal', function () {
            $(this).remove();
        });

    }


});
