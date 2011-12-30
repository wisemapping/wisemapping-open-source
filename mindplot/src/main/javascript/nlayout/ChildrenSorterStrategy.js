mindplot.nlayout.ChildrenSorterStrategy = new Class({
    initialize:function() {

    },

    computeChildrenIdByHeights: function(treeSet, node) {
        throw "Method must be implemented";
    },

    computeOffsets:function(treeSet, node) {
        throw "Method must be implemented";
    },

    insert: function(treeSet, parent, child, order) {
        throw "Method must be implemented";
    },

    detach:function(treeSet, node) {
        throw "Method must be implemented";
    },

    predict:function(treeSet, parent, position) {
        throw "Method must be implemented";
    },

    verify:function(treeSet, node) {
        throw "Method must be implemented";
    },

    toString:function() {
        throw "Method must be implemented: print name";
    }

});

