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

mindplot.widget.LinkEditor = new Class({
    Extends:BootstrapDialog,

    initialize:function (model) {
        $assert(model, "model can not be null");
        this.parent($msg("LINK"));
        var panel = this._buildPanel(model);
        this.setContent(panel);
    },

    _buildPanel:function (model) {
        var result = $('<div></div>').css("padding-top", "5px");
        var form = $('<form></form>').attr({
            'action':'none',
            'id':'linkFormId'
        });
        var text = $('<p></p>').text("Paste your url here:");
        text.css('margin','0px 0px 10px');

        form.append(text);

        // Add Input

        var input = $('<input>').attr({
            'placeholder':'http://www.example.com/',
            'type':'url', //FIXME: THIS not work on IE, see workaround below
            'required':'true',
            'autofocus':'autofocus'
        });
        input.css('width','70%').css('margin','0px 20px');

        if (model.getValue() != null){
            input.value = model.getValue();}
//            type:Browser.ie ? 'text' : 'url', // IE workaround

        // Open Button
        var open = $('<input/>').attr({
                'type':'button',
                'value':$msg('OPEN_LINK')
        });

        open.click(function(){
            alert('clicked!');
        });

        form.append(input);
        form.append(open);

        // Register submit event ...
        form.submit(function (event) {
//            event.stopPropagation();
            event.preventDefault();

            if (input.value != null && input.value.trim() != "") {
                model.setValue(input.value);
            }
            this.close();
        }.bind(this));

//        form.addEvent('submit', function (event) {
//            event.stopPropagation();
//            event.preventDefault();
//
//            if (input.value != null && input.value.trim() != "") {
//                model.setValue(input.value);
//            }
//            this.close();
//        }.bind(this));


        // Add buttons ...

        var buttonContainer = $('<div></div>');
        buttonContainer.css('paddingTop','5').css('textAlign','center');
//        var buttonContainer = new Element('div').setStyles({paddingTop:5, textAlign:'center'});
//
        // Create remove button ...


//        if ($defined(model.getValue())) {
//            var rmButton = new Element('input', {type:'button', value:$msg('REMOVE'), 'class':'btn-primary'});
//            rmButton.setStyle('margin', '5px');
//            rmButton.addClass('button');
//            rmButton.inject(buttonContainer);
//            rmButton.addEvent('click', function (event) {
//                model.setValue(null);
//                event.stopPropagation();
//                this.close();
//            }.bind(this));
//            buttonContainer.inject(form);
//        }

        form.append(buttonContainer);
//        var cButton = new Element('input', {type:'button', value:$msg('CANCEL'), 'class':'btn-secondary'});
//        cButton.setStyle('margin', '5px');
//        cButton.addClass('button');
//        cButton.addEvent('click', function () {
//            this.close();
//        }.bind(this));
//        buttonContainer.inject(form);

//        result.addEvent('keydown', function (event) {
//            event.stopPropagation();
//        });
//

        result.append(form);
        return result;
    }
});
