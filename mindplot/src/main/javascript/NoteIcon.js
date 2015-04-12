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

mindplot.NoteIcon = new Class({
    Extends: mindplot.Icon,
    initialize: function (topic, noteModel, readOnly) {
        $assert(topic, 'topic can not be null');

        this.parent(mindplot.NoteIcon.IMAGE_URL);
        this._linksModel = noteModel;
        this._topic = topic;
        this._readOnly = readOnly;

        this._registerEvents();
    },

    _registerEvents: function () {
        this._image.setCursor('pointer');
        var me = this;

        if (!this._readOnly) {
            // Add on click event to open the editor ...
            this.addEvent('click', function (event) {
                me._topic.showNoteEditor();
                event.stopPropagation();
            });
        }
        this._tip = new mindplot.widget.FloatingTip($(me.getImage()._peer._native), {
            title: $msg('NOTE'),
            container: 'body',
            // Content can also be a function of the target element!
            content: function () {
                return me._buildTooltipContent();
            },
            html: true,
            placement: 'bottom',
            destroyOnExit: true
        });

    },

    _buildTooltipContent: function () {
        if ($("body").find("#textPopoverNote").length == 1) {
            var text = $("body").find("#textPopoverNote");
            text.text(this._linksModel.getText());
        } else {
            var result = $('<div id="textPopoverNote"></div>').css({padding: '5px'});

            var text = $('<div></div>').text(this._linksModel.getText())
                .css({
                    'white-space': 'pre-wrap',
                    'word-wrap': 'break-word'
                }
            );
            result.append(text);
            return result;
        }
    },

    getModel: function () {
        return this._linksModel;
    }
});

mindplot.NoteIcon.IMAGE_URL = "images/notes.png";

