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

mindplot.TextEditor = new Class({
    initialize:function(topic) {
        this._topic = topic;
    },

    _buildEditor : function() {

        this._size = {width:500, height:100};
        var result = new Element('div');
        result.setStyles({
                position:"absolute",
                display: "none",
                zIndex: "8",
                top: 0,
                left:0,
                width:"500px",
                height:"100px"}
        );

        var inputContainer = new Element('div');
        inputContainer.setStyles({
            border:"none",
            overflow:"auto"
        });
        inputContainer.inject(result);

        var inputText = new Element('input', {type:"text",tabindex:'-1', value:""});
        inputText.setStyles({
            border:"none",
            background:"transparent"
        });
        inputText.inject(inputContainer);

        var spanContainer = new Element('div');
        spanContainer.setStyle('visibility', "hidden");
        spanContainer.inject(result);

        var spanElem = new Element('span', {tabindex:"-1"});
        spanElem.setStyle('white-space', "nowrap");
        spanElem.setStyle('nowrap', 'nowrap');
        spanElem.inject(spanContainer);

        return result;
    },

    _registerEvents : function(divElem) {
        var inputElem = this._getInputElem();
        var spanElem = this._getSpanElem();

        divElem.addEvent('keydown', function (event) {
            switch (event.key) {
                case 'esc':
                    this.close(false);
                    break;
                case 'enter':
                    this.close(true);
                    break;
                default:
                    spanElem.innerHTML = inputElem.value;
                    var size = inputElem.value.length + 1;
                    inputElem.size = size;
                    if (spanElem.offsetWidth > (parseInt(divElem.style.width) - 100)) {
                        divElem.style.width = (spanElem.offsetWidth + 100) + "px";
                    }
                    break;
            }
            event.stopPropagation();
        }.bind(this));

        // If the user clicks on the input, all event must be ignored ...
        divElem.addEvent('click', function(event) {
            event.stopPropagation();
        });
        divElem.addEvent('dblclick', function(event) {
            event.stopPropagation();
        });
        divElem.addEvent('mousedown', function(event) {
            event.stopPropagation();
        });
    },

    isVisible : function () {
        return $defined(this._divElem) && this._divElem.getStyle('display') == 'block';
    },

    _updateModel : function () {

        if (this._topic.getText() != this._getText()) {
            var text = this._getText();
            var topicId = this._topic.getId();

            var actionDispatcher = mindplot.ActionDispatcher.getInstance();
            actionDispatcher.changeTextOnTopic([topicId], text);
        }
    },

    show : function (text) {

        if (!this.isVisible()) {
            //Create editor ui
            var editorElem = this._buildEditor();
            // @Todo: What element associate ?
            editorElem.inject($('mindplot'));

            this._divElem = editorElem;
            this._registerEvents(editorElem);
            this._showEditor(text);
        }
    },

    _showEditor : function (defaultText) {

        var topic = this._topic;

        // Hide topic text ...
        topic.getTextShape().setVisibility(false);

        // Set Editor Style
        var nodeText = topic.getTextShape();
        var font = nodeText.getFont();
        font.size = nodeText.getHtmlFontSize();
        font.color = nodeText.getColor();
        this._setStyle(font);

        // Set editor's initial text
        var text = $defined(defaultText) ? defaultText : topic.getText();
        this._setText(text);

        // Set editor's initial size
        var displayFunc = function() {
            var textShape = topic.getTextShape();
            var scale = web2d.peer.utils.TransformUtil.workoutScale(textShape._peer);

            var screenPosition = mindplot.util.Converter.topicToScreenPosition(topic);
            var textWidth = textShape.getWidth();
            var textHeight = textShape.getHeight();
            var iconGroup = topic.getIconGroup();
            var iconGroupSize;

            if ($defined(iconGroup)) {
                iconGroupSize = topic.getIconGroup().getSize();
            }
            else {
                iconGroupSize = {width:0, height:0};
            }

            var position = {x:0,y:0};
            position.x = screenPosition.x - ((textWidth * scale.width) / 2) + (((iconGroupSize.width) * scale.width) / 2);
            var fixError = 1;
            position.y = screenPosition.y - ((textHeight * scale.height) / 2) - fixError;

            var elemSize = topic.getSize();

            // Position the editor and set the size...
            this._setEditorSize(elemSize.width, elemSize.height, scale);
            this._setPosition(position.x, position.y, scale);

            // Make the editor visible ....
            this._divElem.setStyle('display', 'block');

            var inputElem = this._getInputElem();
            inputElem.focus();
            this._changeCursor(inputElem, $defined(defaultText));

        }.bind(this);

        displayFunc.delay(10);
    },

    _setStyle : function (fontStyle) {
        var inputField = this._getInputElem();
        var spanField = this._getSpanElem();
        if (!$defined(fontStyle.font)) {
            fontStyle.font = "Arial";
        }
        if (!$defined(fontStyle.style)) {
            fontStyle.style = "normal";
        }
        if (!$defined(fontStyle.weight)) {
            fontStyle.weight = "normal";
        }
        if (!$defined(fontStyle.size)) {
            fontStyle.size = 12;
        }
        inputField.style.fontSize = fontStyle.size + "px";
        inputField.style.fontFamily = fontStyle.font;
        inputField.style.fontStyle = fontStyle.style;
        inputField.style.fontWeight = fontStyle.weight;
        inputField.style.color = fontStyle.color;
        spanField.style.fontFamily = fontStyle.font;
        spanField.style.fontStyle = fontStyle.style;
        spanField.style.fontWeight = fontStyle.weight;
        spanField.style.fontSize = fontStyle.size + "px";
    },

    _setText : function(text) {
        var inputField = this._getInputElem();
        inputField.size = text.length + 1;
        this._divElem.style.width = (inputField.size * parseInt(inputField.style.fontSize) + 100) + "px";
        var spanField = this._getSpanElem();
        spanField.innerHTML = text;
        inputField.value = text;
    },

    _getText : function() {
        return this._getInputElem().value;
    },

    _getInputElem : function() {
        return this._divElem.getElement('input');
    },

    _getSpanElem : function() {
        return this._divElem.getElement('span');
    },

    _setEditorSize : function (width, height, scale) {
        this._size = {width:width * scale.width, height:height * scale.height};
        this._divElem.style.width = this._size.width * 2 + "px";
        this._divElem.style.height = this._size.height + "px";
    },

    _setPosition : function (x, y) {
        $(this._divElem).setStyles({top : y + "px", left: x + "px"});
    },

    _changeCursor : function(inputElem, selectText) {
        // Select text if it's required ...
        if (inputElem.createTextRange) //ie
        {
            var range = inputElem.createTextRange();
            var pos = inputElem.value.length;
            if (!selectText) {
                range.select();
                range.move("character", pos);
            }
            else {
                range.move("character", pos);
                range.select();
            }
        }
        else if (!selectText) {
            inputElem.setSelectionRange(0, inputElem.value.length);
        }
    },

    close : function(update) {
        if (this.isVisible()) {
            // Update changes ...
            if (!$defined(update) || update) {
                this._updateModel();
            }

            // Let make the visible text in the node visible again ...
            this._topic.getTextShape().setVisibility(true);

            // Remove it form the screen ...
            this._divElem.dispose();
            this._divElem = null;

            console.log("closing ....");
        }
    }
});

