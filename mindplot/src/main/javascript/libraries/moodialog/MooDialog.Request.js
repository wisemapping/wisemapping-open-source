/*
 ---
 name: MooDialog.Request
 description: Loads Data into a Dialog with Request
 authors: Arian Stolwijk
 license:  MIT-style license
 requires: [MooDialog, Core/Request.HTML]
 provides: MooDialog.Request
 ...
 */

MooDialog.Request = new Class({

    Extends: MooDialog,

    initialize: function(url, requestOptions, options) {
        this.parent(options);
        this.requestOptions = requestOptions || {};
        this.requestOptions.update = this.content;
        this.requestOptions.evalScripts = true;
        this.requestOptions.noCache = true;

        this.requestOptions.onFailure = function(xhr) {
            // Intercept form requests ...
            console.log("Failure:");
            console.log(xhr);
        }.bind(this);

        this.requestOptions.onSuccess = function() {
            // Intercept form requests ...
            var forms = this.content.getElements('form');
            forms.forEach(function(form) {
                form.addEvent('submit', function(event) {
                    // Intercept form ...
                    this.requestOptions.url = form.action;
                    this.requestOptions.method = form.method ? form.method : 'post';
                    var request = new Request.HTML(this.requestOptions);
                    request.post(form);
                    event.stopPropagation();
                    return false;
                }.bind(this))
            }.bind(this));
        }.bind(this);

        this.addEvent('open', function() {
            this.requestOptions.url = url;
            this.requestOptions.method = 'get';
            var request = new Request.HTML(this.requestOptions);
            request.send();

            MooDialog.Request.active = this;
        }.bind(this));

        this.addEvent('close', function() {
            MooDialog.Request.active = null;
        }.bind(this));

        if (this.options.autoOpen) this.open();
    },

    setRequestOptions: function(options) {
        this.requestOptions = Object.merge(this.requestOptions, options);
        return this;
    }

});
