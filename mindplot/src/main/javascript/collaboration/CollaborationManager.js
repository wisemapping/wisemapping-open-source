mindplot.collaboration = {};
mindplot.collaboration.CollaborationManager = new Class({
    initialize:function() {
        this.collaborativeModelReady = false;
        this.collaborativeModelReady = null;
        this.wiseReady = false;
    },

    isCollaborationFrameworkAvailable:function() {
        return (typeof goog != "undefined") && (typeof goog.collab != "undefined");
    },

    setCollaborativeFramework:function(framework) {
        this._collaborativeFramework = framework;
        this.collaborativeModelReady = true;
        if (this.wiseReady) {
            buildCollaborativeMindmapDesigner();
        }
    },

    setWiseReady:function(ready) {
        this.wiseReady = ready;
    },

    isCollaborativeFrameworkReady:function() {
        return this.collaborativeModelReady;
    },

    buildWiseModel: function() {
        return this._collaborativeFramework.buildWiseModel();
    },

    getCollaborativeFramework:function() {
        return this._collaborativeFramework;
    }

});

mindplot.collaboration.CollaborationManager.getInstance = function() {
    if (!$defined(mindplot.collaboration.CollaborationManager.__collaborationManager)) {
        mindplot.collaboration.CollaborationManager.__collaborationManager = new mindplot.collaboration.CollaborationManager();
    }
    return mindplot.collaboration.CollaborationManager.__collaborationManager;
}
mindplot.collaboration.CollaborationManager.getInstance();
