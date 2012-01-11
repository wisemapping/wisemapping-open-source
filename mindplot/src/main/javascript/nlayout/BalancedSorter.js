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
mindplot.nlayout.BalancedSorter = new Class({
    Extends: mindplot.nlayout.AbstractBasicSorter,

    initialize: function() {

    },

    computeChildrenIdByHeights: function(treeSet, node) {
        var result = {};
        this._computeChildrenHeight(treeSet, node, result);
        return result;
    },

    _computeChildrenHeight : function(treeSet, node, heightCache) {
        var height = node.getSize().height + (mindplot.nlayout.BalancedSorter.INTERNODE_VERTICAL_PADDING * 2); // 2* Top and down padding;

        var result;
        var children = treeSet.getChildren(node);
        if (children.length == 0) {
            result = height;
        } else {
            var childrenHeight = 0;
            children.forEach(function(child) {
                childrenHeight += this._computeChildrenHeight(treeSet, child, heightCache);
            }, this);

            result = Math.max(height, childrenHeight);
        }

        if (heightCache) {
            heightCache[node.getId()] = result;
        }

        return result;
    },

    predict : function(parent, graph, position) {

        // No children...
        var children = graph.getChildren(parent);
        if (children.length == 0) {
            return [0,parent.getPosition()];  // @Todo:Change x ...
        }

        // Try to fit within ...
        //
        // - Order is change if the position top position is changed ...
        // - Suggested position is the middle between the two topics...
        //
        var result = null;
        children.forEach(function(child) {
            var cpos = child.getPosition();
            if (position.y > cpos.y) {
                result = [child.getOrder(),{x:cpos.x,y:cpos.y + child.getSize().height}];
            }
        });

        // Ok, no overlap. Suggest a new order.
        if (result) {
            var last = children.getLast();
            result = [last.getOrder() + 1,{x:cpos.x,y:cpos.y - (mindplot.nlayout.BalancedSorter.INTERNODE_VERTICAL_PADDING * 4)}];
        }

        return result;
    },

    insert: function(treeSet, parent, child, order) {
        var children = this._getSortedChildren(treeSet, parent);

        // Shift all the elements in one. In case of balanced sorter, order don't need to be continues ...
        var collision = order;
        for (var i = order; i < children.length; i++) {
            var node = children[i];

            // @Todo: This must be review. Order balance need to be defined ...
            if (node.getOrder() == collision) {
                collision = collision + 1;
                node.setOrder(collision);
            }
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
                return {id:child.getId(), order:child.getOrder(), height:this._computeChildrenHeight(treeSet, child)};
            }, this).reverse();


        // Compute the center of the branch ...
        var totalPHeight = 0;
        var totalNHeight = 0;

        heights.forEach(function(elem) {
            if (elem.order % 2 == 0) {
                totalPHeight += elem.height;
            } else {
                totalNHeight += elem.height;
            }
        });
        var psum = totalPHeight / 2;
        var nsum = totalNHeight / 2;
        var ysum = 0;

        // Calculate the offsets ...
        var result = {};
        for (var i = 0; i < heights.length; i++) {
            var direction = heights[i].order % 2 ? -1 : 1;

            if (direction > 0) {
                psum = psum - heights[i].height;
                ysum = psum;
            } else {
                nsum = nsum - heights[i].height;
                ysum = nsum;
            }

            var yOffset = ysum + heights[i].height / 2;
            var xOffset = direction * (node.getSize().width + mindplot.nlayout.SymmetricSorter.INTERNODE_HORIZONTAL_PADDING);

            $assert(!isNaN(xOffset), "xOffset can not be null");
            $assert(!isNaN(yOffset), "yOffset can not be null");

            result[heights[i].id] = {x:xOffset,y:yOffset};
        }
        return result;
    },

    toString:function() {
        return "Balanced Sorter";
    },

    verify:function(treeSet, node) {
        // @todo...
    }
});

mindplot.nlayout.BalancedSorter.INTERNODE_VERTICAL_PADDING = 5;
mindplot.nlayout.BalancedSorter.INTERNODE_HORIZONTAL_PADDING = 5;
