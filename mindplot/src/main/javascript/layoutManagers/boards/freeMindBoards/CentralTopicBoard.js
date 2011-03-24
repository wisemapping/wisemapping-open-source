mindplot.layoutManagers.boards.freeMindBoards.CentralTopicBoard = mindplot.layoutManagers.boards.freeMindBoards.Board.extend({
    options:{

    },
    initialize:function(node, layoutManager, options){
        this.parent(node, layoutManager, options);
    },
    _createTables:function(){
        return [[],[]];
    },
    _getTableForNode:function(node){
        var i = 0;
        var position = node.getPosition();
        if(!position){
            if(Math.sign(node.getParent().getPosition().x) == -1){
                i=1;
            }
        }
        else if(mindplot.util.Shape.isAtRight(position, node.getParent().getPosition()))
            i=1;
        return this._positionTables[i];
    }
});