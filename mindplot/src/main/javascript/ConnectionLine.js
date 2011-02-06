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

mindplot.ConnectionLine = function(sourceNode, targetNode, lineType)
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
        line = this._createLine(lineType,mindplot.ConnectionLine.SIMPLE_CURVED);
        //        line = new web2d.Line();
        if(line.getType()=="CurvedLine"){
            var ctrlPoints = this._getCtrlPoints(sourceNode, targetNode);
            line.setSrcControlPoint(ctrlPoints[0]);
            line.setDestControlPoint(ctrlPoints[1]);
        }
        line.setStroke(1, 'solid', strokeColor);
    } else
    {
        line = this._createLine(lineType,mindplot.ConnectionLine.POLYLINE);
        //        line = new web2d.PolyLine();
        line.setStroke(1, 'solid', strokeColor);
    }

    this._line2d = line;
};

mindplot.ConnectionLine.prototype._getCtrlPoints = function(sourceNode, targetNode){
    var srcPos = sourceNode.getPosition();
    var destPos = targetNode.getPosition();
    var deltaX = Math.abs(Math.abs(srcPos.x) - Math.abs(destPos.x))/3;
    var fix = 1;
    if(mindplot.util.Shape.isAtRight(srcPos, destPos)){
        fix=-1;
    }
    return [new core.Point(deltaX*fix, 0), new core.Point(deltaX*-fix, 0)];
};

mindplot.ConnectionLine.prototype._createLine = function(lineType, defaultStyle){
    if(!core.Utils.isDefined(lineType)){
        lineType = defaultStyle;
    }
    lineType = parseInt(lineType);
    this._lineType = lineType;
    var line = null;
    switch(lineType){
        case mindplot.ConnectionLine.POLYLINE:
            line = new web2d.PolyLine();
            break;
        case mindplot.ConnectionLine.CURVED:
            line = new web2d.CurvedLine();
            break;
        case mindplot.ConnectionLine.SIMPLE_CURVED:
            line = new web2d.CurvedLine();
            line.setStyle(web2d.CurvedLine.SIMPLE_LINE);
            break;
        default:
            line = new web2d.Line();
            break;
    }
    return line;
};

mindplot.ConnectionLine.getStrokeColor = function()
{
    return '#495879';
};

mindplot.ConnectionLine.prototype.setVisibility = function(value)
{
    this._line2d.setVisibility(value);
};

mindplot.ConnectionLine.prototype.redraw = function()
{
    var line2d = this._line2d;
    var sourceTopic = this._sourceTopic;
    var sourcePosition = sourceTopic.getPosition();

    var targetTopic = this._targetTopic;
    var targetPosition = targetTopic.getPosition();

    var sPos,tPos;
    sPos = sourceTopic.workoutOutgoingConnectionPoint(targetPosition, false);
    tPos = targetTopic.workoutIncomingConnectionPoint(sourcePosition, false);

    line2d.setFrom(tPos.x, tPos.y);
    line2d.setTo(sPos.x, sPos.y);

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

mindplot.ConnectionLine.prototype.getSourceTopic = function()
{
    return this._sourceTopic;
};

mindplot.ConnectionLine.prototype.getLineType = function(){
    return this._lineType;
};

mindplot.ConnectionLine.prototype.getLine = function(){
    return this._line2d;
};

mindplot.ConnectionLine.prototype.getModel = function(){
    return this._model;
};

mindplot.ConnectionLine.prototype.setModel = function(model){
    this._model = model;
};

mindplot.ConnectionLine.prototype.getType = function(){
    return "ConnectionLine";
};

mindplot.ConnectionLine.prototype.getId = function(){
    return this._model.getId();
};

mindplot.ConnectionLine.SIMPLE=0;
mindplot.ConnectionLine.POLYLINE=1;
mindplot.ConnectionLine.CURVED=2;
mindplot.ConnectionLine.SIMPLE_CURVED=3;