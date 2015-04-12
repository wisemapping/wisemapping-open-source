/*
 *    Copyright [2015] [wisemapping]
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

mindplot.model.Mindmap = new Class(/** @lends Mindmap */{
        Extends:mindplot.model.IMindmap,
        /**
         * @constructs
         * @param id
         * @param version
         * @extends mindplot.model.IMindmap
         */
        initialize:function (id, version) {
            $assert(id, "Id can not be null");
            this._branches = [];
            this._description = null;
            this._relationships = [];
            this._version = $defined(version) ? version : mindplot.persistence.ModelCodeName.TANGO;
            this._id = id;
        },

        /** */
        getDescription:function () {
            return this._description;
        },

        /** */
        setDescription:function (value) {
            this._description = value;
        },

        /** */
        getId:function () {
            return this._id;
        },

        /** */
        setId:function (id) {
            this._id = id;
        },

        /** */
        getVersion:function () {
            return this._version;
        },

        /** */
        setVersion:function (version) {
            this._version = version;
        },

        /**
         * @param {mindplot.model.NodeModel} nodeModel
         * @throws will throw an error if nodeModel is null, undefined or not a node model object
         * @throws will throw an error if 
         */
        addBranch:function (nodeModel) {
            $assert(nodeModel && nodeModel.isNodeModel(), 'Add node must be invoked with model objects');
            var branches = this.getBranches();
            if (branches.length == 0) {
                $assert(nodeModel.getType() == mindplot.model.INodeModel.CENTRAL_TOPIC_TYPE, "First element must be the central topic");
                nodeModel.setPosition(0, 0);
            } else {
                $assert(nodeModel.getType() != mindplot.model.INodeModel.CENTRAL_TOPIC_TYPE, "Mindmaps only have one cental topic");
            }

            this._branches.push(nodeModel);
        },

        /**
         * @param nodeModel
         */
        removeBranch:function (nodeModel) {
            $assert(nodeModel && nodeModel.isNodeModel(), 'Remove node must be invoked with model objects');
            return this._branches.erase(nodeModel);
        },

        /** */
        getBranches:function () {
            return this._branches;
        },

        /** */
        getRelationships:function () {
            return this._relationships;
        },

        /**
         * @param node
         * @return {Boolean} true if node already exists
         */
        hasAlreadyAdded:function (node) {
            var result = false;

            // Check in not connected nodes.
            var branches = this._branches;
            for (var i = 0; i < branches.length; i++) {
                result = branches[i]._isChildNode(node);
                if (result) {
                    break;
                }
            }
        },

        /**
         * @param type
         * @param id
         * @return the node model created
         */
        createNode:function (type, id) {
            type = !$defined(type) ? mindplot.model.INodeModel.MAIN_TOPIC_TYPE : type;
            return new mindplot.model.NodeModel(type, this, id);
        },

        /**
         * @param sourceNodeId
         * @param targetNodeId
         * @throws will throw an error if source node is null or undefined
         * @throws will throw an error if target node is null or undefined
         * @return the relationship model created
         */
        createRelationship:function (sourceNodeId, targetNodeId) {
            $assert($defined(sourceNodeId), 'from node cannot be null');
            $assert($defined(targetNodeId), 'to node cannot be null');

            return new mindplot.model.RelationshipModel(sourceNodeId, targetNodeId);
        },

        /**
         * @param relationship
         */
        addRelationship:function (relationship) {
            this._relationships.push(relationship);
        },

        /**
         * @param relationship
         */
        deleteRelationship:function (relationship) {
            this._relationships.erase(relationship);
        },

        /**
         * @param id
         * @return the node with the respective id or null if not in the mindmap
         */
        findNodeById:function (id) {
            var result = null;
            for (var i = 0; i < this._branches.length; i++) {
                var branch = this._branches[i];
                result = branch.findNodeById(id);
                if (result) {
                    break;
                }
            }
            return result;
        }
    }
);

/**
 * @param mapId
 * @return an empty mindmap with central topic only
 */
mindplot.model.Mindmap.buildEmpty = function (mapId) {
    var result = new mindplot.model.Mindmap(mapId);
    var node = result.createNode(mindplot.model.INodeModel.CENTRAL_TOPIC_TYPE, 0);
    result.addBranch(node);
    return result;
};