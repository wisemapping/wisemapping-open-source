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

mindplot.DragPivot = new Class({
    initialize:function() {
        this._position = new core.Point();
        this._size = mindplot.DragTopic.PIVOT_SIZE;

        this._straightLine = this._buildStraightLine();
        this._curvedLine = this._buildCurvedLine();
        this._dragPivot = this._buildRect();
        this._connectRect = this._buildRect();
        this._targetTopic = null;
        this._isVisible = false;
    },

    isVisible:function() {
        return this._isVisible;
    },

    getTargetTopic : function() {
        return this._targetTopic;
    },

    _buildStraightLine : function() {
        var line = new web2d.CurvedLine();
        line.setStyle(web2d.CurvedLine.SIMPLE_LINE);
        line.setStroke(1, 'solid', '#CC0033');
        line.setOpacity(0.4);
        line.setVisibility(false);
        return line;
    },

    _buildCurvedLine : function() {
        var line = new web2d.CurvedLine();
        line.setStyle(web2d.CurvedLine.SIMPLE_LINE);
        line.setStroke(1, 'solid', '#CC0033');
        line.setOpacity(0.4);
        line.setVisibility(false);
        return line;
    },


    _redrawLine : function() {
        // Update line position.
        $assert(this.getTargetTopic(), 'Illegal invocation. Target node can not be null');

        var pivotRect = this._getPivotRect();

        // Pivot position has not changed. In this case, position change is not required.
        var targetTopic = this.getTargetTopic();
        var position = this._position;

        // Calculate pivot connection point ...
        var size = this._size;
        var targetPosition = targetTopic.getPosition();
        var line = this._getConnectionLine();

        // Update Line position.
        var isAtRight = mindplot.util.Shape.isAtRight(targetPosition, position);
        var pivotPoint = mindplot.util.Shape.calculateRectConnectionPoint(position, size, isAtRight);
        line.setFrom(pivotPoint.x, pivotPoint.y);

        // Update rect position
        var cx = position.x - (parseInt(size.width) / 2);
        var cy = position.y - (parseInt(size.height) / 2);
        pivotRect.setPosition(cx, cy);

        // Make line visible only when the position has been already changed.
        // This solve several strange effects ;)
        var targetPoint = targetTopic.workoutIncomingConnectionPoint(pivotPoint);
        line.setTo(targetPoint.x, targetPoint.y);
    },

    setPosition : function(point) {
        this._position = point;
        this._redrawLine();
    },

    getPosition : function() {
        return this._position;
    },

    _buildRect : function() {
        var size = this._size;
        var rectAttributes = {fillColor:'#CC0033',opacity:0.4,width:size.width,height:size.height,strokeColor:'#FF9933'};
        var rect = new web2d.Rect(0, rectAttributes);
        rect.setVisibility(false);
        return rect;
    },

    _getPivotRect : function() {
        return this._dragPivot;
    },

    getSize : function() {
        var elem2d = this._getPivotRect();
        return elem2d.getSize();
    },

    setVisibility : function(value) {
        if (this.isVisible() != value) {

            var pivotRect = this._getPivotRect();
            pivotRect.setVisibility(value);

            var connectRect = this._connectRect;
            connectRect.setVisibility(value);

            var line = this._getConnectionLine();
            if (line) {
                line.setVisibility(value);
            }
            this._isVisible = value;
        }
    },

    // If the node is connected, validate that there is a line connecting both...
    _getConnectionLine : function() {
        var result = null;
        var parentTopic = this._targetTopic;
        if (parentTopic) {
            if (parentTopic.getType() == mindplot.model.INodeModel.CENTRAL_TOPIC_TYPE) {
                result = this._straightLine;
            } else {
                result = this._curvedLine;
            }
        }
        return result;
    },

    addToWorkspace : function(workspace) {
        var pivotRect = this._getPivotRect();
        workspace.append(pivotRect);

        var connectToRect = this._connectRect;
        workspace.append(connectToRect);

        // Add a hidden straight line ...
        var straighLine = this._straightLine;
        straighLine.setVisibility(false);
        workspace.append(straighLine);
        straighLine.moveToBack();

        // Add a hidden curved line ...
        var curvedLine = this._curvedLine;
        curvedLine.setVisibility(false);
        workspace.append(curvedLine);
        curvedLine.moveToBack();

        // Add a connect rect ...
        var connectRect = this._connectRect;
        connectRect.setVisibility(false);
        workspace.append(connectRect);
        connectRect.moveToBack();
    },

    removeFromWorkspace : function(workspace) {
        var shape = this._getPivotRect();
        workspace.removeChild(shape);

        var connectToRect = this._connectRect;
        workspace.removeChild(connectToRect);

        if ($defined(this._straightLine)) {
            workspace.removeChild(this._straightLine);
        }

        if ($defined(this._curvedLine)) {
            workspace.removeChild(this._curvedLine);
        }
    },

    connectTo : function(targetTopic, position) {
        $assert(!this._outgoingLine, 'Could not connect an already connected node');
        $assert(targetTopic != this, 'Circular connection are not allowed');
        $assert(position, 'position can not be null');
        $assert(targetTopic, 'parent can not be null');

        this._position = position;
        this._targetTopic = targetTopic;

        // Connected to Rect ...
        var connectRect = this._connectRect;
        var targetSize = targetTopic.getSize();

        // Add 4 pixel in order to keep create a rect bigger than the topic.
        var width = targetSize.width + 4;
        var height = targetSize.height + 4;

        connectRect.setSize(width, height);

        var targetPosition = targetTopic.getPosition();
        var cx = Math.ceil(targetPosition.x - (width / 2));
        var cy = Math.ceil(targetPosition.y - (height / 2));
        connectRect.setPosition(cx, cy);

        // Change elements position ...
        var pivotRect = this._getPivotRect();
        pivotRect.moveToFront();
        pivotRect.setPosition(position.x, position.y);

        this._redrawLine();
    },

    disconnect : function(workspace) {
        $assert(workspace, 'workspace can not be null.');
        $assert(this._targetTopic, 'There are not connected topic.');

        this.setVisibility(false);
        this._targetTopic = null;
    }
});
