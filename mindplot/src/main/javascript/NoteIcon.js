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

mindplot.NoteIcon = new Class({
    Extends:mindplot.Icon,
    initialize:function (topic, noteModel, readOnly) {
        $assert(topic, 'topic can not be null');

        this.parent(mindplot.NoteIcon.IMAGE_URL);
        this._linksModel = noteModel;
        this._topic = topic;
        this._readOnly = readOnly;

        this._registerEvents();
    },

    _registerEvents:function () {
        this._image.setCursor('pointer');

        if (!this._readOnly) {

            // Add on click event to open the editor ...
            this.addEvent('click', function (event) {
                this._topic.showNoteEditor();
                event.stopPropagation();
            }.bind(this));
        }

        this._tip = new mindplot.widget.FloatingTip(this.getImage()._peer._native, {
            // Content can also be a function of the target element!
            content:function () {
                var result = new Element('div');
                result.setStyles({padding:'5px'});

                var title = new Element('div', {text:$msg('NOTE')});
                title.setStyles({
                    'font-weight':'bold',
                    color:'black',
                    'padding-bottom':'5px',
                    width:'100px'
                });
                title.inject(result);

                var text = new Element('div', {text:this._linksModel.getText()});
                text.setStyles({
                        'white-space':'pre-wrap',
                        'word-wrap':'break-word'
                    }
                );
                text.inject(result);


                return result;
            }.bind(this),
            html:true,
            position:'bottom',
            arrowOffset:10,
            center:true,
            arrowSize:15,
            offset:{x:10, y:20},
            className:'notesTip'
        });
    },

    getModel:function () {
        return this._linksModel;
    }
});

mindplot.NoteIcon.IMAGE_URL = "images/notes.png";

