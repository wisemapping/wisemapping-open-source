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

mindplot.FixedDistanceBoard = function(defaultHeight, topic)
{
    this._topic = topic;
    var reference = topic.getPosition();
    mindplot.FixedDistanceBoard.superClass.initialize.call(this, defaultHeight, reference);
    this._height = defaultHeight;
    this._entries = [];
};

objects.extend(mindplot.FixedDistanceBoard, mindplot.Board);

mindplot.FixedDistanceBoard.prototype.getHeight = function()
{
    return this._height;
};

mindplot.FixedDistanceBoard.prototype.lookupEntryByOrder = function(order)
{
    var result = null;
    var entries = this._entries;
    if (order < entries.length)
    {
        result = entries[order];
    }

    if (result == null)
    {
        var defaultHeight = this._defaultWidth;
        var reference = this.getReferencePoint();
        if (entries.length == 0)
        {
            var yReference = reference.y;
            result = this.createBoardEntry(yReference - (defaultHeight / 2), yReference + (defaultHeight / 2), 0);
        } else
        {
            var entriesLenght = entries.length;
            var lastEntry = entries[entriesLenght - 1];
            var lowerLimit = lastEntry.getUpperLimit();
            var upperLimit = lowerLimit + defaultHeight;
            result = this.createBoardEntry(lowerLimit, upperLimit, entriesLenght + 1);
        }
    }
    return result;
};

mindplot.FixedDistanceBoard.prototype.createBoardEntry = function(lowerLimit, upperLimit, order)
{
    var result = new mindplot.BoardEntry(lowerLimit, upperLimit, order);
    var xPos = this.workoutXBorderDistance();
    result.setXPosition(xPos);
    return result;
};

mindplot.FixedDistanceBoard.prototype.updateReferencePoint = function()
{
    var entries = this._entries;
    var parentTopic = this.getTopic();
    var parentPosition = parentTopic.workoutIncomingConnectionPoint(parentTopic.getPosition());
    var referencePoint = this.getReferencePoint();
    var yOffset = parentPosition.y - referencePoint.y;

    for (var i = 0; i < entries.length; i++)
    {
        var entry = entries[i];

        if(core.Utils.isDefined(entry)){
            var upperLimit = entry.getUpperLimit() + yOffset;
            var lowerLimit = entry.getLowerLimit() + yOffset;
            entry.setUpperLimit(upperLimit);
            entry.setLowerLimit(lowerLimit);

            // Fix x position ...
            var xPos = this.workoutXBorderDistance();
            entry.setXPosition(xPos);
            entry.update();
        }
    }
    this._referencePoint = parentPosition.clone();

};

mindplot.FixedDistanceBoard.MAIN_TOPIC_TO_MAIN_TOPIC_DISTANCE = 20;


/**
 * This x distance does't take into account the size of the shape.
 */
mindplot.FixedDistanceBoard.prototype.workoutXBorderDistance = function()
{
    var topic = this.getTopic();

    var topicPosition = topic.getPosition();
    var topicSize = topic.getSize();
    var halfTargetWidth = topicSize.width / 2;
    var result;
    if (topicPosition.x >= 0)
    {
        // It's at right.
        result = topicPosition.x + halfTargetWidth + mindplot.FixedDistanceBoard.MAIN_TOPIC_TO_MAIN_TOPIC_DISTANCE;
    } else
    {
        result = topicPosition.x - (halfTargetWidth + mindplot.FixedDistanceBoard.MAIN_TOPIC_TO_MAIN_TOPIC_DISTANCE);
    }
    return result;
};

mindplot.FixedDistanceBoard.prototype.getTopic = function()
{
    return this._topic;
};

mindplot.FixedDistanceBoard.INTER_TOPIC_DISTANCE = 6;

mindplot.FixedDistanceBoard.prototype.freeEntry = function(entry)
{
    var newEntries = [];
    var entries = this._entries;
    var order = 0;
    for (var i = 0; i < entries.length; i++)
    {
        var e = entries[i];
        if (e == entry)
        {
            order++;
        }
        newEntries[order] = e;
        order++;
    }
    this._entries = newEntries;
};

