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

mindplot.model.IMindmap = new Class(/** @lends IMindmap */{
    /**
     * @constructs
     * @abstract
     */
    initialize : function() {
        throw "Unsupported operation";
    },

    /** */
    getCentralTopic : function() {
        return this.getBranches()[0];
    },

    /** @abstract */
    getDescription : function() {
        throw "Unsupported operation";
    },

    /** @abstract */
    setDescription : function(value) {
        throw "Unsupported operation";
    },

    /** @abstract */
    getId : function() {
        throw "Unsupported operation";
    },

    /** @abstract */
    setId : function(id) {
        throw "Unsupported operation";
    },

    /** @abstract */
    getVersion : function() {
        throw "Unsupported operation";
    },

    /** @abstract */
    setVersion : function(version) {
        throw "Unsupported operation";
    },

    /** @abstract */
    addBranch : function(nodeModel) {
        throw "Unsupported operation";
    },

    /** @abstract */
    getBranches : function() {
        throw "Unsupported operation";
    },

    /** @abstract */
    removeBranch : function(node) {
        throw "Unsupported operation";
    },

    /** @abstract */
    getRelationships : function() {
        throw "Unsupported operation";
    },

    /**
     * @param parent
     * @param child
     * @throws will throw an error if child already has a connection to a parent node
     */
    connect : function(parent, child) {
        // Child already has a parent ?
        $assert(!child.getParent(), 'Child model seems to be already connected');

        //  Connect node...
        parent.append(child);

        // Remove from the branch ...
        this.removeBranch(child);
    },

    /**
     * @param child
     * @throws will throw an error if child is null or undefined
     * @throws will throw an error if child's parent cannot be found
     */
    disconnect : function(child) {
        var parent = child.getParent();
        $assert(child, 'Child can not be null.');
        $assert(parent, 'Child model seems to be already connected');

        parent.removeChild(child);
        this.addBranch(child);
    },

    /** @abstract */
    hasAlreadyAdded : function(node) {
        throw "Unsupported operation";
    },

    /** @abstract */
    createNode : function(type, id) {
        throw "Unsupported operation";
    },

    /** @abstract */
    createRelationship : function(fromNode, toNode) {
        throw "Unsupported operation";
    },

    /** @abstract */
    addRelationship : function(rel) {
        throw "Unsupported operation";
    },

    /** @abstract */
    deleteRelationship : function(relationship) {
        throw "Unsupported operation";
    },

    /** */
    inspect : function() {
        var result = '';
        result = '{ ';

        var branches = this.getBranches();
        result = result + "version:" + this.getVersion();
        result = result + " , [";

        for (var i = 0; i < branches.length; i++) {
            var node = branches[i];
            if (i != 0) {
                result = result + ',\n ';
            }
            result = result + "(" + i + ") =>" + node.inspect();
        }
        result = result + "]";

        result = result + ' } ';
        return result;
    },

    /**
     * @param target
     */
    copyTo : function(target) {
        var source = this;
        var version = source.getVersion();
        target.setVersion(version);

        var desc = this.getDescription();
        target.setDescription(desc);

        // Then the rest of the branches ...
        var sbranchs = source.getBranches();
        _.each(sbranchs, function(snode) {
            var tnode = target.createNode(snode.getType(), snode.getId());
            snode.copyTo(tnode);
            target.addBranch(tnode);
        });
    }
});