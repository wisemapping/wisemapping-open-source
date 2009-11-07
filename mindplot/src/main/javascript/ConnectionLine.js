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

mindplot.ConnectionLine = function(sourceNode, targetNode)
{
    core.assert(targetNode, 'parentNode node can not be null');
    core.assert(sourceNode, 'childNode node can not be null');
    core.assert(sourceNode != targetNode, 'Cilcular connection');

    this._targetTopic = targetNode;
    this._sourceTopic = sourceNode;

    var strokeColor = mindplot.ConnectionLine.getStrokeColor();
    var line;
    if (targetNode.getType() == mindplot.NodeModel.CENTRAL_TOPIC_TYPE)
    {
        line = new web2d.Line();
        line.setStroke(1, 'solid', strokeColor);
    } else
    {
        line = new web2d.PolyLine();
        line.setStroke(1, 'solid', strokeColor);
    }

    this._line2d = line;
};

mindplot.ConnectionLine.getStrokeColor = function()
{
    return '#495879';
};

mindplot.ConnectionLine.prototype.setVisibility = function(value)
{
    var line2d = this._line2d;
    line2d.setVisibility(value);
};

mindplot.ConnectionLine.prototype.redraw = function()
{
    var line2d = this._line2d;
    var sourceTopic = this._sourceTopic;
    var sourcePosition = sourceTopic.getPosition();

    var targetTopic = this._targetTopic;
    var targetPosition = targetTopic.getPosition();

    var sPos = sourceTopic.workoutOutgoingConnectionPoint(targetPosition);
    line2d.setTo(sPos.x, sPos.y);

    var tPos = targetTopic.workoutIncomingConnectionPoint(sourcePosition);
    line2d.setFrom(tPos.x, tPos.y);

    line2d.moveToBack();

    // Add connector ...
    this._positionateConnector(targetTopic);

};

mindplot.ConnectionLine.prototype._positionateConnector = function(targetTopic)
{
    var targetPosition = targetTopic.getPosition();
    var offset = mindplot.Topic.CONNECTOR_WIDTH / 2;
    var targetTopicSize = targetTopic.getSize();
    var y;
    if (targetTopic.getShapeType() == mindplot.NodeModel.SHAPE_TYPE_LINE)
    {
        y = targetTopicSize.height;
    } else
    {
        y = targetTopicSize.height / 2;
    }
    y = y - offset;

    var connector = targetTopic.getShrinkConnector();
    if (targetPosition.x >= 0)
    {
        var x = targetTopicSize.width;
        connector.setPosition(x, y);
    }
    else
    {
        var x = -mindplot.Topic.CONNECTOR_WIDTH;
        connector.setPosition(x, y);
    }
};

mindplot.ConnectionLine.prototype.setStroke = function(color, style, opacity)
{
    var line2d = this._line2d;
    this._line2d.setStroke(null, null, color, opacity);
};


mindplot.ConnectionLine.prototype.addToWorkspace = function(workspace)
{
    workspace.appendChild(this._line2d);
};

mindplot.ConnectionLine.prototype.removeFromWorkspace = function(workspace)
{
    workspace.removeChild(this._line2d);
};

mindplot.ConnectionLine.prototype.getTargetTopic = function()
{
    return this._targetTopic;
};