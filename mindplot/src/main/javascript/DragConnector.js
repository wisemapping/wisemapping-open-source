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
        var candidates = this._searchConnectionCandidates(dragTopic);
        var currentConnection = dragTopic.getConnectedToTopic();


        if (currentConnection && (candidates.length == 0 || candidates[0] != currentConnection)) {
            dragTopic.disconnect(this._workspace);
        }

        // Finally, connect nodes ...
        if (!dragTopic.isConnected() && candidates.length > 0) {
            dragTopic.connectTo(candidates[0]);
        }
    },

    _searchConnectionCandidates:function (dragTopic) {
        var topics = this._designerModel.getTopics();

        // Perform a initial filter to discard topics:
        //  - Exclude dragged topic
        //  - Exclude dragTopic pivot
        //  - Nodes that are collapsed
        var draggedNode = dragTopic.getDraggedTopic();
        topics = topics.filter(function (topic) {
            return  draggedNode != topic && topic != dragTopic._draggedNode && !topic.areChildrenShrunken() && !topic.isCollapsed();
        });

        // Filter all the nodes that are outside the vertical boundary:
        //  * The node is to out of the x scope
        //  * The x distance greater the vertical tolerated distance
        var sourcePosition = dragTopic.getPosition();
        topics = topics.filter(function (topic) {
            var tpos = topic.getPosition();
            var distance = (sourcePosition.x - tpos.x) * Math.sign(sourcePosition.x);

            // Center topic has different alignment than the rest of the nodes. That's why i need to divide it by two...
            var width = topic.getType() == mindplot.model.INodeModel.CENTRAL_TOPIC_TYPE ? topic.getSize().width / 2 : topic.getSize().width;
            return   (distance > width) && ((distance - width) < mindplot.DragConnector.MAX_VERTICAL_CONNECTION_TOLERANCE);
        });

        // Assign a priority based on the distance:
        // - Alignment with the targetNode
        // - Vertical distance
        //
        topics = topics.sort(function (a, b) {
            var aPos = a.getPosition();
            var ad = (sourcePosition.x - aPos.x) * Math.sign(aPos.x);

            var bPos = b.getPosition();
            var bd = (sourcePosition.x - bPos.x) * Math.sign(bPos.x);

            var av = this._isVerticallyAligned(a.getSize(), a.getPosition(), sourcePosition);
            var bv = this._isVerticallyAligned(b.getSize(), b.getPosition(), sourcePosition);

            return  ((bv ? 1000 : 1) + bd) - ((av ? 1000 : 1) + ad);

        }.bind(this));

        console.log("---- out ----");
        topics.each(function (e) {
            console.log(e.getText());
        });
        console.log("---- out ----");


        return topics;
    },

    _isVerticallyAligned:function (targetSize, targetPosition, sourcePosition) {

        return (sourcePosition.y - targetPosition.y) < targetSize.height;

    }

});

mindplot.DragConnector.MAX_VERTICAL_CONNECTION_TOLERANCE = 80;
