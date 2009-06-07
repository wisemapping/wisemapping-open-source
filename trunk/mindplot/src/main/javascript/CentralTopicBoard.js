/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License.  You may obtain a copy of the License at
*
*       http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*
* $Id: file 64488 2006-03-10 17:32:09Z paulo $
*/

mindplot.CentralTopicBoard = function(centralTopic)
{
    var point = new core.Point(0, 0);
    this._rightBoard = new mindplot.VariableDistanceBoard(50, point);
    this._leftBoard = new mindplot.VariableDistanceBoard(50, point);
    this._centralTopic = centralTopic;
};

objects.extend(mindplot.CentralTopicBoard, mindplot.TopicBoard);

mindplot.CentralTopicBoard.prototype._getBoard = function(position)
{
    return (position.x >= 0) ? this._rightBoard : this._leftBoard;
};

mindplot.CentralTopicBoard.prototype._updateHeight = function()
{

};


mindplot.CentralTopicBoard.prototype.positionateDragTopic = function(dragTopic)
{
    core.assert(dragTopic != null, 'dragTopic can not be null');
    core.assert(dragTopic.isDragTopic, 'dragTopic must be DragTopic instance');

    // This node is a main topic node. Position
    var dragPos = dragTopic.getPosition();
    var board = this._getBoard(dragPos);

    // Look for entry  ...
    var entry = board.lookupEntryByPosition(dragPos);

    // Calculate 'y' position base on the entry ...
    var yCoord;
    if (!entry.isAvailable() && entry.getTopic() != dragTopic.getDraggedTopic())
    {
        yCoord = entry.getLowerLimit();
    } else
    {
        yCoord = entry.workoutEntryYCenter();
    }


    // MainTopic can not be positioned over the drag topic ...
    var centralTopic = this._centralTopic;
    var centralTopicSize = centralTopic.getSize();
    var halfWidth = (centralTopicSize.width / 2);
    if (Math.abs(dragPos.x) < halfWidth + 60)
    {
        var distance = halfWidth + 60;
        dragPos.x = (dragPos.x > 0)? distance:-distance;
    }

    // Update board position.
    var pivotPos = new core.Point(dragPos.x, yCoord);
    dragTopic.setBoardPosition(pivotPos);
};


mindplot.CentralTopicBoard.prototype.addBranch = function(topic)
{
    // Update topic position ...
    var position = topic.getPosition();

    var order = topic.getOrder();
    var board = this._getBoard(position);
    var entry = null;
    if (order != null)
    {
        entry = board.lookupEntryByOrder(order);
    } else
    {
        entry = board.lookupEntryByPosition(position);
    }

    // If the entry is not available, I must swap the the entries...
    if (!entry.isAvailable())
    {
        board.freeEntry(entry);
    }

    // Add it to the board ...
    entry.setTopic(topic);
    board.update(entry);
};

mindplot.CentralTopicBoard.prototype.updateChildrenPosition = function(topic, xOffset)
{
    var board = this._rightBoard;
    var oldReferencePosition = board.getReferencePoint();
    var newReferencePosition = new core.Point(oldReferencePosition.x + xOffset, oldReferencePosition.y);
    board.updateReferencePoint(newReferencePosition);

    board = this._leftBoard;
    oldReferencePosition = board.getReferencePoint();
    newReferencePosition = new core.Point(oldReferencePosition.x - xOffset, oldReferencePosition.y);
    board.updateReferencePoint(newReferencePosition);
};

mindplot.CentralTopicBoard.prototype.repositionate = function()
{
    //@todo: implement ..
};