mindplot.layoutManagers.boards.freeMindBoards.CentralTopicBoard = mindplot.layoutManagers.boards.freeMindBoards.Board.extend({
    options:{

    },
    initialize:function(node, layoutManager, options){
        this.parent(node, layoutManager, options);
    },
    _createTables:function(){
        return [[],[]];
    },
    _getTableForNode:function(node, altPosition){
        var i = 0;
        var position = node.getPosition();
        if(typeof altPosition != "undefined" && altPosition!=null)
        {
            position = altPosition;
        }
        if(!$defined(position)){
            if(Math.sign(node.getParent().getPosition().x) == -1){
                i=1;
            }
        }
        else if(Math.sign(position.x)==-1)
            i=1;
        return this._positionTables[i];
    }
});