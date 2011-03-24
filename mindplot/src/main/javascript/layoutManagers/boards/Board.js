mindplot.layoutManagers.boards={};

mindplot.layoutManagers.boards.Board = new Class({

    options: {

    },
    initialize: function(node, layoutManager, options) {
        this.setOptions(options);
        this._node = node;
        this._layoutManager = layoutManager;
    },
    getClassName:function(){
        return mindplot.layoutManagers.boards.Board.NAME;
    },
    removeTopicFromBoard:function(node){
        core.Utils.assert(false, "no Board implementation found!");
    },
    addBranch:function(node){
        core.Utils.assert(false, "no Board implementation found!");
    },
    updateChildrenPosition:function(node){
        core.Utils.assert(false, "no Board implementation found!");
    },
    setNodeMarginTop:function(node, delta){
        core.Utils.assert(false, "no Board implementation found!");
    }
});

mindplot.layoutManagers.boards.Board.NAME ="Board";

mindplot.layoutManagers.boards.Board.implement(new Events);
mindplot.layoutManagers.boards.Board.implement(new Options);