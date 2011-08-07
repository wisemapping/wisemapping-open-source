

mindplot.collaboration.frameworks.brix.BrixFramework = new Class({
    Extends: mindplot.collaboration.frameworks.AbstractCollaborativeFramework,

    initialize: function(model, app){
        this._app = app;
        var collaborativeModelFactory = new mindplot.collaboration.frameworks.brix.BrixCollaborativeModelFactory(this);
        var cModel = null;
        var root = this.getBrixModel().getRoot();
        if(!root.isEmpty()){
            cModel = collaborativeModelFactory.buildCollaborativeModelFor(root.get("mindmap"));
        }
        this.parent(cModel, collaborativeModelFactory);
    },
    addMindmap:function(model){
        var root = this.getBrixModel().getRoot();
        root.put("mindmap",model);
    },
    getBrixModel:function(){
        return this._app.getModel();
    },
    buildWiseModel: function(){
          return this.parent();
    }
});
instanciated=false;
mindplot.collaboration.frameworks.brix.BrixFramework.instanciate=function(){
    if($defined(isGoogleBrix) && !instanciated){
        instanciated=true;
        var app = new goog.collab.CollaborativeApp();
        app.start();
        app.addListener('modelLoad', function(model){
            var framework = new mindplot.collaboration.frameworks.brix.BrixFramework(model, app);
            $wise_collaborationManager.setCollaborativeFramework(framework);
        }.bind(this));
    }
};

mindplot.collaboration.frameworks.brix.BrixFramework.instanciate();


