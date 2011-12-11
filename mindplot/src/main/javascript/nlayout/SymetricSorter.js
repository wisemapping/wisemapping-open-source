mindplot.nlayout.SymetricSorder = new Class({
    Extends: mindplot.nlayout.ChildrenSorterStrategy,
    initialize:function() {

    },

    computeChildrenIdByHeights: function(treeSet, node) {
        var result = {};
        this._computeChildrenHeight(treeSet, node, result);
        return result;
    },


    _computeChildrenHeight : function(treeSet, node, heightCache) {
        var height = node.getSize().height + (mindplot.nlayout.SymetricSorder.INTERNODE_VERTICAL_PADDING * 2); // 2* Top and down padding;

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
            return [0,parent.getPosition()];
        }

        // Try to fit within ...
        //
        // - Order is change if the position top position is changed ...
        // - Suggested position is the middle bitween the two topics...
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
            result = [last.getOrder() + 1,{x:cpos.x,y:cpos.y - (mindplot.nlayout.SymetricSorder.INTERNODE_VERTICAL_PADDING * 4)}];
        }

        return result;
    },

    insert: function(treeSet, parent, child, order) {
        var children = treeSet.getChildren(parent);
        $assert(order <= children.length, "Order must be continues and can not have holes. Order:" + order);

        // Sort array list ..
        children.sort(function(a, b) {
            return a.getOrder() - b.getOrder()
        });

        // Shift all the elements in one .
        for (var i = order; i < children.length; i++) {
            var node = children[i];
            node.setOrder(node.getOrder() + 1);
        }
        child.setOrder(order);
    },

    verify:function(treeSet, node) {
        // Check that all is consistent ...
        var children = treeSet.getChildren(node);
        children.sort(function(a, b) {
            return a.getOrder() - b.getOrder()
        });

        for (var i = 0; i < children.length; i++) {
            $assert(children[i].getOrder() == i, "missing order elements");
        }
    },

    computeOffsets:function(treeSet, node) {
        $assert(treeSet, "treeSet can no be null.");
        $assert(node, "node can no be null.");
        $assert("order can no be null.");

        var children = treeSet.getChildren(node);
        children.sort(function(a, b) {
            return a.getOrder() - b.getOrder()
        });

        // Compute heights ...
        var heights = children.map(function(child) {
            return {id:child.getId(),height:this._computeChildrenHeight(treeSet, child)};
        }.bind(this));

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

            var yOffset = ysum + mindplot.nlayout.SymetricSorder.INTERNODE_VERTICAL_PADDING;
            var xOffset = mindplot.nlayout.SymetricSorder.INTERNODE_HORIZONTAL_PADDING;

            $assert(!isNaN(xOffset), "xOffset can not be null");
            $assert(!isNaN(yOffset), "yOffset can not be null");

            result[heights[i].id] = {x:xOffset,y:yOffset};

        }
        return result;
    }
});

mindplot.nlayout.SymetricSorder.INTERNODE_VERTICAL_PADDING = 5;
mindplot.nlayout.SymetricSorder.INTERNODE_HORIZONTAL_PADDING = 5;


