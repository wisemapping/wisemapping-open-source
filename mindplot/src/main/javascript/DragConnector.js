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

mindplot.DragConnector = new Class({
    initialize:function (designerModel, workspace) {
        $assert(designerModel, 'designerModel can not be null');
        $assert(workspace, 'workspace can not be null');

        // this._layoutManager = layoutManager;
        this._designerModel = designerModel;
        this._workspace = workspace;
    },

    checkConnection:function (dragTopic) {
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
                } else if (Math.abs(dragXPosition - currentXPosition) > mindplot.DragConnector.CENTRAL_TO_MAINTOPIC_MAX_HORIZONTAL_DISTANCE) {
                    dragTopic.disconnect(this._workspace);
                }
            }
        }

        // Finally, connect nodes ...
        if (!dragTopic.isConnected()) {
            var centralTopic = topics[0];
            if ($defined(mainTopicToMainTopicConnection)) {
                dragTopic.connectTo(mainTopicToMainTopicConnection);
            } else if (Math.abs(dragTopic.getPosition().x - centralTopic.getPosition().x) <= mindplot.DragConnector.CENTRAL_TO_MAINTOPIC_MAX_HORIZONTAL_DISTANCE) {
                dragTopic.connectTo(centralTopic);
            }
        }
    },

    _lookUpForMainTopicToMainTopicConnection:function (dragTopic) {
        var topics = this._designerModel.getTopics();

        // Filter all the nodes that are outside the boundary ...
        var draggedNode = dragTopic.getDraggedTopic();
        var sourcePosition = dragTopic.getPosition();
        topics = topics.filter(function (topic) {
            var pos = topic.getPosition();
            return   (sourcePosition.x - pos.x) * Math.sign(pos.x) > 0 && draggedNode != topic;
        });

        // Topics must be ordered based on the distance vertical distance to the drag topic ...
        topics = topics.sort(function (a, b) {
            var aPos = a.getPosition();
            var ad = (sourcePosition.x - aPos.x) * Math.sign(aPos.x);

            var bPos = b.getPosition();
            var bd = (sourcePosition.x - bPos.x) * Math.sign(bPos.x);

            return  ad > bd;
        });


        // Check MainTopic->MainTopic connection...
        var result = null;
        var distance = null;
        for (var i = 0; i < topics.length; i++) {
            var position = dragTopic.getPosition();
            var targetTopic = topics[i];

            var canBeConnected = dragTopic.canBeConnectedTo(targetTopic);
            if (canBeConnected) {
                var targetPosition = targetTopic.getPosition();
                var fix = position.y > targetPosition.y;
                var gap = 0;
                if (targetTopic.getChildren().length > 0) {
                    gap = Math.abs(targetPosition.y - targetTopic.getChildren()[0].getPosition().y)
                }

                var yDistance = Math.abs(position.y - fix * gap - targetPosition.y);
                if (distance == null || yDistance < distance) {
                    result = targetTopic;
                    distance = yDistance;
                    break;
                }
            }
        }
        return result;
    }
});

mindplot.DragConnector.CENTRAL_TO_MAINTOPIC_MAX_HORIZONTAL_DISTANCE = 400;
