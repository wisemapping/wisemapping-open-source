/*
 *    Copyright [2011] [wisemapping]
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
    Extends:MooDialog,
    initialize : function(model) {
        $assert(model, "model can not be null");
        var panel = this._buildPanel(model);
        this.parent({closeButton:false,destroyOnClose:true,title:'Note'});
        this.setContent(panel);
    },

    _buildPanel : function (model) {
        var result = new Element('div');
        var form = new Element('form', {'action': 'none', 'id':'noteFormId'});

        // Add textarea ...
        var textArea = new Element('textarea', {placeholder: 'Write your note here ...'});
        if (model.getValue() != null)
            textArea.value = model.getValue();

        textArea.setStyles({'width':280, 'height':65});
        textArea.inject(form);

        // Add buttons ...
        var buttonContainer = new Element('div').setStyles({paddingTop:5, textAlign:'center'});

        // Create accept button ...
        var okButton = new Element('input', {type:'button', value:'Accept','class':'btn-primary'});
        okButton.addClass('button');
        okButton.addEvent('click', function() {
            model.setValue(textArea.value);
            this.close();
        }.bind(this));
        okButton.inject(buttonContainer);

        // Create move button ...
        var rmButton = new Element('input', {type:'button', value:'Cancel','class':'btn-primary'});
        rmButton.setStyle('margin', '5px');
        rmButton.addClass('button');
        rmButton.inject(buttonContainer);
        rmButton.addEvent('click', function() {
            this.close();
        }.bind(this));

        buttonContainer.inject(form);

        result.addEvent('keydown', function(event) {
            event.stopPropagation();
        });

        form.inject(result);
        return result;
    },

    show : function() {
        this.open();
    }

});
