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

mindplot.MultilineTextEditor = new Class({
    Extends: Events,
    initialize:function(topic) {
        this._topic = topic;
    },

    _buildEditor : function() {

        var result = new Element('div');
        result.setStyles({
                position:"absolute",
                display: "none",
                zIndex: "8",
                overflow:"hidden",
                border: "0 none"
            }
        );

        var textareaElem = new Element('textarea',
            {   tabindex:'-1',
                value:"",
                wrap:'off'
            }
        );

        textareaElem.setStyles({
            border: "1px gray dashed",
            background:"transparent",
            outline: '0 none',
            resize: 'none',
            overflow:"hidden"
        });
        textareaElem.inject(result);
        return result;
    },

    _registerEvents : function(containerElem) {
        var textareaElem = this._getTextareaElem();

        textareaElem.addEvent('keydown', function (event) {
            switch (event.key) {
                case 'esc':
                    this.close(false);
                    break;
                case 'enter':
                    if (event.meta || event.control) {

                        // @todo: Enters must be in any place ...
                        textareaElem.value = textareaElem.value + "\n";
                    }
                    else {
                        this.close(true);
                    }
                    break;
            }
            event.stopPropagation();
        }.bind(this));

        textareaElem.addEvent('keypress', function(event) {
            event.stopPropagation();
        });

        textareaElem.addEvent('keyup', function(event) {
            var text = this._getTextareaElem().value;
            this.fireEvent('input', [event, text]);
            this._adjustEditorSize();
        }.bind(this));

        textareaElem.addEvent('blur', function() {
            // @Todo: Issues if this is enables and esc is pressed.
//            this.close.bind(this).attempt(true);
        }.bind(this));

        // If the user clicks on the input, all event must be ignored ...
        containerElem.addEvent('click', function(event) {
            event.stopPropagation();
        });
        containerElem.addEvent('dblclick', function(event) {
            event.stopPropagation();
        });
        containerElem.addEvent('mousedown', function(event) {
            event.stopPropagation();
        });
    },

    _adjustEditorSize : function() {

        if (this.isVisible()) {
            var textElem = this._getTextareaElem();

            var lines = textElem.value.split('\n');
            var maxLineLength = 1;
            lines.forEach(function(line) {
                if (maxLineLength < line.length)
                    maxLineLength = line.length;
            });

            textElem.setAttribute('cols', maxLineLength);
            textElem.setAttribute('rows', lines.length);

            this._containerElem.setStyles({
                width: (maxLineLength + 3) + 'em',
                height: textElem.getSize().height
            });
        }
    },

    isVisible : function () {
        return $defined(this._containerElem) && this._containerElem.getStyle('display') == 'block';
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
            var containerElem = this._buildEditor();
            containerElem.inject($(document.body));

            this._containerElem = containerElem;
            this._registerEvents(containerElem);
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

        // Set editor's initial size
        var displayFunc = function() {
            // Position the editor and set the size...
            var textShape = this._topic.getTextShape();
            textShape.positionRelativeTo(this._containerElem, {
                position: {x: 'left',y:'top'},
                edge: {x: 'left', y: 'top'}
            });
            this._containerElem.setStyle('display', 'block');

            // Set editor's initial text ...
            var text = $defined(defaultText) ? defaultText : topic.getText();
            this._setText(text);

            // Set the element focus and select the current text ...
            var inputElem = this._getTextareaElem();
            inputElem.focus();
            this._changeCursor(inputElem, $defined(defaultText));

        }.bind(this);

        displayFunc.delay(10);
    },

    _setStyle : function (fontStyle) {
        var inputField = this._getTextareaElem();
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
        var style = {
            fontSize : fontStyle.size + "px",
            fontFamily : fontStyle.font,
            fontStyle : fontStyle.style,
            fontWeight : fontStyle.weight,
            color : fontStyle.color
        };
        inputField.setStyles(style);
        this._containerElem.setStyles(style);
    },

    _setText : function(text) {
        var textareaElem = this._getTextareaElem();
        textareaElem.value = text;
        this._adjustEditorSize();
    },

    _getText : function() {
        return this._getTextareaElem().value;
    },

    _getTextareaElem : function() {
        return this._containerElem.getElement('textarea');
    },

    _changeCursor : function(textareaElem, selectText) {
        // Select text if it's required ...
        if (textareaElem.createTextRange) //ie
        {
            var range = textareaElem.createTextRange();
            var pos = textareaElem.value.length;
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
            textareaElem.setSelectionRange(0, textareaElem.value.length);
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
            this._containerElem.dispose();
            this._containerElem = null;
        }
    }
});

