var BootstrapDialog = new Class({

    initialize: function () {
        this.content = $('<div></div>');
    },


    show: function (title) {
        $assert(title, "message can not be null");

        var modalDialog = $('<div class="modal fade">' +
            '<div class="modal-dialog">' +
            '<div class="modal-content">' +
            '<div class="modal-header">' +
                '<button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>' +
                '<h3 class="modal-title">' + title + '</h3>' +
            '</div>' +
            '<div class="modal-body">' +
            this.content.html() +
            '</div>' +
            '</div>' +
            '</div>' +
            '</div>');

        modalDialog.modal();
    },

    appendToContent:function (content){
        this.content.append(content);
    }
});
