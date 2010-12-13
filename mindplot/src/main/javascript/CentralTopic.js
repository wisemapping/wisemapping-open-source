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

mindplot.CentralTopic = function(model)
{
    core.assert(model, "Model can not be null");
    this.setModel(model);
    var topicBoard = new mindplot.CentralTopicBoard(this);
    mindplot.CentralTopic.superClass.initialize.call(this, topicBoard);
    this.__onLoad = true;
};

objects.extend(mindplot.CentralTopic, mindplot.Topic);


mindplot.CentralTopic.prototype.workoutIncomingConnectionPoint = function(sourcePosition, onBoundingBox)
{
     if(!core.Utils.isDefined(onBoundingBox)){
        onBoundingBox=false;
    }
    var pos = this.getPosition();
    var size = this.getSize();
    var isAtRight = mindplot.util.Shape.isAtRight(sourcePosition, pos);
    var result = null;
    if(onBoundingBox){
        result = new core.Point();
        if(isAtRight){
            console.log("incomming at right");
            result.x = pos.x - (size.width/2)-20;
            result.y = pos.y;
        } else {
            result.x = pos.x;
            result.y = pos.y;
        }
    }else{
        result = pos;
    }
    return result;
};

mindplot.CentralTopic.prototype.getTopicType = function()
{
    return mindplot.NodeModel.CENTRAL_TOPIC_TYPE;
};

mindplot.CentralTopic.prototype.setCursor = function(type)
{
    type = (type == 'move') ? 'default' : type;
    mindplot.CentralTopic.superClass.setCursor.call(this, type);
};

mindplot.CentralTopic.prototype.isConnectedToCentralTopic = function()
{
    return false;
};

mindplot.CentralTopic.prototype.createChildModel = function()
{
    // Create a new node ...
    var model = this.getModel();
    var mindmap = model.getMindmap();
    var childModel = mindmap.createNode(mindplot.NodeModel.MAIN_TOPIC_TYPE);

    if (!core.Utils.isDefined(this.___siblingDirection))
    {
        this.___siblingDirection = 1;
    }

    // Positionate following taking into account this internal flag ...
    if (this.___siblingDirection == 1)
    {

        childModel.setPosition(100, 0);
    } else
    {
        childModel.setPosition(-100, 0);
    }
    this.___siblingDirection = -this.___siblingDirection;

    // Create a new node ...
    childModel.setOrder(0);

    return childModel;
};

mindplot.CentralTopic.prototype._defaultShapeType = function()
{
    return  mindplot.NodeModel.SHAPE_TYPE_ROUNDED_RECT;
};


mindplot.CentralTopic.prototype.updateTopicShape = function()
{

};
mindplot.CentralTopic.prototype._updatePositionOnChangeSize = function(oldSize, newSize) {

    // Center main topic ...
    var zeroPoint = new core.Point(0, 0);
    this.setPosition(zeroPoint);

    // Update children position based on the new figure size ...
    var xOffset = newSize.width - oldSize.width;
    xOffset = Math.round(xOffset / 2);

    if (!this.__onLoad)
    {
        // HACK: on load ignore changes of position in order to avoid adding
        // several times the central topic distance to all the child nodes...

        var topicBoard = this.getTopicBoard();
        topicBoard.updateChildrenPosition(this, xOffset);
        this.__onLoad = false;
    }
};

mindplot.CentralTopic.prototype._defaultText = function()
{
    return "Central Topic";
};

mindplot.CentralTopic.prototype._defaultBackgroundColor = function()
{
    return "#f7f7f7";
};

mindplot.CentralTopic.prototype._defaultBorderColor = function()
{
    return "#023BB9";
};

mindplot.CentralTopic.prototype._defaultFontStyle = function()
{
    return {
        font:"Verdana",
        size: 10,
        style:"normal",
        weight:"bold",
        color:"#023BB9"
    };
};