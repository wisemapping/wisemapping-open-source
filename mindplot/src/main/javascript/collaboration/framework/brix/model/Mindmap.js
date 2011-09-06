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
        Extends:mindplot.model.Mindmap,
        initialize:function(brixModel, brixFramework){
            this.parent();
            this._brixModel = brixModel;
            this._brixFramework = brixFramework;
            if(!$defined(this._brixModel)){
                this._brixModel = this._createBrixModel();
            }else{
                var branches = this._brixModel.get("branches");
                for(var i=0; i<branches.size(); i++){
                    var node = branches.get(i);
                    var nodeModel = new mindplot.collaboration.framework.brix.model.NodeModel(node, this._brixFramework, null, this);
                    this.addBranch(nodeModel, false);
                }
            }
        },
        _createBrixModel:function(){
            var model = this._brixFramework.getBrixModel().create("Map");
            var branches = this._brixFramework.getBrixModel().create("List");
            model.put("branches",branches);
            this._brixFramework.addMindmap(model);
            return model;
        },
        getBrixModel:function(){
            return this._brixModel;
        },
        setId : function(id) {
            this._iconType = id;
        },
        setVersion : function(version) {
            this._version = version;
        },
        addBranch : function(nodeModel, addToModel) {
            this.parent(nodeModel);
            if($defined(addToModel) && addToModel){
                var branches = this._brixModel.get("branches");
                branches.add(nodeModel.getBrixModel());
            }
        },
        connect : function(parent, child) {
            this.parent(parent, child);

            // Remove from the branch ...
            var branches = this._brixModel.get("branches");
            var childIndex = null;
            for(var i = 0; i<branches.size(); i++){
                if(branches.get(i)==child.getBrixModel()){
                    childIndex = i;
                    break;
                }
            }
            if(childIndex!=null){
                branches.remove(childIndex);
            }
        },

        disconnect : function(child) {
            var parent = child.getParent();
            $assert(child, 'Child can not be null.');
            $assert(parent, 'Child model seems to be already connected');

            parent._removeChild(child);

            var branches = this.getBranches();
            branches.push(child);

        },
        _createNode : function(type, id) {
            $assert(type, 'Node type must be specified.');
            var result = new mindplot.collaboration.framework.brix.model.NodeModel(null, this._brixFramework, type, this, id);
            return result;
        },

        createRelationship : function(fromNode, toNode) {
            $assert(fromNode, 'from node cannot be null');
            $assert(toNode, 'to node cannot be null');

            return new mindplot.model.RelationshipModel(fromNode, toNode);
        },

        addRelationship : function(relationship) {
            this._relationships.push(relationship);
        },

        removeRelationship : function(relationship) {
            this._relationships.erase(relationship);
        }
    }
);