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

mindplot.CentralTopicBoard = new Class({
    Extends: mindplot.Board,
    initialize:function(centralTopic, layoutManager) {
        var point = new core.Point(0, 0);
        this._layoutManager = layoutManager;
        this._rightBoard = new mindplot.VariableDistanceBoard(50, point);
        this._leftBoard = new mindplot.VariableDistanceBoard(50, point);
        this._centralTopic = centralTopic;
    },

    _getBoard : function(position) {
        return (position.x >= 0) ? this._rightBoard : this._leftBoard;
    },

    positionateDragTopic : function(dragTopic) {
        $assert(dragTopic != null, 'dragTopic can not be null');
        $assert(dragTopic.isDragTopic, 'dragTopic must be DragTopic instance');

        // This node is a main topic node. Position
        var dragPos = dragTopic.getPosition();
        var board = this._getBoard(dragPos);

        // Look for entry  ...
        var entry = board.lookupEntryByPosition(dragPos);

        // Calculate 'y' position base on the entry ...
        var yCoord;
        if (!entry.isAvailable() && entry.getTopic() != dragTopic.getDraggedTopic()) {
            yCoord = entry.getLowerLimit();
        } else {
            yCoord = entry.workoutEntryYCenter();
        }


        // MainTopic can not be positioned over the drag topic ...
        var centralTopic = this._centralTopic;
        var centralTopicSize = centralTopic.getSize();
        var halfWidth = (centralTopicSize.width / 2);
        if (Math.abs(dragPos.x) < halfWidth + 60) {
            var distance = halfWidth + 60;
            dragPos.x = (dragPos.x > 0) ? distance : -distance;
        }

        // Update board position.
        var pivotPos = new core.Point(dragPos.x, yCoord);
        dragTopic.setBoardPosition(pivotPos);
    },


    addBranch : function(topic) {
        // Update topic position ...
        var position = topic.getPosition();

        var order = topic.getOrder();
        var board = this._getBoard(position);
        var entry = null;
        if (order != null) {
            entry = board.lookupEntryByOrder(order);
        } else {
            entry = board.lookupEntryByPosition(position);
        }

        // If the entry is not available, I must swap the the entries...
        if (!entry.isAvailable()) {
            board.freeEntry(entry);
        }

        // Add it to the board ...
        entry.setTopic(topic);
        board.update(entry);
    },

    updateChildrenPosition : function(topic, xOffset, modifiedTopics) {
        var board = this._rightBoard;
        var oldReferencePosition = board.getReferencePoint();
        var newReferencePosition = new core.Point(oldReferencePosition.x + xOffset, oldReferencePosition.y);
        board.updateReferencePoint(newReferencePosition);

        board = this._leftBoard;
        oldReferencePosition = board.getReferencePoint();
        newReferencePosition = new core.Point(oldReferencePosition.x - xOffset, oldReferencePosition.y);
        board.updateReferencePoint(newReferencePosition);
    },

    repositionate : function() {
        //@todo: implement ..
    }
});