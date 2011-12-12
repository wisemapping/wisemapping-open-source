mindplot.nlayout.RootedTreeSet = new Class({
    initialize:function() {
        this._rootNodes = [];
    },

    setRoot:function(root) {
        $assert(root, 'root can not be null');
        this._rootNodes.push(this._decodate(root));
    },

    getTreeRoots:function() {
        return this._rootNodes;
    },

    _decodate:function(node) {
        node._children = [];
        return node;
    },

    add: function(node) {
        $assert(node, 'node can not be null');
        $assert(!node._children, 'node already added');
        this._rootNodes.push(this._decodate(node));
    },

    remove: function(nodeId) {
        $assert($defined(nodeId), 'nodeId can not be null');
        var node = this.find(nodeId);
        this._rootNodes.erase(node);
    },

    connect: function(parentId, childId) {
        $assert($defined(parentId), 'parent can not be null');
        $assert($defined(childId), 'child can not be null');

        var parent = this.find(parentId);
        var child = this.find(childId, true);
        $assert(!child._parent, 'node already connected. Id:' + child.getId() + ",previous:" + child._parent);

        parent._children.push(child);
        child._parent = parent;
        this._rootNodes.erase(child);
    },

    disconnect: function(nodeId) {
        $assert($defined(nodeId), 'nodeId can not be null');
        var node = this.find(nodeId);
        $assert(node._parent, "Node is not connected");

        node._parent._children.erase(node);
        this._rootNodes.push(node);
        node._parent = null;
    },

    find:function(id, validate) {
        $assert($defined(id), 'id can not be null');

        var graphs = this._rootNodes;
        var result = null;
        for (var i = 0; i < graphs.length; i++) {
            var node = graphs[i];
            result = this._find(id, node);
            if (result) {
                break;
            }
        }
        $assert($defined(validate) ? result : true, 'node could not be found id:' + id);
        return result;

    },

    _find:function(id, parent) {
        if (parent.getId() == id) {
            return parent;

        }

        var result = null;
        var children = parent._children;
        for (var i = 0; i < children.length; i++) {
            var child = children[i];
            result = this._find(id, child);
            if (result)
                break;
        }

        return result;
    },

    getChildren:function(node) {
        $assert(node, 'node can not be null');
        return node._children;
    },

    getParent:function(node) {
        $assert(node, 'node can not be null');
        return node._parent;
    },

    dump: function() {
        var branches = this._rootNodes;
        var result = "";
        for (var i = 0; i < branches.length; i++) {
            var branch = branches[i];
            result += this._dump(branch, "");
        }
        return result;
    },

    _dump:function(node, indent) {
        var result = indent + node + "\n";
        var children = this.getChildren(node);
        for (var i = 0; i < children.length; i++) {
            var child = children[i];
            result += this._dump(child, indent + "   ");
        }

        return result;
    },

    updateBranchPosition : function(node, position) {

        var oldPos = node.getPosition();
        node.setPosition(position);

        var xOffset = oldPos.x - position.x;
        var yOffset = oldPos.y - position.y;

        var children = this.getChildren(node);
        children.forEach(function(child) {
            this._shiftBranchPosition(child, xOffset, yOffset);
        }.bind(this));

    },

    _shiftBranchPosition : function(node, xOffset, yOffset) {
        var position = node.getPosition();
        node.setPosition({x:position.x + xOffset, y:position.y + yOffset});

        var children = this.getChildren(node);
        children.forEach(function(child) {
            this._shiftBranchPosition(child, xOffset, yOffset);
        }.bind(this));
    }

});

