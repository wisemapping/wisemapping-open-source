mindplot.collaboration.frameworks = {};

mindplot.collaboration.frameworks.AbstractCollaborativeFramework = new Class({
    initialize: function(model, collaborativeModelFactory){
        this._collaborativeModelFactory = collaborativeModelFactory;
        if(!$defined(model)){
            model = this._buildInitialCollaborativeModel();
        }
        this._model = model;
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
        }.bind(this))
        return mindmap;
    },
    _buildInitialCollaborativeModel: function(){
        var mindmap = this._collaborativeModelFactory.buildMindMap();
        this.addMindmap(mindmap);
        var centralTopic = mindmap.createNode(mindplot.model.NodeModel.CENTRAL_TOPIC_TYPE);
        mindmap.addBranch(centralTopic);
        return mindmap;
    },
    addMindmap:function(model){}

});