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

mindplot.DragPivot = function()
{
    this._position = new core.Point();
    this._size = mindplot.DragTopic.PIVOT_SIZE;
    this._line = null;

    this._straightLine = this._buildStraightLine();
    this._curvedLine = this._buildCurvedLine();
    this._dragPivot = this._buildRect();
    this._connectRect = this._buildRect();
    this._targetTopic = null;
};

mindplot.DragPivot.prototype.getTargetTopic = function()
{
    return this._targetTopic;
};

mindplot.DragPivot.prototype._buildStraightLine = function()
{
    var line = new web2d.Line();
    line.setStroke(1, 'solid', '#CC0033');
    line.setOpacity(0.4);
    line.setVisibility(false);
    return line;
};

mindplot.DragPivot.prototype._buildCurvedLine = function()
{
    var line = new web2d.PolyLine();
    line.setStroke(1, 'solid', '#CC0033');
    line.setOpacity(0.4);
    line.setVisibility(false);
    return line;
};

mindplot.DragPivot.prototype._redraw = function(pivotPosition)
{
    // Update line position.
    core.assert(this.getTargetTopic(), 'Illegal invocation. Target node can not be null');

    var pivotRect = this._getPivotRect();
    var currentPivotPosition = pivotRect.getPosition();

    // Pivot position has not changed. In this case, position change is not required.
    var targetTopic = this.getTargetTopic();
    if (currentPivotPosition.x != pivotPosition.x || currentPivotPosition.y != pivotPosition.y)
    {
        var position = this._position;
        var fromPoint = targetTopic.workoutIncomingConnectionPoint(position);

        // Calculate pivot connection point ...
        var size = this._size;
        var targetPosition = targetTopic.getPosition();
        var line = this._line;

        // Update Line position.
        var isAtRight = mindplot.util.Shape.isAtRight(targetPosition, position);
        var pivotPoint = mindplot.util.Shape.calculateRectConnectionPoint(position, size, isAtRight);
        line.setFrom(pivotPoint.x, pivotPoint.y);

        // Update rect position
        pivotRect.setPosition(pivotPosition.x, pivotPosition.y);

        // Display elements if it's required...
        if (!pivotRect.isVisible())
        {
            // Make line visible only when the position has been already changed.
            // This solve several strange effects ;)
            var targetPoint = targetTopic.workoutIncomingConnectionPoint(pivotPoint);
            line.setTo(targetPoint.x, targetPoint.y);

            this.setVisibility(true);
        }
    }
};

mindplot.DragPivot.prototype.setPosition = function(point)
{
    this._position = point;

    // Update visual position.
    var pivotRect = this._getPivotRect();
    var size = this.getSize();

    var cx = point.x - (parseInt(size.width) / 2);
    var cy = point.y - (parseInt(size.height) / 2);

    // Update line  ...
    if (this.getTargetTopic())
    {
        var pivotPosition = {x:cx,y:cy};
        this._redraw(pivotPosition);
    }
};

mindplot.DragPivot.prototype.getPosition = function()
{
    return this._position;
};

mindplot.DragPivot.prototype._buildRect = function()
{
    var size = this._size;
    var rectAttributes = {fillColor:'#CC0033',opacity:0.4,width:size.width,height:size.height,strokeColor:'#FF9933'};
    var rect = new web2d.Rect(0, rectAttributes);
    rect.setVisibility(false);
    return rect;
};

mindplot.DragPivot.prototype._buildConnectRect = function()
{
    var size = this._size;
    var rectAttributes = {fillColor:'#CC0033',opacity:0.4,width:size.width,height:size.height,strokeColor:'#FF9933'};
    var result = new web2d.Rect(0, rectAttributes);
    return result;
};

mindplot.DragPivot.prototype._getPivotRect = function()
{
    return this._dragPivot;
};

mindplot.DragPivot.prototype.getSize = function()
{
    var elem2d = this._getPivotRect();
    return elem2d.getSize();
};

mindplot.DragPivot.prototype.setVisibility = function(value)
{
    var pivotRect = this._getPivotRect();
    pivotRect.setVisibility(value);

    var connectRect = this._connectRect;
    connectRect.setVisibility(value);
    if (core.Utils.isDefined(this._line))
    {
        this._line.setVisibility(value);
    }
};

mindplot.DragPivot.prototype.addToWorkspace = function(workspace)
{
    var pivotRect = this._getPivotRect();
    workspace.appendChild(pivotRect);

    var connectToRect = this._connectRect;
    workspace.appendChild(connectToRect);

    // Add a hidden straight line ...
    var straighLine = this._straightLine;
    straighLine.setVisibility(false);
    workspace.appendChild(straighLine);
    straighLine.moveToBack();

    // Add a hidden curved line ...
    var curvedLine = this._curvedLine;
    curvedLine.setVisibility(false);
    workspace.appendChild(curvedLine);
    curvedLine.moveToBack();

    // Add a connect rect ...
    var connectRect = this._connectRect;
    connectRect.setVisibility(false);
    workspace.appendChild(connectRect);
    connectRect.moveToBack();
};

mindplot.DragPivot.prototype.removeFromWorkspace = function(workspace)
{
    var shape = this._getPivotRect();
    workspace.removeChild(shape);

    var connectToRect = this._connectRect;
    workspace.removeChild(connectToRect);

    if (core.Utils.isDefined(this._straightLine))
    {
        workspace.removeChild(this._straightLine);
    }

    if (core.Utils.isDefined(this._curvedLine))
    {
        workspace.removeChild(this._curvedLine);
    }
};

mindplot.DragPivot.prototype.connectTo = function(targetTopic)
{
    core.assert(!this._outgoingLine, 'Could not connect an already connected node');
    core.assert(targetTopic != this, 'Cilcular connection are not allowed');
    core.assert(targetTopic, 'parent can not be null');

    this._targetTopic = targetTopic;
    if (targetTopic.getType() == mindplot.NodeModel.CENTRAL_TOPIC_TYPE)
    {
        this._line = this._straightLine;
    } else
    {
        this._line = this._curvedLine;
    }

    // Show pivot ...
    var line = this._line;

    // Connected to Rect ...
    var connectRect = this._connectRect;
    var targetSize = targetTopic.getSize();
    var width = targetSize.width;
    var height = targetSize.height;
    connectRect.setSize(width, height);

    var targetPosition = targetTopic.getPosition();
    var cx = Math.ceil(targetPosition.x - (width / 2));
    var cy = Math.ceil(targetPosition.y - (height / 2));
    connectRect.setPosition(cx, cy);

    // Change elements position ...
    var pivotRect = this._getPivotRect();
    pivotRect.moveToFront();

};

mindplot.DragPivot.prototype.disconnect = function(workspace)
{
    core.assert(workspace, 'workspace can not be null.');
    core.assert(this._targetTopic, 'There are not connected topic.');

    this.setVisibility(false);
    this._targetTopic = null;
    this._line = null;
};
