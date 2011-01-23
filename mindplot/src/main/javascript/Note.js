/*
*    Copyright [2011] [wisemapping]
*
*   Licensed under the Apache License, Version 2.0 (the "License") plus the
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

mindplot.Note = function(textModel, topic, designer) {
    var divContainer=designer.getWorkSpace().getScreenManager().getContainer();
    var bubbleTip = mindplot.BubbleTip.getInstance(divContainer);
    mindplot.Icon.call(this, mindplot.Note.IMAGE_URL);
    this._noteModel = textModel;
    this._topic = topic;
    this._designer = designer;
    var image = this.getImage();
    var imgContainer = new Element('div').setStyles({textAlign:'center'});
    this._textElem = new Element('div').setStyles({'max-height':100,'max-width':300, 'overflow':'auto'});
    var text = unescape(textModel.getText());
    text = text.replace(/\n/ig,"<br/>");
    text = text.replace(/<script/ig, "&lt;script");
    text = text.replace(/<\/script/ig, "&lt;\/script");
    this._textElem.innerHTML = text;
    this._text=textModel.getText();

    this._textElem.inject(imgContainer);

    var container = new Element('div');

    imgContainer.inject(container);

    if(!designer._viewMode){
        var buttonContainer = new Element('div').setStyles({paddingTop:5, textAlign:'center'});
        var editBtn = new Element('input', {type:'button', value:'Edit','class':'btn-primary'}).addClass('button').inject(buttonContainer);
        var removeBtn = new Element('input', {type:'button', value:'Remove','class':'btn-primary'}).addClass('button').inject(buttonContainer);

        editBtn.setStyle("margin-right", "3px");
        removeBtn.setStyle("margin-left", "3px");

        removeBtn.addEvent('click', function(event) {
            var command = new mindplot.commands.RemoveNoteFromTopicCommand(this._topic.getId());
            designer._actionRunner.execute(command);
            bubbleTip.forceClose();
        }.bindWithEvent(this));

        var okButtonId = 'okNoteButtonId';
        editBtn.addEvent('click', function(event) {
            var topic = this._topic;
            var designer = this._designer;
            var note = this;

            var msg = new Element('div');
            var textarea = new Element('div').inject(msg);
            textarea.innerHTML = "Text"

            var formElem = new Element('form', {'action': 'none', 'id':'noteFormId'});
            var text = textModel.getText();
            text = unescape(text);
            var textInput = new Element('textarea', {'value':text}).setStyles({'width':280, 'height':50});
            textInput.inject(formElem);
            formElem.inject(msg)

            var okFunction = function(e) {
                var result = true;
                var text = textInput.value;
                text = escape(text);
                note._noteModel.setText(text);
                return result;
            };

            formElem.addEvent('submit', function(e)
            {
                $(okButtonId).fireEvent('click', e);
                e = new Event(e);
                e.stop();
            });


            var dialog = mindplot.Note.buildDialog(designer, okFunction, okButtonId);
            dialog.adopt(msg).show();

        }.bindWithEvent(this));
        buttonContainer.inject(container);
    }


    var note = this;
    image.addEventListener('mouseover', function(event) {
        var text = textModel.getText();
        text = unescape(text);
        text = text.replace(/\n/ig,"<br/>");
        text = text.replace(/<script/ig, "&lt;script");
        text = text.replace(/<\/script/ig, "&lt;\/script");
        this._textElem.innerHTML = text;

        bubbleTip.open(event, container, note);
    }.bind(this));
    image.addEventListener('mousemove', function(event) {
        bubbleTip.updatePosition(event);
    });
    image.addEventListener('mouseout', function(event) {
        bubbleTip.close(event);
    });
};

objects.extend(mindplot.Note, mindplot.Icon);

mindplot.Note.prototype.initialize = function() {

};

mindplot.Note.prototype.getText=function(){
    return this._text;
};

mindplot.Note.prototype.getModel=function(){
    return this._noteModel;
};

mindplot.Note.buildDialog = function(designer, okFunction, okButtonId) {
    var windoo = new Windoo({
        title: 'Write note',
        theme: Windoo.Themes.wise,
        modal:true,
        buttons:{'menu':false, 'close':false, 'minimize':false, 'roll':false, 'maximize':false},
        destroyOnClose:true,
        height:130
    });

    var cancel = new Element('input', {'type': 'button', 'class':'btn-primary', 'value': 'Cancel','class':'btn-primary'}).setStyle('margin-right', "5px");
    cancel.setStyle('margin-left', "5px");
    cancel.addEvent('click', function(event) {
        $(document).addEvent('keydown', designer.keyEventHandler.bindWithEvent(designer));
        windoo.close();
    }.bindWithEvent(this));

    var ok = new Element('input', {'type': 'button', 'class':'btn-primary', 'value': 'Ok','class':'btn-primary','id':okButtonId}).setStyle('marginRight', 10);
    ok.addEvent('click', function(event) {
        var couldBeUpdated = okFunction.attempt();
        if (couldBeUpdated)
        {
            $(document).addEvent('keydown', designer.keyEventHandler.bindWithEvent(designer));
            windoo.close();
        }
    }.bindWithEvent(this));

    var panel = new Element('div', {'styles': {'padding-top': 10, 'text-align': 'right'}}).adopt(ok, cancel);

    windoo.addPanel(panel);
    $(document).removeEvents('keydown');
    return windoo;
};

mindplot.Note.IMAGE_URL = "../images/note.png";

