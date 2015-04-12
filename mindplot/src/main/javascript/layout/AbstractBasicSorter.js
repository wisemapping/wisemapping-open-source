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

/**
 * @class
 * @extends mindplot.layout.ChildrenSorterStrategy
 */
mindplot.layout.AbstractBasicSorter = new Class(/** @lends AbstractBasicSorter */{
    Extends: mindplot.layout.ChildrenSorterStrategy,

    /**
     * @param {} treeSet
     * @param {} node
     * @return the height of a node and its children if existing and not shrunken
     */
    computeChildrenIdByHeights: function(treeSet, node) {
        var result = {};
        this._computeChildrenHeight(treeSet, node, result);
        return result;
    },

    _getVerticalPadding: function() {
        return mindplot.layout.AbstractBasicSorter.INTERNODE_VERTICAL_PADDING;
    },

    _computeChildrenHeight : function(treeSet, node, heightCache) {
        var height = node.getSize().height + (this._getVerticalPadding() * 2); // 2* Top and down padding;

        var result;
        var children = treeSet.getChildren(node);
        if (children.length == 0 || node.areChildrenShrunken()) {
            result = height;
        } else {
            var childrenHeight = 0;
            _.each(children, function(child) {
                childrenHeight += this._computeChildrenHeight(treeSet, child, heightCache);
            }, this);

            result = Math.max(height, childrenHeight);
        }

        if (heightCache) {
            heightCache[node.getId()] = result;
        }

        return result;
    },

    _getSortedChildren:function(treeSet, node) {
        var result = treeSet.getChildren(node);
        result.sort(function(a, b) {
            return a.getOrder() - b.getOrder()
        });
        return result;
    },

    _getRelativeDirection: function(reference, position) {
        var offset = position.x - reference.x;
        return offset >= 0 ? 1 : -1;
    }

});

/**
 * @constant
 * @type {Number}
 * @default
 */
mindplot.layout.AbstractBasicSorter.INTERNODE_VERTICAL_PADDING = 5;
/**
 * @constant
 * @type {Number}
 * @default
 */
mindplot.layout.AbstractBasicSorter.INTERNODE_HORIZONTAL_PADDING = 30;