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
mindplot.nlayout.LayoutManager = new Class({
    Extends: Events,
    initialize: function(rootNodeId, rootSize) {
        $assert($defined(rootNodeId), "rootNodeId can not be null");
        $assert(rootSize, "rootSize can not be null");

        this._treeSet = new mindplot.nlayout.RootedTreeSet();
        this._layout = new mindplot.nlayout.OriginalLayout(this._treeSet);

        var rootNode = this._layout.createNode(rootNodeId, rootSize, {x:0,y:0}, 'root');
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

        var node = this._treeSet.find(id);
        node.setShrunken(value);
    },

    find: function(id) {
        return this._treeSet.find(id);
    },

    move: function() {
        //TODO(gb): implement
    },

    connectNode: function(parentId, childId, order) {
        $assert($defined(parentId), "parentId can not be null");
        $assert($defined(childId), "childId can not be null");
        $assert($defined(order), "order can not be null");

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

    predict: function(parentId, position) {
        $assert($defined(parentId), "parentId can not be null");

        var parent = this._treeSet.find(parentId);
        var sorter = parent.getSorter();
        return sorter.predict(parent, this._treeSet, position);
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
    },

    _flushEvents: function() {
        this._events.forEach(function(event) {
            this.fireEvent('change', event);
        }, this);
        this._events = [];
    },

    _collectChanges: function(nodes) {
        if (!nodes)
            nodes = this._treeSet.getTreeRoots();

        nodes.forEach(function(node) {
            if (node.hasOrderChanged() || node.hasPositionChanged()) {

                // Find or create a event ...
                var id = node.getId();
                var event = this._events.some(function(event) {
                    return event.id == id;
                });
                if (!event) {
                    event = new mindplot.nlayout.ChangeEvent(id);
                }

                // Update nodes ...
                event.setOrder(node.getOrder());
                event.setPosition(node.getPosition());

                node.resetPositionState();
                node.resetOrderState();
                this._events.push(event);
            }
            this._collectChanges(this._treeSet.getChildren(node));
        }, this);

    }

});

