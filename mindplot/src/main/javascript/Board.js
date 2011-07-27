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

mindplot.Board = new Class({
    initialize : function(defaultHeight, referencePoint) {
        $assert(referencePoint, "referencePoint can not be null");
        this._defaultWidth = defaultHeight;
        this._entries = new mindplot.BidirectionalArray();
        this._referencePoint = referencePoint;
    },

    getReferencePoint : function() {
        return this._referencePoint;
    },

    _removeEntryByOrder : function(order, position) {
        var board = this._getBoard(position);
        var entry = board.lookupEntryByOrder(order);

        $assert(!entry.isAvailable(), 'Entry must not be available in order to be removed.Entry Order:' + order);
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
});

/**
 * ---------------------------------------
 */
mindplot.BidirectionalArray = new Class({

    initialize: function() {
        this._leftElem = [];
        this._rightElem = [];
    },

    get :function(index, sign) {
        $assert($defined(index), 'Illegal argument, index must be passed.');
        if ($defined(sign)) {
            $assert(index >= 0, 'Illegal absIndex value');
            index = index * sign;
        }

        var result = null;
        if (index >= 0 && index < this._rightElem.length) {
            result = this._rightElem[index];
        } else if (index < 0 && Math.abs(index) < this._leftElem.length) {
            result = this._leftElem[Math.abs(index)];
        }
        return result;
    },

    set : function(index, elem) {
        $assert($defined(index), 'Illegal index value');

        var array = (index >= 0) ? this._rightElem : this._leftElem;
        array[Math.abs(index)] = elem;
    },

    length : function(index) {
        $assert($defined(index), 'Illegal index value');
        return (index >= 0) ? this._rightElem.length : this._leftElem.length;
    },

    upperLength : function() {
        return this.length(1);
    },

    lowerLength : function() {
        return this.length(-1);
    },

    inspect : function() {
        var result = '{';
        var lenght = this._leftElem.length;
        for (var i = 0; i < lenght; i++) {
            var entry = this._leftElem[lenght - i - 1];
            if (entry != null) {
                if (i != 0) {
                    result += ', ';
                }
                result += entry.inspect();
            }
        }

        lenght = this._rightElem.length;
        for (var i = 0; i < lenght; i++) {
            var entry = this._rightElem[i];
            if (entry != null) {
                if (i != 0) {
                    result += ', ';
                }
                result += entry.inspect();
            }
        }
        result += '}';

        return result;

    }
});