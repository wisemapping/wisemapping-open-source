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

mindplot.RelationshipPivot = new Class({
    initialize: function(workspace, designer) {
        $assert(workspace, "workspace can not be null");
        $assert(designer, "designer can not be null");
        this._workspace = workspace;
        this._designer = designer;

        this._mouseMoveEvent = this._mouseMove.bind(this);
        this._onClickEvent = this._cleanOnMouseClick.bind(this);
        this._onTopicClick = this._connectOnFocus.bind(this);

    },

    start : function(sourceTopic, targetPos) {
        $assert(sourceTopic, "sourceTopic can not be null");
        $assert(targetPos, "targetPos can not be null");

        this.dispose();
        this._sourceTopic = sourceTopic;
        if (sourceTopic != null) {
            this._workspace.enableWorkspaceEvents(false);

            var sourcePos = sourceTopic.getPosition();
            this._pivot = new web2d.CurvedLine();
            this._pivot.setStyle(web2d.CurvedLine.SIMPLE_LINE);
            this._pivot.setDashed(2, 2);
            this._pivot.setFrom(sourcePos.x, sourcePos.y);
            this._pivot.setTo(targetPos.x, targetPos.y);
            this._workspace.appendChild(this._pivot);


            this._workspace.addEvent('mousemove', this._mouseMoveEvent);
            this._workspace.addEvent('click', this._onClickEvent);

            // Register focus events on all topics ...
            var model = this._designer.getModel();
            var topics = model.getTopics();
            topics.forEach(function(topic) {
                topic.addEvent('ontfocus', this._onTopicClick);
            }.bind(this));
        }

    },

    dispose : function() {
        var workspace = this._workspace;

        if (this._isActive()) {
            workspace.removeEvent('mousemove', this._mouseMoveEvent);
            workspace.removeEvent('click', this._onClickEvent);

            var model = this._designer.getModel();
            var topics = model.getTopics();
            topics.forEach(function(topic) {
                topic.removeEvent('ontfocus', this._onTopicClick);
            }.bind(this));

            workspace.removeChild(this._pivot);
            workspace.enableWorkspaceEvents(true);

            this._sourceTopic = null;
            this._pivot = null;
        }
    },

    _mouseMove : function(event) {
        var screen = this._workspace.getScreenManager();
        var pos = screen.getWorkspaceMousePosition(event);

        this._pivot.setTo(pos.x - 1, pos.y - 1);
        event.stopPropagation();
        return false;
    },

    _cleanOnMouseClick : function (event) {

        // The user clicks on a desktop on in other element that is not a node.
        this.dispose();
        event.stopPropagation();
    },

    _connectOnFocus : function(topic) {
        var sourceTopic = this._sourceTopic;
        this.dispose();
        this._designer.connectByRelation(sourceTopic, topic);

    },

    _isActive : function() {
        return this._pivot != null;
    }
});

