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

mindplot.MainTopic = new Class({
    Extends: mindplot.Topic,
    initialize : function(model, options) {
        this.parent(model, options);
    },

    INNER_RECT_ATTRIBUTES : {stroke:'0.5 solid #009900'},

    createSiblingModel : function(positionate) {
        var result = null;
        var parentTopic = this.getOutgoingConnectedTopic();
        if (parentTopic != null) {
            // Create a new node ...
            var model = this.getModel();
            var mindmap = model.getMindmap();
            result = mindmap.createNode(mindplot.model.INodeModel.MAIN_TOPIC_TYPE);

            // Positionate following taking into account the sibling position.
            if (positionate && parentTopic.getType() == mindplot.model.INodeModel.CENTRAL_TOPIC_TYPE) {
                var pos = this.getPosition();
                result.setPosition(pos.x, pos.y);
            }

            // Create a new node ...
            var order = this.getOrder() + 1;
            result.setOrder(order);
        }
        return result;
    },

    createChildModel : function(prepositionate) {
        // Create a new node ...
        var model = this.getModel();
        var mindmap = model.getMindmap();
        var childModel = mindmap.createNode(mindplot.model.INodeModel.MAIN_TOPIC_TYPE);

        // Get the hights model order position ...
        var children = this._getChildren();
        var order = -1;
        for (var i = 0; i < children.length; i++) {
            var child = children[i];
            if (child.getOrder() > order) {
                order = child.getOrder();
            }
        }
        // Create a new node ...
        childModel.setOrder(order + 1);
        return childModel;
    },


    _buildDragShape : function() {
        var innerShape = this.buildShape(this.INNER_RECT_ATTRIBUTES);
        var size = this.getSize();
        innerShape.setSize(size.width, size.height);
        innerShape.setPosition(0, 0);
        innerShape.setOpacity(0.5);
        innerShape.setCursor('default');
        innerShape.setVisibility(true);

        var brColor = this.getBorderColor();
        innerShape.setAttribute("strokeColor", brColor);

        var bgColor = this.getBackgroundColor();
        innerShape.setAttribute("fillColor", bgColor);

        //  Create group ...
        var groupAttributes = {width: 100, height:100,coordSizeWidth:100,coordSizeHeight:100};
        var group = new web2d.Group(groupAttributes);
        group.appendChild(innerShape);

        // Add Text ...
        var textShape = this._buildTextShape(true);
        var text = this.getText();
        textShape.setText(text);
        textShape.setOpacity(0.5);
        group.appendChild(textShape);

        return group;
    },


    _defaultShapeType : function() {
        return mindplot.model.INodeModel.SHAPE_TYPE_LINE;
    },

    updateTopicShape : function(targetTopic, workspace) {
        // Change figure based on the connected topic ...
        var model = this.getModel();
        var shapeType = model.getShapeType();
        if (targetTopic.getType() != mindplot.model.INodeModel.CENTRAL_TOPIC_TYPE) {
            if (!$defined(shapeType)) {
                // Get the real shape type ...
                shapeType = this.getShapeType();
                this._setShapeType(shapeType, false);
            }
        }
        this._helpers.forEach(function(helper) {
            helper.moveToFront();
        });
    },

    disconnect : function(workspace) {
        this.parent(workspace);
        var size = this.getSize();

        var model = this.getModel();
        var shapeType = model.getShapeType();
        if (!$defined(shapeType)) {
            // Change figure ...
            shapeType = this.getShapeType();
            this._setShapeType(mindplot.model.INodeModel.SHAPE_TYPE_ROUNDED_RECT, false);
        }
        var innerShape = this.getInnerShape();
        innerShape.setVisibility(true);
    },

    getTopicType : function() {
        return "MainTopic";
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

    workoutIncomingConnectionPoint : function(sourcePosition) {
        $assert(sourcePosition, 'sourcePoint can not be null');
        var pos = this.getPosition();
        var size = this.getSize();

        var isAtRight = mindplot.util.Shape.isAtRight(sourcePosition, pos);
        var result = mindplot.util.Shape.calculateRectConnectionPoint(pos, size, isAtRight);
        if (this.getShapeType() == mindplot.model.INodeModel.SHAPE_TYPE_LINE) {
            result.y = result.y + (this.getSize().height / 2);
        }

        // Move a little the position...
        var offset = mindplot.Topic.CONNECTOR_WIDTH / 2;
        if (this.getPosition().x > 0) {
            result.x = result.x + offset;
        } else {
            result.x = result.x - offset;
        }

        result.x = Math.ceil(result.x);
        result.y = Math.ceil(result.y);
        return result;

    },

    workoutOutgoingConnectionPoint : function(targetPosition) {
        $assert(targetPosition, 'targetPoint can not be null');
        var pos = this.getPosition();
        var size = this.getSize();

        var isAtRight = mindplot.util.Shape.isAtRight(targetPosition, pos);
        var result;
        if (this.getShapeType() == mindplot.model.INodeModel.SHAPE_TYPE_LINE) {
            result = new core.Point();
            if (!isAtRight) {
                result.x = pos.x + (size.width / 2);
            } else {
                result.x = pos.x - (size.width / 2);
            }
            result.y = pos.y + (size.height / 2);

        } else {
            result = mindplot.util.Shape.calculateRectConnectionPoint(pos, size, isAtRight, true);
        }
        result.x = Math.ceil(result.x);
        result.y = Math.ceil(result.y);
        return result;
    },

    _getInnerPadding : function() {
        var result;
        var parent = this.getModel().getParent();
        if (parent && mindplot.model.INodeModel.MAIN_TOPIC_TYPE == parent.getType()) {
            result = 3;
        }
        else {
            result = 4;
        }
        return result;
    },

    isConnectedToCentralTopic : function() {
        var model = this.getModel();
        var parent = model.getParent();

        return parent && parent.getType() === mindplot.model.INodeModel.CENTRAL_TOPIC_TYPE;
    },

    _defaultText : function() {
        var targetTopic = this.getOutgoingConnectedTopic();
        var result = "";
        if ($defined(targetTopic)) {
            if (targetTopic.getType() == mindplot.model.INodeModel.CENTRAL_TOPIC_TYPE) {
                result = "Main Topic";
            } else {
                result = "Sub Topic";
            }
        } else {
            result = "Isolated Topic";
        }
        return result;
    },

    _defaultFontStyle : function() {
        var targetTopic = this.getOutgoingConnectedTopic();
        var result;
        if ($defined(targetTopic)) {
            if (targetTopic.getType() == mindplot.model.INodeModel.CENTRAL_TOPIC_TYPE) {
                result = {
                    font:"Arial",
                    size: 8,
                    style:"normal",
                    weight:"normal",
                    color:"#525c61"
                };
            } else {
                result = {
                    font:"Arial",
                    size: 6,
                    style:"normal",
                    weight:"normal",
                    color:"#525c61"
                };
            }
        } else {
            result = {
                font:"Verdana",
                size: 8,
                style:"normal",
                weight:"normal",
                color:"#525c61"
            };
        }
        return result;
    },

    _defaultBackgroundColor : function() {
        return "#E0E5EF";
    },

    _defaultBorderColor : function() {
        return '#023BB9';
    },
    addSibling : function() {
        var order = this.getOrder();
    }
});