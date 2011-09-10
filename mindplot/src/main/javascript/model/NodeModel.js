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

mindplot.model.NodeModel = new Class({
    Extends: mindplot.model.INodeModel,
    initialize:function(type, mindmap, id) {
        $assert(type, 'Node type can not be null');
        $assert(mindmap, 'mindmap can not be null');
        this._properties = {};

        this.parent(mindmap);
        this.setId(id);
        this.setType(type);
        this.areChildrenShrinked(false);
        this.setSize(50, 20);

        this._children = [];
        this._icons = [];
        this._links = [];
        this._notes = [];
    },

    putProperty : function(key, value) {
        $defined(key, 'key can not be null');
        this._properties[key] = value;
    },


    getProperties: function() {
        return this._properties;
    },

    getProperty : function(key) {
        $defined(key, 'key can not be null');
        var result = this._properties[key];
        return !$defined(result) ? null : result;
    },

    clone  : function() {
        var result = new mindplot.model.NodeModel(this.getType(), this._mindmap);
        result._children = this._children.each(function(node) {
            var cnode = node.clone();
            cnode._parent = result;
            return cnode;
        });

        result._properties = this._properties.clone();
        result._icons = this._icons.clone();
        result._links = this._links.clone();
        result._notes = this._notes.clone();
        return result;
    },

    addChildren : function() {
        $assert(child && child.isNodeModel(), 'Only NodeModel can be appended to Mindmap object');
        this._children.push(child);
        child._parent = this;
    },

    createLink  : function(url) {
        $assert(url, 'Link URL must be specified.');
        return new mindplot.model.LinkModel(url, this);
    },

    addLink  : function(link) {
        $assert(link && link.isLinkModel(), 'Only LinkModel can be appended to Mindmap object as links');
        this._links.push(link);
    },

    _removeLink  : function(link) {
        $assert(link && link.isLinkModel(), 'Only LinkModel can be appended to Mindmap object as links');
        this._links.erase(link);
    },

    createNote  : function(text) {
        $assert(text != null, 'note text must be specified.');
        return new mindplot.model.NoteModel(text, this);
    },

    addNote  : function(note) {
        $assert(note && note.isNoteModel(), 'Only NoteModel can be appended to Mindmap object as links');
        this._notes.push(note);
    },

    removeNote  : function(note) {
        $assert(note && note.isNoteModel(), 'Only NoteModel can be appended to Mindmap object as links');
        this._notes.erase(note);
    },

    createIcon  : function(iconType) {
        $assert(iconType, 'IconType must be specified.');
        return new mindplot.model.IconModel(iconType, this);
    },

    addIcon  : function(icon) {
        $assert(icon && icon.isIconModel(), 'Only IconModel can be appended to Mindmap object as icons');
        this._icons.push(icon);
    },

    removeIcon  : function(icon) {
        $assert(icon && icon.isIconModel(), 'Only IconModel can be appended to Mindmap object as icons');
        this._icons.erase(icon);
    },

    removeLastIcon  : function() {
        this._icons.pop();
    },

    appendChild  : function(child) {
        $assert(child && child.isNodeModel(), 'Only NodeModel can be appended to Mindmap object');
        this._children.push(child);
        child._parent = this;
    },

    removeChild  : function(child) {
        $assert(child && child.isNodeModel(), 'Only NodeModel can be appended to Mindmap object.');
        this._children.erase(child);
        child._parent = null;
    },

    getChildren  : function() {
        return this._children;
    },

    getIcons  : function() {
        return this._icons;
    },

    getLinks  : function() {
        return this._links;
    },

    getNotes  : function() {
        return this._notes;
    },

    getParent  : function() {
        return this._parent;
    },

    setParent  : function(parent) {
        $assert(parent != this, 'The same node can not be parent and child if itself.');
        this._parent = parent;
    },

    canBeConnected  : function(sourceModel, sourcePosition, targetTopicHeight) {
        $assert(sourceModel != this, 'The same node can not be parent and child if itself.');
        $assert(sourcePosition, 'childPosition can not be null.');
        $assert(targetTopicHeight, 'childrenWidth can not be null.');

        // Only can be connected if the node is in the left or rigth.
        var targetModel = this;
        var mindmap = targetModel.getMindmap();
        var targetPosition = targetModel.getPosition();
        var result = false;

        if (sourceModel.getType() == mindplot.model.INodeModel.MAIN_TOPIC_TYPE) {
            // Finally, check current node ubication.
            var targetTopicSize = targetModel.getSize();
            var yDistance = Math.abs(sourcePosition.y - targetPosition.y);
            var gap = 35 + targetTopicHeight / 2;
            if (targetModel.getChildren().length > 0) {
                gap += Math.abs(targetPosition.y - targetModel.getChildren()[0].getPosition().y);
            }

            if (yDistance <= gap) {
                // Circular connection ?
                if (!sourceModel._isChildNode(this)) {
                    var toleranceDistance = (targetTopicSize.width / 2) + targetTopicHeight;

                    var xDistance = sourcePosition.x - targetPosition.x;
                    var isTargetAtRightFromCentral = targetPosition.x >= 0;

                    if (isTargetAtRightFromCentral) {
                        if (xDistance >= -targetTopicSize.width / 2 && xDistance <= mindplot.model.INodeModel.MAIN_TOPIC_TO_MAIN_TOPIC_DISTANCE / 2 + (targetTopicSize.width / 2)) {
                            result = true;
                        }

                    } else {
                        if (xDistance <= targetTopicSize.width / 2 && Math.abs(xDistance) <= mindplot.model.INodeModel.MAIN_TOPIC_TO_MAIN_TOPIC_DISTANCE / 2 + (targetTopicSize.width / 2)) {
                            result = true;
                        }
                    }
                }
            }
        } else {
            throw "No implemented yet";
        }
        return result;
    },

    _isChildNode  : function(node) {
        var result = false;
        if (node == this) {
            result = true;
        } else {
            var children = this.getChildren();
            for (var i = 0; i < children.length; i++) {
                var child = children[i];
                result = child._isChildNode(node);
                if (result) {
                    break;
                }
            }
        }
        return result;

    },

    inspect  : function() {
        return '(type:' + this.getType() + ' , id: ' + this.getId() + ')';
    }
});