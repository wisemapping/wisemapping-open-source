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
        parent._appendChild(child);

        // Remove from the branch ...
        this.removeBranch(child);
    },

    disconnect : function(child) {
        var parent = child.getParent();
        $assert(child, 'Child can not be null.');
        $assert(parent, 'Child model seems to be already connected');

        parent._removeChild(child);

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

    addRelationship : function(relationship) {
        throw "Unsupported operation";
    },

    removeRelationship : function(relationship) {
        throw "Unsupported operation";
    },

    inspect : function() {
        var result = '';
        result = '{ ';

        var branches = this.getBranches();
        result = result + "version:" + this.getVersion();
        for (var i = 0; i < branches.length; i++) {
            var node = branches[i];
            if (i != 0) {
                result = result + ', ';
            }
            result = result + this._toString(node);
        }
        result = result + ' } ';
        return result;
    },

    _toString : function(node) {
        var result = node.inspect();
        var children = node.getChildren();

        for (var i = 0; i < children.length; i++) {
            var child = children[i];

            if (i == 0) {
                result = result + '-> {';
            } else {
                result = result + ', ';
            }

            result = result + this._toString(child);

            if (i == children.length - 1) {
                result = result + '}';
            }
        }

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
        sbranchs.forEach(function(snode) {
            var tnode = target.createNode(snode.getType(), snode.getId());
            snode.copyTo(tnode);
            target.addBranch(tnode);
        });
    }
});