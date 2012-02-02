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
mindplot.layout.SymmetricSorter = new Class({
    Extends: mindplot.layout.AbstractBasicSorter,
    initialize:function() {

    },

    predict : function(graph, parent, node, position, free) {
        // If its a free node...
        if (free) {
            $assert($defined(position), "position cannot be null for predict in free positioning");

            //TODO(gb): check this. Should direction be obtained by the sorter?
            var rootNode = graph.getRootNode(parent);
            var direction = parent.getPosition().x > rootNode.getPosition().x ? 1 : -1;

            var xPos = direction > 0 ?
                (position.x >= parent.getPosition().x ? position.x : parent.getPosition().x) :
                (position.x <= parent.getPosition().x ? position.x : parent.getPosition().x);

            return [0, {x: xPos, y:position.y}];
        }

        var rootNode = graph.getRootNode(parent);

        // If it is a dragged node...
        if (node) {
            $assert($defined(position), "position cannot be null for predict in dragging");
            var nodeDirection = this._getRelativeDirection(rootNode.getPosition(), node.getPosition());
            var positionDirection = this._getRelativeDirection(rootNode.getPosition(), position);
            var siblings = graph.getSiblings(node);

            var sameParent = parent == graph.getParent(node);
            if (siblings.length == 0 && nodeDirection == positionDirection && sameParent) {
                return [node.getOrder(), node.getPosition()];
            }
        }

        // Regular node
        var direction = parent.getPosition().x > rootNode.getPosition().x ? 1 : -1;

        // No children...
        var children = graph.getChildren(parent).filter(function(child) { return child != node; });
        if (children.length == 0) {
            position = position || {x:parent.getPosition().x + direction, y:parent.getPosition().y};
            var pos = {
                x: parent.getPosition().x + direction * (parent.getSize().width + mindplot.layout.SymmetricSorter.INTERNODE_HORIZONTAL_PADDING),
                y:parent.getPosition().y
            };
            return [0, pos];
        }

        // Try to fit within ...
        var result = null;
        var last = children.getLast();
        position = position || {x:last.getPosition().x + direction, y:last.getPosition().y + 1};
        children.each(function(child, index) {
            var cpos = child.getPosition();
            if (position.y > cpos.y) {
                var yOffset = child == last ?
                    child.getSize().height + mindplot.layout.SymmetricSorter.INTERNODE_VERTICAL_PADDING * 2 :
                    (children[index + 1].getPosition().y + children[index + 1].getSize().height / 2 - child.getPosition().y) / 2;
                result = [child.getOrder() + 1,{x:cpos.x, y:cpos.y + yOffset}];
            }
        });

        // Position wasn't below any node, so it must be inserted above
        if (!result) {
            var first = children[0];
            result = [0, {
                x:first.getPosition().x,
                y:first.getPosition().y - first.getSize().height - mindplot.layout.SymmetricSorter.INTERNODE_VERTICAL_PADDING * 2
            }];
        }

        return result;
    },

    insert: function(treeSet, parent, child, order) {
        var children = this._getSortedChildren(treeSet, parent);
        $assert(order <= children.length, "Order must be continues and can not have holes. Order:" + order);

        // Shift all the elements in one .
        for (var i = order; i < children.length; i++) {
            var node = children[i];
            node.setOrder(i + 1);
        }
        child.setOrder(order);
    },

    detach:function(treeSet, node) {
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

    computeOffsets:function(treeSet, node) {
        $assert(treeSet, "treeSet can no be null.");
        $assert(node, "node can no be null.");

        var children = this._getSortedChildren(treeSet, node);

        // Compute heights ...
        var heights = children.map(
            function(child) {
                return {id:child.getId(), order:child.getOrder(), position: child.getPosition(), width: child.getSize().width, height: this._computeChildrenHeight(treeSet, child)};
            }, this).reverse();

        // Compute the center of the branch ...
        var totalHeight = 0;
        heights.forEach(function(elem) {
            totalHeight += elem.height;
        });
        var ysum = totalHeight / 2;

        // Calculate the offsets ...
        var result = {};
        for (var i = 0; i < heights.length; i++) {
            ysum = ysum - heights[i].height;
            var childNode = treeSet.find(heights[i].id);
            var parent = treeSet.getParent(childNode);

            var rootNode = treeSet.getRootNode(childNode);
            var direction = parent.getPosition().x > rootNode.getPosition().x ? 1 : -1;

            var yOffset = ysum + heights[i].height / 2;
            var xOffset = direction * (heights[i].width / 2 + node.getSize().width / 2 + mindplot.layout.SymmetricSorter.INTERNODE_HORIZONTAL_PADDING);

            $assert(!isNaN(xOffset), "xOffset can not be null");
            $assert(!isNaN(yOffset), "yOffset can not be null");

            result[heights[i].id] = {x:xOffset,y:yOffset};
        }
        return result;
    },

    verify:function(treeSet, node) {
        // Check that all is consistent ...
        var children = this._getSortedChildren(treeSet, node);

        for (var i = 0; i < children.length; i++) {
            $assert(children[i].getOrder() == i, "missing order elements");
        }
    },

    getDirection: function(treeSet, node) {
        var parent = treeSet.getParent(node);
        var rootNode = treeSet.getRootNode(node);
        return parent.getPosition().x >= rootNode.getPosition().x ? 1 : -1;
    },

    toString:function() {
        return "Symmetric Sorter";
    }
});

mindplot.layout.SymmetricSorter.INTERNODE_VERTICAL_PADDING = 5;
mindplot.layout.SymmetricSorter.INTERNODE_HORIZONTAL_PADDING = 30;


