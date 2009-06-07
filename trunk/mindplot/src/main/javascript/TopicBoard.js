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

mindplot.TopicBoard = function()
{
    this._height = null;
};

mindplot.TopicBoard.prototype._removeEntryByOrder = function(order, position)
{
    var board = this._getBoard(position);
    var entry = board.lookupEntryByOrder(order);

    core.assert(!entry.isAvailable(), 'Entry must not be available in order to be removed.Entry Order:' + order);
    entry.removeTopic();
    board.update(entry);
};

mindplot.TopicBoard.prototype.removeTopicFromBoard = function(topic)
{
    var position = topic.getPosition();
    var order = topic.getOrder();

    this._removeEntryByOrder(order, position);
    topic.setOrder(null);
};

mindplot.TopicBoard.prototype.positionateDragTopic = function(dragTopic)
{
    throw "this method must be overrided";
};

mindplot.TopicBoard.prototype.getHeight = function()
{
    var board = this._getBoard();
    return board.getHeight();
};