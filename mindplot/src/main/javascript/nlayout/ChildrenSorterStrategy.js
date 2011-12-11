mindplot.nlayout.ChildrenSorterStrategy = new Class({
    initialize:function() {

    },

    predict:function(treeSet, parent, position) {
        throw "Method must be implemented";
    },

    sorter: function(treeSet, parent, child, order) {
        throw "Method must be implemented";
    },

    computeChildrenIdByHeights: function(treeSet, node) {
        throw "Method must be implemented";
    },

    computeOffsets:function(treeSet, node) {
        throw "Method must be implemented";

    }
});

