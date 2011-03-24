mindplot.layoutManagers.boards.freeMindBoards.MainTopicBoard = mindplot.layoutManagers.boards.freeMindBoards.Board.extend({
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