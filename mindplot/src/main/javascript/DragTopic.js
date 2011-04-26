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

mindplot.DragTopic = function(dragShape, draggedNode)
{
    core.assert(core.Utils.isDefined(dragShape), 'Rect can not be null.');
    core.assert(core.Utils.isDefined(draggedNode), 'draggedNode can not be null.');

    this._elem2d = dragShape;
    this._order = null;
    this._draggedNode = draggedNode;
    this._position = new core.Point();
};

mindplot.DragTopic.initialize = function(workspace)
{
    var pivot = mindplot.DragTopic.__getDragPivot();
    workspace.appendChild(pivot);
};

mindplot.DragTopic.prototype.setOrder = function(order)
{
    this._order = order;
};

mindplot.DragTopic.prototype.setPosition = function(x, y)
{
    this._position.setValue(x, y);

    // Elements are positioned in the center.
    // All topic element must be positioned based on the innerShape.
    var draggedNode = this._draggedNode;
    var size = draggedNode.getSize();

    var cx = Math.ceil(x - (size.width / 2));
    var cy = Math.ceil(y - (size.height / 2));

    // Update visual position.
    this._elem2d.setPosition(cx, cy);
};

mindplot.DragTopic.prototype.getInnerShape = function()
{
    return this._elem2d;
};

mindplot.DragTopic.prototype.disconnect = function(workspace)
{
    // Clear connection line ...
    var dragPivot = this._getDragPivot();
    dragPivot.disconnect(workspace);
};

mindplot.DragTopic.prototype.canBeConnectedTo = function(targetTopic)
{
    core.assert(core.Utils.isDefined(targetTopic), 'parent can not be null');

    var result = true;
    if (!targetTopic.areChildrenShrinked() && !targetTopic.isCollapsed())
    {
        // Dragged node can not be connected to himself.
        if (targetTopic == this._draggedNode)
        {
            result = false;
        } else
        {
            var draggedNode = this.getDraggedTopic();
            var topicPosition = this.getPosition();

            var targetTopicModel = targetTopic.getModel();
            var childTopicModel = draggedNode.getModel();

           result = targetTopicModel.canBeConnected(childTopicModel, topicPosition, 18);
        }
    } else
    {
        result = false;
    }
    return result;
};

mindplot.DragTopic.prototype.connectTo = function(parent)
{
    core.assert(parent, 'Parent connection node can not be null.');

    var dragPivot = this._getDragPivot();
    dragPivot.connectTo(parent);
};

mindplot.DragTopic.prototype.getDraggedTopic = function()
{
    return  this._draggedNode;
};


mindplot.DragTopic.prototype.removeFromWorkspace = function(workspace)
{
    // Remove drag shadow.
    workspace.removeChild(this._elem2d);

    // Remove pivot shape. To improve performace it will not be removed. Only the visilility will be changed.
    var dragPivot = this._getDragPivot();
    dragPivot.setVisibility(false);
};

mindplot.DragTopic.prototype.addToWorkspace = function(workspace)
{
    workspace.appendChild(this._elem2d);
    var dragPivot = this._getDragPivot();

    dragPivot.addToWorkspace(workspace);
    dragPivot.setVisibility(true);
};

mindplot.DragTopic.prototype._getDragPivot = function()
{
    return mindplot.DragTopic.__getDragPivot();
};

mindplot.DragTopic.__getDragPivot = function()
{
    var result = mindplot.DragTopic._dragPivot;
    if (!core.Utils.isDefined(result))
    {
        result = new mindplot.DragPivot();
        mindplot.DragTopic._dragPivot = result;
    }
    return result;
};


mindplot.DragTopic.prototype.getPosition = function()
{
    return this._position;
};

mindplot.DragTopic.prototype.isDragTopic = function()
{
    return true;
};

mindplot.DragTopic.prototype.updateDraggedTopic = function(workspace)
{
    core.assert(workspace, 'workspace can not be null');

    var dragPivot = this._getDragPivot();
    var draggedTopic = this.getDraggedTopic();

    var isDragConnected = this.isConnected();
    var actionRunner = mindplot.DesignerActionRunner.getInstance();
    var topicId = draggedTopic.getId();
    var command = new mindplot.commands.DragTopicCommand(topicId);

  if (isDragConnected)
    {

        var targetTopic = this.getConnectedToTopic();
        if (targetTopic.getType() == mindplot.NodeModel.CENTRAL_TOPIC_TYPE)
        {
            // Update topic position ...
            var dragPivotPosition = dragPivot.getPosition();

            // Must position the dragged topic taking into account the current node size.
            var pivotSize = dragPivot.getSize();
            var draggedTopicSize = draggedTopic.getSize();
            var xOffset = draggedTopicSize.width - pivotSize.width;
            xOffset = Math.round(xOffset / 2);

            if (dragPivotPosition.x > 0)
            {
                dragPivotPosition.x = parseInt(dragPivotPosition.x) + xOffset;
            }
            else
            {
                dragPivotPosition.x = parseInt(dragPivotPosition.x) - xOffset;
            }
            // Set new position ...
            command.setPosition(dragPivotPosition);

        } else
        {
            // Main topic connections can be positioned only with the order ...
            command.setOrder(this._order);
        }

        // Set new parent topic ..
        command.setParetTopic(targetTopic);
    } else {

        // If the node is not connected, positionate based on the original drag topic position.
        var dragPosition = this.getPosition();
        command = new mindplot.commands.DragTopicCommand(topicId, dragPosition);
        command.setPosition(dragPosition);
    }
    actionRunner.execute(command);
};

mindplot.DragTopic.prototype.setBoardPosition = function(point)
{
    core.assert(point, 'point can not be null');
    var dragPivot = this._getDragPivot();
    dragPivot.setPosition(point);
};


mindplot.DragTopic.prototype.getBoardPosition = function(point)
{
    core.assert(point, 'point can not be null');
    var dragPivot = this._getDragPivot();
    return dragPivot.getPosition();
};

mindplot.DragTopic.prototype.getConnectedToTopic = function()
{
    var dragPivot = this._getDragPivot();
    return dragPivot.getTargetTopic();
};

mindplot.DragTopic.prototype.isConnected = function()
{
    return this.getConnectedToTopic() != null;
};

mindplot.DragTopic.PIVOT_SIZE = {width:50,height:10};
