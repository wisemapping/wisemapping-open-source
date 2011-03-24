mindplot.layoutManagers.boards.freeMindBoards={};

mindplot.layoutManagers.boards.freeMindBoards.Board = mindplot.layoutManagers.boards.Board.extend({
    options:{

    },
    initialize:function(node, layoutManager, options){
        this.parent(node, layoutManager, options);
        this._positionTables = this._createTables();
    },
    _createTables:function(){
        core.Utils.assert(false, "no Board implementation found!")
    },
    _getTableForNode:function(node){
        core.Utils.assert(false, "no Board implementation found!")
    },
    removeTopicFromBoard:function(node){
        var table = this._getTableForNode(node);
        var position = node.getPosition();
        var y = position.y;

        //search for position
        for(var i = 0; i< table.length ; i++){
            var entry = table[i];
            if (entry.position == y){
                this._removeEntry(node, table, i);
                break;
            }
        }
    },
    addBranch:function(node){
        var result = this.findNodeEntryIndex(node);
        this._insertNewEntry(node, result.table, result.index);
    },
    _insertNewEntry:function(node, table, index){
        var entry = new mindplot.layoutManagers.boards.freeMindBoards.Entry(node);
        table.splice(index, 0, entry);
        this._updateTable(index, table);
    },
    _removeEntry:function(node, table, index){
        table.splice(index, 1);
        this._updateTable(index, table);
    },
    _updateTable:function(index, table){
        var i = index;
        if(index >= table.length){
            i = table.length -1;
        }

        var modifiedTopics = [];
        var delta = null;
        //check from index to 0;
        if(i>0){
            var entry = table[i];
            var prevEntry = table[i-1];
            var marginTop = entry.getPosition() + entry.getMarginTop();
            var marginBottom = prevEntry.getPosition() - prevEntry.getMarginBottom();
            if(marginTop>marginBottom){
                delta = marginBottom - marginTop;
                i--;
                while(i >= 0){
                    this._moveTopic(table[i], delta, modifiedTopics);
                    i--;
                }
            }
        }

        i = index;
        delta = null;

        //check from index to length
        if( i<table.length-1){
            entry = table[i];
            var nextEntry = table[i+1];
            marginBottom = entry.getPosition() - entry.getMarginBottom();
            marginTop = nextEntry.getPosition() + nextEntry.getMarginTop();
            if(marginTop>marginBottom){
                delta = marginTop-marginBottom;
                i++;
                while(i<table.length){
                    this._moveTopic(table[i], delta, modifiedTopics);
                    i++;
                }
            }
        }
    },
    _moveTopic:function(entry, delta, modifiedTopics){
        var pos = entry.getPosition();
        pos -= delta;
        entry.setPosition(pos);
        modifiedTopics.push(entry);
    },
    updateChildrenPosition:function(node){
        var result = this.findNodeEntryIndex(node);
        this._updateTable(result.index, result.table);
    },
    findNodeEntryIndex:function(node){
        var table = this._getTableForNode(node);
        var position = node.getPosition();
        var y = position.y;

        //search for position
        var i;
        for(i = 0; i< table.length ; i++){
            var entry = table[i];
            if (entry.getPosition() < y){
                break;
            }
        }
        return {index:i, table:table};
    },
    setNodeMarginTop:function(node, delta){
        var result = this.findNodeEntryIndex(node);
        var entry = result.table[result.index];
        var marginTop = entry.getMarginTop()-delta.y;
        entry.setMarginTop(marginTop);
    }
});