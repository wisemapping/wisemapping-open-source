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

mindplot.model.NoteModel = new Class({
    initialize : function(text, topic) {
        $assert(text, 'text text can not be null');
        $assert(topic, 'topic can not be null');
        this._text = text;
        this._topic = topic;
    },

    getText:function() {
            return this._text;
    },

    setText : function(text) {
        this._text = text;
    },

    getTopic : function() {
        return this._topic;
    },

    isNoteModel : function() {
        return true;
    }
});