mindplot.collaboration.frameworks.brix.BrixCollaborativeModelFactory = new Class({
    Extends:mindplot.collaboration.frameworks.AbstractCollaborativeModelFactory,
    initialize:function(brixFramework){
        this._brixFramework = brixFramework;
    },
    buildMindMap:function(){
        return new mindplot.collaboration.frameworks.brix.model.Mindmap(null, this._brixFramework);
    },
    buildCollaborativeModelFor:function(model){
        return new mindplot.collaboration.frameworks.brix.model.Mindmap(model, this._brixFramework);
    }
});