mindplot.FixedDistanceBoard.prototype.repositionate = function()
{
    // Workout width and update topic height.
    var entries = this._entries;
    var height = 0;
    var model = this._topic.getModel();
    if (entries.length >= 1 && !model.areChildrenShrinked())
    {
        for (var i = 0; i < entries.length; i++)
        {
            var e = entries[i];
            if (e && e.getTopic())
            {
                var topic = e.getTopic();
                var topicBoard = topic.getTopicBoard();
                var topicBoardHeight = topicBoard.getHeight();


                height += topicBoardHeight + mindplot.FixedDistanceBoard.INTER_TOPIC_DISTANCE;
            }
        }
    }
    else {
        var topic = this._topic;
        height = topic.getSize().height + mindplot.FixedDistanceBoard.INTER_TOPIC_DISTANCE;
    }

    var oldHeight = this._height;
    this._height = height;

    // I must update all the parent nodes first...
    if (oldHeight != this._height)
    {
        var topic = this._topic;
        var parentTopic = topic.getParent();
        if (parentTopic != null)
        {
            var board = parentTopic.getTopicBoard();
            board.repositionate();
        }
    }


    // @todo: Esto hace backtraking. Hay que cambiar la implementacion del set position de
    // forma tal que no se mande a hacer el update de todos los hijos.

    // Workout center the new topic center...
    var refence = this.getReferencePoint();
    var lowerLimit;
    if (entries.length > 0)
    {
        var topic = entries[0].getTopic();
        var firstNodeHeight = topic.getSize().height;
        lowerLimit = refence.y - (height / 2) - (firstNodeHeight / 2) + 1;
    }

    var upperLimit = null;

    // Start moving all the elements ...
    var newEntries = [];
    var order = 0;
    for (var i = 0; i < entries.length; i++)
    {
        var e = entries[i];
        if (e && e.getTopic())
        {

            var currentTopic = e.getTopic();
            e.setLowerLimit(lowerLimit);

            // Update entry ...
            var topicBoard = currentTopic.getTopicBoard();
            var topicBoardHeight = topicBoard.getHeight();

            upperLimit = lowerLimit + topicBoardHeight + mindplot.FixedDistanceBoard.INTER_TOPIC_DISTANCE;
            e.setUpperLimit(upperLimit);
            lowerLimit = upperLimit;

            e.setOrder(order);
            currentTopic.setOrder(order);

            e.update();
            newEntries[order] = e;
            order++;
        }
    }
    this._entries = newEntries;
};

mindplot.FixedDistanceBoard.prototype.removeTopic = function(topic)
{
    var order = topic.getOrder();
    var entry = this.lookupEntryByOrder(order);
    core.assert(!entry.isAvailable(), "Illegal state");

    entry.setTopic(null);
    topic.setOrder(null);
    this._entries.remove(entry);

    // Repositionate all elements ...
    this.repositionate();
};

mindplot.FixedDistanceBoard.prototype.addTopic = function(order, topic)
{

    // If the entry is not available, I must swap the the entries...
    var entry = this.lookupEntryByOrder(order);
    if (!entry.isAvailable())
    {
        this.freeEntry(entry);
        // Create a dummy entry ...
        // Puaj, do something with this...
        entry = this.createBoardEntry(-1, 0, order);
        this._entries[order] = entry;
    }
    this._entries[order] = entry;

    // Add to the board ...
    entry.setTopic(topic, false);

    // Repositionate all elements ...
    this.repositionate();
};

mindplot.FixedDistanceBoard.prototype.lookupEntryByPosition = function(pos)
{
    core.assert(core.Utils.isDefined(pos), 'position can not be null');

    var entries = this._entries;
    var result = null;
    for (var i = 0; i < entries.length; i++)
    {
        var entry = entries[i];
        if (pos.y < entry.getUpperLimit() && pos.y >= entry.getLowerLimit())
        {
            result = entry;
        }
    }

    if (result == null)
    {
        var defaultHeight = this._defaultWidth;
        if (entries.length == 0)
        {
            var reference = this.getReferencePoint();
            var yReference = reference.y;
            result = this.createBoardEntry(yReference - (defaultHeight / 2), yReference + (defaultHeight / 2), 0);
        } else
        {
            var firstEntry = entries[0];
            if (pos.y < firstEntry.getLowerLimit())
            {
                var upperLimit = firstEntry.getLowerLimit();
                var lowerLimit = upperLimit + defaultHeight;
                result = this.createBoardEntry(lowerLimit, upperLimit, -1);
            } else
            {
                var entriesLenght = entries.length;
                var lastEntry = entries[entriesLenght - 1];
                var lowerLimit = lastEntry.getUpperLimit();
                var upperLimit = lowerLimit + defaultHeight;
                result = this.createBoardEntry(lowerLimit, upperLimit, entriesLenght);
            }
        }
    }

    return result;
};
