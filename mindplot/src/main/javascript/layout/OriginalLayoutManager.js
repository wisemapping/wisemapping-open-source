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

mindplot.layout.OriginalLayoutManager = new Class({
    Extends:mindplot.layout.BaseLayoutManager,
    options:{

    },
    initialize:function(designer, options) {
        this.parent(designer, options);
        this._dragTopicPositioner = new mindplot.DragTopicPositioner(this);

        // Init drag manager.
        var workSpace = this.getDesigner().getWorkSpace();
        this._dragger = this._buildDragManager(workSpace);

        // Add shapes to speed up the loading process ...
        mindplot.DragTopic.init(workSpace);
    },
    prepareNode:function(node, children) {
        // Sort children by order to solve adding order in for OriginalLayoutManager...
        var nodesByOrder = new Hash();
        var maxOrder = 0;
        var result = [];
        if (children.length > 0) {
            for (var i = 0; i < children.length; i++) {
                var child = children[i];
                var order = child.getOrder();
                if (!$defined(order)) {
                    order = ++maxOrder;
                    child.setOrder(order);
                }

                if (nodesByOrder.has(order)) {
                    if (Math.sign(child.getPosition().x) == Math.sign(nodesByOrder.get(order).getPosition().x)) {
                        //duplicated order. Change order to next available.
                        order = ++maxOrder;
                        child.setOrder(order);
                    }
                } else {
                    nodesByOrder.set(order, child);
                    if (order > maxOrder)
                        maxOrder = order;
                }
                result[order] = child;
            }
        }
        nodesByOrder = null;
        return node.getTopicType() != mindplot.model.NodeModel.CENTRAL_TOPIC_TYPE ? result : children;
    },

    _nodeResizeEvent:function(node) {

    },

    _nodeRepositionateEvent:function(node) {
        this.getTopicBoardForTopic(node).repositionate();
    },

    getDragTopicPositioner : function() {
        return this._dragTopicPositioner;
    },

    _buildDragManager: function(workspace) {
        // Init dragger manager.
        var dragger = new mindplot.DragManager(workspace);
        var topics = this.getDesigner()._getTopics();

        var dragTopicPositioner = this.getDragTopicPositioner();

        dragger.addEventListener('startdragging', function(event, node) {
            // Enable all mouse events.
            for (var i = 0; i < topics.length; i++) {
                topics[i].setMouseEventsEnabled(false);
            }
        });

        dragger.addEventListener('dragging', function(event, dragTopic) {
            // Update the state and connections of the topic ...
            dragTopicPositioner.positionateDragTopic(dragTopic);
        });

        dragger.addEventListener('enddragging', function(event, dragTopic) {
            // Enable all mouse events.
            for (var i = 0; i < topics.length; i++) {
                topics[i].setMouseEventsEnabled(true);
            }
            // Topic must be positioned in the real board postion.
            if (dragTopic._isInTheWorkspace) {
                var draggedTopic = dragTopic.getDraggedTopic();

                // Hide topic during draw ...
                draggedTopic.setBranchVisibility(false);
                var parentNode = draggedTopic.getParent();
                dragTopic.updateDraggedTopic(workspace);


                // Make all node visible ...
                draggedTopic.setVisibility(true);
                if (parentNode != null) {
                    parentNode.setBranchVisibility(true);
                }
            }
        });

        return dragger;
    },

    registerListenersOnNode : function(topic) {
        // Register node listeners ...
        var designer = this.getDesigner();
        topic.addEventListener('click', function(event) {
            designer.onObjectFocusEvent(topic, event);
        });

        // Add drag behaviour ...
        if (topic.getType() != mindplot.model.NodeModel.CENTRAL_TOPIC_TYPE) {

            // Central Topic doesn't support to be dragged
            var dragger = this._dragger;
            dragger.add(topic);
        }

        // Register editor events ...
        if (!$defined(this.getDesigner()._viewMode) || ($defined(this.getDesigner()._viewMode) && !this.getDesigner()._viewMode)) {
            this.getDesigner()._editor.listenEventOnNode(topic, 'dblclick', true);
        }

    },

    _createMainTopicBoard:function(node) {
        return new mindplot.MainTopicBoard(node, this);
    },

    _createCentralTopicBoard:function(node) {
        return new mindplot.CentralTopicBoard(node, this);
    },

    getClassName:function() {
        return mindplot.layout.OriginalLayoutManager.NAME;
    }
});

mindplot.layout.OriginalLayoutManager.NAME = "OriginalLayoutManager";