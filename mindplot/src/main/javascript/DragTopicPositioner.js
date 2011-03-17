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

mindplot.DragTopicPositioner = function(layoutManager)
{
    core.assert(layoutManager, 'layoutManager can not be null');
    this._layoutManager = layoutManager;
    this._topics = layoutManager.getDesigner()._getTopics();
    this._workspace = layoutManager.getDesigner().getWorkSpace();
};

mindplot.DragTopicPositioner.prototype.positionateDragTopic = function(dragTopic)
{
    // Workout the real position of the element on the board.
    var dragTopicPosition = dragTopic.getPosition();
    var draggedTopic = dragTopic.getDraggedTopic();

    // Topic can be connected ?
    this._checkDragTopicConnection(dragTopic);

    // Position topic in the board
    if (dragTopic.isConnected())
    {
        var targetTopic = dragTopic.getConnectedToTopic();
        var topicBoard = this._layoutManager.getTopicBoardForTopic(targetTopic);
        topicBoard.positionateDragTopic(dragTopic);
    }
};

mindplot.DragTopicPositioner.CENTRAL_TO_MAINTOPIC_MAX_HORIZONTAL_DISTANCE = 300;

mindplot.DragTopicPositioner.prototype._checkDragTopicConnection = function(dragTopic)
{
    var topics = this._topics;

    // Must be disconnected from their current connection ?.
    var mainTopicToMainTopicConnection = this._lookUpForMainTopicToMainTopicConnection(dragTopic);
    var currentConnection = dragTopic.getConnectedToTopic();
    if (currentConnection)
    {
        // MainTopic->MainTopicConnection.
        if (currentConnection.getType()==mindplot.NodeModel.MAIN_TOPIC_TYPE)
        {
            if(mainTopicToMainTopicConnection != currentConnection)
            {
                dragTopic.disconnect(this._workspace);
            }
        }
        else if (currentConnection.getType() == mindplot.NodeModel.CENTRAL_TOPIC_TYPE)
        {
            // Distance if greater that the allowed.
            var dragXPosition = dragTopic.getPosition().x;
            var currentXPosition = currentConnection.getPosition().x;

            if(mainTopicToMainTopicConnection)
            {
                // I have to change the current connection to a main topic.
                dragTopic.disconnect(this._workspace);
            }else
            if (Math.abs(dragXPosition-currentXPosition) > mindplot.DragTopicPositioner.CENTRAL_TO_MAINTOPIC_MAX_HORIZONTAL_DISTANCE)
            {
                dragTopic.disconnect(this._workspace);
            }
        }
    }

    // Finally, connect nodes ...
    if (!dragTopic.isConnected())
    {
        var centalTopic = topics[0];
        if (mainTopicToMainTopicConnection)
        {
            dragTopic.connectTo(mainTopicToMainTopicConnection);
        } else if (Math.abs(dragTopic.getPosition().x - centalTopic.getPosition().x) <= mindplot.DragTopicPositioner.CENTRAL_TO_MAINTOPIC_MAX_HORIZONTAL_DISTANCE)
        {
            dragTopic.connectTo(centalTopic);
        }
    }
};

mindplot.DragTopicPositioner.prototype._lookUpForMainTopicToMainTopicConnection = function(dragTopic)
{
    var topics = this._topics;
    var result = null;
    var clouserDistance = -1;
    var draggedNode = dragTopic.getDraggedTopic();

    // Check MainTopic->MainTopic connection...
    for (var i = 0; i < topics.length; i++)
    {
        var targetTopic = topics[i];
        var position = dragTopic.getPosition();
        if (targetTopic.getType() != mindplot.NodeModel.CENTRAL_TOPIC_TYPE && targetTopic != draggedNode)
        {
            var canBeConnected = dragTopic.canBeConnectedTo(targetTopic);
            if (canBeConnected)
            {
                result = targetTopic;
                break;
            }
        }
    }
    return result;
};