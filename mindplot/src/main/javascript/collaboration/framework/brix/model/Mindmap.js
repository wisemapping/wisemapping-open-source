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
mindplot.collaboration.framework.brix.model.Mindmap = new Class({
        Extends:mindplot.model.IMindmap,
        initialize:function(brixFramework, brixModel) {
            $assert(brixFramework, 'brixFramework can not be null');

            this._brixFramework = brixFramework;
            if (!$defined(brixModel)) {
                this._brixModel = this._createBrixModel();
            } else {
                this._brixModel = brixModel;
            }
        },

        getVersion: function() {
            return this._brixModel.get('version');
        },

        setVersion: function(value) {
            this._brixModel.put('version', value);
        },

        getDescription: function() {
            return this._brixModel.get('description');
        },

        setDescription: function(value) {
            this._brixModel.put('description', value);
        },

        _createBrixModel:function() {
            var model = this._brixFramework.getBrixModel().create("Map");
            var branches = this._brixFramework.getBrixModel().create("List");
            model.put("branches", branches);
            return model;
        },

        getBrixModel:function() {
            return this._brixModel;
        },

        getBranches : function() {
            var result = [];
            var branches = this._brixModel.get("branches");
            for (var i = 0; i < branches.size(); i++) {
                var node = branches.get(i);
                var nodeModel = new mindplot.collaboration.framework.brix.model.NodeModel(this._brixFramework, node, this);
                result.push(nodeModel);
            }
            return result;
        },

        addBranch : function(nodeModel) {
            $assert(nodeModel, "nodeModel can not be null");
            var branches = this._brixModel.get("branches");
            branches.add(nodeModel.getBrixModel());
        },

        removeBranch : function(nodeModel) {
            $assert(nodeModel, "nodeModel can not be null");
            $assert(nodeModel.getType() != mindplot.model.INodeModel.CENTRAL_TOPIC_TYPE, "central topic can not be removed");

            var branches = this._brixModel.get("branches");
            for (var i = 0; i < branches.size(); i++) {
                if (branches.get(i) == nodeModel.getBrixModel()) {
                    branches.remove(i);
                    break;

                }
            }
        },

        createNode : function(type, id) {
            return  mindplot.collaboration.framework.brix.model.NodeModel.create(this._brixFramework, this, type, id);
        }

    }
);

