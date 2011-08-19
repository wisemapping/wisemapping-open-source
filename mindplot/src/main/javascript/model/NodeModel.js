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
    initialize:function(type, mindmap, id) {
        $assert(type, 'Node type can not be null');
        $assert(mindmap, 'mindmap can not be null');

        this._order = null;
        this._type = type;
        this._children = [];
        this._icons = [];
        this._links = [];
        this._notes = [];
        this._size = {width:50,height:20};
        this._position = null;
        if ($defined(id)) {
            if (!$defined(mindplot.model.NodeModel._uuid) || id > mindplot.model.NodeModel._uuid) {
                mindplot.model.NodeModel._uuid = id;
            }
            this._id = id;
        } else {
            this._id = mindplot.model.NodeModel._nextUUID();
        }
        this._mindmap = mindmap;
        this._text = null;
        this._shapeType = null;
        this._fontFamily = null;
        this._fontSize = null;
        this._fontStyle = null;
        this._fontWeight = null;
        this._fontColor = null;
        this._borderColor = null;
        this._backgroundColor = null;
        this._areChildrenShrinked = false;
    },

    clone  : function() {
        var result = new mindplot.model.NodeModel(this._type, this._mindmap);
        result._order = this._order;
        result._type = this._type;
        result._children = this._children.map(function(item, index) {
            var model = item.clone();
            model._parent = result;
            return model;
        });


        result._icons = this._icons;
        result._links = this._links;
        result._notes = this._notes;
        result._size = this._size;
        result._position = this._position;
        result._id = this._id;
        result._mindmap = this._mindmap;
        result._text = this._text;
        result._shapeType = this._shapeType;
        result._fontFamily = this._fontFamily;
        result._fontSize = this._fontSize;
        result._fontStyle = this._fontStyle;
        result._fontWeight = this._fontWeight;
        result._fontColor = this._fontColor;
        result._borderColor = this._borderColor;
        result._backgroundColor = this._backgroundColor;
        result._areChildrenShrinked = this._areChildrenShrinked;
        return result;
    },

    areChildrenShrinked  : function() {
        return this._areChildrenShrinked;
    },

    setChildrenShrinked  : function(value) {
        this._areChildrenShrinked = value;
    },

    getId  : function() {
        return this._id;
    },


    setId  : function(id) {
        this._id = id;
        if (mindplot.model.NodeModel._uuid < id) {
            mindplot.model.NodeModel._uuid = id;
        }
    },

    getType  : function() {
        return this._type;
    },

    setText  : function(text) {
        this._text = text;
    },

    getText  : function() {
        return this._text;
    },

    isNodeModel  : function() {
        return true;
    },

    isConnected  : function() {
        return this._parent != null;
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

    _removeNote  : function(note) {
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

    _removeIcon  : function(icon) {
        $assert(icon && icon.isIconModel(), 'Only IconModel can be appended to Mindmap object as icons');
        this._icons.erase(icon);
    },

    removeLastIcon  : function() {
        this._icons.pop();
    },

    _appendChild  : function(child) {
        $assert(child && child.isNodeModel(), 'Only NodeModel can be appended to Mindmap object');
        this._children.push(child);
        child._parent = this;
    },

    _removeChild  : function(child) {
        $assert(child && child.isNodeModel(), 'Only NodeModel can be appended to Mindmap object.');
        this._children.erase(child);
        child._parent = null;
    },

    setPosition  : function(x, y) {
        if (!$defined(this._position)) {
            this._position = new core.Point();
        }
        this._position.x = parseInt(x);
        this._position.y = parseInt(y);
    },

    getPosition  : function() {
        return this._position;
    },

    setFinalPosition  : function(x, y) {
        $assert(x, "x coordinate must be defined");
        $assert(y, "y coordinate must be defined");

        if (!$defined(this._finalPosition)) {
            this._finalPosition = new core.Point();
        }
        this._finalPosition.x = parseInt(x);
        this._finalPosition.y = parseInt(y);
    },

    getFinalPosition  : function() {
        return this._finalPosition;
    },

    setSize  : function(width, height) {
        this._size.width = width;
        this._size.height = height;
    },

    getSize  : function() {
        return {width:this._size.width,height:this._size.height};
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

    getMindmap  : function() {
        return this._mindmap;
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

        if (sourceModel.getType() == mindplot.model.NodeModel.MAIN_TOPIC_TYPE) {
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
                        if (xDistance >= -targetTopicSize.width / 2 && xDistance <= mindplot.model.NodeModel.MAIN_TOPIC_TO_MAIN_TOPIC_DISTANCE / 2 + (targetTopicSize.width / 2)) {
                            result = true;
                        }

                    } else {
                        if (xDistance <= targetTopicSize.width / 2 && Math.abs(xDistance) <= mindplot.model.NodeModel.MAIN_TOPIC_TO_MAIN_TOPIC_DISTANCE / 2 + (targetTopicSize.width / 2)) {
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

    connectTo  : function(parent) {
        var mindmap = this.getMindmap();
        mindmap.connect(parent, this);
        this._parent = parent;
    },

    disconnect  : function() {
        var mindmap = this.getMindmap();
        mindmap.disconnect(this);
    },

    getOrder  : function() {
        return this._order;
    },

    getShapeType  : function() {
        return this._shapeType;
    },

    setShapeType  : function(type) {
        this._shapeType = type;
    },

    setOrder  : function(value) {
        this._order = value;
    },

    setFontFamily  : function(value) {
        this._fontFamily = value;
    },

    getOrder  : function() {
        return this._order;
    },

    getFontFamily  : function() {
        return this._fontFamily;
    },

    setFontStyle  : function(value) {
        this._fontStyle = value;
    },

    getFontStyle  : function() {
        return this._fontStyle;
    },

    setFontWeight  : function(value) {
        this._fontWeight = value;
    },

    getFontWeight  : function() {
        return this._fontWeight;
    },

    setFontColor  : function(value) {
        this._fontColor = value;
    },

    getFontColor  : function() {
        return this._fontColor;
    },

    setFontSize  : function(value) {
        this._fontSize = value;
    },

    getFontSize  : function() {
        return this._fontSize;
    },

    getBorderColor  : function() {
        return this._borderColor;
    },

    setBorderColor  : function(color) {
        this._borderColor = color;
    },

    getBackgroundColor  : function() {
        return this._backgroundColor;
    },

    setBackgroundColor  : function(color) {
        this._backgroundColor = color;
    },

    deleteNode  : function() {
        var mindmap = this._mindmap;

        // if it has children nodes, Their must be disconnected.
        var lenght = this._children;
        for (var i = 0; i < lenght; i++) {
            var child = this._children[i];
            mindmap.disconnect(child);
        }

        var parent = this._parent;
        if ($defined(parent)) {
            // if it is connected, I must remove it from the parent..
            mindmap.disconnect(this);
        }

        // It's an isolated node. It must be a hole branch ...
        var branches = mindmap.getBranches();
        branches.erase(this);

    },

   inspect  : function() {
        return '(type:' + this.getType() + ' , id: ' + this.getId() + ')';
    }
});

mindplot.model.NodeModel.CENTRAL_TOPIC_TYPE = 'CentralTopic';
mindplot.model.NodeModel.MAIN_TOPIC_TYPE = 'MainTopic';
mindplot.model.NodeModel.DRAGGED_TOPIC_TYPE = 'DraggedTopic';

mindplot.model.NodeModel.SHAPE_TYPE_RECT = 'rectagle';
mindplot.model.NodeModel.SHAPE_TYPE_ROUNDED_RECT = 'rounded rectagle';
mindplot.model.NodeModel.SHAPE_TYPE_ELIPSE = 'elipse';
mindplot.model.NodeModel.SHAPE_TYPE_LINE = 'line';

mindplot.model.NodeModel.MAIN_TOPIC_TO_MAIN_TOPIC_DISTANCE = 220;

/**
 * @todo: This method must be implemented.
 */
mindplot.model.NodeModel._nextUUID = function() {
    if (!$defined(this._uuid)) {
        this._uuid = 0;
    }

    this._uuid = this._uuid + 1;
    return this._uuid;
}

