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
                '<h2 class="modal-title">' + title + '</h2>' +
            '</div>' +
            '<div class="modal-body">' +
            this.content.html() +
            '</div>' +

            '<div class="modal-footer">' +
                '<button type="button" class="btn btn-primary">Accept</button>' +
                '<button type="button" class="btn btn-default" data-dismiss="modal">Close</button>' +
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
