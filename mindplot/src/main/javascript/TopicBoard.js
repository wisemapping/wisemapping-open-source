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

//@Todo: Por que lo cambiaste a Board ?
mindplot.TopicBoard = new Class({

        initialize: function() {
            this._height = null;
        },

        _removeEntryByOrder : function(order, position) {
            var board = this._getBoard(position);
            var entry = board.lookupEntryByOrder(order);

            core.assert(!entry.isAvailable(), 'Entry must not be available in order to be removed.Entry Order:' + order);
            entry.removeTopic();
            board.update(entry);
        },

        removeTopicFromBoard : function(topic) {
            var position = topic.getPosition();
            var order = topic.getOrder();

            this._removeEntryByOrder(order, position);
            topic.setOrder(null);
        },

        positionateDragTopic :function(dragTopic) {
            throw "this method must be overrided";
        },

        getHeight: function() {
            var board = this._getBoard();
            return board.getHeight();
        }
    }
);

