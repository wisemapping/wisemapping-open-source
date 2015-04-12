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

mindplot.DragTopic = new Class({
    initialize:function (dragShape, draggedNode, layoutManger) {
        $assert(dragShape, 'Rect can not be null.');
        $assert(draggedNode, 'draggedNode can not be null.');
        $assert(layoutManger, 'layoutManger can not be null.');

        this._elem2d = dragShape;
        this._order = null;
        this._draggedNode = draggedNode;
        this._layoutManager = layoutManger;
        this._position = new core.Point();
        this._isInWorkspace = false;
        this._isFreeLayoutEnabled = false;
    },

    setOrder:function (order) {
        this._order = order;
    },

    setPosition:function (x, y) {
        // Update drag shadow position ....
        var position = {x:x, y:y};
        if (this.isFreeLayoutOn() && this.isConnected()) {
            var _layoutManager = this._layoutManager;
            var par = this.getConnectedToTopic();
            position = _layoutManager.predict(par.getId(), this._draggedNode.getId(), position, true).position;
        }
        this._position.setValue(position.x, position.y);

        // Elements are positioned in the center.
        // All topic element must be positioned based on the innerShape.
        var draggedNode = this._draggedNode;
        var size = draggedNode.getSize();
        var cx = position.x - (position.x > 0 ? 0 : size.width);
        var cy = Math.ceil(position.y - (size.height / 2));
        this._elem2d.setPosition(cx, cy);

        // In case is not free, pivot must be draw ...
        if (this.isConnected() && !this.isFreeLayoutOn()) {
            var parent = this.getConnectedToTopic();
            var predict = this._layoutManager.predict(parent.getId(), this._draggedNode.getId(), this.getPosition());
            if (this._order != predict.order) {
                var dragPivot = this._getDragPivot();
                var pivotPosition = predict.position;
                dragPivot.connectTo(parent, pivotPosition);
                this.setOrder(predict.order);
            }
        }
    },

    updateFreeLayout:function (event) {
        var isFreeEnabled = (event.metaKey && Browser.Platform.mac) || (event.ctrlKey && !Browser.Platform.mac);
        if (this.isFreeLayoutOn() != isFreeEnabled) {
            var dragPivot = this._getDragPivot();
            dragPivot.setVisibility(!isFreeEnabled);
            this._isFreeLayoutEnabled = isFreeEnabled;
        }
    },

    setVisibility:function (value) {
        var dragPivot = this._getDragPivot();
        dragPivot.setVisibility(value);
    },

    isVisible:function () {
        var dragPivot = this._getDragPivot();
        return dragPivot.isVisible();
    },

    getInnerShape:function () {
        return this._elem2d;
    },

    disconnect:function (workspace) {
        // Clear connection line ...
        var dragPivot = this._getDragPivot();
        dragPivot.disconnect(workspace);
    },

    connectTo:function (parent) {
        $assert(parent, 'Parent connection node can not be null.');

        // Where it should be connected ?
        var predict = designer._eventBussDispatcher._layoutManager.predict(parent.getId(), this._draggedNode.getId(), this.getPosition());

        // Connect pivot ...
        var dragPivot = this._getDragPivot();
        var position = predict.position;
        dragPivot.connectTo(parent, position);
        dragPivot.setVisibility(true);

        this.setOrder(predict.order);
    },

    getDraggedTopic:function () {
        return  this._draggedNode;
    },

    removeFromWorkspace:function (workspace) {
        if (this._isInWorkspace) {
            // Remove drag shadow.
            workspace.removeChild(this._elem2d);

            // Remove pivot shape. To improve performance it will not be removed. Only the visibility will be changed.
            var dragPivot = this._getDragPivot();
            dragPivot.setVisibility(false);

            this._isInWorkspace = false;
        }
    },

    isInWorkspace:function () {
        return this._isInWorkspace;
    },

    addToWorkspace:function (workspace) {
        if (!this._isInWorkspace) {
            workspace.append(this._elem2d);
            var dragPivot = this._getDragPivot();
            dragPivot.addToWorkspace(workspace);
            this._isInWorkspace = true;
        }
    },

    _getDragPivot:function () {
        return mindplot.DragTopic.__getDragPivot();
    },

    getPosition:function () {
        return this._position;
    },

    isDragTopic:function () {
        return true;
    },

    applyChanges:function (workspace) {
        $assert(workspace, 'workspace can not be null');


        var actionDispatcher = mindplot.ActionDispatcher.getInstance();
        var draggedTopic = this.getDraggedTopic();
        var topicId = draggedTopic.getId();
        var position = this.getPosition();

        if (!this.isFreeLayoutOn()) {
            var order = null;
            var parent = null;
            var isDragConnected = this.isConnected();
            if (isDragConnected) {
                var targetTopic = this.getConnectedToTopic();
                order = this._order;
                parent = targetTopic;
            }

            // If the node is not connected, position based on the original drag topic position.
            actionDispatcher.dragTopic(topicId, position, order, parent);
        } else {
            actionDispatcher.moveTopic(topicId, position);
        }
    },

    getConnectedToTopic:function () {
        var dragPivot = this._getDragPivot();
        return dragPivot.getTargetTopic();
    },

    isConnected:function () {
        return this.getConnectedToTopic() != null;
    },

    isFreeLayoutOn:function () {
//        return  this._isFreeLayoutEnabled;
        // Disable free layout ...
        return false;
    }

});

mindplot.DragTopic.PIVOT_SIZE = {width:50, height:6};

mindplot.DragTopic.init = function (workspace) {

    $assert(workspace, "workspace can not be null");
    var pivot = mindplot.DragTopic.__getDragPivot();
    workspace.append(pivot);
};

mindplot.DragTopic.__getDragPivot = function () {
    var result = mindplot.DragTopic._dragPivot;
    if (!$defined(result)) {
        result = new mindplot.DragPivot();
        mindplot.DragTopic._dragPivot = result;
    }
    return result;
};
                             
