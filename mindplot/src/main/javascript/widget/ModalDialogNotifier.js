/*
 *    Copyright [2012] [wisemapping]
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
    Extends:MooDialog,
    initialize:function () {
        this.parent({
                closeButton:false,
                destroyOnClose:true,
                autoOpen:true,
                useEscKey:false,
                title:"",
                onInitialize:function (wrapper) {
                    wrapper.setStyle('opacity', 0);
                    this.wrapper.setStyle('display', 'none');
                    this.fx = new Fx.Morph(wrapper, {
                        duration:100,
                        transition:Fx.Transitions.Bounce.easeOut
                    });
                },

                onBeforeOpen:function () {
                    var panel = this._buildPanel();
                    this.setContent(panel);

                    this.overlay = new Overlay(this.options.inject, {
                        duration:this.options.duration
                    });
                    if (this.options.closeOnOverlayClick)
                        this.overlay.addEvent('click', this.close.bind(this));
                    this.overlay.open();
                    this.fx.start({
                        'margin-top':[-200, -100],
                        opacity:[0, 1]
                    }).chain(function () {
                        this.fireEvent('show');
                        this.wrapper.setStyle('display', 'block');
                    }.bind(this));
                },

                onBeforeClose:function () {
                    this.fx.start({
                        'margin-top':[-100, 0],
                        opacity:0,
                        duration:200
                    }).chain(function () {
                        this.wrapper.setStyle('display', 'none');
                        this.fireEvent('hide');

                    }.bind(this));
                }}
        );
        this.message = null;
    },

    show:function (message, title) {
        $assert(message, "message can not be null");
        this._messsage = message;
        this.options.title.setText($defined(title) ? title : "Outch!!. An unexpected error has occurred");
        this.open();
    },

    destroy:function () {
        this.parent();
        this.overlay.destroy();
    },

    _buildPanel:function () {
        var result = new Element('div');
        result.setStyles({
            'text-align':'center',
            width:'400px'
        });
        var p = new Element('p', {'text':this._messsage});
        p.inject(result);

        var img = new Element('img', {'src':'images/alert-sign.png'});
        img.inject(result);

        return result;
    }
});


var dialogNotifier = new mindplot.widget.ModalDialogNotifier();
$notifyModal = dialogNotifier.show.bind(dialogNotifier);

