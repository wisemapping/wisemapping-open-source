/*
 *    Copyright [2012] [wisemapping]
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
mindplot.model.IMindmap = new Class({
    initialize : function() {
        throw "Unsupported operation";
    },

    getCentralTopic : function() {
        return this.getBranches()[0];
    },

    getDescription : function() {
        throw "Unsupported operation";
    },

    setDescription : function(value) {
        throw "Unsupported operation";
    },

    getId : function() {
        throw "Unsupported operation";
    },

    setId : function(id) {
        throw "Unsupported operation";
    },

    getVersion : function() {
        throw "Unsupported operation";
    },

    setVersion : function(version) {
        throw "Unsupported operation";
    },

    addBranch : function(nodeModel) {
        throw "Unsupported operation";
    },

    getBranches : function() {
        throw "Unsupported operation";
    },

    removeBranch : function(node) {
        throw "Unsupported operation";
    },

    getRelationships : function() {
        throw "Unsupported operation";
    },

    connect : function(parent, child) {
        // Child already has a parent ?
        $assert(!child.getParent(), 'Child model seems to be already connected');

        //  Connect node...
        parent.appendChild(child);

        // Remove from the branch ...
        this.removeBranch(child);
    },

    disconnect : function(child) {
        var parent = child.getParent();
        $assert(child, 'Child can not be null.');
        $assert(parent, 'Child model seems to be already connected');

        parent.removeChild(child);
        this.addBranch(child);
    },

    hasAlreadyAdded : function(node) {
        throw "Unsupported operation";
    },

    createNode : function(type, id) {
        throw "Unsupported operation";
    },

    createRelationship : function(fromNode, toNode) {
        throw "Unsupported operation";
    },

    addRelationship : function(rel) {
        throw "Unsupported operation";
    },

    deleteRelationship : function(relationship) {
        throw "Unsupported operation";
    },

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

    copyTo : function(target) {
        var source = this;
        var version = source.getVersion();
        target.setVersion(version);

        var desc = this.getDescription();
        target.setDescription(desc);

        // Then the rest of the branches ...
        var sbranchs = source.getBranches();
        sbranchs.each(function(snode) {
            var tnode = target.createNode(snode.getType(), snode.getId());
            snode.copyTo(tnode);
            target.addBranch(tnode);
        });
    }
});