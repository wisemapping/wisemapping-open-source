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


mindplot.MediaTopic = new Class({
    Extends:mindplot.Topic,
    initialize : function(model, options) {
        this.parent(model, options);
    },

    getInnerShape : function() {
        if (!$defined(this._innerShape)) {
            // Create inner box.
            var model = this.getModel();

            this._innerShape = new web2d.Image();
            this._innerShape.setHref(model.getImageUrl());
            this._innerShape.setPosition(0, 0);
        }
        return this._innerShape;
    },

    getOuterShape : function() {
        if (!$defined(this._outerShape)) {
            var rect = new web2d.Rect(0, mindplot.Topic.OUTER_SHAPE_ATTRIBUTES);
            rect.setPosition(-2, -3);
            rect.setOpacity(0);
            this._outerShape = rect;
        }

        return this._outerShape;
    },


    _buildShape : function() {
        var groupAttributes = {width: 100, height:100,coordSizeWidth:100,coordSizeHeight:100};
        var group = new web2d.Group(groupAttributes);
        this._set2DElement(group);

        // Shape must be build based on the model width ...
        var outerShape = this.getOuterShape();
        var innerShape = this.getInnerShape();
        var shrinkConnector = this.getShrinkConnector();

        // Add to the group ...
        group.appendChild(outerShape);
        group.appendChild(innerShape);

        // Update figure size ...
//        var model = this.getModel();
//        if (model.getLinks().length != 0 || model.getNotes().length != 0 || model.getIcons().length != 0) {
//            this.getOrBuildIconGroup();
//        }

        if (this.getType() != mindplot.model.INodeModel.CENTRAL_TOPIC_TYPE) {
            shrinkConnector.addToWorkspace(group);
        }

        // Register listeners ...
        this._registerDefaultListenersToElement(group, this);


    },

    workoutOutgoingConnectionPoint : function(targetPosition) {
        $assert(targetPosition, 'targetPoint can not be null');
        var pos = this.getPosition();

        var result;
        result = new core.Point();
        var groupPosition = this._elem2d.getPosition();
        var innerShareSize = this.getInnerShape().getSize();
        var isAtRight = mindplot.util.Shape.isAtRight(targetPosition, pos);

        result = mindplot.util.Shape.calculateRectConnectionPoint(pos, this.getSize(), isAtRight, true);
        return result;
    },

    createDragNode : function(layoutManager) {
        var result = this.parent(layoutManager);

        // Is the node already connected ?
        var targetTopic = this.getOutgoingConnectedTopic();
        if ($defined(targetTopic)) {
            result.connectTo(targetTopic);
            result.setVisibility(false);
        }
        return result;
    },

    _adjustShapes : function() {
        if (this._isInWorkspace) {

            var size = this.getModel().getSize();
            this.setSize(size, true);

        }
    },

    _updatePositionOnChangeSize : function(oldSize, newSize) {

        var xOffset = Math.round((newSize.width - oldSize.width) / 2);
        var pos = this.getPosition();
        if ($defined(pos)) {
            if (pos.x > 0) {
                pos.x = pos.x + xOffset;
            } else {
                pos.x = pos.x - xOffset;
            }
            this.setPosition(pos);
        }
    },

    updateTopicShape : function() {
        // Todo: verify ...
    },

    closeEditors : function() {
        //@Todo:
    }
});
