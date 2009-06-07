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

mindplot.NodeGraph = function(nodeModel)
{
    this._mouseEvents = true;
    this.setModel(nodeModel);
    this._onFocus = false;
};


mindplot.NodeGraph.prototype.getType = function()
{
    var model = this.getModel();
    return model.getType();
};

mindplot.NodeGraph.prototype.getId = function()
{
    return this.getModel().getId();
};

mindplot.NodeGraph.prototype.setId = function(id)
{
    this.getModel().setId(id);
};

mindplot.NodeGraph.prototype._set2DElement = function(elem2d)
{
    this._elem2d = elem2d;
};

mindplot.NodeGraph.prototype.get2DElement = function()
{
    core.assert(this._elem2d, 'NodeGraph has not been initialized propertly');
    return this._elem2d;
};

mindplot.NodeGraph.prototype.setPosition = function(point)
{
    // Elements are positioned in the center.
    var size = this._model.getSize();
    this._elem2d.setPosition(point.x - (size.width / 2), point.y - (size.height / 2));
    this._model.setPosition(point.x, point.y);
};

mindplot.NodeGraph.prototype.addEventListener = function(type, listener)
{
    var elem = this.get2DElement();
    elem.addEventListener(type, listener);
};

mindplot.NodeGraph.prototype.isNodeGraph = function()
{
    return true;
};

mindplot.NodeGraph.prototype.setMouseEventsEnabled = function(isEnabled)
{
    this._mouseEvents = isEnabled;
};

mindplot.NodeGraph.prototype.isMouseEventsEnabled = function()
{
    return this._mouseEvents;
};

mindplot.NodeGraph.prototype.getSize = function()
{
    return this._model.getSize();
};

mindplot.NodeGraph.prototype.setSize = function(size)
{
    this._model.setSize(size.width, size.height);
};

mindplot.NodeGraph.create = function(nodeModel)
{
    core.assert(nodeModel, 'Model can not be null');

    var type = nodeModel.getType();
    core.assert(type, 'Node model type can not be null');

    var result;
    if (type == mindplot.NodeModel.CENTRAL_TOPIC_TYPE)
    {
        result = new mindplot.CentralTopic(nodeModel);
    } else
        if (type == mindplot.NodeModel.MAIN_TOPIC_TYPE)
        {
            result = new mindplot.MainTopic(nodeModel);
        } else
        {
            assert(false, "unsupported node type:" + type);
        }

    return result;
};

mindplot.NodeGraph.prototype.getModel = function()
{
    core.assert(this._model, 'Model has not been initialized yet');
    return  this._model;
};

mindplot.NodeGraph.prototype.setModel = function(model)
{
    core.assert(model, 'Model can not be null');
    this._model = model;
};

mindplot.NodeGraph.prototype.getId = function()
{
    return this._model.getId();
};

mindplot.NodeGraph.prototype.setOnFocus = function(focus)
{
    this._onFocus = focus;
    var outerShape = this.getOuterShape();
    if (focus)
    {
        outerShape.setFill('#c7d8ff');
        outerShape.setOpacity(1);

    } else
    {
        // @todo: node must not know about the topic.

        outerShape.setFill(mindplot.Topic.OUTER_SHAPE_ATTRIBUTES.fillColor);
        outerShape.setOpacity(0);
    }
    this.setCursor('move');
};

mindplot.NodeGraph.prototype.isOnFocus = function()
{
    return this._onFocus;
};

mindplot.NodeGraph.prototype.dispose = function(workspace)
{
    workspace.removeChild(this);
};

mindplot.NodeGraph.prototype.createDragNode = function()
{
    var dragShape = this._buildDragShape();
    return  new mindplot.DragTopic(dragShape, this);
};

mindplot.NodeGraph.prototype._buildDragShape = function()
{
    core.assert(false, '_buildDragShape must be implemented by all nodes.');
};

mindplot.NodeGraph.prototype.getPosition = function()
{
    var model = this.getModel();
    return model.getPosition();
};