mindplot.layoutManagers.BaseLayoutManager = new Class({

    options: {

    },

    initialize: function(designer, options) {
        this.setOptions(options);
        this._boards = new Hash();
        this._designer = designer;
        mindplot.EventBus.instance.addEvent(mindplot.EventBus.events.NodeResizeEvent,this._nodeResizeEvent.bind(this));
        mindplot.EventBus.instance.addEvent(mindplot.EventBus.events.NodeMoveEvent,this._nodeMoveEvent.bind(this));
        mindplot.EventBus.instance.addEvent(mindplot.EventBus.events.NodeDisconnectEvent,this._nodeDisconnectEvent.bind(this));
        mindplot.EventBus.instance.addEvent(mindplot.EventBus.events.NodeConnectEvent,this._nodeConnectEvent.bind(this));
        mindplot.EventBus.instance.addEvent(mindplot.EventBus.events.NodeRepositionateEvent,this._NodeRepositionateEvent.bind(this));
    },
    _nodeResizeEvent:function(node){
    },
    _nodeMoveEvent:function(node){
        //todo: Usar un solo board para todos los nodos. Testear que ande el set margin cuando se mueven los nodos.
        this.getTopicBoardForTopic(node).updateChildrenPosition(node);
    },
    _nodeDisconnectEvent:function(targetNode, node){
        this.getTopicBoardForTopic(targetNode).removeTopicFromBoard(node);
    },
    _nodeConnectEvent:function(targetNode, node){
        this.getTopicBoardForTopic(targetNode).addBranch(node);
    },
    _NodeRepositionateEvent:function(node){
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
        var board = null;
        if (this._isCentralTopic(node))
            board = this._createCentralTopicBoard(node);
        else
            board = this._createMainTopicBoard(node);
        var id = node.getId();
        this._boards[id]=board;
        return board;
    },
    _createMainTopicBoard:function(node){
        return new mindplot.layoutManagers.boards.Board(node, this);
    },
    _createCentralTopicBoard:function(node){
        return new mindplot.layoutManagers.boards.Board(node, this);
    },
    getDesigner:function(){
        return this._designer;
    },
    _isCentralTopic:function(node){
        var type = node.getModel().getType();
        return type == mindplot.NodeModel.CENTRAL_TOPIC_TYPE;
    },
    getClassName:function(){
        return mindplot.layoutManagers.BaseLayoutManager.NAME;
    }
});

mindplot.layoutManagers.BaseLayoutManager.NAME ="BaseLayoutManager"; 

mindplot.layoutManagers.BaseLayoutManager.implement(new Events);
mindplot.layoutManagers.BaseLayoutManager.implement(new Options);