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

mindplot.layout.boards.original.MainTopicBoard = new Class({
    Extends:mindplot.layout.boards.original.Board,
    initialize:function(topic, layoutManager) {
        this._layoutManager = layoutManager;
        this._topic = topic;
        this._board = null;
        this._height = 0;
    },


    _getBoard: function() {
        if (!$defined(this._board)) {
            var topic = this._topic;
            this._board = new mindplot.layout.boards.original.FixedDistanceBoard(mindplot.MainTopic.DEFAULT_MAIN_TOPIC_HEIGHT, topic, this._layoutManager);
        }
        return this._board;
    },

    updateReferencePoint : function(position) {
        this._board.updateReferencePoint(position);
    },

    updateChildrenPosition : function(topic) {
        var board = this._getBoard();
        board.updateReferencePoint();
    },

    positionateDragTopic : function(dragTopic) {
        $assert(dragTopic != null, 'dragTopic can not be null');
        $assert(dragTopic.isDragTopic, 'dragTopic must be DragTopic instance');

        // This node is a main topic node. Position
        var dragPos = dragTopic.getPosition();
        var board = this._getBoard();

        // Look for entry  ...
        var entry = board.lookupEntryByPosition(dragPos);

        // Calculate 'y' position base on the entry ...
        var yCoord;
        if (!entry.isAvailable() && entry.getTopic() != dragTopic.getDraggedTopic()) {
            yCoord = entry.getLowerLimit();
        } else {
            yCoord = entry.workoutEntryYCenter();
        }

        // Update board position.
        var targetTopic = dragTopic.getConnectedToTopic();
        var xCoord = this._workoutXBorderDistance(targetTopic);

        // Add the size of the pivot to the distance ...
        var halfPivotWidth = mindplot.DragTopic.PIVOT_SIZE.width / 2;
        xCoord = xCoord + ((dragPos.x > 0) ? halfPivotWidth : -halfPivotWidth);

        var pivotPos = new core.Point(xCoord, yCoord);
        dragTopic.setBoardPosition(pivotPos);

        var order = entry.getOrder();
        dragTopic.setOrder(order);
    },

    /**
     * This x distance doesn't take into account the size of the shape.
     */
    _workoutXBorderDistance : function(topic) {
        $assert(topic, 'topic can not be null');
        var board = this._getBoard();
        return board.workoutXBorderDistance(topic);
    },

    addBranch : function(topic) {
        var order = topic.getOrder();
        $assert($defined(order), "Order must be defined");

        // If the entry is not available, I must swap the the entries...
        var board = this._getBoard();
        var entry = board.lookupEntryByOrder(order);
        if (!entry.isAvailable()) {
            board.freeEntry(entry);
        }

        // Add the topic to the board ...
        board.addTopic(order, topic);

        // Repositionate all the parent topics ...
        var currentTopic = this._topic;
        if (currentTopic.getOutgoingConnectedTopic()) {
            var parentTopic = currentTopic.getOutgoingConnectedTopic();
            mindplot.EventBus.instance.fireEvent(mindplot.EventBus.events.NodeRepositionateEvent, [parentTopic]);
        }
    },

    repositionate : function() {
        var board = this._getBoard();
        board.repositionate();
    },

    removeTopicFromBoard : function(topic) {
        var board = this._getBoard();
        board.removeTopic(topic);

        // Repositionate all the parent topics ...
        var parentTopic = this._topic;
        if (parentTopic.getOutgoingConnectedTopic()) {
            var connectedTopic = parentTopic.getOutgoingConnectedTopic();
            mindplot.EventBus.instance.fireEvent(mindplot.EventBus.events.NodeRepositionateEvent, [connectedTopic]);
        }
    }
});