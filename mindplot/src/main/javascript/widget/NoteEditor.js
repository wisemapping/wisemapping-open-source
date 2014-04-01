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

mindplot.widget.NoteEditor = new Class({
    Extends:BootstrapDialog,
    initialize:function (model) {
        console.log("Re-impl required ....");
        $assert(model, "model can not be null");
        this.parent();
        var panel = this._buildPanel(model);
//        this.parent({
//            closeButton:true,
//            destroyOnClose:true,
//            title:$msg('NOTE'),
//            onInitialize:function (wrapper) {
//                wrapper.setStyle('opacity', 0);
//                this.fx = new Fx.Morph(wrapper, {
//                    duration:600,
//                    transition:Fx.Transitions.Bounce.easeOut
//                });
//            },
//
//            onBeforeOpen:function () {
//                this.overlay = new Overlay(this.options.inject, {
//                    duration:this.options.duration
//                });
//                if (this.options.closeOnOverlayClick)
//                    this.overlay.addEvent('click', this.close.bind(this));
//                this.overlay.open();
//
//                this.fx.start({
//                    'margin-top':[-200, -100],
//                    opacity:[0, 1]
//                }).chain(function () {
//                    this.fireEvent('show');
//                }.bind(this));
//            },
//
//            onBeforeClose:function () {
//                this.fx.start({
//                    'margin-top':[-100, 0],
//                    opacity:0
//                }).chain(function () {
//                    this.fireEvent('hide');
//                }.bind(this));
//                this.overlay.destroy();
//            }
//        });
//        this.setContent(panel);
        this.appendToContent(panel);
    },

    _buildPanel:function (model) {
        var result = $('<div></div>').css("padding-top", "5px");
        var form = $('<form></form>').attr('action','none').attr('id','noteFormId');

        // Add textarea

        var textArea = $('<textarea></textarea>').attr(
                'placeholder',$msg('WRITE_YOUR_TEXT_HERE')).attr(
                'required','true').attr(
                'autofocus','autofocus');
        textArea.css( 'width','100%').css('height',80).css('resize','none');
        form.append(textArea);

//        var textArea = new Element('textarea',
//            {placeholder:$msg('WRITE_YOUR_TEXT_HERE'),
//                required:true,
//                autofocus:'autofocus'
//            });
//        textArea.setStyles({
//            'width':'100%',
//            'height':80, resize:'none'
//        });

        if (model.getValue() != null){
            textArea.value = model.getValue();
        }

        // Register submit event

          form.submit(function (event) {
            event.preventDefault();
            event.stopPropagation();
            if (textArea.value) {
                model.setValue(textArea.value);
            }
            this.close();
          }.bind(this));

//        form.addEvent('submit', function (event) {
//            event.preventDefault();
//            event.stopPropagation();
//            if (textArea.value) {
//                model.setValue(textArea.value);
//            }
//            this.close();
//        }.bind(this));

        // Add buttons ...
        var buttonContainer = $('<div></div>');
        buttonContainer.css('paddingTop','5').css('textAlign','center');
//        var buttonContainer = new Element('div').setStyles({paddingTop:5, textAlign:'right'});

        // Create accept button ...
        var okButton = $('<input>');
        okButton.attr('type','submit').attr(
             'value',$msg('ACCEPT')).attr(
             'class','btn-primary');
        buttonContainer.append(okButton);

//        var okButton = new Element('input', {type:'submit', value:$msg('ACCEPT'), 'class':'btn-primary'});
//        okButton.addClass('button');
//        okButton.inject(buttonContainer);

        // Create remove button ...
//        if ($defined(model.getValue())) {
//            var rmButton = new Element('input', {type:'button', value:$msg('REMOVE'), 'class':'btn-primary'});
//            rmButton.setStyle('margin', '5px');
//            rmButton.addClass('button');
//            rmButton.inject(buttonContainer);
//            rmButton.addEvent('click', function () {
//                model.setValue(null);
//                this.close();
//            }.bind(this));
//            buttonContainer.inject(form);
//        }

        // Create cancel button ...
        var cancelButton = $('<input>');
        cancelButton.attr('id','cancel').attr('type','button').attr(
              'value',$msg('CANCEL')).attr(
              'class','btn-secondary');
        cancelButton.css('margin','5px');
        cancelButton.click(function () {this.close();});
        buttonContainer.append(cancelButton);

        form.append(buttonContainer);

//        var cButton = new Element('input', {type:'button', value:$msg('CANCEL'), 'class':'btn-secondary'});
//        cButton.setStyle('margin', '5px');
//        cButton.addClass('button');
//        cButton.inject(buttonContainer);
//        cButton.addEvent('click', function () {
//            this.close();
//        }.bind(this));
//        buttonContainer.inject(form);

//        result.addEvent('keydown', function (event) {
//            event.stopPropagation();
//        });
        result.append(form);
        return result;
    },

    show:function () {
        this.parent("Note");
    }

});
