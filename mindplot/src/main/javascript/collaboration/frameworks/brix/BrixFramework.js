mindplot.collaboration.frameworks.brix.BrixFramework = new Class({
    Extends: mindplot.collaboration.frameworks.AbstractCollaborativeFramework,

    initialize: function(model, app) {
        this._app = app;
        var collaborativeModelFactory = new mindplot.collaboration.frameworks.brix.BrixCollaborativeModelFactory(this);
        var cModel = null;
        var root = this.getBrixModel().getRoot();
        if (!root.isEmpty()) {
            cModel = collaborativeModelFactory.buildCollaborativeModelFor(root.get("mindmap"));
        }
        this.parent(cModel, collaborativeModelFactory);
    },
    addMindmap:function(model) {
        var root = this.getBrixModel().getRoot();
        root.put("mindmap", model);
    },
    getBrixModel:function() {
        return this._app.getModel();
    },
    buildWiseModel: function() {
        return this.parent();
    }
});
instanciated = false;
mindplot.collaboration.frameworks.brix.BrixFramework.instanciate = function() {
    if ((typeof isGoogleBrix != "undefined") && !instanciated) {
        instanciated = true;
        var app = new goog.collab.CollaborativeApp();
        mindplot.collaboration.frameworks.brix.BrixFramework.buildMenu(app);
        app.start();
        app.addListener('modelLoad', function(model) {
            var framework = new mindplot.collaboration.frameworks.brix.BrixFramework(model, app);
            mindplot.collaboration.CollaborationManager.getInstance().setCollaborativeFramework(framework);
        }.bind(this));
    }
};

mindplot.collaboration.frameworks.brix.BrixFramework.buildMenu = function(app) {
    var menuBar = new goog.collab.ui.MenuBar();

    // Configure toolbar menu ...
    var fileMenu = menuBar.addSubMenu("File");
    fileMenu.addItem("Save", function() {
    });
    fileMenu.addItem("Export", function() {
    });

    var editMenu = menuBar.addSubMenu("Edit");
    editMenu.addItem("Undo", function() {
    });
    editMenu.addItem("Redo", function() {
    });

    var formatMenu = menuBar.addSubMenu("Format");
    formatMenu.addItem("Bold", function() {
    });

    var helpMenu = menuBar.addSubMenu("Help");
    helpMenu.addItem("Shortcuts", function() {
    });

    app.setMenuBar(menuBar);
};
mindplot.collaboration.frameworks.brix.BrixFramework.instanciate();


