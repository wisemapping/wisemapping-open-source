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

mindplot.layout.SymmetricSorter = new Class(/** @lends SymmetricSorter */{
    Extends:mindplot.layout.AbstractBasicSorter,
    /**
     * @constructs
     * @extends mindplot.layout.AbstractBasicSorter
     */
    initialize:function () {

    },

    /**
     * Predict the order and position of a dragged node.
     *
     * @param graph The tree set
     * @param parent The parent of the node
     * @param node The node
     * @param position The position of the drag
     * @param free Free drag or not
     * @return {*}
     */
    predict:function (graph, parent, node, position, free) {

        var self = this;
        var rootNode = graph.getRootNode(parent);

        // If its a free node...
        if (free) {
            $assert($defined(position), "position cannot be null for predict in free positioning");
            $assert($defined(node), "node cannot be null for predict in free positioning");

            var direction = this._getRelativeDirection(rootNode.getPosition(), parent.getPosition());
            var limitXPos = parent.getPosition().x + direction * (parent.getSize().width / 2 + node.getSize().width / 2 + mindplot.layout.SymmetricSorter.INTERNODE_HORIZONTAL_PADDING);

            var xPos = direction > 0 ?
                (position.x >= limitXPos ? position.x : limitXPos) :
                (position.x <= limitXPos ? position.x : limitXPos);

            return [0, {x:xPos, y:position.y}];
        }

        // Its not a dragged node (it is being added)
        if (!node) {
            var parentDirection = self._getRelativeDirection(rootNode.getPosition(), parent.getPosition());
            var position = {
                x:parent.getPosition().x + parentDirection * (parent.getSize().width + mindplot.layout.SymmetricSorter.INTERNODE_HORIZONTAL_PADDING),
                y:parent.getPosition().y
            };
            return [graph.getChildren(parent).length, position];
        }

        // If it is a dragged node...
        $assert($defined(position), "position cannot be null for predict in dragging");
        var nodeDirection = this._getRelativeDirection(rootNode.getPosition(), node.getPosition());
        var positionDirection = this._getRelativeDirection(rootNode.getPosition(), position);
        var siblings = graph.getSiblings(node);

        // node has no siblings and its trying to reconnect to its own parent
        var sameParent = parent == graph.getParent(node);
        if (siblings.length == 0 && nodeDirection == positionDirection && sameParent) {
            return [node.getOrder(), node.getPosition()];
        }

        var parentChildren = graph.getChildren(parent);

        if (parentChildren.length == 0) {
            // Fit as a child of the parent node...
            var position = {
                x:parent.getPosition().x + positionDirection * (parent.getSize().width + mindplot.layout.SymmetricSorter.INTERNODE_HORIZONTAL_PADDING),
                y:parent.getPosition().y
            };
            return [0, position];
        } else {
            // Try to fit within ...
            var result = null;
            var last = parentChildren.getLast();
            for (var i = 0; i < parentChildren.length; i++) {
                var parentChild = parentChildren[i];
                var nodeAfter = (i + 1) == parentChild.length ? null : parentChildren[i + 1];

                // Fit at the bottom
                if (!nodeAfter && position.y > parentChild.getPosition().y) {
                    var order = (graph.getParent(node)  && graph.getParent(node).getId() == parent.getId()) ?
                        last.getOrder() : last.getOrder() + 1;
                    var position = {
                        x:parentChild.getPosition().x,
                        y:parentChild.getPosition().y + parentChild.getSize().height + mindplot.layout.SymmetricSorter.INTERNODE_VERTICAL_PADDING * 2
                    };
                    return [order, position];
                }

                // Fit after this node
                if (nodeAfter && position.y > parentChild.getPosition().y && position.y < nodeAfter.getPosition().y) {
                    if (nodeAfter.getId() == node.getId() || parentChild.getId() == node.getId()) {
                        return [node.getOrder(), node.getPosition()];
                    } else {
                        var order = position.y > node.getPosition().y ?
                            nodeAfter.getOrder() - 1 : parentChild.getOrder() + 1;
                        var position = {
                            x:parentChild.getPosition().x,
                            y:parentChild.getPosition().y + (nodeAfter.getPosition().y - parentChild.getPosition().y) / 2
                        };

                        return [order, position];
                    }
                }
            }
        }

        // Position wasn't below any node, so it must be fitted above the first
        var first = parentChildren[0];
        var position = {
            x:first.getPosition().x,
            y:first.getPosition().y - first.getSize().height - mindplot.layout.SymmetricSorter.INTERNODE_VERTICAL_PADDING * 2
        };
        return [0, position];
    },

    /** 
     * @param treeSet
     * @param parent
     * @param child
     * @param order
     * @throws will throw an error if the order is not strictly continuous
     */
    insert:function (treeSet, parent, child, order) {
        var children = this._getSortedChildren(treeSet, parent);
        $assert(order <= children.length, "Order must be continues and can not have holes. Order:" + order);

        // Shift all the elements in one .
        for (var i = order; i < children.length; i++) {
            var node = children[i];
            node.setOrder(i + 1);
        }
        child.setOrder(order);
    },

    /** 
     * @param treeSet
     * @param node
     * @throws will throw an error if the node is in the wrong position*/
    detach:function (treeSet, node) {
        var parent = treeSet.getParent(node);
        var children = this._getSortedChildren(treeSet, parent);
        var order = node.getOrder();
        $assert(children[order] === node, "Node seems not to be in the right position");

        // Shift all the nodes ...
        for (var i = node.getOrder() + 1; i < children.length; i++) {
            var child = children[i];
            child.setOrder(child.getOrder() - 1);
        }
        node.setOrder(0);
    },

    /** 
     * @param treeSet
     * @param node
     * @throws will throw an error if treeSet is null or undefined
     * @throws will throw an error if node is null or undefined
     * @throws will throw an error if the calculated x offset cannot be converted to a numeric 
     * value, is null or undefined
     * @throws will throw an error if the calculated y offset cannot be converted to a numeric 
     * value, is null or undefined
     * @return offsets
     */
    computeOffsets:function (treeSet, node) {
        $assert(treeSet, "treeSet can no be null.");
        $assert(node, "node can no be null.");

        var children = this._getSortedChildren(treeSet, node);

        // Compute heights ...
        var heights = children.map(
            function (child) {
                return {id:child.getId(), order:child.getOrder(), position:child.getPosition(), width:child.getSize().width, height:this._computeChildrenHeight(treeSet, child)};
            }, this).reverse();

        // Compute the center of the branch ...
        var totalHeight = 0;
        _.each(heights, function (elem) {
            totalHeight += elem.height;
        });
        var ysum = totalHeight / 2;

        // Calculate the offsets ...
        var result = {};
        for (var i = 0; i < heights.length; i++) {
            ysum = ysum - heights[i].height;
            var childNode = treeSet.find(heights[i].id);
            var direction = this.getChildDirection(treeSet, childNode);

            var yOffset = ysum + heights[i].height / 2;
            var xOffset = direction * (heights[i].width / 2 + node.getSize().width / 2 + mindplot.layout.SymmetricSorter.INTERNODE_HORIZONTAL_PADDING);

            $assert(!isNaN(xOffset), "xOffset can not be null");
            $assert(!isNaN(yOffset), "yOffset can not be null");

            result[heights[i].id] = {x:xOffset, y:yOffset};
        }
        return result;
    },

    /** 
     * @param treeSet
     * @param node
     * @throws will throw an error if order elements are missing
     */
    verify:function (treeSet, node) {
        // Check that all is consistent ...
        var children = this._getSortedChildren(treeSet, node);

        for (var i = 0; i < children.length; i++) {
            $assert(children[i].getOrder() == i, "missing order elements");
        }
    },

    /** 
     * @param treeSet
     * @param child
     * @return direction of the given child from its parent or from the root node, if isolated*/
    getChildDirection:function (treeSet, child) {
        $assert(treeSet, "treeSet can no be null.");
        $assert(treeSet.getParent(child), "This should not happen");

        var result;
        var rootNode = treeSet.getRootNode(child);
        if (treeSet.getParent(child) == rootNode) {
            // This is the case of a isolated child ... In this case, the directions is based on the root.
            result = Math.sign(rootNode.getPosition().x);
        } else {
            // if this is not the case, honor the direction of the parent ...
            var parent = treeSet.getParent(child);
            var grandParent = treeSet.getParent(parent);
            var sorter = grandParent.getSorter();
            result = sorter.getChildDirection(treeSet, parent);

        }
        return result;
    },

    /** @return {String} the print name of this class */
    toString:function () {
        return "Symmetric Sorter";
    },

    _getVerticalPadding:function () {
        return mindplot.layout.SymmetricSorter.INTERNODE_VERTICAL_PADDING;
    }
});

/**
 * @constant
 * @type {Number}
 * @default
 */
mindplot.layout.SymmetricSorter.INTERNODE_VERTICAL_PADDING = 5;
/**
 * @constant
 * @type {Number}
 * @default
 */
mindplot.layout.SymmetricSorter.INTERNODE_HORIZONTAL_PADDING = 30;


