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

mindplot.collaboration.framework.brix.model.NodeModel = new Class({
    Extends: mindplot.model.INodeModel,
    initialize : function(brixFramework, brixModel, mindmap) {
        $assert(brixFramework, "brixFramework can not null");
        $assert(brixModel, "brixModel can not null");

        this.parent(mindmap);
        this._brixModel = brixModel;
        this._brixFramework = brixFramework;
        this._addBrixListeners();
    },

    _addBrixListeners : function() {

        // Nodes creation should be cached ...
        if (!this._brixModel.__registered) {
            // Register listener for properties changes ....
            this._brixModel.addListener("valueChanged", function(event) {
                var key = event.getProperty();

                var actionDispatcher = this._brixFramework.getActionDispatcher();
                var value = event.getNewValue();

                var funName = 'change' + key.capitalize() + 'ToTopic';
                if (!$defined(actionDispatcher[funName])) {
                    throw "No implementation for:" + funName;
                }
                console.log("This action dispatcher:" + funName);
                actionDispatcher[funName]([this.getId()], value);
            }.bind(this));

            var children = this._brixModel.get("children");
            children.addListener("valuesAdded", function(event) {
                var brixNodeModel = event.getValues().get(0);

                var cmodel = new mindplot.collaboration.framework.brix.model.NodeModel(this._brixFramework, brixNodeModel, this.getMindmap());

                // @Todo: This is not ok...
                var model = new mindplot.model.NodeModel(cmodel.getType(), designer.getMindmap(), this.getId());
                cmodel.copyTo(model);

                this._brixFramework.getActionDispatcher().addTopic(model, this.getId(), true);
            }.bind(this));

            this._brixModel.__registered = true;
        }
    },

    getChildren : function() {
        var result = [];
        var children = this._brixModel.get("children");
        for (var i = 0; i < children.size(); i++) {
            var node = children.get(i);
            var nodeModel = new mindplot.collaboration.framework.brix.model.NodeModel(this._brixFramework, node, this);
            result.push(nodeModel);
        }
        return result;
    },


    getBrixModel:function() {
        return this._brixModel;
    },

    putProperty : function(key, value) {
        $defined(key, 'key can not be null');
        this._brixModel.put(key, value);
    },

    getProperty : function(key) {
        $defined(key, 'key can not be null');
        return this._brixModel.get(key);
    },

    getPropertiesKeys : function() {
        return  this._brixModel.getKeys();
    },

    getParent : function() {
        return this._brixModel._parent;
    },

    appendChild : function(node) {
        $assert(node && node.isNodeModel(), 'Only NodeModel can be appended to Mindmap object');
        var children = this._brixModel.get("children");
        children.add(node.getBrixModel());
        node.getBrixModel()._parent = this;
    }
});

mindplot.collaboration.framework.brix.model.NodeModel.create = function(brixFramework, mindmap, type, id) {
    $assert(brixFramework, 'brixFramework can not be null');
    $assert(mindmap, 'mindmap can not be null');
    $assert(type, 'type can not be null');
    $assert($defined(id), 'id can not be null');

    var brixModel = brixFramework.getBrixModel().create("Map");
    brixModel.put("type", type);
    brixModel.put("id", id);

    var children = brixFramework.getBrixModel().create("List");
    brixModel.put("children", children);

    return new mindplot.collaboration.framework.brix.model.NodeModel(brixFramework, brixModel, mindmap);
};