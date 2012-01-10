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
mindplot.nlayout.GridSorter = new Class({
    Extends: mindplot.nlayout.SymetricSorter,

    computeOffsets: function(treeSet, node) {
        $assert(treeSet, "treeSet can no be null.");
        $assert(node, "node can no be null.");
        $assert("order can no be null.");

        var children = this._getSortedChildren(treeSet, node);

        // Compute heights ...
        var heights = children.map(function(child) {
            return {id:child.getId(),height:this._computeChildrenHeight(treeSet, child)};
        }.bind(this));

        // Calculate the offsets ...
        var result = {};
        for (var i = 0; i < heights.length; i++) {
            var even = i%2 == 0 ? 1 : -1;

            var zeroHeight = i == 0 ? 0 : heights[0].height/2 * even;
            var middleHeight = 0;
            for (var j=i-2; j>0; j=j-2) {
                middleHeight += heights[j].height * even;
            }
            var finalHeight = i == 0 ? 0 : heights[i].height/2 * even;

            var yOffset = zeroHeight + middleHeight +finalHeight;
            var xOffset = node.getSize().width + mindplot.nlayout.GridSorter.GRID_HORIZONTAR_SIZE;

            $assert(!isNaN(xOffset), "xOffset can not be null");
            $assert(!isNaN(yOffset), "yOffset can not be null");

            result[heights[i].id] = {x:xOffset,y:yOffset};

        }
        return result;
    },

    toString:function() {
        return "Grid Sorter";
    }

});

mindplot.nlayout.GridSorter.GRID_HORIZONTAR_SIZE = 20;
mindplot.nlayout.GridSorter.INTER_NODE_VERTICAL_DISTANCE = 50;

