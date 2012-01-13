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
        if (children.length == 0 || node.areChildrenShrunken()) {
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
        // Filter nodes on one side..
        var children = this._getChildrenForSide(parent, graph, position);

        // Try to fit within ...
        var result = null;
        var last = children.getLast();
        children.each(function(child, index) {
            var cpos = child.getPosition();
            if (position.y > cpos.y) {
                yOffset = child == last ?
                    child.getSize().height + mindplot.nlayout.BalancedSorter.INTERNODE_VERTICAL_PADDING * 2 :
                    (children[index + 1].getPosition().y - child.getPosition().y)/2;
                result = [child.getOrder() + 2,{x:cpos.x, y:cpos.y + yOffset}];
            }
        });

        // Position wasn't below any node, so it must be inserted above
        if (!result) {
            var first = children[0];
            result = [position.x > 0 ? 0 : 1, {
                x:first.getPosition().x,
                y:first.getPosition().y - first.getSize().height - mindplot.nlayout.BalancedSorter.INTERNODE_VERTICAL_PADDING * 2
            }];
        }

        return result;
    },

    insert: function(treeSet, parent, child, order) {
        var children = this._getChildrenForOrder(parent, treeSet, order);

        // Shift all the elements by two, so side is the same.
        // In case of balanced sorter, order don't need to be continuous...
        var max = 0;
        for (var i = 0; i < children.length; i++) {
            var node = children[i];
            max = Math.max(max, node.getOrder());
            if (node.getOrder() >= order) {
                max = Math.max(max, node.getOrder() + 2);
                node.setOrder(node.getOrder() + 2);
            }
        }

        var newOrder = order > (max+1) ? (max + 2) : order;
        child.setOrder(newOrder);
    },

    detach:function(treeSet, node) {
        var parent = treeSet.getParent(node);
        // Filter nodes on one side..
        var children = this._getChildrenForOrder(parent, treeSet, node.getOrder());

        children.each(function(child, index) {
            if (child.getOrder() > node.getOrder()) {
                child.setOrder(child.getOrder() - 2);
            }
        });
        node.setOrder(node.getOrder() % 2 == 0 ? 0 : 1);
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
            var xOffset = direction * (node.getSize().width + mindplot.nlayout.BalancedSorter.INTERNODE_HORIZONTAL_PADDING);

            $assert(!isNaN(xOffset), "xOffset can not be null");
            $assert(!isNaN(yOffset), "yOffset can not be null");

            result[heights[i].id] = {x:xOffset,y:yOffset};
        }
        return result;
    },

    _getChildrenForSide: function(parent, graph, position) {
        return graph.getChildren(parent).filter(function(child) {
            return position.x > 0 ? child.getPosition().x > 0 : child.getPosition().x < 0;
        });
    },

    _getChildrenForOrder: function(parent, graph, order) {
        return this._getSortedChildren(graph, parent).filter(function(node) {
            return node.getOrder() % 2 == order % 2;
        });
    },

    verify:function(treeSet, node) {
        // Check that all is consistent ...
        var children = this._getChildrenForOrder(node, treeSet, node.getOrder());

        // All odd ordered nodes should be "continuous" by themselves
        // All even numbered nodes should be "continuous" by themselves
        var factor = node.getOrder() % 2 == 0 ? 2 : 1;
        for (var i = 0; i < children.length; i++) {
            $assert(children[i].getOrder() == (i*factor), "missing order elements");
        }
    },

    toString:function() {
        return "Balanced Sorter";
    }
});

mindplot.nlayout.BalancedSorter.INTERNODE_VERTICAL_PADDING = 5;
mindplot.nlayout.BalancedSorter.INTERNODE_HORIZONTAL_PADDING = 30;
