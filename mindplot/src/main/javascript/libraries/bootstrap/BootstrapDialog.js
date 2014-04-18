var BootstrapDialog = new Class({
    Implements: Options,

    options: {
        cancelButton: false,
        closeButton: false,
        acceptButton: true,
        removeButton:false
    },

    initialize: function (title, options) {
        this.setOptions(options);
        this._native = $('<div class="modal fade"></div>').append('<div class="modal-dialog" style="margin:150px auto"></div>');
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
        if (this.options.acceptButton || this.options.removeButton || this.options.cancelButton) {
            footer = $('<div class="modal-footer" style="paddingTop:5;textAlign:center">');
        }
        if (this.options.acceptButton) {
            this.acceptButton = $('<button type="button" class="btn btn-primary" id="acceptBtn" data-dismiss="modal">'+ $msg('ACCEPT') + '</button>');
            footer.append(this.acceptButton);
            this.acceptButton.on('click', this.onAcceptClick)
        }
        if (this.options.removeButton) {
            this.removeButton = $('<button type="button" class="btn btn-secondary" id="removeBtn" data-dismiss="modal">'+ $msg('REMOVE') +'</button>');
            footer.append(this.removeButton);
            this.removeButton.on('click', {data:'hola'}, this.onRemoveClick);
            this.removeButton.hide();
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

    onAcceptClick: function(event) {
        //this method should be abstract
    },


    onRemoveClick: function(event) {
        //this method should be abstract
    },

    show: function () {
        this._native.modal();
    },

    setContent: function(content) {
        // faltaria remover body previo
        this._native.find('.modal-body').append(content);
    },

    setStyle:function(width){
        this._native.find('.modal-dialog').css("width",width);
    },

    close: function() {
        this._native.modal('hide');
    },

    showRemoveButton: function(){
      this.removeButton.show();
    }
});
