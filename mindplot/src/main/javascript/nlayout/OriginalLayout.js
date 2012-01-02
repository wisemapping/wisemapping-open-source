mindplot.nlayout.OriginalLayout = new Class({
    initialize: function(treeSet) {
        this._treeSet = treeSet;
    },

    createNode:function(id, size, position, type) {
        $assert($defined(id), "id can not be null");
        $assert(size, "size can not be null");
        $assert(position, "position can not be null");
        $assert(type, "type can not be null");

        var strategy = type === 'root' ? mindplot.nlayout.OriginalLayout.GRID_SORTER : mindplot.nlayout.OriginalLayout.SYMETRIC_SORTER;
        return new mindplot.nlayout.Node(id, size, position, strategy);
    },

    connectNode: function(parentId, childId, order) {

        var parent = this._treeSet.find(parentId);
        var child = this._treeSet.find(childId);

        // Insert the new node ...
        var sorter = parent.getSorter();
        sorter.insert(this._treeSet, parent, child, order);

        // Connect the new node ...
        this._treeSet.connect(parentId, childId);

        // Fire a basic validation ...
        sorter.verify(this._treeSet, parent);
    },

    disconnectNode: function(nodeId) {
        var node = this._treeSet.find(nodeId);
        $assert(this._treeSet.getParent(node), "Node already disconnected");

        // Remove from children list.
        var sorter = node.getSorter();
        sorter.detach(this._treeSet, node);

        // Disconnect the new node ...
        this._treeSet.disconnect(nodeId);

        // Fire a basic validation ...
        sorter.verify(this._treeSet, node);
    },

    layout: function() {
        var roots = this._treeSet.getTreeRoots();
        roots.forEach(function(node) {

            // Calculate all node heights ...
            var sorter = node.getSorter();

            // @Todo: This must not be implemented in this way.Each sorter could have different notion of heights ...
            var heightById = sorter.computeChildrenIdByHeights(this._treeSet, node);

            this._layoutChildren(node, heightById);
        }, this);
    },

    _layoutChildren: function(node, heightById) {

        var nodeId = node.getId();
        var children = this._treeSet.getChildren(node);
        var childrenOrderMoved = children.some(function(child) {
            return child.hasOrderChanged();
        });

        // If ether any of the nodes has been changed of position or the height of the children is not
        // the same, children nodes must be repositioned ....
        var newBranchHeight = heightById[nodeId];
        var heightChanged = node._branchHeight != newBranchHeight;
        if (childrenOrderMoved || heightChanged) {

            var sorter = node.getSorter();
            var offsetById = sorter.computeOffsets(this._treeSet, node);
            var parentPosition = node.getPosition();

            children.forEach(function(child) {
                var offset = offsetById[child.getId()];
                var parentX = parentPosition.x;
                var parentY = parentPosition.y;

                var verticalOffset = (node.getSize().height / 2);

                var newPos = {x:parentX + offset.x,y:parentY + offset.y + verticalOffset};
                this._treeSet.updateBranchPosition(child, newPos);
            }.bind(this));

            node._branchHeight = newBranchHeight;
        }

        // Continue reordering the children nodes ...
        children.forEach(function(child) {
            this._layoutChildren(child, heightById);
        }, this);
    }

});

mindplot.nlayout.OriginalLayout.SYMETRIC_SORTER = new mindplot.nlayout.SymetricSorder();
mindplot.nlayout.OriginalLayout.GRID_SORTER = new mindplot.nlayout.GridSorter();
//mindplot.nlayout.OriginalLayout.GRID_SORTER = new mindplot.nlayout.SymetricSorder();



