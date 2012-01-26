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
mindplot.layout.OriginalLayout = new Class({
    initialize: function(treeSet) {
        this._treeSet = treeSet;
    },

    createNode:function(id, size, position, type) {
        $assert($defined(id), "id can not be null");
        $assert(size, "size can not be null");
        $assert(position, "position can not be null");
        $assert(type, "type can not be null");

        var strategy = type === 'root' ?
            mindplot.layout.OriginalLayout.BALANCED_SORTER :
            mindplot.layout.OriginalLayout.SYMMETRIC_SORTER;
        return new mindplot.layout.Node(id, size, position, strategy);
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
        var parent = this._treeSet.getParent(node);
        $assert(parent, "Node already disconnected");

        // Make it fixed
        node.setFree(false);
        node.setFreeDisplacement({x:0, y:0});

        // Remove from children list.
        var sorter = parent.getSorter();
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

            var heightById = sorter.computeChildrenIdByHeights(this._treeSet, node);

            this._layoutChildren(node, heightById);

            this._fixOverlapping(node, heightById);
        }, this);
    },

    _layoutChildren: function(node, heightById) {

        var nodeId = node.getId();
        var children = this._treeSet.getChildren(node);
        var parent = this._treeSet.getParent(node);
        var childrenOrderMoved = children.some(function(child) { return child.hasOrderChanged(); });
        var childrenSizeChanged = children.some(function(child) { return child.hasSizeChanged(); });

        // If ether any of the nodes has been changed of position or the height of the children is not
        // the same, children nodes must be repositioned ....
        var newBranchHeight = heightById[nodeId];

        var parentHeightChanged = $defined(parent) ? parent._heightChanged : false;
        var heightChanged = node._branchHeight != newBranchHeight;
        node._heightChanged = heightChanged || parentHeightChanged;

        if (childrenOrderMoved || childrenSizeChanged || heightChanged || parentHeightChanged) {
            var sorter = node.getSorter();
            var offsetById = sorter.computeOffsets(this._treeSet, node);
            var parentPosition = node.getPosition();

            children.forEach(function(child) {
                var offset = offsetById[child.getId()];

                offset.x += child.getFreeDisplacement().x;
                offset.y += child.getFreeDisplacement().y;

                var parentX = parentPosition.x;
                var parentY = parentPosition.y;

                var newPos = {x:parentX + offset.x, y:parentY + offset.y};
                this._treeSet.updateBranchPosition(child, newPos);
            }.bind(this));

            node._branchHeight = newBranchHeight;
        }

        // Continue reordering the children nodes ...
        children.forEach(function(child) {
            this._layoutChildren(child, heightById);
        }, this);
    },

    _fixOverlapping: function(node, heightById) {
        var children = this._treeSet.getChildren(node);

        if (node.isFree()) {
            this._shiftBranches(node, heightById);
        }

        children.forEach(function(child) {
            this._fixOverlapping(child, heightById);
        }, this);
    },

    _shiftBranches: function(node, heightById) {
        var branchesToShift = this._treeSet.getBranchesInVerticalDirection(node, node.getFreeDisplacement().y);

        var last = node;
        branchesToShift.forEach(function(branch) {
            if (this._branchesOverlap(branch, last, heightById)) {
                this._treeSet.shiftBranchPosition(branch, 0, node.getFreeDisplacement().y);
            }
            last = branch;
        },this);
    },

    _branchesOverlap: function(branchA, branchB, heightById) {
        var topA = branchA.getPosition().y - heightById[branchA.getId()]/2;
        var bottomA = branchA.getPosition().y + heightById[branchA.getId()]/2;
        var topB = branchB.getPosition().y - heightById[branchB.getId()]/2;
        var bottomB = branchB.getPosition().y + heightById[branchB.getId()]/2;

        return !(topA >= bottomB || bottomA <= topB);
    }

});

mindplot.layout.OriginalLayout.SYMMETRIC_SORTER = new mindplot.layout.SymmetricSorter();
mindplot.layout.OriginalLayout.BALANCED_SORTER = new mindplot.layout.BalancedSorter();



