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

mindplot.layout.OriginalLayout = new Class(/** @lends OriginalLayout */{
    /**
     * @constructs
     * @param treeSet
     */
    initialize:function (treeSet) {
        this._treeSet = treeSet;
    },

    /** */
    createNode:function (id, size, position, type) {
        $assert($defined(id), "id can not be null");
        $assert(size, "size can not be null");
        $assert(position, "position can not be null");
        $assert(type, "type can not be null");

        var strategy = type === 'root' ?
            mindplot.layout.OriginalLayout.BALANCED_SORTER :
            mindplot.layout.OriginalLayout.SYMMETRIC_SORTER;
        return new mindplot.layout.Node(id, size, position, strategy);
    },

    /** */
    connectNode:function (parentId, childId, order) {

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

    /** */
    disconnectNode:function (nodeId) {
        var node = this._treeSet.find(nodeId);
        var parent = this._treeSet.getParent(node);
        $assert(parent, "Node already disconnected");

        // Make it fixed
        node.setFree(false);
        node.resetFreeDisplacement();

        // Remove from children list.
        var sorter = parent.getSorter();
        sorter.detach(this._treeSet, node);

        // Disconnect the new node ...
        this._treeSet.disconnect(nodeId);

        // Fire a basic validation ...
        parent.getSorter().verify(this._treeSet, parent);
    },

    /** */
    layout:function () {
        var roots = this._treeSet.getTreeRoots();
        _.each(roots, function (node) {

            // Calculate all node heights ...
            var sorter = node.getSorter();

            var heightById = sorter.computeChildrenIdByHeights(this._treeSet, node);

            this._layoutChildren(node, heightById);

            this._fixOverlapping(node, heightById);
        }, this);
    },

    _layoutChildren:function (node, heightById) {

        var nodeId = node.getId();
        var children = this._treeSet.getChildren(node);
        var parent = this._treeSet.getParent(node);
        var childrenOrderMoved = children.some(function (child) {
            return child.hasOrderChanged();
        });
        var childrenSizeChanged = children.some(function (child) {
            return child.hasSizeChanged();
        });

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
            var me = this;
            _.each(children, function (child) {
                var offset = offsetById[child.getId()];

                var childFreeDisplacement = child.getFreeDisplacement();
                var direction = node.getSorter().getChildDirection(me._treeSet, child);

                if ((direction > 0 && childFreeDisplacement.x < 0) || (direction < 0 && childFreeDisplacement.x > 0)) {
                    child.resetFreeDisplacement();
                    child.setFreeDisplacement({x:-childFreeDisplacement.x, y:childFreeDisplacement.y});
                }

                offset.x += child.getFreeDisplacement().x;
                offset.y += child.getFreeDisplacement().y;

                var parentX = parentPosition.x;
                var parentY = parentPosition.y;

                var newPos = {x:parentX + offset.x, y:parentY + offset.y + me._calculateAlignOffset(node, child, heightById)};
                me._treeSet.updateBranchPosition(child, newPos);
            });

            node._branchHeight = newBranchHeight;
        }

        // Continue reordering the children nodes ...
        _.each(children, function (child) {
            this._layoutChildren(child, heightById);
        }, this);
    },

    _calculateAlignOffset:function (node, child, heightById) {
        if (child.isFree()) {
            return 0;
        }

        var offset = 0;

        var nodeHeight = node.getSize().height;
        var childHeight = child.getSize().height;

        if (this._treeSet.isStartOfSubBranch(child) && this._branchIsTaller(child, heightById)) {
            if (this._treeSet.hasSinglePathToSingleLeaf(child)) {
                offset = heightById[child.getId()] / 2 - (childHeight + child.getSorter()._getVerticalPadding() * 2) / 2;
            } else {
                offset = this._treeSet.isLeaf(child) ? 0 : -(childHeight - nodeHeight) / 2;
            }
        } else if (nodeHeight > childHeight) {
            if (this._treeSet.getSiblings(child).length > 0) {
                offset = 0;
            } else {
                offset = nodeHeight / 2 - childHeight / 2;
            }
        }
        else if (childHeight > nodeHeight) {
            if (this._treeSet.getSiblings(child).length > 0) {
                offset = 0;
            } else {
                offset = -(childHeight / 2 - nodeHeight / 2);
            }
        }

        return offset;
    },

    _branchIsTaller:function (node, heightById) {
        return heightById[node.getId()] > (node.getSize().height + node.getSorter()._getVerticalPadding() * 2);
    },

    _fixOverlapping:function (node, heightById) {
        var children = this._treeSet.getChildren(node);

        if (node.isFree()) {
            this._shiftBranches(node, heightById);
        }

        _.each(children, function (child) {
            this._fixOverlapping(child, heightById);
        }, this);
    },

    _shiftBranches:function (node, heightById) {
        var shiftedBranches = [node];

        var siblingsToShift = this._treeSet.getSiblingsInVerticalDirection(node, node.getFreeDisplacement().y);
        var last = node;
        _.each(siblingsToShift, function (sibling) {
            var overlappingOccurs = shiftedBranches.some(function (shiftedBranch) {
                return this._branchesOverlap(shiftedBranch, sibling, heightById);
            }, this);

            if (!sibling.isFree() || overlappingOccurs) {
                var sAmount = node.getFreeDisplacement().y;
                this._treeSet.shiftBranchPosition(sibling, 0, sAmount);
                shiftedBranches.push(sibling);
            }
        }, this);

        var branchesToShift = this._treeSet.getBranchesInVerticalDirection(node, node.getFreeDisplacement().y).filter(function (branch) {
            return !shiftedBranches.contains(branch);
        });

        _.each(branchesToShift, function (branch) {
            var bAmount = node.getFreeDisplacement().y;
            this._treeSet.shiftBranchPosition(branch, 0, bAmount);
            shiftedBranches.push(branch);
            last = branch;
        }, this);
    },

    _branchesOverlap:function (branchA, branchB, heightById) {
        // a branch doesn't really overlap with itself
        if (branchA == branchB) {
            return false;
        }

        var topA = branchA.getPosition().y - heightById[branchA.getId()] / 2;
        var bottomA = branchA.getPosition().y + heightById[branchA.getId()] / 2;
        var topB = branchB.getPosition().y - heightById[branchB.getId()] / 2;
        var bottomB = branchB.getPosition().y + heightById[branchB.getId()] / 2;

        return !(topA >= bottomB || bottomA <= topB);
    }

});


/**
 * @type {mindplot.layout.SymmetricSorter}
 */
mindplot.layout.OriginalLayout.SYMMETRIC_SORTER = new mindplot.layout.SymmetricSorter();
/**
 * @type {mindplot.layout.BalancedSorter}
 */
mindplot.layout.OriginalLayout.BALANCED_SORTER = new mindplot.layout.BalancedSorter();



