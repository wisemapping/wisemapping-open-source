mindplot.layoutManagers.OriginalLayoutManager = mindplot.layoutManagers.BaseLayoutManager.extend({
    options:{

    },
    initialize:function(designer, options){
        this.parent(designer, options);
        this._dragTopicPositioner = new mindplot.DragTopicPositioner(this);
        // Init dragger manager.
        var workSpace = this.getDesigner().getWorkSpace();
        this._dragger = this._buildDragManager(workSpace);

        // Add shapes to speed up the loading process ...
        mindplot.DragTopic.initialize(workSpace);
    },
    prepareChildrenList:function(node, children){
        // Sort children by order to solve adding order in for OriginalLayoutManager...
        var result = [];
        if (node.getTopicType()!=mindplot.NodeModel.CENTRAL_TOPIC_TYPE && children.length > 0)
        {
            for (var i = 0; i < children.length; i++)
            {
                var child = children[i];
                var order = child.getOrder();
                if (order != null)
                {
                    result[order] = child;
                } else
                {
                    result.push(child);
                }
            }
        }
        return result;
    },
    _nodeResizeEvent:function(node){

    },
    _nodeRepositionateEvent:function(node){
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

        // Register editor events ...
        if (!this.getDesigner()._viewMode)
        {
            this.getDesigner()._editor.listenEventOnNode(topic, 'dblclick', true);
        }

    },
    _createMainTopicBoard:function(node){
        return new mindplot.MainTopicBoard(node, this);
    },
    _createCentralTopicBoard:function(node){
        return new mindplot.CentralTopicBoard(node,this);
    },
    getClassName:function(){
        return mindplot.layoutManagers.OriginalLayoutManager.NAME;
    }
});

mindplot.layoutManagers.OriginalLayoutManager.NAME ="OriginalLayoutManager"; 