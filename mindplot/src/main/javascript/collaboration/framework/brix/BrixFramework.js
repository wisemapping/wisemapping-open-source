/*
 *    Copyright [2011] [wisemapping]
 *
 *   Licensed under WiseMapping Public License, Version 1.0 (the "License").
 *   It is basically the Apache License, Version 2.0 (the "License") plus the
 *   "powered by wisemapping" text requirement on every single page;
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the license at
 *
 *       http://www.wisemapping.org/license
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

mindplot.collaboration.framework.brix.BrixFramework = new Class({
    Extends: mindplot.collaboration.framework.AbstractCollaborativeFramework,
    initialize: function(model, app) {
        this._app = app;
        var factory = new mindplot.collaboration.framework.brix.BrixCollaborativeModelFactory(this);
        var root = this.getBrixModel().getRoot();
        var cmodel = null;
        var brixMap = root.get("mindmap");
        if (brixMap != null) {
            cmodel = factory.buildMindmap(brixMap);
        } else {
            cmodel = factory.createNewMindmap();
            root.put("mindmap", cmodel.getBrixModel());
        }
        this.parent(cmodel);
        console.log("cmodel:" + cmodel.inspect());
    },

    getBrixModel:function() {
        return this._app.getModel();
    }

});

instanciated = false;
mindplot.collaboration.framework.brix.BrixFramework.init = function(onload) {
    $assert(onload, "load function can not be null");

    if (!instanciated) {
        var app = new goog.collab.CollaborativeApp();
        mindplot.collaboration.framework.brix.BrixFramework.buildMenu(app);
        app.start();

        app.addListener('modelLoad', function(model) {
            var framework = new mindplot.collaboration.framework.brix.BrixFramework(model, app);
            mindplot.collaboration.CollaborationManager.getInstance().setCollaborativeFramework(framework);
            onload();
        }.bind(this));
        instanciated = true;
    }
};

mindplot.collaboration.framework.brix.BrixFramework.buildMenu = function(app) {
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


