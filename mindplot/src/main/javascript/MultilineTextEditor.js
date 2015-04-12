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

mindplot.MultilineTextEditor = new Class({
    Extends: mindplot.Events,
    initialize: function () {
        this._topic = null;
        this._timeoutId = -1;
    },

    _buildEditor: function () {

        var result = $('<div></div>')
            .attr('id', 'textContainer')
            .css({
                display: "none",
                zIndex: "8",
                overflow: "hidden",
                border: "0 none"
            });


        var textareaElem = $('<textarea tabindex="-1" value="" wrap="off" ></textarea>')
            .css({
                border: "1px gray dashed",
                background: "rgba(98, 135, 167, .3)",
                outline: '0 none',
                resize: 'none',
                overflow: "hidden"
            });

        result.append(textareaElem);
        return result;
    },

    _registerEvents: function (containerElem) {
        var textareaElem = this._getTextareaElem();
        var me = this;
        textareaElem.on('keydown', function (event) {
            switch (jQuery.hotkeys.specialKeys[event.keyCode]) {
                case 'esc':
                    me.close(false);
                    break;
                case 'enter':
                    if (event.metaKey || event.ctrlKey) {

                        // Add return ...
                        var text = textareaElem.val();
                        var cursorPosition = text.length;
                        if (textareaElem.selectionStart) {
                            cursorPosition = textareaElem.selectionStart;
                        }

                        var head = text.substring(0, cursorPosition);
                        var tail = "";
                        if (cursorPosition < text.length) {
                            tail = text.substring(cursorPosition, text.length);
                        }
                        textareaElem.val(head + "\n" + tail);

                        // Position cursor ...
                        if (textareaElem[0].setSelectionRange) {
                            textareaElem.focus();
                            textareaElem[0].setSelectionRange(cursorPosition + 1, cursorPosition + 1);
                        } else if (textareaElem.createTextRange) {
                            var range = textareaElem.createTextRange();
                            range.moveStart('character', cursorPosition + 1);
                            range.select();
                        }

                    }
                    else {
                        me.close(true);
                    }
                    break;
                case 'tab':
                    event.preventDefault();
                    var start = $(this).get(0).selectionStart;
                    var end = $(this).get(0).selectionEnd;

                    // set textarea value to: text before caret + tab + text after caret
                    $(this).val($(this).val().substring(0, start) + "\t" + $(this).val().substring(end));

                    // put caret at right position again
                    $(this).get(0).selectionStart = $(this).get(0).selectionEnd = start + 1;
                    break;
            }
            event.stopPropagation();
        });

        textareaElem.on('keypress', function (event) {
            event.stopPropagation();
        });

        textareaElem.on('keyup', function (event) {
            var text = me._getTextareaElem().val();
            me.fireEvent('input', [event, text]);
            me._adjustEditorSize();
        });

        // If the user clicks on the input, all event must be ignored ...
        containerElem.on('click', function (event) {
            event.stopPropagation();
        });
        containerElem.on('dblclick', function (event) {
            event.stopPropagation();
        });
        containerElem.on('mousedown', function (event) {
            event.stopPropagation();
        });
    },

    _adjustEditorSize: function () {

        if (this.isVisible()) {
            var textElem = this._getTextareaElem();

            var lines = textElem.val().split('\n');
            var maxLineLength = 1;
            _.each(lines, function (line) {
                if (maxLineLength < line.length)
                    maxLineLength = line.length;
            });

            textElem.attr('cols', maxLineLength);
            textElem.attr('rows', lines.length);

            this._containerElem.css({
                width: (maxLineLength + 3) + 'em',
                height: textElem.height()
            });
        }
    },

    isVisible: function () {
        return $defined(this._containerElem) && this._containerElem.css('display') == 'block';
    },

    _updateModel: function () {

        if (this._topic.getText() != this._getText()) {
            var text = this._getText();
            var topicId = this._topic.getId();

            var actionDispatcher = mindplot.ActionDispatcher.getInstance();
            actionDispatcher.changeTextToTopic([topicId], text);
        }
    },

    show: function (topic, text) {
        // Close a previous node editor if it's opened ...
        if (this._topic) {
            this.close(false);
        }

        this._topic = topic;
        if (!this.isVisible()) {
            //Create editor ui
            var containerElem = this._buildEditor();
            $('body').append(containerElem);

            this._containerElem = containerElem;
            this._registerEvents(containerElem);
            this._showEditor(text);
        }
    },

    _showEditor: function (defaultText) {

        var topic = this._topic;

        // Hide topic text ...
        topic.getTextShape().setVisibility(false);

        // Set Editor Style
        var nodeText = topic.getTextShape();
        var font = nodeText.getFont();
        font.size = nodeText.getHtmlFontSize();
        font.color = nodeText.getColor();
        this._setStyle(font);
        var me = this;
        // Set editor's initial size
        var displayFunc = function () {
            // Position the editor and set the size...
            var textShape = topic.getTextShape();

            me._containerElem.css('display', 'block');

            //FIXME: Im not sure if this is best way...
            var shapePosition = textShape.getNativePosition();
            me._containerElem.offset(shapePosition);

            // Set editor's initial text ...
            var text = $defined(defaultText) ? defaultText : topic.getText();
            me._setText(text);

            // Set the element focus and select the current text ...
            var inputElem = me._getTextareaElem();
            me._positionCursor(inputElem, !$defined(defaultText));

        };

        this._timeoutId = displayFunc.delay(10);
    },

    _setStyle: function (fontStyle) {
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
            fontSize: fontStyle.size + "px",
            fontFamily: fontStyle.font,
            fontStyle: fontStyle.style,
            fontWeight: fontStyle.weight,
            color: fontStyle.color
        };
        inputField.css(style);
        this._containerElem.css(style);
    },

    _setText: function (text) {
        var textareaElem = this._getTextareaElem();
        textareaElem.val(text);
        this._adjustEditorSize();
    },

    _getText: function () {
        return this._getTextareaElem().val();
    },

    _getTextareaElem: function () {
        return this._containerElem.find('textarea');
    },

    _positionCursor: function (textareaElem, selectText) {
        textareaElem.focus();
        var lengh = textareaElem.val().length;
        if (selectText) {
            // Mark text as selected ...
            if (textareaElem.createTextRange) {
                var rang = textareaElem.createTextRange();
                rang.select();
                rang.move("character", lengh);
            }
            else {
                textareaElem[0].setSelectionRange(0, lengh);
            }

        } else {
            // Move the cursor to the last character ..
            if (textareaElem.createTextRange) {
                var range = textareaElem.createTextRange();
                range.move("character", lengh);
            } else {
                if (Browser.ie11) {
                    textareaElem[0].selectionStart = lengh;
                    textareaElem[0].selectionEnd = lengh;
                } else {
                    textareaElem.selectionStart = lengh;
                    textareaElem.selectionEnd = lengh;
                }
                textareaElem.focus();
            }
        }

    },

    close: function (update) {
        if (this.isVisible() && this._topic) {
            // Update changes ...
            clearTimeout(this._timeoutId);

            if (!$defined(update) || update) {
                this._updateModel();
            }

            // Let make the visible text in the node visible again ...
            this._topic.getTextShape().setVisibility(true);

            // Remove it form the screen ...
            this._containerElem.remove();
            this._containerElem = null;
            this._timeoutId = -1;
        }
        this._topic = null;
    }
});

