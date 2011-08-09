mindplot.collaboration.frameworks = {};

mindplot.collaboration.frameworks.AbstractCollaborativeFramework = new Class({
    initialize: function(model, collaborativeModelFactory){
        this._collaborativeModelFactory = collaborativeModelFactory;
        if(!$defined(model)){
            model = this._buildInitialCollaborativeModel();
        }
        this._model = model;
        this._actionDispatcher = null;
    },
    getModel: function(){
        return this._model;
    },
    buildWiseModel: function(){
        var cmindMap = this.getModel();
        var mindmap = new mindplot.model.Mindmap();
        var branches = cmindMap.getBranches();
        branches.forEach(function(branch){
            var type = branch.getType();
            var id = branch.getId();
            var node = mindmap.createNode(type,id);
            node.setText(branch.getText());
            mindmap.addBranch(node);
        }.bind(this));
        return mindmap;
    },
    _buildInitialCollaborativeModel: function(){
        var mindmap = this._collaborativeModelFactory.buildMindMap();
        var centralTopic = mindmap.createNode(mindplot.model.NodeModel.CENTRAL_TOPIC_TYPE);
        mindmap.addBranch(centralTopic, true);
        this.addMindmap(mindmap);
        return mindmap;
    },
    addMindmap:function(model){},
    getTopic:function(id){
        var branches = this.getModel().getBranches();
        for(var i = 0; i<branches.length; i++){
            if(branches[i].getId()==id){
                return branches[i];
            }
        }
        return null;
    },
    getActionDispatcher:function(){
        if(this._actionDispatcher == null){
            var context = mindplot.ActionDispatcher.getInstance()._commandContext;
            this._actionDispatcher = new mindplot.LocalActionDispatcher(context);
        }
        return this._actionDispatcher;
    }

});