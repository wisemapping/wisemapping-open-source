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

mindplot.MainTopic = function(model)
{
    core.assert(model, "Model can not be null");
    this.setModel(model);
    var topicBoard = new mindplot.MainTopicBoard(this);
    mindplot.MainTopic.superClass.initialize.call(this, topicBoard);
};

objects.extend(mindplot.MainTopic, mindplot.Topic);

mindplot.MainTopic.prototype.INNER_RECT_ATTRIBUTES = {stroke:'0.5 solid #009900'};

mindplot.MainTopic.prototype.createSiblingModel = function()
{
    var siblingModel = null;
    var parentTopic = this.getOutgoingConnectedTopic();
    if (parentTopic != null)
    {
        // Create a new node ...
        var model = this.getModel();
        var mindmap = model.getMindmap();
        siblingModel = mindmap.createNode(mindplot.NodeModel.MAIN_TOPIC_TYPE);

        // Positionate following taking into account the sibling positon.
        if (parentTopic.getType() == mindplot.NodeModel.CENTRAL_TOPIC_TYPE)
        {
            var pos = this.getPosition();
            siblingModel.setPosition(pos.x, pos.y);
        }

        // Create a new node ...
        var order = this.getOrder() + 1;
        siblingModel.setOrder(order);
    }
    return siblingModel;
};

mindplot.MainTopic.prototype.createChildModel = function()
{
    // Create a new node ...
    var model = this.getModel();
    var mindmap = model.getMindmap();
    var childModel = mindmap.createNode(mindplot.NodeModel.MAIN_TOPIC_TYPE);

    // Get the hights model order position ...
    var children = this._getChildren();
    var order = -1;
    for (var i = 0; i < children.length; i++)
    {
        var child = children[i];
        if (child.getOrder() > order)
        {
            order = child.getOrder();
        }
    }
    // Create a new node ...
    childModel.setOrder(order + 1);
    return childModel;
};


mindplot.MainTopic.prototype._buildDragShape = function()
{
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
};


mindplot.MainTopic.prototype._defaultShapeType = function()
{
    var targetTopic = this.getOutgoingConnectedTopic();
    var result;
    if (targetTopic)
    {
        if (targetTopic.getType() != mindplot.NodeModel.CENTRAL_TOPIC_TYPE)
        {
            result = mindplot.NodeModel.SHAPE_TYPE_LINE;

        } else
        {
            result = mindplot.NodeModel.SHAPE_TYPE_ROUNDED_RECT;
        }
    } else
    {
        result = mindplot.NodeModel.SHAPE_TYPE_ROUNDED_RECT;
    }
    return result;
};

mindplot.MainTopic.prototype.updateTopicShape = function(targetTopic, workspace)
{
    // Change figure based on the connected topic ...
    if (targetTopic.getType() != mindplot.NodeModel.CENTRAL_TOPIC_TYPE)
    {
        var model = this.getModel();
        var shapeType = model.getShapeType();
        if (!shapeType)
        {
            // Get the real shape type ...
            shapeType = this.getShapeType();
            this._setShapeType(mindplot.NodeModel.SHAPE_TYPE_LINE, false);
        }
    }
};

mindplot.MainTopic.prototype.disconnect = function(workspace)
{
    mindplot.MainTopic.superClass.disconnect.call(this, workspace);
    var size = this.getSize();

    var model = this.getModel();
    var shapeType = model.getShapeType();
    if (!shapeType)
    {
        // Change figure ...
        shapeType = this.getShapeType();
        this._setShapeType(mindplot.NodeModel.SHAPE_TYPE_ROUNDED_RECT, false);
    }
};

mindplot.MainTopic.prototype.getTopicType = function()
{
    return "MainTopic";
};


mindplot.MainTopic.prototype._updatePositionOnChangeSize = function(oldSize, newSize) {

    var xOffset = (newSize.width - oldSize.width) / 2;
    var pos = this.getPosition();
    if (core.Utils.isDefined(pos))
    {
        if (pos.x > 0)
        {
            pos.x = pos.x + xOffset;
        } else
        {
            pos.x = pos.x - xOffset;
        }
        this.setPosition(pos);

        // If height has changed, I must repositionate all elements ...
        if (oldSize.height != newSize.height)
        {
            var topicBoard = this.getTopicBoard();
            // topicBoard.repositionate();
        }
    }
};

