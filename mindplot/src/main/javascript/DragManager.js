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

mindplot.DragManager = function(workspace)
{
    this._workspace = workspace;
    this._listeners = {};
    var dragManager = this;
};

mindplot.DragManager.prototype.add = function(node)
{
    // Add behaviour ...
    var workspace = this._workspace;
    var screen = workspace.getScreenManager();
    var dragManager = this;

    var mouseDownListener = function(event)
    {
        if (workspace.isWorkspaceEventsEnabled())
        {
            // Disable double drag... 
            workspace.enableWorkspaceEvents(false);

            // Set initial position.
            var dragNode = node.createDragNode();
            var mousePos = screen.getWorkspaceMousePosition(event);
            dragNode.setPosition(mousePos.x, mousePos.y);

            // Register mouse move listener ...
            var mouseMoveListener = dragManager._buildMouseMoveListener(workspace, dragNode, dragManager);
            screen.addEventListener('mousemove', mouseMoveListener);

            // Register mouse up listeners ...
            var mouseUpListener = dragManager._buildMouseUpListener(workspace, node, dragNode, dragManager);
            screen.addEventListener('mouseup', mouseUpListener);

            // Execute Listeners ..
            var startDragListener = dragManager._listeners['startdragging'];
            startDragListener(event, node);

            // Change cursor.
            window.document.body.style.cursor = 'move';
        }
    };
    dragManager._mouseDownListener = mouseDownListener;

    node.addEventListener('mousedown', mouseDownListener);
};

mindplot.DragManager.prototype.remove = function(node)
{
    var nodes = this._topics;
    var contained = false;
    var index = -1;
    for (var i = 0; i < nodes.length; i++) {
        if (nodes[i] == node) {
            contained = true;
            index = i;
        }
    }
    if (contained)
    {
        elem = new Array();
    }
};

mindplot.DragManager.prototype._buildMouseMoveListener = function(workspace, dragNode, dragManager)
{
    var screen = workspace.getScreenManager();
    var result = function(event) {

        if (!dragNode._isInTheWorkspace)
        {
            // Add shadow node to the workspace.
            workspace.appendChild(dragNode);
            dragNode._isInTheWorkspace = true;
        }

        var pos = screen.getWorkspaceMousePosition(event);
        dragNode.setPosition(pos.x, pos.y);

        // Call mouse move listeners ...
        var dragListener = dragManager._listeners['dragging'];
        if (core.Utils.isDefined(dragListener))
        {
            dragListener(event, dragNode);
        }

        event.preventDefault();
    }.bindWithEvent(this);
    dragManager._mouseMoveListener = result;
    return result;
};

mindplot.DragManager.prototype._buildMouseUpListener = function(workspace, node, dragNode, dragManager)
{
    var screen = workspace.getScreenManager();
    var result = function(event) {

        core.assert(dragNode.isDragTopic, 'dragNode must be an DragTopic');

        // Remove drag node from the workspace.
        var hasBeenDragged = dragNode._isInTheWorkspace;
        if (dragNode._isInTheWorkspace)
        {
            dragNode.removeFromWorkspace(workspace);
        }

        // Remove all the events.
        screen.removeEventListener('mousemove', dragManager._mouseMoveListener);
        screen.removeEventListener('mouseup', dragManager._mouseUpListener);

        // Help GC
        dragManager._mouseMoveListener = null;
        dragManager._mouseUpListener = null;

        // Execute Listeners only if the node has been moved.
        var endDragListener = dragManager._listeners['enddragging'];
        endDragListener(event, dragNode);

        if (hasBeenDragged)
        {
            dragNode._isInTheWorkspace = false;
        }

        // Change the cursor to the default.
        window.document.body.style.cursor = 'default';

        workspace.enableWorkspaceEvents(true);

    };
    dragManager._mouseUpListener = result;
    return result;
};

/**
 * type:
 *  - startdragging.
 *  - dragging
 *  - enddragging
 */
mindplot.DragManager.prototype. addEventListener = function(type, listener)
{
    this._listeners[type] = listener;
};

mindplot.DragManager.DRAG_PRECISION_IN_SEG = 100;
