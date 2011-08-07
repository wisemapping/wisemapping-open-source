mindplot.collaboration = {};
mindplot.collaboration.CollaborationManager = new Class({
    initialize:function(){
        this.collaborativeModelReady = false;
        this.collaborativeModelReady = null;
        this.wiseReady = false;
    },
    isCollaborationFrameworkAvailable:function(){
        return $defined(goog.collab.CollaborativeApp);
    },
    setCollaborativeFramework:function(framework){
        this._collaborativeFramework = framework;
        this.collaborativeModelReady = true;
        if(this.wiseReady){
            buildCollaborativeMindmapDesigner();
        }
    },
    setWiseReady:function(ready){
        this.wiseReady=ready;
    },
    isCollaborativeFrameworkReady:function(){
        return this.collaborativeModelReady;
    },
    buildWiseModel: function(){
        return this._collaborativeFramework.buildWiseModel();
    }
});

$wise_collaborationManager = new mindplot.collaboration.CollaborationManager();
