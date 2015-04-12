/*
 *    Copyright [2015] [wisemapping]
 *
 *   Licensed under WiseMapping Public License, Version 1.0 (the "License").
 *   It is basically the Apache License, Version 2.0 (the "License") plus the
 *   "powered by wisemapping" text requirement on every single page;
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the license at
 *
 *       http://www.wisemapping.org/license
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

mindplot.widget.ModalDialogNotifier = new Class({

    initialize: function () {},

    //FIXME: replace by alert()
    show: function (message, title) {
        $assert(message, "message can not be null");

        var modalDialog = $('<div class="modal fade">' +
                        '<div class="modal-dialog">' +
                            '<div class="modal-content">' +
                                '<div class="modal-body"></div>' +
                                    '<div class="alert alert-block alert-warning">' +
                                        '<img src="images/alert-sign.png">' +
                                        '<div style="display: inline-block" class="alert-content"></div>' +
                                    '</div>' +
                                '<div class="modal-footer">' +
                                    '<button type="button" class="btn btn-default" data-dismiss="modal">Close</button>' +
                                '</div>' +
                            '</div>' +
                        '</div>' +
                      '</div>');

        var p = '<p>' + message + '</p>'
        var h4 = title ? '<h4>' + title + '</h4>' : "";

        modalDialog.find('.alert-content').append(h4 + p);
        modalDialog.modal();
    }
});


var dialogNotifier = new mindplot.widget.ModalDialogNotifier();
$notifyModal = dialogNotifier.show.bind(dialogNotifier);

