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

mindplot.MultilineTextEditor = new Class({
    Extends:Events,
    initialize:function () {
        this._topic = null;
        this._timeoutId = -1;
    },

    _buildEditor:function () {

        var result = new Element('div');
        result.setStyles({
                position:"absolute",
                display:"none",
                zIndex:"8",
                overflow:"hidden",
                border:"0 none"
            }
        );

        var textareaElem = new Element('textarea',
            {   tabindex:'-1',
                value:"",
                wrap:'off'
            }
        );

        textareaElem.setStyles({
            border:"1px gray dashed",
            background:"rgba(98, 135, 167, .3)",
            outline:'0 none',
            resize:'none',
            overflow:"hidden"
        });
        textareaElem.inject(result);
        return result;
    },

    _registerEvents:function (containerElem) {
        var textareaElem = this._getTextareaElem();

        textareaElem.addEvent('keydown', function (event) {
            switch (event.key) {
                case 'esc':
                    this.close(false);
                    break;
                case 'enter':
                    if (event.meta || event.control) {

                        // Add return ...
                        var text = textareaElem.value;
                        var cursorPosition = text.length;
                        if (textareaElem.selectionStart) {
                            cursorPosition = textareaElem.selectionStart;
                        }

                        var head = text.substring(0, cursorPosition);
                        var tail = "";
                        if (cursorPosition < text.length) {
                            tail = text.substring(cursorPosition, text.length);
                        }
                        textareaElem.value = head + "\n" + tail;

                        // Position cursor ...
                        if (textareaElem.setSelectionRange) {
                            textareaElem.focus();
                            textareaElem.setSelectionRange(cursorPosition + 1, cursorPosition + 1);
                        } else if (textareaElem.createTextRange) {
                            var range = textareaElem.createTextRange();
                            range.moveStart('character', cursorPosition + 1);
                            range.select();
                        }

                    }
                    else {
                        this.close(true);
                    }
                    break;
            }
            event.stopPropagation();
        }.bind(this));

        textareaElem.addEvent('keypress', function (event) {
            event.stopPropagation();
        });

        textareaElem.addEvent('keyup', function (event) {
            var text = this._getTextareaElem().value;
            this.fireEvent('input', [event, text]);
            this._adjustEditorSize();
        }.bind(this));

        // If the user clicks on the input, all event must be ignored ...
        containerElem.addEvent('click', function (event) {
            event.stopPropagation();
        });
        containerElem.addEvent('dblclick', function (event) {
            event.stopPropagation();
        });
        containerElem.addEvent('mousedown', function (event) {
            event.stopPropagation();
        });
    },

    _adjustEditorSize:function () {

        if (this.isVisible()) {
            var textElem = this._getTextareaElem();

            var lines = textElem.value.split('\n');
            var maxLineLength = 1;
            lines.each(function (line) {
                if (maxLineLength < line.length)
                    maxLineLength = line.length;
            });

            textElem.setAttribute('cols', maxLineLength);
            textElem.setAttribute('rows', lines.length);

            this._containerElem.setStyles({
                width:(maxLineLength + 3) + 'em',
                height:textElem.getSize().height
            });
        }
    },

    isVisible:function () {
        return $defined(this._containerElem) && this._containerElem.getStyle('display') == 'block';
    },

    _updateModel:function () {

        if (this._topic.getText() != this._getText()) {
            var text = this._getText();
            // Do not send windows return chars ...
            text = text.replace("\r","");
            var topicId = this._topic.getId();

            var actionDispatcher = mindplot.ActionDispatcher.getInstance();
            actionDispatcher.changeTextToTopic([topicId], text);
        }
    },

    show:function (topic, text) {
        // Close a previous node editor if it's opened ...
        if (this._topic) {
            this.close(false);
        }

        this._topic = topic;
        if (!this.isVisible()) {
            //Create editor ui
            var containerElem = this._buildEditor();
            containerElem.inject($(document.body));

            this._containerElem = containerElem;
            this._registerEvents(containerElem);
            this._showEditor(text);
        }
    },

    _showEditor:function (defaultText) {

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
        var displayFunc = function () {
            // Position the editor and set the size...
            var textShape = topic.getTextShape();
            textShape.positionRelativeTo(this._containerElem, {
                position:{x:'left', y:'top'},
                edge:{x:'left', y:'top'}
            });
            this._containerElem.setStyle('display', 'block');

            // Set editor's initial text ...
            var text = $defined(defaultText) ? defaultText : topic.getText();
            this._setText(text);

            // Set the element focus and select the current text ...
            var inputElem = this._getTextareaElem();
            this._positionCursor(inputElem, !$defined(defaultText));

        }.bind(this);

        this._timeoutId = displayFunc.delay(10);
    },

    _setStyle:function (fontStyle) {
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
            fontSize:fontStyle.size + "px",
            fontFamily:fontStyle.font,
            fontStyle:fontStyle.style,
            fontWeight:fontStyle.weight,
            color:fontStyle.color
        };
        inputField.setStyles(style);
        this._containerElem.setStyles(style);
    },

    _setText:function (text) {
        var textareaElem = this._getTextareaElem();
        textareaElem.value = text;
        this._adjustEditorSize();
    },

    _getText:function () {
        return this._getTextareaElem().value;
    },

    _getTextareaElem:function () {
        return this._containerElem.getElement('textarea');
    },

    _positionCursor:function (textareaElem, selectText) {
        textareaElem.focus();
        if (selectText) {
            // Mark text as selected ...
            if (textareaElem.createTextRange) {
                var rang = textareaElem.createTextRange();
                rang.select();
                rang.move("character", textareaElem.value.length);
            }
            else {
                textareaElem.setSelectionRange(0, textareaElem.value.length);
            }

        } else {
            // Move the cursor to the last character ..
            if (textareaElem.createTextRange) {
                var range = textareaElem.createTextRange();
                range.move("character", textareaElem.value.length);
            } else {
                textareaElem.selectionStart = textareaElem.value.length;
            }
        }

    },

    close:function (update) {
        if (this.isVisible() && this._topic) {
            // Update changes ...
            clearTimeout(this._timeoutId);

            if (!$defined(update) || update) {
                this._updateModel();
            }

            // Let make the visible text in the node visible again ...
            this._topic.getTextShape().setVisibility(true);

            // Remove it form the screen ...
            this._containerElem.dispose();
            this._containerElem = null;
            this._timeoutId = -1;
        }
        this._topic = null;
    }
});

