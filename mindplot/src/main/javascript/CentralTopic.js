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

mindplot.CentralTopic = function(model)
{
    $assert(model, "Model can not be null");
    this.setModel(model);
    mindplot.CentralTopic.superClass.initialize.call(this);
    this.__onLoad = true;
};

objects.extend(mindplot.CentralTopic, mindplot.Topic);


mindplot.CentralTopic.prototype.workoutIncomingConnectionPoint = function(sourcePosition)
{
    return this.getPosition();
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

mindplot.CentralTopic.prototype.createChildModel = function(prepositionate)
{
    // Create a new node ...
    var model = this.getModel();
    var mindmap = model.getMindmap();
    var childModel = mindmap.createNode(mindplot.NodeModel.MAIN_TOPIC_TYPE);

    if(prepositionate){
        if (!$defined(this.___siblingDirection))
        {
            this.___siblingDirection = 1;
        }

        // Position following taking into account this internal flag ...
        if (this.___siblingDirection == 1)
        {

            childModel.setPosition(150, 0);
        } else
        {
            childModel.setPosition(-150, 0);
        }
        this.___siblingDirection = -this.___siblingDirection;
    }
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
mindplot.CentralTopic.prototype._updatePositionOnChangeSize = function(oldSize, newSize, updatePosition) {

    // Center main topic ...
    var zeroPoint = new core.Point(0, 0);
    this.setPosition(zeroPoint);
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