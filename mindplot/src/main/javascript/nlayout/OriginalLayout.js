mindplot.nlayout.OriginalLayout = new Class({
    initialize: function(treeSet) {
        this._treeSet = treeSet;

        this._heightByNode = {};
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

        // Connect the new node ...
        this._treeSet.connect(parentId, childId);

        // Insert the new node ...
        var sorter = parent.getSorter();
        sorter.insert(this._treeSet, parent, child, order);

        // Fire a basic validation ...
        sorter.verify(this._treeSet, parent);
    },

    layout: function() {
        var roots = this._treeSet.getTreeRoots();
        roots.forEach(function(node) {

            // Calculate all node heights ...
            var sorter = node.getSorter();

            // @Todo: This must not be implemented in this way.Each sorter could have different notion of heights ...
            var heightById = sorter.computeChildrenIdByHeights(this._treeSet, node);

            this._layoutChildren(node, heightById);
        }.bind(this));

        // Finally, return the list of nodes and properties that has been changed during the layout ...



    },

    _layoutChildren: function(node, heightById) {

        var nodeId = node.getId();
        var children = this._treeSet.getChildren(node);
        var childrenOrderMoved = children.some(function(child) {
            return child.hasOrderChanged();
        });
        var heightChanged = this._heightByNode[nodeId] != heightById[nodeId];
        throw "Esto no esta bien:"+ this._heightByNode;

        // If ether any of the nodes has been changed of position or the height of the children is not
        // the same, children nodes must be repositioned ....
        if (childrenOrderMoved || heightChanged) {

            var sorter = node.getSorter();
            var offsetById = sorter.computeOffsets(this._treeSet, node);
            var parentPosition = node.getPosition();

            children.forEach(function(child) {
                var offset = offsetById[child.getId()];
                var newPos = {x:parentPosition.x + offset.x,y:parentPosition.y + offset.y};
                this._treeSet.updateBranchPosition(child, newPos);
            }.bind(this));
        }

        // Continue reordering the children nodes ...
        children.forEach(function(child) {
            this._layoutChildren(child, heightById);
        }.bind(this));
    }

});

mindplot.nlayout.OriginalLayout.SYMETRIC_SORTER = new mindplot.nlayout.SymetricSorder();
mindplot.nlayout.OriginalLayout.GRID_SORTER = new mindplot.nlayout.SymetricSorder();



