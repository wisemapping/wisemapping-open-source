mindplot.layoutManagers.FreeMindLayoutManager = mindplot.layoutManagers.BaseLayoutManager.extend({
    options:{

    },
    initialize:function(designer, options){
        this.parent(designer, options);
    },
    registerListenersOnNode : function(topic)
    {
        var id = topic.getId();
        console.log("registering on node: "+id);
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

        /*// Register editor events ...
        if (!this._viewMode)
        {
            this._editor.listenEventOnNode(topic, 'dblclick', true);
        }*/

    },
    _mousedownListener:function(event,topic){

        var workSpace = this._designer.getWorkSpace();
        if (workSpace.isWorkspaceEventsEnabled())
        {
            // Disable double drag...
            workSpace.enableWorkspaceEvents(false);
            
            var id = topic.getId();
            console.log("down on node: "+id);
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
        var screen = this._designer.getWorkSpace().getScreenManager();
        var nodePos = node.getPosition();
        var pos = screen.getWorkspaceMousePosition(event);
        var x = nodePos.x - pos.x;
        var y = nodePos.y - pos.y;
        var delta = new core.Point(x, y);
        var board = this.getTopicBoardForTopic(node.getParent());
        board.setNodeMarginTop(node, delta);
        //update children position
        this._updateNodePos(node, delta);
        event.preventDefault();
    },
    _mouseUpListener:function(event, node){
        var id = node.getId();
        console.log("up on node: "+id);
        
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

//        var topicId = draggedTopic.getId();
//        var command = new mindplot.commands.DragTopicCommand(topicId);
        
    },
    _updateNodePos:function(node, delta){
        var pos = node.getPosition();
        node.setPosition(new core.Point(pos.x-delta.x, pos.y-delta.y));
        /*var children = node._getChildren();
        for (var i = 0; i < children.length; i++)
        {
            this._updateNodePos(children[i],delta);
        }*/
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
});

mindplot.layoutManagers.FreeMindLayoutManager.NAME ="FreeMindLayoutManager"; 