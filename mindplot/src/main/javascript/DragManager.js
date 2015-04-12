/*
 *    Copyright [2015] [wisemapping]
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

mindplot.DragManager = new Class({
    initialize:function(workspace, eventDispatcher) {
        this._workspace = workspace;
        this._designerModel = workspace;
        this._listeners = {};
        this._isDragInProcess = false;
        this._eventDispatcher = eventDispatcher;
        mindplot.DragTopic.init(this._workspace);
    },

    add : function(node) {
        // Add behaviour ...
        var workspace = this._workspace;
        var screen = workspace.getScreenManager();
        var dragManager = this;
        var me = this;
        var mouseDownListener = function(event) {
            if (workspace.isWorkspaceEventsEnabled()) {
                // Disable double drag...
                workspace.enableWorkspaceEvents(false);

                // Set initial position.
                var layoutManager = me._eventDispatcher.getLayoutManager();
                var dragNode = node.createDragNode(layoutManager);

                // Register mouse move listener ...
                var mouseMoveListener = dragManager._buildMouseMoveListener(workspace, dragNode, dragManager);
                screen.addEvent('mousemove', mouseMoveListener);

                // Register mouse up listeners ...
                var mouseUpListener = dragManager._buildMouseUpListener(workspace, node, dragNode, dragManager);
                screen.addEvent('mouseup', mouseUpListener);

                // Change cursor.
                window.document.body.style.cursor = 'move';
            }
        };
        node.addEvent('mousedown', mouseDownListener);
    },

    remove : function(node) {
        var nodes = this._topics;
        var contained = false;
        var index = -1;
        for (var i = 0; i < nodes.length; i++) {
            if (nodes[i] == node) {
                contained = true;
                index = i;
            }
        }
    },

    _buildMouseMoveListener : function(workspace, dragNode, dragManager) {
        var screen = workspace.getScreenManager();
        var me = this;
        var result = function(event) {

            if (!me._isDragInProcess) {
                // Execute Listeners ..
                var startDragListener = dragManager._listeners['startdragging'];
                startDragListener(event, dragNode);

                // Add shadow node to the workspace.
                workspace.append(dragNode);

                me._isDragInProcess = true;
            }

            var pos = screen.getWorkspaceMousePosition(event);
            dragNode.setPosition(pos.x, pos.y);

            // Call mouse move listeners ...
            var dragListener = dragManager._listeners['dragging'];
            if ($defined(dragListener)) {
                dragListener(event, dragNode);
            }

            event.preventDefault();

        };
        dragManager._mouseMoveListener = result;
        return result;
    },

    _buildMouseUpListener : function(workspace, node, dragNode, dragManager) {
        var screen = workspace.getScreenManager();
        var me = this;
        var result = function(event) {
            $assert(dragNode.isDragTopic, 'dragNode must be an DragTopic');

            // Remove all the events.
            screen.removeEvent('mousemove', dragManager._mouseMoveListener);
            screen.removeEvent('mouseup', dragManager._mouseUpListener);

            // Help GC
            dragManager._mouseMoveListener = null;
            dragManager._mouseUpListener = null;

            workspace.enableWorkspaceEvents(true);
            // Change the cursor to the default.
            window.document.body.style.cursor = 'default';

            if (me._isDragInProcess) {

                // Execute Listeners only if the node has been moved.
                var endDragListener = dragManager._listeners['enddragging'];
                endDragListener(event, dragNode);

                // Remove drag node from the workspace.
                dragNode.removeFromWorkspace(workspace);

                me._isDragInProcess = false;
            }


        };
        dragManager._mouseUpListener = result;
        return result;
    },

    /**
     * type:
     *  - startdragging.
     *  - dragging
     *  - enddragging
     */
    addEvent : function(type, listener) {
        this._listeners[type] = listener;
    }
});