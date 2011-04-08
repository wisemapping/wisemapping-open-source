mindplot.layoutManagers.FreeMindLayoutManager = mindplot.layoutManagers.BaseLayoutManager.extend({
    options:{

    },
    initialize:function(designer, options){
        this.parent(designer, options);
    },
    _nodeConnectEvent:function(targetNode, node){
        if(!this._isCentralTopic(node)){
            this.parent(targetNode, node);
        }
    },
    registerListenersOnNode : function(topic)
    {
        var id = topic.getId();
        // Register node listeners ...
        var designer = this.getDesigner();
        topic.addEventListener('onfocus', function(event)
        {
            designer.onObjectFocusEvent.attempt([topic, event], designer);
        });

        // Add drag behaviour ...
        if (topic.getType() != mindplot.NodeModel.CENTRAL_TOPIC_TYPE)
        {
            topic.addEventListener("mousedown",this._mousedownListener.bindWithEvent(this,[topic]));
        }

         // Register editor events ...
        if (!this.getDesigner()._viewMode)
        {
            this.getDesigner()._editor.listenEventOnNode(topic, 'dblclick', true);
        }

    },
    _mousedownListener:function(event,topic){

        var workSpace = this._designer.getWorkSpace();
        if (workSpace.isWorkspaceEventsEnabled())
        {
            // Disable double drag...
            workSpace.enableWorkspaceEvents(false);
            
            var id = topic.getId();
            this._command = new mindplot.commands.freeMind.DragTopicCommand();
            this._modifiedTopics = new Hash();

            var topics = this.getDesigner()._getTopics();
            // Disable all mouse events.
            for (var i = 0; i < topics.length; i++)
            {
                topics[i].setMouseEventsEnabled(false);
            }

            var ev = new Event(event);

            var screen = workSpace.getScreenManager();

            // Set initial position.
            //        var mousePos = screen.getWorkspaceMousePosition(event);

            this._isMovingNode=false;

            // Register mouse move listener ...
            this._mouseMoveListenerInstance = this._mouseMoveListener.bindWithEvent(this,[topic]);
            screen.addEventListener('mousemove', this._mouseMoveListenerInstance);

            // Register mouse up listeners ...
            this._mouseUpListenerInstance = this._mouseUpListener.bindWithEvent(this,[topic]);
            screen.addEventListener('mouseup', this._mouseUpListenerInstance);

            // Change cursor.
            window.document.body.style.cursor = 'move';
        }
    },
    _mouseMoveListener:function(event, node){
        if(!this._isMovingNode){
            this._isMovingNode=true;
            var screen = this._designer.getWorkSpace().getScreenManager();
            var nodePos = node.getPosition().clone();
            var pos = screen.getWorkspaceMousePosition(event);
            pos.x = Math.round(pos.x);
            pos.y = Math.round(pos.y);
            //If still in same side
            if(Math.sign(nodePos.x)==Math.sign(pos.x) || (Math.sign(nodePos.x)!=Math.sign(pos.x) && !this._isCentralTopic(node.getParent()))){
                var x = nodePos.x - pos.x;
                var y = nodePos.y - pos.y;
                var delta = new core.Point(Math.round(x), Math.round(y));
                var board = this.getTopicBoardForTopic(node.getParent());
                board.updateEntry(node, delta, this._modifiedTopics);
            } else {
                var parentBoard = this.getTopicBoardForTopic(node.getParent());
                var entryObj = parentBoard.findNodeEntryIndex(node);
                var entry = entryObj.table[entryObj.index];
                //.removeTopicFromBoard(node,this._modifiedTopics);
                parentBoard._removeEntry(node, entryObj.table, entryObj.index, this._modifiedTopics);
                this._changeChildrenSide(node, pos, this._modifiedTopics);
                node.setPosition(pos.clone(), false);
                if(this._modifiedTopics.set){
                    var key = node.getId();
                    if(this._modifiedTopics.hasKey(key)){
                        nodePos = this._modifiedTopics.get(key).originalPos;
                    }
                    this._modifiedTopics.set(key,{originalPos:nodePos, newPos:pos});
                }
                entryObj = parentBoard.findNewNodeEntryIndex(entry);
                parentBoard._addEntry(entry, entryObj.table, entryObj.index);
                parentBoard._updateTable(entryObj.index,  entryObj.table, this._modifiedTopics, true);
                //this.getTopicBoardForTopic(node.getParent()).addBranch(node,this._modifiedTopics);

            }
            this._isMovingNode=false;
        }
        event.preventDefault();
    },
    _changeChildrenSide:function(node, newPos, modifiedTopics){
        var children = node._getChildren();
        if(children.length>0){
            var refPos = node.getPosition();
            for( var i = 0 ; i< children.length ; i++){
                var child = children[i];
                this._changeChildrenSide(child);
                var childPos = child.getPosition();
                var oldPos=childPos.clone();
                childPos.x = newPos.x +(childPos.x - refPos.x)*-1;
                childPos.y = newPos.y +(childPos.y - refPos.y);
                child.setPosition(childPos, false);
                if(modifiedTopics.set){
                    var key = node.getId();
                    if(modifiedTopics.hasKey(key)){
                        childPos = this._modifiedTopics.get(key).originalPos;
                    }
                    this._modifiedTopics.set(key,{originalPos:oldPos, newPos:childPos});
                }
            }
        }
    },
    _mouseUpListener:function(event, node){
        var id = node.getId();

        var screen = this._designer.getWorkSpace().getScreenManager();
        // Remove all the events.
        screen.removeEventListener('mousemove', this._mouseMoveListenerInstance);
        screen.removeEventListener('mouseup', this._mouseUpListenerInstance);
        delete this._mouseMoveListenerInstance;
        delete this._mouseUpListenerInstance;

        var topics = this.getDesigner()._getTopics();
        // Disable all mouse events.
        for (var i = 0; i < topics.length; i++)
        {
            topics[i].setMouseEventsEnabled(true);
        }

        // Change the cursor to the default.
        window.document.body.style.cursor = 'default';

        this._designer.getWorkSpace().enableWorkspaceEvents(true);

        this._command.setModifiedTopics(this._modifiedTopics);
        var actionRunner = mindplot.DesignerActionRunner.getInstance();
        actionRunner.execute(this._command);
        this._command=null;
        this._modifiedTopics=null;

//        var topicId = draggedTopic.getId();
//        var command = new mindplot.commands.DragTopicCommand(topicId);
        
    },
    getClassName:function(){
        return mindplot.layoutManagers.FreeMindLayoutManager.NAME;
    },
    _createMainTopicBoard:function(node){
        return new mindplot.layoutManagers.boards.freeMindBoards.MainTopicBoard(node, this);
    },
    _createCentralTopicBoard:function(node){
        return new mindplot.layoutManagers.boards.freeMindBoards.CentralTopicBoard(node, this);
    },
    _updateParentBoard:function(node, modifiedTopics){
        var parent = node.getParent();
        if(!this._isCentralTopic(parent)){
            var parentBoard = this.getTopicBoardForTopic(parent.getParent());
            var result = parentBoard.findNodeEntryIndex(parent);
            var parentEntry = result.table[result.index];
            var board = this.getTopicBoardForTopic(parent);
            var table = board._getTableForNode(null);
            if(table.length>0){
                var firstChild = table[0];
                var marginTop = parentEntry.getPosition()-(firstChild.getPosition()-firstChild.getTotalMarginTop());
                parentBoard.setNodeChildrenMarginTop(parentEntry,marginTop);
                var lastChild = table[table.length-1];
                var marginBottom = (lastChild.getPosition()+lastChild.getTotalMarginBottom())-parentEntry.getPosition();
                parentBoard.setNodeChildrenMarginBottom(parentEntry,marginBottom);
                parentBoard._updateTable(result.index, result.table, modifiedTopics, false);
            }
            this._updateParentBoard(parent, modifiedTopics);
        }
    },
    _updateChildrenBoards:function(node, delta, modifiedTopics){
        var board = this.getTopicBoardForTopic(node);
        var topics = board._getTableForNode(null);
        for(var i=0; i<topics.length; i++){
            board._updateEntryPos(topics[i],delta, modifiedTopics, false);
        }
    }
});

mindplot.layoutManagers.FreeMindLayoutManager.NAME ="FreeMindLayoutManager"; 