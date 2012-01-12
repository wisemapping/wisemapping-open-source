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

mindplot.DragTopicPositioner = new Class({
    initialize:function(designerModel, workspace) {
        $assert(designerModel, 'designerModel can not be null');
        $assert(workspace, 'workspace can not be null');

        // this._layoutManager = layoutManager;
        this._designerModel = designerModel;
        this._workspace = workspace;
    },

    positionateDragTopic : function(dragTopic) {
        // Workout the real position of the element on the board.
        var dragTopicPosition = dragTopic.getPosition();
        var draggedTopic = dragTopic.getDraggedTopic();

        // Topic can be connected ?
        this._checkDragTopicConnection(dragTopic);

        // Position topic in the board
        if (dragTopic.isConnected()) {
            var targetTopic = dragTopic.getConnectedToTopic();
            // @todo: Hack ...
            var position = designer._eventBussDispatcher._layoutManager.predict(targetTopic.getId(),dragTopicPosition);
            console.log(position);
        }
    },

    _checkDragTopicConnection : function(dragTopic) {
        var topics = this._designerModel.getTopics();

        // Must be disconnected from their current connection ?.
        var mainTopicToMainTopicConnection = this._lookUpForMainTopicToMainTopicConnection(dragTopic);
        var currentConnection = dragTopic.getConnectedToTopic();
        if ($defined(currentConnection)) {
            // MainTopic->MainTopicConnection.
            if (currentConnection.getType() == mindplot.model.INodeModel.MAIN_TOPIC_TYPE) {
                if (mainTopicToMainTopicConnection != currentConnection) {
                    dragTopic.disconnect(this._workspace);
                }
            }
            else if (currentConnection.getType() == mindplot.model.INodeModel.CENTRAL_TOPIC_TYPE) {
                // Distance if greater that the allowed.
                var dragXPosition = dragTopic.getPosition().x;
                var currentXPosition = currentConnection.getPosition().x;

                if ($defined(mainTopicToMainTopicConnection)) {
                    // I have to change the current connection to a main topic.
                    dragTopic.disconnect(this._workspace);
                } else
                if (Math.abs(dragXPosition - currentXPosition) > mindplot.DragTopicPositioner.CENTRAL_TO_MAINTOPIC_MAX_HORIZONTAL_DISTANCE) {
                    dragTopic.disconnect(this._workspace);
                }
            }
        }

        // Finally, connect nodes ...
        if (!dragTopic.isConnected()) {
            var centalTopic = topics[0];
            if ($defined(mainTopicToMainTopicConnection)) {
                dragTopic.connectTo(mainTopicToMainTopicConnection);
            } else if (Math.abs(dragTopic.getPosition().x - centalTopic.getPosition().x) <= mindplot.DragTopicPositioner.CENTRAL_TO_MAINTOPIC_MAX_HORIZONTAL_DISTANCE) {
                dragTopic.connectTo(centalTopic);
            }
        }
    },

    _lookUpForMainTopicToMainTopicConnection : function(dragTopic) {
        var topics = this._designerModel.getTopics();
        var result = null;
        var draggedNode = dragTopic.getDraggedTopic();
        var distance = null;

        // Check MainTopic->MainTopic connection...
        for (var i = 0; i < topics.length; i++) {
            var targetTopic = topics[i];
            var position = dragTopic.getPosition();
            if (targetTopic.getType() != mindplot.model.INodeModel.CENTRAL_TOPIC_TYPE && targetTopic != draggedNode) {
                var canBeConnected = dragTopic.canBeConnectedTo(targetTopic);
                if (canBeConnected) {
                    var targetPosition = targetTopic.getPosition();
                    var fix = position.y > targetPosition.y;
                    var gap = 0;
                    if (targetTopic._getChildren().length > 0) {
                        gap = Math.abs(targetPosition.y - targetTopic._getChildren()[0].getPosition().y)
                    }
                    var yDistance = Math.abs(position.y - fix * gap - targetPosition.y);
                    if (distance == null || yDistance < distance) {
                        result = targetTopic;
                        distance = yDistance;
                    }

                }
            }
        }
        return result;
    }
});

mindplot.DragTopicPositioner.CENTRAL_TO_MAINTOPIC_MAX_HORIZONTAL_DISTANCE = 400;
