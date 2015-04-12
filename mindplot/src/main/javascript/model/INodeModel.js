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

mindplot.model.INodeModel = new Class(/** @lends INodeModel */{
    /**
     * @constructs
     * @param mindmap
     */
    initialize: function(mindmap) {
        $assert(mindmap && mindmap.getBranches, 'mindmap can not be null');
        this._mindmap = mindmap;
    },

    /** */
    getId  : function() {
        return this.getProperty('id');
    },

    /** */
    setId  : function(id) {
        if ($defined(id) && id > mindplot.model.INodeModel._uuid) {
            mindplot.model.INodeModel._uuid = id;
        }
        if (!$defined(id)) {
            id = mindplot.model.INodeModel._nextUUID();
        }

        this.putProperty('id', id);

    },

    /** */
    getType  : function() {
        return this.getProperty('type');
    },

    /** */
    setType  : function(type) {
        this.putProperty('type', type);
    },

    /** */
    setText  : function(text) {
        this.putProperty('text', text);
    },

    /** */
    getText  : function() {
        return this.getProperty('text');
    },

    /** */
    setPosition  : function(x, y) {
        $assert(!isNaN(parseInt(x)), "x position is not valid:" + x);
        $assert(!isNaN(parseInt(y)), "y position is not valid:" + y);
        this.putProperty('position', '{x:' + parseInt(x) + ',y:' + parseInt(y) + '}');
    },

    /** */
    getPosition  : function() {
        var value = this.getProperty('position');
        var result = null;
        if (value != null) {
            result = eval("(" + value + ")");
        }
        return result;
    },

    /** */
    setImageSize  : function(width, height) {
        this.putProperty('imageSize', '{width:' + width + ',height:' + height + '}');
    },

    /** */
    getImageSize  : function() {
        var value = this.getProperty('imageSize');
        var result = null;
        if (value != null) {
            result = eval("(" + value + ")");
        }
        return result;
    },

    /** */
    setImageUrl:function(url) {
        this.putProperty('imageUrl', url);

    },

    /** */
    getMetadata:function() {
        return this.getProperty('metadata');
    },

    /** */
    setMetadata:function(json) {
        this.putProperty('metadata', json);

    },

    /** */
    getImageUrl:function() {
        return this.getProperty('imageUrl');
    },

    /** */
    getMindmap  : function() {
        return this._mindmap;
    },

    /**
     * lets the mindmap handle the disconnect node operation
     * @see mindplot.model.IMindmap.disconnect
     */
    disconnect  : function() {
        var mindmap = this.getMindmap();
        mindmap.disconnect(this);
    },

    /** */
    getShapeType  : function() {
        return this.getProperty('shapeType');
    },

    /** */
    setShapeType  : function(type) {
        this.putProperty('shapeType', type);
    },

    /** */
    setOrder  : function(value) {
        $assert(typeof value === 'number' && isFinite(value) || value == null, "Order must be null or a number");
        this.putProperty('order', value);
    },

    /** */
    getOrder  : function() {
        return this.getProperty('order');
    },

    /** */
    setFontFamily  : function(fontFamily) {
        this.putProperty('fontFamily', fontFamily);
    },

    /** */
    getFontFamily  : function() {
        return this.getProperty('fontFamily');
    },

    /** */
    setFontStyle  : function(fontStyle) {
        this.putProperty('fontStyle', fontStyle);
    },

    /** */
    getFontStyle  : function() {
        return this.getProperty('fontStyle');
    },

    /** */
    setFontWeight  : function(weight) {
        this.putProperty('fontWeight', weight);
    },

    /** */
    getFontWeight  : function() {
        return this.getProperty('fontWeight');
    },

    /** */
    setFontColor  : function(color) {
        this.putProperty('fontColor', color);
    },

    /** */
    getFontColor  : function() {
        return this.getProperty('fontColor');
    },

    /** */
    setFontSize  : function(size) {
        this.putProperty('fontSize', size);
    },

    /** */
    getFontSize  : function() {
        return this.getProperty('fontSize');
    },

    /** */
    getBorderColor  : function() {
        return this.getProperty('borderColor');
    },

    /** */
    setBorderColor  : function(color) {
        this.putProperty('borderColor', color);
    },

    /** */
    getBackgroundColor  : function() {
        return this.getProperty('backgroundColor');
    },

    /** */
    setBackgroundColor  : function(color) {
        this.putProperty('backgroundColor', color);
    },

    /** */
    areChildrenShrunken  : function() {
        var result = this.getProperty('shrunken');
        return $defined(result) ? result : false;
    },

    /**
     * @return {Boolean} true if the children nodes are hidden by the shrink option
     */
    setChildrenShrunken  : function(value) {
        this.putProperty('shrunken', value);
    },

    /**
     * @return {Boolean} true
     */
    isNodeModel  : function() {
        return true;
    },

    /**
     * @return {Boolean} true if the node model has a parent assigned to it
     */
    isConnected  : function() {
        return this.getParent() != null;
    },

    /** @abstract */
    append : function(node) {
        throw "Unsupported operation";
    },

    /**
     * lets the mindmap handle the connect node operation
     * @throws will throw an error if parent is null or undefined
     * @see mindplot.model.IMindmap.connect
     */
    connectTo  : function(parent) {
        $assert(parent, "parent can not be null");
        var mindmap = this.getMindmap();
        mindmap.connect(parent, this);
    },

    /**
     * @param target
     * @return target
     */
    copyTo : function(target) {
        var source = this;
        // Copy properties ...
        var keys = source.getPropertiesKeys();
        _.each(keys, function(key) {
            var value = source.getProperty(key);
            target.putProperty(key, value);
        });

        // Copy children ...
        var children = this.getChildren();
        var tmindmap = target.getMindmap();

        _.each(function(children, snode) {
            var tnode = tmindmap.createNode(snode.getType(), snode.getId());
            snode.copyTo(tnode);
            target.append(tnode);
        });

        return target;
    },

    /**
     * lets parent handle the delete node operation, or, if none defined, calls the mindmap to 
     * remove the respective branch
     */
    deleteNode  : function() {
        var mindmap = this.getMindmap();

//        console.log("Before:" + mindmap.inspect());
        var parent = this.getParent();
        if ($defined(parent)) {
            parent.removeChild(this);
        } else {
            // If it has not parent, it must be an isolate topic ...
            mindmap.removeBranch(this);
        }
        // It's an isolated node. It must be a hole branch ...
//        console.log("After:" + mindmap.inspect());
    },

    /** @abstract */
    getPropertiesKeys : function() {
        throw "Unsupported operation";
    },

    /** @abstract */
    putProperty: function(key, value) {
        throw "Unsupported operation";
    },

    /** @abstract */
    setParent  : function(parent) {
        throw "Unsupported operation";
    },

    /** @abstract */
    getChildren  : function() {
        throw "Unsupported operation";
    },

    /** @abstract */
    getParent  : function() {
        throw "Unsupported operation";
    },

    /** @abstract */
    clone  : function() {
        throw "Unsupported operation";
    },

    /** */
    inspect  : function() {
        var result = '{ type: ' + this.getType() +
            ' , id: ' + this.getId() +
            ' , text: ' + this.getText();

        var children = this.getChildren();
        if (children.length > 0) {
            result = result + ", children: {(size:" + children.length;
            _.each(children, function(node) {
                result = result + "=> (";
                var keys = node.getPropertiesKeys();
                _.each(keys, function(key) {
                    var value = node.getProperty(key);
                    result = result + key + ":" + value + ",";
                });
                result = result + "}"
            });
        }

        result = result + ' }';
        return result;
    },

    /** @abstract */
    removeChild : function(child) {
        throw "Unsupported operation";

    }
});

/**
 * @enum {String}
 */
mindplot.model.TopicShape =
{
    RECTANGLE : 'rectagle',
    ROUNDED_RECT :  'rounded rectagle',
    ELLIPSE : 'elipse',
    LINE : 'line',
    IMAGE : 'image'
};

/**
 * @constant
 * @type {String}
 * @default
 */
mindplot.model.INodeModel.CENTRAL_TOPIC_TYPE = 'CentralTopic';
/**
 * @constant
 * @type {String}
 * @default
 */
mindplot.model.INodeModel.MAIN_TOPIC_TYPE = 'MainTopic';

/**
 * @constant
 * @type {Number}
 * @default
 */
mindplot.model.INodeModel.MAIN_TOPIC_TO_MAIN_TOPIC_DISTANCE = 220;

/**
 * @todo: This method must be implemented. (unascribed)
 */
mindplot.model.INodeModel._nextUUID = function() {
    if (!$defined(mindplot.model.INodeModel._uuid)) {
        mindplot.model.INodeModel._uuid = 0;
    }

    mindplot.model.INodeModel._uuid = mindplot.model.INodeModel._uuid + 1;
    return mindplot.model.INodeModel._uuid;
};
mindplot.model.INodeModel._uuid = 0;

