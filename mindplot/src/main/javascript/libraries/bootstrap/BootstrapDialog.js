var BootstrapDialog = new Class({
    Implements: Options,

    options: {
        cancelButton: false,
        closeButton: false,
        acceptButton: true,
        removeButton:false,
        errorMessage: false,
        onEventData:{}
    },

    initialize: function (title, options) {
        this.setOptions(options);
        this.options.onEventData.dialog = this;
        this._native = $('<div class="modal fade" tabindex="-1"></div>').append('<div class="modal-dialog"></div>');
        var content = $('<div class="modal-content"></div>');
        var header = this._buildHeader(title);
        if (header) {
            content.append(header);
        }
        var body = $('<div class="modal-body"></div>');
        if(this.options.errorMessage){
            var error = $('<div class="alert alert-danger"></div>');
            error.hide();
            body.append(error);
        }
        content.append(body);
        var footer = this._buildFooter();
        if (footer) {
            content.append(footer);
        }
        this._native.find(".modal-dialog").append(content);
        this._native.on('hidden.bs.modal', function() {
            $(this).remove();
        });
        this._native.on('shown.bs.modal', this.onDialogShown);
    },

    _buildFooter: function() {
        var footer = null;
        if (this.options.acceptButton || this.options.removeButton || this.options.cancelButton) {
            footer = $('<div class="modal-footer" style="paddingTop:5;textAlign:center">');
        }
        if (this.options.acceptButton) {
            this.acceptButton = $('<button type="button" class="btn btn-primary" id="acceptBtn" data-dismiss="modal">'+ $msg('ACCEPT') + '</button>');
            footer.append(this.acceptButton);
            this.acceptButton.unbind('click').on("click",this.options.onEventData, this.onAcceptClick)
        }
        if (this.options.removeButton) {
            this.removeButton = $('<button type="button" class="btn btn-secondary" id="removeBtn" data-dismiss="modal">'+ $msg('REMOVE') +'</button>');
            footer.append(this.removeButton);
            this.removeButton.on('click', this.options.onEventData, this.onRemoveClick);
        }
        if (this.options.cancelButton) {
            footer.append('<button type="button" class="btn btn-secondary" data-dismiss="modal">'+ $msg('CANCEL') +'</button>');
        }
        return footer;
    },

    _buildHeader: function(title) {
        var header = null;
        if (this.options.closeButton || title) {
            header = $('<div class="modal-header"></div>');
        }
        if (this.options.closeButton) {
            header.append(
                '<button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>'
            );
        }
        if (title) {
            header.append('<h2 class="modal-title">' + title + '</h2>');
        }
        return header;
    },

    onAcceptClick: function(event) {
        throw "Unsupported operation";
    },

    onDialogShown: function() {},
    onRemoveClick: function(event) {
        throw "Unsupported operation";
    },

    show: function () {
        this._native.modal();
    },

    setContent: function(content) {
        var modalBody = this._native.find('.modal-body');
        modalBody.append(content);
    },

    css: function(options){
        this._native.find('.modal-dialog').css(options);
    },

    close: function() {
        this._native.modal('hide');
    },

    alertError: function(message){
        this._native.find('.alert-danger').text(message);
        this._native.find('.alert-danger').show();
    },

    cleanError: function(){
        this._native.find('.alert-danger').hide();
    }
});
