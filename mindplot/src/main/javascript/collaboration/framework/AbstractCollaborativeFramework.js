mindplot.collaboration.framework.AbstractCollaborativeFramework = new Class({
    initialize: function(model) {
        $assert(model, "model can not be null");
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

    _findTopic : function(nodes, id) {
        var result;
        for (var i = 0; i < nodes.length; i++) {
            var node = nodes[i];
            if (node.getId() == id) {
                result = node;
            } else {
                var children = node.getChildren();
                result = this._findTopic(children, id)
            }

            if (result != null) {
                break;
            }
        }
        return result;
    },

    getTopic:function(id) {
        $assert($defined(id), "id can not be null");
        var branches = this.getModel().getBranches();
        var result = this._findTopic(branches, id);
        $assert(result, "Could not find topic:" + id);
        return result;
    },

    getActionDispatcher:function() {
        if (this._actionDispatcher == null) {
            var context = mindplot.ActionDispatcher.getInstance()._commandContext;
            this._actionDispatcher = new mindplot.StandaloneActionDispatcher(context);
        }
        return this._actionDispatcher;
    }

});