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
        this.requestOptions = requestOptions || {method:'get'};
        this.requestOptions.url = url;
        this.requestOptions.update = this.content;
        this.requestOptions.evalScripts = true;
        this.addEvent('open', function() {
            var request = new Request.HTML(this.requestOptions).send();
            MooDialog.Request.active = this;
        }.bind(this));

        if (this.options.autoOpen) this.open();

    },

    setRequestOptions: function(options) {
        this.requestOptions = Object.merge(this.requestOptions, options);
        return this;
    }

});
