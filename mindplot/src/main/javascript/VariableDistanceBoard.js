/*
*    Copyright [2011] [wisemapping]
*
*   Licensed under the Apache License, Version 2.0 (the "License") plus the
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

mindplot.VariableDistanceBoard = function(defaultHeight, referencePoint)
{
    mindplot.VariableDistanceBoard.superClass.initialize.call(this, defaultHeight, referencePoint);

    var zeroEntryCoordinate = referencePoint.y;
    var entry = this.createBoardEntry(zeroEntryCoordinate - (defaultHeight / 2), zeroEntryCoordinate + (defaultHeight / 2), 0);
    this._entries.set(0, entry);
};


objects.extend(mindplot.VariableDistanceBoard, mindplot.Board);

mindplot.VariableDistanceBoard.prototype.lookupEntryByOrder = function(order)
{
    var entries = this._entries;
    var index = this._orderToIndex(order);

    var result = entries.get(index);
    if (!result)
    {
        // I've not found a entry. I have to create a new one.
        var i = 1;
        var zeroEntry = entries.get(0);
        var distance = zeroEntry.getWidth() / 2;
        var indexSign = Math.sign(index);
        var absIndex = Math.abs(index);
        while (i < absIndex)
        {
            // Move to the next entry ...
            var width;
            var entry = entries.get(i, indexSign);
            if (entry != null)
            {
                distance += entry.getWidth();
            } else
            {
                distance += this._defaultWidth;
            }

            i++;
        }

        // Caculate limits ...
        var upperLimit = -1;
        var lowerLimit = -1;
        var offset = zeroEntry.workoutEntryYCenter();
        if (index >= 0)
        {
            lowerLimit = offset + distance;
            upperLimit = lowerLimit + this._defaultWidth;
        } else
        {
            upperLimit = offset - distance;
            lowerLimit = upperLimit - this._defaultWidth;
        }

        result = this.createBoardEntry(lowerLimit, upperLimit, order);
    }
    return result;
};

mindplot.VariableDistanceBoard.prototype.createBoardEntry = function(lowerLimit, upperLimit, order)
{
    return  new mindplot.BoardEntry(lowerLimit, upperLimit, order);
};

mindplot.VariableDistanceBoard.prototype.updateReferencePoint = function(position)
{
    var entries = this._entries;
    var referencePoint = this._referencePoint;

    // Update zero entry current position.
    this._referencePoint = position.clone();
    var yOffset = position.y - referencePoint.y;

    var i = -entries.lowerLength();
    for (; i <= entries.length(1); i++)
    {
        var entry = entries.get(i);
        if (entry != null)
        {
            var upperLimit = entry.getUpperLimit() + yOffset;
            var lowerLimit = entry.getLowerLimit() + yOffset;
            entry.setUpperLimit(upperLimit);
            entry.setLowerLimit(lowerLimit);

            // Update topic position ...
            if (!entry.isAvailable())
            {
                var topic = entry.getTopic();
                var topicPosition = topic.getPosition();
                topicPosition.y = topicPosition.y + yOffset;

                // MainTopicToCentral must be positioned based on the referencePoint.
                var xOffset = position.x - referencePoint.x;
                topicPosition.x = topicPosition.x + xOffset;

                topic.setPosition(topicPosition);
            }
        }
    }
};


mindplot.VariableDistanceBoard.prototype.lookupEntryByPosition = function(pos)
{
    core.assert(core.Utils.isDefined(pos), 'position can not be null');
    var entries = this._entries;
    var zeroEntry = entries.get(0);
    if (zeroEntry.isCoordinateIn(pos.y))
    {
        return zeroEntry;
    }

    // Is Upper or lower ?
    var sign = -1;
    if (pos.y >= zeroEntry.getUpperLimit())
    {
        sign = 1;
    }

    var i = 1;
    var tempEntry = this.createBoardEntry();
    var currentEntry = zeroEntry;
    while (true)
    {
        // Move to the next entry ...
        var index = i * sign;
        var entry = entries.get(index);
        if (entry)
        {
            currentEntry = entry;
        } else
        {
            // Calculate boundaries...
            var lowerLimit, upperLimit;
            if (sign > 0)
            {
                lowerLimit = currentEntry.getUpperLimit();
                upperLimit = lowerLimit + this._defaultWidth;
            }
            else
            {
                upperLimit = currentEntry.getLowerLimit();
                lowerLimit = upperLimit - this._defaultWidth;
            }

            // Update current entry.
            currentEntry = tempEntry;
            currentEntry.setLowerLimit(lowerLimit);
            currentEntry.setUpperLimit(upperLimit);

            var order = this._indexToOrder(index);
            currentEntry.setOrder(order);
        }

        // Have I found the item?
        if (currentEntry.isCoordinateIn(pos.y))
        {
            break;
        }
        i++;
    }
    return currentEntry;
};


mindplot.VariableDistanceBoard.prototype.update = function(entry)
{
    core.assert(entry, 'Entry can not be null');
    var order = entry.getOrder();
    var index = this._orderToIndex(order);

    this._entries.set(index, entry);

};


mindplot.VariableDistanceBoard.prototype.getLastNoAvailalbleEntry = function()
{
    var entries = this._entries;
    var lowerLength = entries.lowerLength();
    var upperLength = entries.upperLength();

    var result = null;
    var i = -lowerLength;
    while (i <= upperLength)
    {
        var entry = entries.get(i);
        if (entry && !entry.isAvailable())
        {
            result = entry;
            break;
        }
        i++;
    }
    return result;

};

mindplot.VariableDistanceBoard.prototype.getFirstNoAvailableEntry = function()
{
    var entries = this._entries;
    var lowerLength = -entries.lowerLength();
    var upperLength = entries.upperLength();

    var result = null;
    var i = upperLength;
    while (i >= lowerLength)
    {
        var entry = entries.get(i);
        if (entry && !entry.isAvailable())
        {
            result = entry;
            break;
        }
        i--;
    }
    return result;
};


mindplot.VariableDistanceBoard.prototype.freeEntry = function(entry)
{
    var order = entry.getOrder();
    var entries = this._entries;

    var index = this._orderToIndex(order);
    var indexSign = Math.sign(index);
    var lenght = entries.length(index);

    var currentTopic = entry.getTopic();
    var i = Math.abs(index) + 1;
    while (currentTopic)
    {
        var e = entries.get(i, indexSign);
        if (currentTopic && !e)
        {
            var entryOrder = this._indexToOrder(i * indexSign);
            e = this.lookupEntryByOrder(entryOrder);
        }

        // Move the topic to the next entry ...
        var topic = null;
        if (e)
        {
            topic = e.getTopic();
            if (currentTopic)
            {
                e.setTopic(currentTopic);
            }
            this.update(e);
        }
        currentTopic = topic;
        i++;
    }

    // Clear the entry topic ...
    entry.setTopic(null);
};

mindplot.VariableDistanceBoard.prototype._orderToIndex = function(order)
{
    var index = Math.round(order / 2);
    return ((order % 2) == 0) ? index : -index;
};
mindplot.VariableDistanceBoard.prototype._indexToOrder = function(index)
{
    var order = Math.abs(index) * 2;
    return (index >= 0)? order: order - 1;
};

mindplot.VariableDistanceBoard.prototype.inspect = function()
{
    return this._entries.inspect();
};

