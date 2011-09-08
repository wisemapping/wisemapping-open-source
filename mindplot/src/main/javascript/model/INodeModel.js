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

mindplot.model.INodeModel = new Class({
    initialize: function(mindmap) {
        $assert(mindmap, 'mindmap can not be null');
        this._mindmap = mindmap;
    },

    getId  : function() {
        return this.getProperty('id');
    },

    setId  : function(id) {
        if ($defined(id) && id > mindplot.model.INodeModel._uuid) {
            mindplot.model.INodeModel._uuid = id;
        }
        if (!$defined(id)) {
            id = mindplot.model.INodeModel._nextUUID();
        }

        this.putProperty('id', id);

    },

    getType  : function() {
        return this.getProperty('type');
    },

    setType  : function(type) {
        this.putProperty('type', type);
    },

    setText  : function(text) {
        this.putProperty('text', text);
    },

    getText  : function() {
        return this.getProperty('text');
    },

    setPosition  : function(x, y) {
        this.putProperty('position', '{x:' + parseInt(x) + ',y:' + parseInt(y) + '}');
    },

    getPosition  : function() {
        var value = this.getProperty('position');
        var result = null;
        if (value != null) {
            result = eval("(" + value + ")");
        }
        return result;
    },

    setSize  : function(width, height) {
        this.putProperty('size', '{width:' + width + ',height:' + height + '}');
    },

    getSize  : function() {
        var value = this.getProperty('size');
        var result = null;
        if (value != null) {
            result = eval("(" + value + ")");
        }
        return result;
    },

    getMindmap  : function() {
        return this._mindmap;
    },

    disconnect  : function() {
        var mindmap = this.getMindmap();
        mindmap.disconnect(this);
    },

    getShapeType  : function() {
        return this.getProperty('shapeType');
    },

    setShapeType  : function(type) {
        this.putProperty('shapeType', type);
    },

    setOrder  : function(value) {
        this.putProperty('order', value);
    },

    getOrder  : function() {
        return this.getProperty('order');
    },

    setFontFamily  : function(fontFamily) {
        this.putProperty('fontFamily', fontFamily);
    },

    getFontFamily  : function() {
        return this.getProperty('fontFamily');
    },

    setFontStyle  : function(fontStyle) {
        this.putProperty('fontStyle', fontStyle);
    },

    getFontStyle  : function() {
        return this.getProperty('fontStyle');
    },

    setFontWeight  : function(weight) {
        this.putProperty('fontWeight', weight);
    },

    getFontWeight  : function() {
        return this.getProperty('fontWeight');
    },

    setFontColor  : function(color) {
        this.putProperty('fontColor', color);
    },

    getFontColor  : function() {
        return this.getProperty('fontColor');
    },

    setFontSize  : function(size) {
        this.putProperty('fontSize', size);
    },

    getFontSize  : function() {
        return this.getProperty('fontSize');
    },

    getBorderColor  : function() {
        return this.getProperty('borderColor');
    },

    setBorderColor  : function(color) {
        this.putProperty('borderColor', color);
    },

    getBackgroundColor  : function() {
        return this.getProperty('backgroundColor');
    },

    setBackgroundColor  : function(color) {
        this.putProperty('backgroundColor', color);
    },

    areChildrenShrinked  : function() {
        return this.getProperty('childrenShrinked');
    },

    setChildrenShrinked  : function(value) {
        this.putProperty('childrenShrinked', value);
    },

    setFinalPosition  : function(x, y) {
        $assert(x, "x coordinate must be defined");
        $assert(y, "y coordinate must be defined");

        this.putProperty('finalPosition', '{x:' + parseInt(x) + ',y:' + parseInt(y) + '}');
    },

    getFinalPosition  : function() {
        var value = this.getProperty('finalPosition');
        return eval("(" + value + ")");

    },

    isNodeModel  : function() {
        return true;
    },

    isConnected  : function() {
        return this.getParent() != null;
    },


    putProperty: function(key, value) {
        throw "Unsupported operation";
    },

    setProperty: function(key, value) {
        throw "Unsupported operation";
    },

    setParent  : function(parent) {
        throw "Unsupported operation";
    },

    deleteNode  : function() {
        throw "Unsupported operation";
    },

    createLink  : function(url) {
        throw "Unsupported operation";
    },

    addLink  : function(link) {
        throw "Unsupported operation";
    },

    createNote  : function(text) {
        throw "Unsupported operation";
    },

    addNote  : function(note) {
        throw "Unsupported operation";
    },

    removeNote  : function(note) {
        throw "Unsupported operation";
    },

    createIcon  : function(iconType) {
        throw "Unsupported operation";
    },

    addIcon  : function(icon) {
        throw "Unsupported operation";
    },

    removeIcon  : function(icon) {
        throw "Unsupported operation";
    },

    removeLastIcon  : function() {
        throw "Unsupported operation";
    },

    getChildren  : function() {
        throw "Unsupported operation";
    },

    getIcons  : function() {
        throw "Unsupported operation";
    },

    getLinks  : function() {
        throw "Unsupported operation";
    },

    getNotes  : function() {
        throw "Unsupported operation";
    },

    getParent  : function() {
        throw "Unsupported operation";
    },

    clone  : function() {
        throw "Unsupported operation";
    },

    inspect  : function() {
        return '(type:' + this.getType() + ' , id: ' + this.getId() + ')';
    }
});

mindplot.model.INodeModel.CENTRAL_TOPIC_TYPE = 'CentralTopic';
mindplot.model.INodeModel.MAIN_TOPIC_TYPE = 'MainTopic';

mindplot.model.INodeModel.SHAPE_TYPE_RECT = 'rectagle';
mindplot.model.INodeModel.SHAPE_TYPE_ROUNDED_RECT = 'rounded rectagle';
mindplot.model.INodeModel.SHAPE_TYPE_ELIPSE = 'elipse';
mindplot.model.INodeModel.SHAPE_TYPE_LINE = 'line';

mindplot.model.INodeModel.MAIN_TOPIC_TO_MAIN_TOPIC_DISTANCE = 220;

/**
 * @todo: This method must be implemented.
 */
mindplot.model.INodeModel._nextUUID = function() {
    if (!$defined(mindplot.model.INodeModel._uuid)) {
        mindplot.model.INodeModel._uuid = 0;
    }

    mindplot.model.INodeModel._uuid = mindplot.model.INodeModel._uuid + 1;
    return mindplot.model.INodeModel._uuid;
};
mindplot.model.INodeModel._uuid = 0;

