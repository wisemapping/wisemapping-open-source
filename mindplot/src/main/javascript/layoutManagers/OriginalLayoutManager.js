mindplot.layoutManagers.OriginalLayoutManager = mindplot.layoutManagers.BaseLayoutManager.extend({
    options:{

    },
    initialize:function(designer, options){
        this.parent(designer, options);
        this._boards = new Hash();
        this._dragTopicPositioner = new mindplot.DragTopicPositioner(this);
        // Init dragger manager.
        var workSpace = this.getDesigner().getWorkSpace();
        this._dragger = this._buildDragManager(workSpace);

        // Add shapes to speed up the loading process ...
        mindplot.DragTopic.initialize(workSpace);

        mindplot.EventBus.instance.addEvent(mindplot.EventBus.events.NodeResizeEvent,this._nodeResizeEvent.bind(this));
        mindplot.EventBus.instance.addEvent(mindplot.EventBus.events.NodeMoveEvent,this._nodeMoveEvent.bind(this));
        mindplot.EventBus.instance.addEvent(mindplot.EventBus.events.NodeDisconnectEvent,this._nodeDisconnectEvent.bind(this));
        mindplot.EventBus.instance.addEvent(mindplot.EventBus.events.NodeConnectEvent,this._nodeConnectEvent.bind(this));
        mindplot.EventBus.instance.addEvent(mindplot.EventBus.events.NodeRepositionateEvent,this._NodeRepositionateEvent.bind(this));
    },
    _nodeResizeEvent:function(node){
        var size = node.getSize();
        if(!this._isCentralTopic(node))
            this.getTopicBoardForTopic(node).updateChildrenPosition(node,size.height/2);
    },
    _nodeMoveEvent:function(node){
        this.getTopicBoardForTopic(node).updateChildrenPosition(node);
    },
    _nodeDisconnectEvent:function(targetNode, node){
        this.getTopicBoardForTopic(targetNode).removeTopicFromBoard(node);
    },
    _nodeConnectEvent:function(targetNode, node){
        this.getTopicBoardForTopic(targetNode).addBranch(node);
    },
    _NodeRepositionateEvent:function(node){
        this.getTopicBoardForTopic(node).repositionate();
    },
    getDragTopicPositioner : function()
    {
        return this._dragTopicPositioner;
    },
    _buildDragManager: function(workspace)
    {
        // Init dragger manager.
        var dragger = new mindplot.DragManager(workspace);
        var topics = this.getDesigner()._getTopics();

        var dragTopicPositioner = this.getDragTopicPositioner();

        dragger.addEventListener('startdragging', function(event, node)
        {
            // Enable all mouse events.
            for (var i = 0; i < topics.length; i++)
            {
                topics[i].setMouseEventsEnabled(false);
            }
        });

        dragger.addEventListener('dragging', function(event, dragTopic)
        {
            // Update the state and connections of the topic ...
            dragTopicPositioner.positionateDragTopic(dragTopic);
        });

        dragger.addEventListener('enddragging', function(event, dragTopic)
        {
            // Enable all mouse events.
            for (var i = 0; i < topics.length; i++)
            {
                topics[i].setMouseEventsEnabled(true);
            }
            // Topic must be positioned in the real board postion.
            if (dragTopic._isInTheWorkspace)
            {
                var draggedTopic = dragTopic.getDraggedTopic();

                // Hide topic during draw ...
                draggedTopic.setBranchVisibility(false);
                var parentNode = draggedTopic.getParent();
                dragTopic.updateDraggedTopic(workspace);


                // Make all node visible ...
                draggedTopic.setVisibility(true);
                if (parentNode != null)
                {
                    parentNode.setBranchVisibility(true);
                }
            }
        });

        return dragger;
    },
    registerListenersOnNode : function(topic)
    {
        // Register node listeners ...
        var designer = this.getDesigner();
        topic.addEventListener('onfocus', function(event)
        {
            designer.onObjectFocusEvent.attempt([topic, event], designer);
        });

        // Add drag behaviour ...
        if (topic.getType() != mindplot.NodeModel.CENTRAL_TOPIC_TYPE)
        {

            // Central Topic doesn't support to be dragged
            var dragger = this._dragger;
            dragger.add(topic);
        }

        /*// Register editor events ...
        if (!this._viewMode)
        {
            this._editor.listenEventOnNode(topic, 'dblclick', true);
        }*/

    },
    getTopicBoardForTopic:function(node){
        var id = node.getId()
        var result = this._boards[id];
        if(!result){
            result = this.addNode(node);
        }
        return result;
    },
    addNode:function(node){
        var boardClass = mindplot.MainTopicBoard;
        if (this._isCentralTopic(node))
            boardClass = mindplot.CentralTopicBoard;
        var board = new boardClass(node, this);
        var id = node.getId();
        this._boards[id]=board;
        this.parent();
        return board;
    },
    _isCentralTopic:function(node){
        var type = node.getModel().getType();
        return type == mindplot.NodeModel.CENTRAL_TOPIC_TYPE;
    },
    getType:function(){
        return mindplot.layoutManagers.OriginalLayoutManager.NAME;
    }
});

mindplot.layoutManagers.OriginalLayoutManager.NAME ="OriginalLayoutManager"; 