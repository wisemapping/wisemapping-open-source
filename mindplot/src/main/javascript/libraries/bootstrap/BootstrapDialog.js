var BootstrapDialog = new Class({

    initialize: function () {
        this._native = $('<div></div>');
    },


    show: function (title) {
        $assert(title, "message can not be null");

        var modalDialog = $(
            '<div class="modal fade">' +
                '<div class="modal-dialog">' +
                    '<div class="modal-content">' +
                        '<div class="modal-header">' +
                            '<button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>' +
                            '<h3 class="modal-title">' + title + '</h3>' +
                        '</div>' +
                        '<div class="modal-body">' +
                            this._native.html() +
                        '</div>' +
                    '</div>' +
                '</div>' +
            '</div>');
        modalDialog.modal();
    },

    setContent:function (content){
        this._native.append(content);
    }
});
