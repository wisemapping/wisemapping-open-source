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
        var node = this._treeSet.find(id);
        node.setSize(size);
        // @Todo: finish...
    },

    updateShirkState: function(id, isShrink) {
        // @Todo: finish...
    },

    connectNode: function(parentId, childId, order) {
        $assert($defined(parentId), "parentId can not be null");
        $assert($defined(childId), "childId can not be null");
        $assert($defined(order), "order can not be null");

        this._layout.connectNode(parentId, childId, order);
    },

    disconnectNode: function(id) {
        $assert($defined(id), "id can not be null");
        this._layout.disconnectNode(id);
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

    },

    predict: function(parentId, childId, position) {
        $assert($defined(parentId), "parentId can not be null");
        $assert($defined(childId), "childId can not be null");
        $assert(position, "childId can not be null");

        var parent = this._treeSet.find(parentId);
        var sorter = parent.getSorter();
        return  sorter.predict(parent, this._treeSet, position);
    },

    dump: function() {
        console.log(this._treeSet.dump());
    },

    plot: function(position) {
        var size = {w:200,h:200};
        var padding = 20,
            squaresize = 20;
        var canvas = Raphael(position.x + padding, position.y + padding, size.w, size.h);
        canvas.drawGrid(0, 0, size.w, size.h, size.w/squaresize, size.h/squaresize);
        this._treeSet.plot(canvas);
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
                if (node.hasOrderChanged()) {
                    event.setOrder(node.getOrder());
                    node.resetOrderState();

                }

                if (node.hasPositionChanged()) {
                    event.setPosition(node.getPosition());
                    node.resetPositionState();
                }
                this._events.push(event);
            }
            this._collectChanges(this._treeSet.getChildren(node));
        }, this);

    }

});

