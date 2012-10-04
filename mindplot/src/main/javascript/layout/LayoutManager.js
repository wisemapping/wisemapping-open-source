/*
 *    Copyright [2012] [wisemapping]
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
mindplot.layout.LayoutManager = new Class({
    Extends: Events,
    initialize: function(rootNodeId, rootSize) {
        $assert($defined(rootNodeId), "rootNodeId can not be null");
        $assert(rootSize, "rootSize can not be null");
        var position = position || {x:0, y:0};

        this._treeSet = new mindplot.layout.RootedTreeSet();
        this._layout = new mindplot.layout.OriginalLayout(this._treeSet);

        var rootNode = this._layout.createNode(rootNodeId, rootSize, position, 'root');
        this._treeSet.setRoot(rootNode);
        this._events = [];
    },

    updateNodeSize: function(id, size) {
        $assert($defined(id), "id can not be null");

        var node = this._treeSet.find(id);
        node.setSize(size);
    },

    updateShrinkState: function(id, value) {
        $assert($defined(id), "id can not be null");
        $assert($defined(value), "value can not be null");

        var node = this._treeSet.find(id);
        node.setShrunken(value);

        return this;
    },

    find: function(id) {
        return this._treeSet.find(id);
    },

    moveNode: function(id, position) {
        $assert($defined(id), "id cannot be null");
        $assert($defined(position), "position cannot be null");
        $assert($defined(position.x), "x can not be null");
        $assert($defined(position.y), "y can not be null");

        var node = this._treeSet.find(id);
        // @Todo: this should not be here. This is broking the isolated node support...
//        node.setFree(true);
//        node.setFreeDisplacement({x:position.x - node.getPosition().x, y:position.y - node.getPosition().y});
        node.setPosition(position);
    },

    connectNode: function(parentId, childId, order) {
        $assert($defined(parentId), "parentId cannot be null");
        $assert($defined(childId), "childId cannot be null");
        $assert($defined(order), "order cannot be null");

        this._layout.connectNode(parentId, childId, order);

        return this;
    },

    disconnectNode: function(id) {
        $assert($defined(id), "id can not be null");
        this._layout.disconnectNode(id);

        return this;
    },

    addNode:function(id, size, position) {
        $assert($defined(id), "id can not be null");
        var result = this._layout.createNode(id, size, position, 'topic');
        this._treeSet.add(result);

        return this;
    },

    removeNode: function(id) {
        $assert($defined(id), "id can not be null");
        var node = this._treeSet.find(id);

        // Is It connected ?
        if (this._treeSet.getParent(node)) {
            this.disconnectNode(id);
        }

        // Remove the all the branch ...
        this._treeSet.remove(id);

        return this;
    },

    predict: function(parentId, nodeId, position, free) {
        $assert($defined(parentId), "parentId can not be null");

        var parent = this._treeSet.find(parentId);
        var node = nodeId ? this._treeSet.find(nodeId) : null;
        var sorter = parent.getSorter();

        var result = sorter.predict(this._treeSet, parent, node, position, free);
        return {order:result[0],position:result[1]};
    },

    dump: function() {
        console.log(this._treeSet.dump());
    },

    plot: function(containerId, size) {
        $assert(containerId, "containerId cannot be null");
        size = size || {width:200,height:200};
        var squaresize = 10;
        var canvas = Raphael(containerId, size.width, size.height);
        canvas.drawGrid(0, 0, size.width, size.height, size.width / squaresize, size.height / squaresize);
        this._treeSet.plot(canvas);

        return canvas;
    },

    layout: function(fireEvents) {
        // File repositioning ...
        this._layout.layout();

        // Collect changes ...
        this._collectChanges();

        if (!$(fireEvents) || fireEvents) {
            this._flushEvents();
        }

        return this;
    },

    _flushEvents: function() {
        this._events.each(function(event) {
            this.fireEvent('change', event);
        }, this);
        this._events = [];
    },

    _collectChanges: function(nodes) {
        if (!nodes)
            nodes = this._treeSet.getTreeRoots();

        nodes.each(function(node) {
            if (node.hasOrderChanged() || node.hasPositionChanged()) {

                // Find or create a event ...
                var id = node.getId();
                var event = this._events.some(function(event) {
                    return event.id == id;
                });
                if (!event) {
                    event = new mindplot.layout.ChangeEvent(id);
                }

                // Update nodes ...
                event.setOrder(node.getOrder());
                event.setPosition(node.getPosition());

                node.resetPositionState();
                node.resetOrderState();
                node.resetFreeState();
                this._events.push(event);
            }
            this._collectChanges(this._treeSet.getChildren(node));
        }, this);
    }

});

