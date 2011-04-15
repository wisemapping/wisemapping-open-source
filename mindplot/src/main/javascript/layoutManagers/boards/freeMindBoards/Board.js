mindplot.layoutManagers.boards.freeMindBoards={};

mindplot.layoutManagers.boards.freeMindBoards.Board = mindplot.layoutManagers.boards.Board.extend({
    options:{

    },
    initialize:function(node, layoutManager, options){
        this.parent(node, layoutManager, options);
        this._positionTables = this._createTables();
    },
    _createTables:function(){
        core.assert(false, "no Board implementation found!")
    },
    _getTableForNode:function(node, position){
        core.assert(false, "no Board implementation found!")
    },
    removeTopicFromBoard:function(node, modifiedTopics){
        var pos;
        if(node._originalPosition)
            pos = node._originalPosition;
        var result = this.findNodeEntryIndex(node, pos);
        core.assert(result.index<result.table.length,"node not found. Could not remove");
        this._removeEntry(node, result.table, result.index, modifiedTopics);
    },
    addBranch:function(node,modifiedTopics){
        var pos = (this._layoutManager._isMovingNode?node.getPosition():node.getModel().getFinalPosition() || node.getPosition());
        var entry = new mindplot.layoutManagers.boards.freeMindBoards.Entry(node, !this._layoutManager._isMovingNode);
        var result = this.findNewNodeEntryIndex(entry);

        // if creating a sibling or child
        if(!this._layoutManager._isMovingNode && this._layoutManager.getDesigner().getSelectedNodes().length>0){
            var selectedNode = this._layoutManager.getDesigner().getSelectedNodes()[0];
            if(!pos){
                if(selectedNode.getParent()!= null && node.getParent().getId() == selectedNode.getParent().getId()){
                    //creating a sibling - Lets put the new node below the selected node.
                    var parentBoard = this._layoutManager.getTopicBoardForTopic(selectedNode.getParent());
                    var selectedNodeResult = parentBoard.findNodeEntryIndex(selectedNode);
                    var selectedNodeEntry = selectedNodeResult.table[selectedNodeResult.index];
                    var x = null;
                    if(this._layoutManager._isCentralTopic(selectedNode.getParent())){
                        var nodeX = entry.getNode().getPosition().x;
                        if(Math.sign(nodeX)!=Math.sign(selectedNode.getPosition().x)){
                            x =nodeX *-1;
                        }
                        result.table = selectedNodeResult.table;
                    }
                    entry.setPosition(x, selectedNodeEntry.getPosition()+selectedNodeEntry.getTotalMarginBottom() + entry.getMarginTop());
                    result.index = selectedNodeResult.index+1;
                } else if(node.getParent().getId() == selectedNode.getId()){
                    //creating a child node - Lest put the new node as the last child.
                    var selectedNodeBoard = this._layoutManager.getTopicBoardForTopic(selectedNode);
                    var table = selectedNodeBoard._getTableForNode(node);
                    if(table.length>0){
                        //if no children use the position set by Entry initializer. Otherwise place as last child
                        var lastChild = table[table.length-1];
                        entry.setPosition(null, lastChild.getPosition()+lastChild.getTotalMarginBottom() + entry.getMarginTop());
                    }
                    result.index = table.length;
                }
            }
        }
        this._addEntry(entry, result.table, result.index);
        if(pos){
            if(result.index>0){
                var prevEntry =result.table[result.index-1];
                entry.setMarginTop(pos.y-(prevEntry.getPosition() + prevEntry.getTotalMarginBottom()));
            }
            else if(result.table.length>1){
                var nextEntry = result.table[1];
                nextEntry.setMarginTop((nextEntry.getPosition() - nextEntry.getTotalMarginTop())-pos.y);
            }
            var parent = node.getParent();
            if(!this._layoutManager._isCentralTopic(parent) && parent.getParent()!=null && (result.index == 0 || result.index==result.table.length-1)){
                var board = this._layoutManager.getTopicBoardForTopic(parent.getParent());
                var res2 = board.findNodeEntryIndex(parent);
                var parentEntry = res2.table[res2.index];
                var totalMarginTop = parentEntry.getTotalMarginTop();
                var totalMarginBottom = parentEntry.getTotalMarginBottom();
                var parentPosition = parentEntry.getPosition();
                if(result.index==0 && pos.y < parentPosition){
                    var childrenMarginTop = parentEntry.getPosition()-(pos.y-entry.getTotalMarginTop());
                    parentEntry.setMarginTop(totalMarginTop-childrenMarginTop);
                }else if(result.index==result.table.length-1 && pos.y>parentPosition){
                    var childrenMarginBottom = (pos.y+entry.getTotalMarginBottom())-parentEntry.getPosition();
                    parentEntry.setMarginBottom(totalMarginBottom - childrenMarginBottom);
                }
            }
        }
        this._updateTable(result.index, result.table,modifiedTopics, false);
        this._layoutManager._updateParentBoard(node, modifiedTopics);
    },
    _removeEntry:function(node, table, index, modifiedTopics){
        table.splice(index, 1);
        this._updateTable(index>0?index-1:index, table, modifiedTopics, false);
    },
    _addEntry:function(entry, table, index){
        table.splice(index, 0, entry);
    },
    _updateTable:function(index, table, modifiedTopics, updateParents){
        var i = index;
        if(index >= table.length){
            i = table.length -1;
        }
        var delta = null;
        //check from index to 0;
        if(i>0){
            var entry = table[i];
            var prevEntry = table[i-1];

            var margin = entry.getTotalMarginTop() + prevEntry.getTotalMarginBottom();
            var distance = Math.abs(prevEntry.getPosition() - entry.getPosition());
            if(distance!=margin){
                delta = (distance - margin)*Math.sign(prevEntry.getPosition() - entry.getPosition());
                i--;
                while(i >= 0){
                    this._updateEntryPos(table[i], new core.Point(null, delta), modifiedTopics, updateParents);
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
            var margin = entry.getTotalMarginBottom() + nextEntry.getTotalMarginTop();
            var distance = Math.abs(entry.getPosition() - nextEntry.getPosition());
            if(distance!=margin){
                delta = (distance - margin)*Math.sign(nextEntry.getPosition() - entry.getPosition());
                i++;
                while(i<table.length){
                    this._updateEntryPos(table[i], new core.Point(null, delta), modifiedTopics, updateParents);
                    i++;
                }
            }
        }

    },
    updateChildrenPosition:function(node, modifiedTopics){
        var result = this.findNodeEntryIndex(node);
        this._updateTable(result.index, result.table, modifiedTopics, false);
    },
    findNodeEntryIndex:function(node, position){
        var table = this._getTableForNode(node, position);

        //search for position
        var i;
        for(i = 0; i< table.length ; i++){
            var entry = table[i];
            if (entry.getNode().getId() == node.getId()){
                break;
            }
        }
        return {index:i, table:table};
    },
    findNewNodeEntryIndex:function(entry){
        var table = this._getTableForNode(entry.getNode());
        var position = entry.getPosition();
        //search for position
        var i;
        for(i = 0; i< table.length ; i++){
            var tableEntry = table[i];
            if (tableEntry.getPosition() > position){
                break;
            }
        }
        return {index:i, table:table};
    },
    setNodeMarginTop:function(entry, delta){
        var marginTop = entry.getMarginTop()-delta.y;
        entry.setMarginTop(marginTop);
    },
    setNodeMarginBottom:function(entry, delta){
        var marginBottom = entry.getMarginBottom()-delta.y;
        entry.setMarginBottom(marginBottom);
    },
    setNodeChildrenMarginTop:function(entry, delta){
        entry.setChildrenMarginTop(delta);
    },
    setNodeChildrenMarginBottom:function(entry, delta){
        entry.setChildrenMarginBottom(delta);
    },
    updateEntry:function(node, delta, modifiedTopics){
        var result = this.findNodeEntryIndex(node);
        if(result.index < result.table.length){
            var entry = result.table[result.index];
            if(result.index!=0)
                this.setNodeMarginTop(entry, delta);
            this._updateEntryPos(entry, delta, modifiedTopics, false);
            this._updateTable(result.index, result.table, modifiedTopics, false);
            this._layoutManager._updateParentBoard(entry.getNode(), modifiedTopics);
        }
    },
    _updateEntryPos:function(entry, delta, modifiedTopics, updateParents){
        var pos = entry.getNode().getPosition().clone();
        var newPos = new core.Point(pos.x-(delta.x==null?0:delta.x), pos.y-delta.y);
        entry.setPosition(newPos.x, newPos.y);
        this._layoutManager._updateChildrenBoards(entry.getNode(), delta, modifiedTopics);
        if(modifiedTopics.set){
            var key = entry.getId();
            if(modifiedTopics.hasKey(key)){
                pos = modifiedTopics.get(key).originalPos;
            }
            modifiedTopics.set(key,{originalPos:pos, newPos:newPos});
        }
    }
});