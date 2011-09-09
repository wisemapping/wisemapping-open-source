mindplot.collaboration.framework.AbstractCollaborativeFramework = new Class({
    initialize: function(model, collaborativeModelFactory)
    {
        this._collaborativeModelFactory = collaborativeModelFactory;
        if (!$defined(model)) {
            model = this._buildInitialCollaborativeModel();
        }
        this._model = model;
        this._actionDispatcher = null;
    },

    getModel : function() {
        return this._model;
    },

    buildMindmap : function() {

        var cmind = this.getModel();
        var mmind = new mindplot.model.Mindmap();
        cmind.copyTo(mmind);

        return mmind;
    },

    _buildInitialCollaborativeModel: function() {

        var mindmap = this._collaborativeModelFactory.buildMindMap();
        var centralTopic = mindmap.createNode(mindplot.model.INodeModel.CENTRAL_TOPIC_TYPE);
        mindmap.addBranch(centralTopic, true);
        this.addMindmap(mindmap);
        return mindmap;
    },

    addMindmap:function(model) {
        throw "method to implement";
    },

    _findTopic : function(topics, id) {
        var result;
        for (var i = 0; i < topics.length; i++) {
            var topic = topics[i];
            if (topic.getId() == id) {
                result = topic;
            } else {
                var children = topic.getChildren();
                result = this._findTopic(children, id)
            }

            if (result != null) {
                break;
            }
        }
        return result;
    },

    getTopic:function(id) {
        $assert(id, "id can not be null")
        var branches = this.getModel().getBranches();
        var result =  this._findTopic(branches, id);
        $assert(result, "Could not find topic:" + id);
        return result;
    },

    getActionDispatcher:function() {
        if (this._actionDispatcher == null) {
            var context = mindplot.ActionDispatcher.getInstance()._commandContext;
            this._actionDispatcher = new mindplot.LocalActionDispatcher(context);
        }
        return this._actionDispatcher;
    }

});