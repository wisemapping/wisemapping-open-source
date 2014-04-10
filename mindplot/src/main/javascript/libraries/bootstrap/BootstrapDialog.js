var BootstrapDialog = new Class({
    Implements: Options,

    options: {
        cancelButton: false,
        closeButton: false,
        acceptButton: true
    },

    initialize: function (title, options) {
        this.setOptions(options);
        this._native = $('<div class="modal fade"></div>').append('<div class="modal-dialog"></div>');
        var content = $('<div class="modal-content"></div>');
        var header = this._buildHeader(title);
        if (header) {
            content.append(header);
        }
        content.append('<div class="modal-body"></div>');
        var footer = this._buildFooter();
        if (footer) {
            content.append(footer);
        }

        this._native.find(".modal-dialog").append(content);
    },

    _buildFooter: function() {
        var footer = null;
        if (this.options.acceptButton || this.options.cancelButton) {
            footer = $('<div class="modal-footer">');
        }
        if (this.options.acceptButton) { //falta agregar $msg('ACCEPT')
            footer.append('<input type="submit" id="acceptBtn" class="btn btn-primary" value="Accept"/>');
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
            header.append('<h3 class="modal-title">' + title + '</h3>');
        }
        return header;
    },


    show: function () {
        this._native.modal();
    },

    setContent: function(content) {
        // faltaria remover body previo
        this._native.find('.modal-body').append(content);
    },

    close: function() {
        this._native.modal('hide');
    }
});