mindplot.MainTopic.prototype.setPosition = function(point)
{
    mindplot.MainTopic.superClass.setPosition.call(this, point);

    // Update board zero entry position...
    var topicBoard = this.getTopicBoard();
    topicBoard.updateChildrenPosition(this);
};

mindplot.MainTopic.prototype.workoutIncomingConnectionPoint = function(sourcePosition)
{
    core.assert(sourcePosition, 'sourcePoint can not be null');
    var pos = this.getPosition();
    var size = this.getSize();

    var isAtRight = mindplot.util.Shape.isAtRight(sourcePosition, pos);
    var result = mindplot.util.Shape.calculateRectConnectionPoint(pos, size, isAtRight);
    if (this.getShapeType() == mindplot.NodeModel.SHAPE_TYPE_LINE)
    {
        result.y = result.y + (this.getSize().height / 2);
    }

    // Move a little the position...
    var offset = mindplot.Topic.CONNECTOR_WIDTH / 2;
    if (this.getPosition().x > 0)
    {
        result.x = result.x + offset;
    } else
    {
        result.x = result.x - offset;
    }
    return result;

};

mindplot.MainTopic.prototype.workoutOutgoingConnectionPoint = function(targetPosition)
{
    core.assert(targetPosition, 'targetPoint can not be null');
    var pos = this.getPosition();
    var size = this.getSize();

    var isAtRight = mindplot.util.Shape.isAtRight(targetPosition, pos);
    var result;
    if (this.getShapeType() == mindplot.NodeModel.SHAPE_TYPE_LINE)
    {
        if (!this.isConnectedToCentralTopic())
        {
            result = new core.Point();
            if (!isAtRight)
            {
                result.x = pos.x - (size.width / 2);
            } else
            {
                result.x = pos.x + (size.width / 2);
            }
            result.y = pos.y + (size.height / 2);
        } else
        {
            // In this case, connetion line is not used as shape figure.
            result = mindplot.util.Shape.calculateRectConnectionPoint(pos, size, isAtRight, true);
            result.y = pos.y + (size.height / 2);

            // Correction factor ...
            if (!isAtRight)
            {
                result.x = result.x + 2;
            } else
            {
                result.x = result.x - 2;
            }

        }
    } else
    {
        result = mindplot.util.Shape.calculateRectConnectionPoint(pos, size, isAtRight, true);
    }
    return result;
};


mindplot.MainTopic.prototype.isConnectedToCentralTopic = function()
{
    var model = this.getModel();
    var parent = model.getParent();

    return parent && parent.getType() === mindplot.NodeModel.CENTRAL_TOPIC_TYPE;
};

mindplot.MainTopic.prototype._defaultText = function()
{
    var targetTopic = this.getOutgoingConnectedTopic();
    var result = "";
    if (targetTopic)
    {
        if (targetTopic.getType() == mindplot.NodeModel.CENTRAL_TOPIC_TYPE)
        {
            result = "Main Topic";
        } else
        {
            result = "Sub Topic";
        }
    } else
    {
        result = "Isolated Topic";
    }
    return result;
};

mindplot.MainTopic.prototype._defaultFontStyle = function()
{
    var targetTopic = this.getOutgoingConnectedTopic();
    var result;
    if (targetTopic)
    {
        if (targetTopic.getType() == mindplot.NodeModel.CENTRAL_TOPIC_TYPE)
        {
            result = {
                font:"Arial",
                size: 8,
                style:"normal",
                weight:"normal",
                color:"#525c61"
            };
        } else
        {
            result = {
                font:"Arial",
                size: 6,
                style:"normal",
                weight:"normal",
                color:"#525c61"
            };
        }
    } else
    {
        result = {
            font:"Verdana",
            size: 8,
            style:"normal",
            weight:"normal",
            color:"#525c61"
        };
    }
    return result;
};

mindplot.MainTopic.prototype._defaultBackgroundColor = function()
{
    return "#E0E5EF";
};

mindplot.MainTopic.prototype._defaultBorderColor = function()
{
    return '#023BB9';
};
mindplot.MainTopic.prototype.addSibling = function()
{
    var order = this.getOrder();


};
