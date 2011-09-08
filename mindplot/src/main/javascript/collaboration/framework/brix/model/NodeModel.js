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
    initialize : function(brixModel, brixFramework, mindmap) {
        $assert(brixModel, "brixModel can not null");
        $assert(brixFramework, "brixFramework can not null");

        this.parent(mindmap);
        this._brixModel = brixModel;
        this._brixFramework = brixFramework;
        this._injectSetAndGet();
        this._addBrixListeners();
    },

    _addBrixListeners:function() {
        // Register listener for properties changes ....
        this._brixModel.addListener("valueChanged", function(event) {
            var key = event.getProperty();

            var actionDispatcher = this._brixFramework.getActionDispatcher();
            var value = event.getNewValue();

            var funName = 'change' + key.capitalize() + 'ToTopic';
            if (!$defined(actionDispatcher[funName])) {
                throw "No implementation for:" + funName;
            }
            actionDispatcher[funName](this.getId(), value);
        }.bind(this));

        this._brixModel.get("children").addListener("valuesAdded", this._childAddedListener.bind(this));
    },

    _childAddedListener:function(event) {
        var newValue = event.getValues().get(0);
        var cmodel = new mindplot.collaboration.framework.brix.model.NodeModel(newValue, this._brixFramework, null, this.getMindmap());
        this._appendChild(cmodel, false);

        var model = new mindplot.model.NodeModel(newValue.get("type"), designer.getMindmap(), newValue.get("id"));
        var position = newValue.get("position");
        var x = position.get("x");
        var y = position.get("y");
        model.setPosition(x, y);
        this._brixFramework.getActionDispatcher().addTopic(model, this.getId(), true);
    },

    getBrixModel:function() {
        return this._brixModel;
    }
});

mindplot.collaboration.framework.brix.model.NodeModel.create = function(brixFramework, type, id, mindmap) {
    var brixModel = brixFramework.getBrixModel().create("Map");
    brixModel.put("type", type);
    brixModel.put("id", this._id); // @todo...

    var children = brixFramework.getBrixModel().create("List");
    brixModel.put("children", children);

    return new mindplot.collaboration.framework.brix.model.NodeModel(brixFramework, brixModel, mindmap);
};