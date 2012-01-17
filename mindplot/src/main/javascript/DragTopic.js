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

mindplot.DragTopic = new Class({
    initialize:function(dragShape, draggedNode) {
        $assert(dragShape, 'Rect can not be null.');
        $assert(draggedNode, 'draggedNode can not be null.');

        this._elem2d = dragShape;
        this._order = null;
        this._draggedNode = draggedNode;
        this._position = new core.Point();
    },

    setOrder : function(order) {
        this._order = order;
    },

    setPosition : function(x, y) {
        this._position.setValue(x, y);

        // Elements are positioned in the center.
        // All topic element must be positioned based on the innerShape.
        var draggedNode = this._draggedNode;
        var size = draggedNode.getSize();

        var cx = Math.ceil(x - (size.width / 2));
        var cy = Math.ceil(y - (size.height / 2));

        // Update visual position.
        this._elem2d.setPosition(cx, cy);

        if (this.isConnected()) {
            var parent = this.getConnectedToTopic();
            var predict = designer._eventBussDispatcher._layoutManager.predict(parent.getId(), this.getPosition());
            if (this._order != predict.order) {
                var dragPivot = this._getDragPivot();
                var position = predict.position;
                dragPivot.connectTo(parent, position);
                this.setOrder(predict.order);
            }
        }
    },

    getInnerShape : function() {
        return this._elem2d;
    },

    disconnect : function(workspace) {
        // Clear connection line ...
        var dragPivot = this._getDragPivot();
        dragPivot.disconnect(workspace);
    },

    canBeConnectedTo : function(targetTopic) {
        $assert(targetTopic, 'parent can not be null');

        var result = true;
        if (!targetTopic.areChildrenShrunken() && !targetTopic.isCollapsed()) {
            // Dragged node can not be connected to himself.
            if (targetTopic == this._draggedNode) {
                result = false;
            } else {
                var draggedNode = this.getDraggedTopic();
                var topicPosition = this.getPosition();

                var targetTopicModel = targetTopic.getModel();
                var childTopicModel = draggedNode.getModel();

                result = targetTopicModel.canBeConnected(childTopicModel, topicPosition, 18);
            }
        } else {
            result = false;
        }
        return result;
    },

    connectTo : function(parent) {
        $assert(parent, 'Parent connection node can not be null.');

        // Where it should be connected ?
        var predict = designer._eventBussDispatcher._layoutManager.predict(parent.getId(), this.getPosition());

        // Connect pivot ...
        var dragPivot = this._getDragPivot();
        var position = predict.position;
        dragPivot.connectTo(parent, position);

        this.setOrder(predict.order);
    },

    getDraggedTopic : function() {
        return  this._draggedNode;
    },

    removeFromWorkspace : function(workspace) {
        // Remove drag shadow.
        workspace.removeChild(this._elem2d);

        // Remove pivot shape. To improve performace it will not be removed. Only the visibility will be changed.
        var dragPivot = this._getDragPivot();
        dragPivot.setVisibility(false);
    },

    addToWorkspace : function(workspace) {
        workspace.appendChild(this._elem2d);
        var dragPivot = this._getDragPivot();

        dragPivot.addToWorkspace(workspace);
    },

    _getDragPivot : function() {
        return mindplot.DragTopic.__getDragPivot();
    },

    getPosition:function() {
        return this._position;
    },

    isDragTopic : function() {
        return true;
    },

    applyChanges : function(workspace) {
        $assert(workspace, 'workspace can not be null');

        var draggedTopic = this.getDraggedTopic();

        var isDragConnected = this.isConnected();
        var actionDispatcher = mindplot.ActionDispatcher.getInstance();
        var topicId = draggedTopic.getId();

        var dragPosition = this.getPosition();
        var order = null;
        var parent = null;
        if (isDragConnected) {
            var targetTopic = this.getConnectedToTopic();
            order = this._order;
            parent = targetTopic;
        }

        // If the node is not connected, position based on the original drag topic position.
        actionDispatcher.dragTopic(topicId, dragPosition, order, parent);

    },

    getConnectedToTopic : function() {
        var dragPivot = this._getDragPivot();
        return dragPivot.getTargetTopic();
    },

    isConnected : function() {
        return this.getConnectedToTopic() != null;
    }

})
    ;

mindplot.DragTopic.PIVOT_SIZE = {width:50,height:6};

mindplot.DragTopic.init = function(workspace) {

    $assert(workspace, "workspace can not be null");
    var pivot = mindplot.DragTopic.__getDragPivot();
    workspace.appendChild(pivot);
};

mindplot.DragTopic.__getDragPivot = function() {
    var result = mindplot.DragTopic._dragPivot;
    if (!$defined(result)) {
        result = new mindplot.DragPivot();
        mindplot.DragTopic._dragPivot = result;
    }
    return result;
}
                             
