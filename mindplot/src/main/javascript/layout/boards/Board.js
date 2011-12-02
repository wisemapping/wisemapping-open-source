mindplot.layout.boards.Board = new Class({
    Implements: [Events,Options],
    options: {

    },
    initialize: function(node, layoutManager, options) {
        this.setOptions(options);
        this._node = node;
        this._layoutManager = layoutManager;
    },
    getClassName:function() {
        return mindplot.layout.boards.Board.NAME;
    },
    removeTopicFromBoard:function(node, modifiedTopics) {
        $assert(false, "no Board implementation found!");
    },
    addBranch:function(node, modifiedTopics) {
        $assert(false, "no Board implementation found!");
    },
    updateChildrenPosition:function(node, modifiedTopics) {
        $assert(false, "no Board implementation found!");
    },
    setNodeMarginTop:function(node, delta) {
        $assert(false, "no Board implementation found!");
    },
    getNode:function() {
        return this._node;
    }
});

mindplot.layout.boards.Board.NAME = "Board";
