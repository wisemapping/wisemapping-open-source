mindplot.layout.boards.freemind.MainTopicBoard = mindplot.layout.boards.freemind.Board.extend({
    options:{

    },
    initialize:function(node, layoutManager, options){
        this.parent(node, layoutManager, options);        
    },
    _createTables:function(){
        return [[]];
    },
    _getTableForNode:function(node){
        return this._positionTables[0];
    }
});