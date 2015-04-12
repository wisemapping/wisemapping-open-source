/*
 *    Copyright [2015] [wisemapping]
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

mindplot.RelationshipPivot = new Class({
    initialize: function (workspace, designer) {
        $assert(workspace, "workspace can not be null");
        $assert(designer, "designer can not be null");
        this._workspace = workspace;
        this._designer = designer;

        //FIXME: the aim of the migration is remove .bind mootools method, please remove these!
        this._mouseMoveEvent = this._mouseMove.bind(this);
        this._onClickEvent = this._cleanOnMouseClick.bind(this);
        this._onTopicClick = this._connectOnFocus.bind(this);

    },

    start: function (sourceTopic, targetPos) {
        $assert(sourceTopic, "sourceTopic can not be null");
        $assert(targetPos, "targetPos can not be null");

        this.dispose();
        this._sourceTopic = sourceTopic;
        if (sourceTopic != null) {
            this._workspace.enableWorkspaceEvents(false);

            var sourcePos = sourceTopic.getPosition();
            var strokeColor = mindplot.Relationship.getStrokeColor();

            this._pivot = new web2d.CurvedLine();
            this._pivot.setStyle(web2d.CurvedLine.SIMPLE_LINE);

            var fromPos = this._calculateFromPosition(sourcePos);
            this._pivot.setFrom(fromPos.x, fromPos.y);

            this._pivot.setTo(targetPos.x, targetPos.y);
            this._pivot.setStroke(2, 'solid', strokeColor);
            this._pivot.setDashed(4, 2);

            this._startArrow = new web2d.Arrow();
            this._startArrow.setStrokeColor(strokeColor);
            this._startArrow.setStrokeWidth(2);
            this._startArrow.setFrom(sourcePos.x, sourcePos.y);

            this._workspace.append(this._pivot);
            this._workspace.append(this._startArrow);

            this._workspace.addEvent('mousemove', this._mouseMoveEvent);
            this._workspace.addEvent('click', this._onClickEvent);

            // Register focus events on all topics ...
            var model = this._designer.getModel();
            var topics = model.getTopics();
            _.each(topics, function (topic) {
                topic.addEvent('ontfocus', this._onTopicClick);
            }.bind(this));
        }

    },

    dispose: function () {
        var workspace = this._workspace;

        if (this._isActive()) {
            workspace.removeEvent('mousemove', this._mouseMoveEvent);
            workspace.removeEvent('click', this._onClickEvent);

            var model = this._designer.getModel();
            var topics = model.getTopics();
            var me = this;
            _.each(topics, function (topic) {
                topic.removeEvent('ontfocus', me._onTopicClick);
            });

            workspace.removeChild(this._pivot);
            workspace.removeChild(this._startArrow);
            workspace.enableWorkspaceEvents(true);

            this._sourceTopic = null;
            this._pivot = null;
            this._startArrow = null;
        }
    },

    _mouseMove: function (event) {
        var screen = this._workspace.getScreenManager();
        var pos = screen.getWorkspaceMousePosition(event);

        // Leave the arrow a couple of pixels away from the cursor.
        var sourcePosition = this._sourceTopic.getPosition();
        var gapDistance = Math.sign(pos.x - sourcePosition.x) * 5;

        var sPos = this._calculateFromPosition(pos);
        this._pivot.setFrom(sPos.x, sPos.y);

        // Update target position ...
        this._pivot.setTo(pos.x - gapDistance, pos.y);

        var controlPoints = this._pivot.getControlPoints();
        this._startArrow.setFrom(pos.x - gapDistance, pos.y);
        this._startArrow.setControlPoint(controlPoints[1]);

        event.stopPropagation();
        return false;
    },

    _cleanOnMouseClick: function (event) {

        // The user clicks on a desktop on in other element that is not a node.
        this.dispose();
        event.stopPropagation();
    },

    _calculateFromPosition: function (toPosition) {

        // Calculate origin position ...
        var sourcePosition = this._sourceTopic.getPosition();
        if (this._sourceTopic.getType() == mindplot.model.INodeModel.CENTRAL_TOPIC_TYPE) {
            sourcePosition = mindplot.util.Shape.workoutIncomingConnectionPoint(this._sourceTopic, toPosition);
        }
        var controlPoint = mindplot.util.Shape.calculateDefaultControlPoints(sourcePosition, toPosition);

        var spoint = new core.Point();
        spoint.x = parseInt(controlPoint[0].x) + parseInt(sourcePosition.x);
        spoint.y = parseInt(controlPoint[0].y) + parseInt(sourcePosition.y);
        return mindplot.util.Shape.calculateRelationShipPointCoordinates(this._sourceTopic, spoint);
    },

    _connectOnFocus: function (event, targetTopic) {
        var sourceTopic = this._sourceTopic;
        var mindmap = this._designer.getMindmap();

        // Avoid circular connections ...
        if (targetTopic.getId() != sourceTopic.getId()) {
            var relModel = mindmap.createRelationship(targetTopic.getId(), sourceTopic.getId());
            this._designer._actionDispatcher.addRelationship(relModel);
        }
        this.dispose();
    },

    _isActive: function () {
        return this._pivot != null;
    }
});

