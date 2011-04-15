mindplot.layoutManagers.FreeMindLayoutManager = mindplot.layoutManagers.BaseLayoutManager.extend({
    options:{

    },
    initialize:function(designer, options){
        this.parent(designer, options);
    },
    _nodeConnectEvent:function(targetNode, node){
        if(node.relationship){
            this._movingNode(targetNode, node);
        }
        else if(!this._isCentralTopic(node)){
            this.parent(targetNode, node);
        }
    },
    _nodeDisconnectEvent:function(targetNode, node){
        if(node.relationship){
        }
        else{
            this.parent(targetNode, node);
            this._updateBoard(targetNode,[]);
        }
    },
    _nodeShrinkEvent:function(node){
        this._updateBoard(node,[]);
    },
    prepareChildrenList:function(node, children){
        var result = children.sort(function(n1, n2){
            if(n1.getPosition() && n2.getPosition())
                return n1.getPosition().y>n2.getPosition().y;
            else
                return true;
        });
        return result;
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
            topic.addEventListener("mousedown",this._reconnectMouseDownListener.bindWithEvent(this,[topic]));
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
            this._mouseInitialPos = screen.getWorkspaceMousePosition(event);
            var pos = topic.getPosition();
            this._mouseInitialPos.x = 0;
            this._mouseInitialPos.y = pos.y - Math.round(this._mouseInitialPos.y);

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
            nodePos.x-=this._mouseInitialPos.x;
            nodePos.y-=this._mouseInitialPos.y;
            var pos = screen.getWorkspaceMousePosition(event);
            pos.x = Math.round(pos.x);
            pos.y = Math.round(pos.y);
            //if isolated topic
            if(node.getParent()==null){
                //If still in same side
                if(Math.sign(nodePos.x)==Math.sign(pos.x)){
                    var x = nodePos.x - pos.x;
                    var y = nodePos.y - pos.y;
                    var delta = new core.Point(Math.round(x), Math.round(y));
                    var actualPos = node.getPosition().clone();
                    var newPos = new core.Point(actualPos.x-(delta.x==null?0:delta.x), actualPos.y-delta.y);
                    node.setPosition(newPos, false);
                    this._addToModifiedList(this._modifiedTopics, node.getId(), actualPos, newPos);
                    this._updateChildrenBoards(node, delta, this._modifiedTopics);
                }else{
                    this._changeChildrenSide(node, pos, this._modifiedTopics);
                    node.setPosition(pos.clone(), false);
                    this._addToModifiedList(this._modifiedTopics, node.getId(), nodePos, pos);
                }
            }else{
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

                }
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
                var childPos = child.getPosition().clone();
                var oldPos=childPos.clone();
                childPos.x = newPos.x +(childPos.x - refPos.x)*-1;
                childPos.y = newPos.y +(childPos.y - refPos.y);
                this._changeChildrenSide(child, childPos, modifiedTopics);
                child.setPosition(childPos, false);
                if(modifiedTopics.set){
                    var key = node.getId();
                    if(modifiedTopics.hasKey(key)){
                        oldPos = this._modifiedTopics.get(key).originalPos;
                    }
                    this._modifiedTopics.set(key,{originalPos:oldPos, newPos:childPos});
                }
            }
        }
    },
    _mouseUpListener:function(event, node){

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
        this._mouseInitialPos=null;

    },
    getClassName:function(){
        return mindplot.layoutManagers.FreeMindLayoutManager.NAME;
    },
    _createMainTopicBoard:function(node){
        return new mindplot.layoutManagers.boards.freeMindBoards.MainTopicBoard(node, this);
    },
    _createCentralTopicBoard:function(node){
        return new mindplot.layoutManagers.boards.freeMindBoards.CentralTopicBoard(node, this);
    }
    ,
    _updateParentBoard:function(node, modifiedTopics){
        this._updateBoard(node.getParent(), modifiedTopics);
    },
    _updateBoard:function(node, modifiedTopics){
        var parent = node;
        if(!this._isCentralTopic(parent) && parent.getParent()!=null){
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
            } else {
                parentBoard.setNodeChildrenMarginTop(parentEntry, 0);
                parentBoard.setNodeChildrenMarginBottom(parentEntry, 0);
            }
            parentBoard._updateTable(result.index, result.table, modifiedTopics, false);
            this._updateParentBoard(parent, modifiedTopics);
        }
    },
    _updateChildrenBoards:function(node, delta, modifiedTopics){
        var board = this.getTopicBoardForTopic(node);
        var topics = board._getTableForNode(null);
        for(var i=0; i<topics.length; i++){
            board._updateEntryPos(topics[i],delta, modifiedTopics, false);
        }
    },
    addHelpers:function(node){
        if (node.getType() != mindplot.NodeModel.CENTRAL_TOPIC_TYPE)
            this._addMoveHelper(node);
    },
    _addMoveHelper:function(node){
        var moveShape = new mindplot.ActionIcon(node, mindplot.layoutManagers.FreeMindLayoutManager.MOVE_IMAGE_URL);
        moveShape.setCursor('move');
        var positionate = function(node){
            if(node.getId() == this.getNode().getId()){
                var size = this.getNode().getSize();
                this.setPosition(size.width/2,0);
            }
        }.bind(moveShape);
        positionate(node);
        moveShape.setVisibility(false);
        mindplot.EventBus.instance.addEvent(mindplot.EventBus.events.NodeResizeEvent,positionate);
        var show = function(node){
            if(node.getId() == this.getNode().getId()){
                this.setVisibility(true);
            }
        }.bind(moveShape);
        var hide = function(node){
            if(node.getId() == this.getNode().getId()){
                this.setVisibility(false);
            }
        }.bind(moveShape);
        mindplot.EventBus.instance.addEvent(mindplot.EventBus.events.NodeMouseOverEvent,show);
        mindplot.EventBus.instance.addEvent(mindplot.EventBus.events.NodeMouseOutEvent,hide);
        node.addHelper(moveShape);
        moveShape.addEventListener("mousedown",this._mousedownListener.bindWithEvent(this,[node]));

    },
    needsPrepositioning:function(){
        return false;
    },
    _reconnectMouseDownListener:function(event, topic){
        var workSpace = this._designer.getWorkSpace();
        if (workSpace.isWorkspaceEventsEnabled())
        {
            // Disable double drag...
            workSpace.enableWorkspaceEvents(false);

            var id = topic.getId();
            this._command = new mindplot.commands.freeMind.ReconnectTopicCommand();
            this._modifiedTopics = new Hash();
            this._mouseOverListeners = new Hash();
            this._mouseOutListeners = new Hash();

            if(topic.getParent()!=null){
                var board = this.getTopicBoardForTopic(topic.getParent());
                this._currentIndex = board.findNodeEntryIndex(topic).index;
            }

            var topics = this.getDesigner()._getTopics();
            // Disable all mouse events.
            for (var i = 0; i < topics.length; i++)
            {
                topics[i].setMouseEventsEnabled(false);
                if(topics[i].getId()!=topic.getId()){
                    var overListener = this._reconnectMouseOverListener.bindWithEvent(topics[i],[this]);
                    topics[i].addEventListener('mouseover',overListener);
                    this._mouseOverListeners.set(topics[i].getId(),overListener);
                    var outListener = this._reconnectMouseOutListener.bindWithEvent(topics[i],[this]);
                    topics[i].addEventListener('mouseout',outListener);
                    this._mouseOutListeners.set(topics[i].getId(),outListener);
                }
            }
            this._updateTopicsForReconnect(topic, mindplot.layoutManagers.FreeMindLayoutManager.RECONNECT_NODES_OPACITY);
            var line = topic.getOutgoingLine();
            if(line){
                line.setVisibility(false);
            }
            this._createIndicatorShapes();

            var ev = new Event(event);

            var screen = workSpace.getScreenManager();

            this._isMovingNode=false;

            // Register mouse move listener ...
            this._mouseMoveListenerInstance = this._reconnectMouseMoveListener.bindWithEvent(this,[topic]);
            screen.addEventListener('mousemove', this._mouseMoveListenerInstance);

            // Register mouse up listeners ...
            this._mouseUpListenerInstance = this._reconnectMouseUpListener.bindWithEvent(this,[topic]);
            screen.addEventListener('mouseup', this._mouseUpListenerInstance);

            // Change cursor.
            window.document.body.style.cursor = 'move';
        }
    },
    _reconnectMouseMoveListener:function(event, node){
        if(!this._isMovingNode){
            this._isMovingNode=true;
            var screen = this._designer.getWorkSpace().getScreenManager();
            var nodePos = node.getPosition().clone();
            var pos = screen.getWorkspaceMousePosition(event);
            pos.x = Math.round(pos.x);
            pos.y = Math.round(pos.y);
            //If still in same side
            if(Math.sign(nodePos.x)==Math.sign(pos.x)){
                var x = nodePos.x - pos.x;
                var y = nodePos.y - pos.y;
                var delta = new core.Point(Math.round(x), Math.round(y));
                var newPos = new core.Point(nodePos.x-(delta.x==null?0:delta.x), nodePos.y-delta.y);
                node.setPosition(newPos, false);
                this._updateChildrenBoards(node, delta, this._modifiedTopics);
            } else {
                this._changeChildrenSide(node, pos, this._modifiedTopics);
                node.setPosition(pos.clone(), false);
//                entryObj = parentBoard.findNewNodeEntryIndex(entry);
//                parentBoard._addEntry(entry, entryObj.table, entryObj.index);
//                parentBoard._updateTable(entryObj.index,  entryObj.table, this._modifiedTopics, true);

            }
            if(this._modifiedTopics.set){
                var key = node.getId();
                if(this._modifiedTopics.hasKey(key)){
                    nodePos = this._modifiedTopics.get(key).originalPos;
                }
                this._modifiedTopics.set(key,{originalPos:nodePos, newPos:pos});
            }
            this._isMovingNode=false;
        }
        event.preventDefault();
    },
    _reconnectMouseUpListener:function(event, node){
         var screen = this._designer.getWorkSpace().getScreenManager();
        // Remove all the events.
        screen.removeEventListener('mousemove', this._mouseMoveListenerInstance);
        screen.removeEventListener('mouseup', this._mouseUpListenerInstance);
        delete this._mouseMoveListenerInstance;
        delete this._mouseUpListenerInstance;

        var topics = this.getDesigner()._getTopics();
        // Disable all mouse events.
        for (var i = topics.length-1; i >=0; i--)
        {
            topics[i].setMouseEventsEnabled(true);
            if(topics[i].getId()!=node.getId()){
                var overListener = this._mouseOverListeners.get(topics[i].getId());
                topics[i].removeEventListener('mouseover',overListener);
                var outListener = this._mouseOutListeners.get(topics[i].getId());
                topics[i].removeEventListener('mouseout',outListener);
            }
        }

        this._restoreTopicsForReconnect(node);

        this._removeIndicatorShapes(node);

        //Check that it has to be relocated
        if(this._createShape !=null){
            if(this._createShape == "Child"){
                if(node.getParent()!=null && node.getParent().getId() == this._targetNode.getId()){
                    var mod = this._modifiedTopics.get(node.getId());
                    if(Math.sign(mod.originalPos.x) == Math.sign(node.getPosition().x))
                        this._createShape = null;
                }
            }else if(node.getParent()!=null && this._targetNode.getParent()!= null && node.getParent().getId() == this._targetNode.getParent().getId()){
                var chkboard = this.getTopicBoardForTopic(this._targetNode.getParent());
                var mod = this._modifiedTopics.get(node.getId());
                var chk = chkboard.findNodeEntryIndex(node, mod.originalPos);
                if(this._createShape == "Sibling_top"){
                    if(chk.table>this._currentIndex+1){
                        var nextEntry = chk.table[this._currentIndex+1];
                        if(nextEntry.getNode().getId() == this._targetNode.getId()){
                            this._createShape = null;
                        }
                    }
                } else if(this._currentIndex>0){
                    var prevEntry = chk.table[this._currentIndex-1];
                    if(prevEntry.getNode().getId() == this._targetNode.getId()){
                        this._createShape = null;
                    }
                }
            }
        }

        if(this._createShape == null){
            //cancel everything.
            var line = node.getOutgoingLine();
            if(line){
                line.setVisibility(true);
            }
            core.Utils.animatePosition(this._modifiedTopics, null, this.getDesigner());
        }else{
            this._command.setModifiedTopics(this._modifiedTopics);
            this._command.setDraggedTopic(node, this._currentIndex);
            this._command.setTargetNode(this._targetNode);
            this._command.setAs(this._createShape);
            //todo:Create command
            var actionRunner = mindplot.DesignerActionRunner.getInstance();
            actionRunner.execute(this._command);
        }

        // Change the cursor to the default.
        window.document.body.style.cursor = 'default';

        this._designer.getWorkSpace().enableWorkspaceEvents(true);

        this._command=null;
        this._modifiedTopics=null;
        this._mouseInitialPos=null;
        this._mouseOverListeners=null;
        this._mouseOutListeners=null;
        this._targetNode = null;
        this._createShape = null;
    },
    //function binded to the node with the over event
    _reconnectMouseOverListener:function(event, layoutManager){
        var size = this.getSize();
        var screen = layoutManager.getDesigner().getWorkSpace().getScreenManager();
        var pos = screen.getWorkspaceMousePosition(event);
        pos.x = Math.round(pos.x);
        pos.y = Math.round(pos.y);
        var nodePos = this.getPosition();
        //if it is on the child half side, or it is central topic add it as child
        if(layoutManager._isCentralTopic(this) || this.getParent()==null || ((Math.sign(nodePos.x)>0 && pos.x>nodePos.x) || (Math.sign(nodePos.x)<0 && pos.x<nodePos.x))){
            layoutManager._updateIndicatorShapes(this, mindplot.layoutManagers.FreeMindLayoutManager.RECONNECT_SHAPE_CHILD, pos);
        }else{
            //is a sibling. if mouse in top half sibling goes above this one
            if(pos.y<nodePos.y){
                layoutManager._updateIndicatorShapes(this, mindplot.layoutManagers.FreeMindLayoutManager.RECONNECT_SHAPE_SIBLING_TOP);
            }else{
                //if mouse in bottom half sibling goes below this one
                layoutManager._updateIndicatorShapes(this, mindplot.layoutManagers.FreeMindLayoutManager.RECONNECT_SHAPE_SIBLING_BOTTOM);
            }
        }
    },
    _createIndicatorShapes:function(){
        if(!core.Utils.isDefined(this._createChildShape) || !core.Utils.isDefined(this._createSiblingShape)){
            var rectAttributes = {fillColor:'#CC0033',opacity:0.4,width:30,height:30,strokeColor:'#FF9933'};
            var rect = new web2d.Rect(0, rectAttributes);
            rect.setVisibility(false);
            this._createChildShape = rect;

            rect = new web2d.Rect(0, rectAttributes);
            rect.setVisibility(false);
            this._createSiblingShape = rect;
        }
    },
    _updateIndicatorShapes:function(topic, shape, mousePos){
        if(this._createChildShape.getParent()!=null|| this._createSiblingShape.getParent()!=null){
            this._createChildShape.getParent().removeChild(this._createChildShape._peer);
            this._createSiblingShape.getParent().removeChild(this._createSiblingShape._peer);
        }
        topic.get2DElement().appendChild(this._createChildShape);
        topic.get2DElement().appendChild(this._createSiblingShape);
        var size = topic.getSize();
        var position = topic.getPosition();
        if(shape == mindplot.layoutManagers.FreeMindLayoutManager.RECONNECT_SHAPE_CHILD){
            this._createChildShape.setSize(size.width/2, size.height);
            var sign = mousePos?Math.sign(mousePos.x):Math.sign(position.x);
            this._createChildShape.setPosition(sign>0?size.width/2:0, 0);
            this._createChildShape.setVisibility(true);
            this._createSiblingShape.setVisibility(false);
            this._createShape = "Child";
            this._targetNode = topic;
        } else if(shape == mindplot.layoutManagers.FreeMindLayoutManager.RECONNECT_SHAPE_SIBLING_TOP){
            this._createSiblingShape.setSize(size.width,size.height/2);
            this._createSiblingShape.setPosition(0,0);
            this._createSiblingShape.setVisibility(true);
            this._createChildShape.setVisibility(false);
            this._createShape = "Sibling_top";
            this._targetNode = topic;
        }else if(shape == mindplot.layoutManagers.FreeMindLayoutManager.RECONNECT_SHAPE_SIBLING_BOTTOM){
            this._createSiblingShape.setSize(size.width,size.height/2);
            this._createSiblingShape.setPosition(0,size.height/2);
            this._createSiblingShape.setVisibility(true);
            this._createChildShape.setVisibility(false);
            this._createShape = "Sibling_bottom";
            this._targetNode = topic;
        } else {
            this._createSiblingShape.setVisibility(false);
            this._createChildShape.setVisibility(false);
            this._createShape = null;
            this._targetNode = null;
        }
    },
    _removeIndicatorShapes:function(node){
        if(this._createChildShape.getParent()!=null|| this._createSiblingShape.getParent()!=null){
            this._createChildShape.getParent().removeChild(this._createChildShape._peer);
            this._createSiblingShape.getParent().removeChild(this._createSiblingShape._peer);
        }
    },
    _reconnectMouseOutListener:function(event, layoutManager){
        layoutManager._updateIndicatorShapes(this, null);
    },
    _updateTopicsForReconnect:function(topic, opacity){
        topic.setOpacity(opacity);
        topic.moveToBack();
        var children = topic._getChildren();
        for(var k = 0; k<children.length; k++){
            this._updateTopicsForReconnect(children[k], opacity);
        }
    },
    _restoreTopicsForReconnect:function(topic){
        var children = topic._getChildren();
        for(var k = 0; k<children.length; k++){
            this._restoreTopicsForReconnect(children[k]);
        }
        topic.setOpacity(1);
        topic.moveToFront();
    },
    _movingNode:function(targetNode, node){
        var entry;
        if(node._relationship_oldParent!=null){
            var parentBoard = this.getTopicBoardForTopic(node._relationship_oldParent);
            var entryObj;
            if(this._isCentralTopic(node._relationship_oldParent)){
                var oldPos = node._originalPosition;
                entryObj = parentBoard.findNodeEntryIndex(node,oldPos);
            }else{
                entryObj = parentBoard.findNodeEntryIndex(node);
            }
            entry = entryObj.table[entryObj.index];
            parentBoard._removeEntry(node, entryObj.table, entryObj.index, []);
        }
        else{
            //if is an isolated topic, create entry and update margins.
            entry = new mindplot.layoutManagers.boards.freeMindBoards.Entry(node, false);
            var board = this.getTopicBoardForTopic(node);
            var table = board._getTableForNode(null);
            if(table.length>0){
                var firstChild = table[0];
                var marginTop = entry.getPosition()-(firstChild.getPosition()-firstChild.getTotalMarginTop());
                board.setNodeChildrenMarginTop(entry,marginTop);
                var lastChild = table[table.length-1];
                var marginBottom = (lastChild.getPosition()+lastChild.getTotalMarginBottom())-entry.getPosition();
                board.setNodeChildrenMarginBottom(entry,marginBottom);
            } else {
                board.setNodeChildrenMarginTop(entry, 0);
                board.setNodeChildrenMarginBottom(entry, 0);
            }
        }
        var targetBoard = this.getTopicBoardForTopic(targetNode);
        var table = targetBoard._getTableForNode(node);
        var index;
        if(node.relationship == 'undo'){
            index = node._relationship_index;
            //I need to update all entries because nodes position have been changed by command

        }else{
            if(node.relationship == "Child"){

                var newNodePos=new core.Point();
                if(table.length>0){
                    //if no children use the position set by Entry initializer. Otherwise place as last child
                    var lastChild = table[table.length-1];
                    newNodePos.y = lastChild.getPosition()+lastChild.getTotalMarginBottom() + entry.getTotalMarginTop();
                } else {
                    newNodePos.y = targetNode.getPosition().y;
                }
                var parentPos = targetNode.getPosition();
                var pwidth = targetNode.getSize().width;
                var width = node.getSize().width;
                if(this._isCentralTopic(targetNode)){
                    newNodePos.x = Math.sign(node.getPosition().x) * (entry._DEFAULT_X_GAP + pwidth/2 + width/2)
                }
                else{
                    newNodePos.x = parentPos.x + Math.sign(parentPos.x) * (entry._DEFAULT_X_GAP + pwidth/2 + width/2);
                }

                index = table.length;
            } else {
                //moving as sibling of targetNode

                var sibObj = targetBoard.findNodeEntryIndex(node._relationship_sibling_node);
                var siblingEntry =sibObj.table[sibObj.index];

                var newNodePos=new core.Point();
                if(node.relationship == "Sibling_top"){
                    if(sibObj.index==0){
                        newNodePos.y = siblingEntry.getPosition();
                    }else{
                        newNodePos.y =siblingEntry.getPosition()-siblingEntry.getTotalMarginTop()+entry.getTotalMarginTop();
                    }
                    index = sibObj.index;
                }
                else{
                    newNodePos.y = siblingEntry.getPosition()+siblingEntry.getTotalMarginBottom() + entry.getTotalMarginTop();
                    index = sibObj.index+1;
                }
                var parentPos = targetNode.getPosition();
                var pwidth = targetNode.getSize().width;
                var width = node.getSize().width;
                if(this._isCentralTopic(targetNode)){
                    newNodePos.x = Math.sign(node.getPosition().x) * (entry._DEFAULT_X_GAP + pwidth/2 + width/2)
                }
                else{
                    newNodePos.x = parentPos.x + Math.sign(parentPos.x) * (entry._DEFAULT_X_GAP + pwidth/2 + width/2);
                }
            }
            var nodePos = node.getPosition();
            var x = nodePos.x - newNodePos.x;
            var y = nodePos.y - newNodePos.y;
            var delta = new core.Point(Math.round(x), Math.round(y));
            entry.setPosition(newNodePos.x, newNodePos.y);
            this._updateChildrenBoards(node, delta, []);
        }
        targetBoard._addEntry(entry, table, index);
        targetBoard._updateTable(index,  table, [], true);
        this._updateBoard(targetNode,[]);
        if(node._relationship_oldParent!=null)
            this._updateBoard(node._relationship_oldParent,[]);

        mindplot.EventBus.instance.fireEvent(mindplot.EventBus.events.NodeMouseOutEvent,[node ]);
    },
    _addToModifiedList:function(modifiedTopics, key, originalpos, newPos){
        if(modifiedTopics.set){
            if(modifiedTopics.hasKey(key)){
                originalpos = modifiedTopics.get(key).originalPos;
            }
            modifiedTopics.set(key,{originalPos:originalpos, newPos:newPos});
        }
    }
});

mindplot.layoutManagers.FreeMindLayoutManager.NAME ="FreeMindLayoutManager";
mindplot.layoutManagers.FreeMindLayoutManager.MOVE_IMAGE_URL = "../images/move.png";
mindplot.layoutManagers.FreeMindLayoutManager.RECONNECT_NODES_OPACITY = 0.4;
mindplot.layoutManagers.FreeMindLayoutManager.RECONNECT_SHAPE_CHILD = "child";
mindplot.layoutManagers.FreeMindLayoutManager.RECONNECT_SHAPE_SIBLING_TOP = "top";
mindplot.layoutManagers.FreeMindLayoutManager.RECONNECT_SHAPE_SIBLING_BOTTOM = "bottom";