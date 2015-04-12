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

mindplot.model.NodeModel = new Class(/** @lends NodeModel */{
    Extends:mindplot.model.INodeModel,
    /**
     * @constructs
     * @param {String} type node type
     * @param mindmap
     * @param id
     */
    initialize:function (type, mindmap, id) {
        $assert(type, 'Node type can not be null');
        $assert(mindmap, 'mindmap can not be null');
        this._properties = {};

        this.parent(mindmap);
        this.setId(id);
        this.setType(type);
        this.areChildrenShrunken(false);

        this._children = [];
        this._feature = [];
    },

    /** 
     * @param type
     * @param attributes
     * @return {mindplot.model.FeatureModel} the created feature model
     */
    createFeature:function (type, attributes) {
        return mindplot.TopicFeature.createModel(type, attributes);
    },

    /** 
     * @param feature
     * @throws will throw an error if feature is null or undefined
     */
    addFeature:function (feature) {
        $assert(feature, 'feature can not be null');
        this._feature.push(feature);
    },

    /** */
    getFeatures:function () {
        return this._feature;
    },

    /** 
     * @param feature
     * @throws will throw an error if feature is null or undefined
     * @throws will throw an error if the feature could not be removed
     */
    removeFeature:function (feature) {
        $assert(feature, 'feature can not be null');
        var size = this._feature.length;
        this._feature = this._feature.filter(function (f) {
            return feature.getId() != f.getId();
        });
        $assert(size - 1 == this._feature.length, 'Could not be removed ...');

    },

    /** 
     * @param {String} type the feature type, e.g. icon or link
     * @throws will throw an error if type is null or undefined
     */
    findFeatureByType:function (type) {
        $assert(type, 'type can not be null');
        return this._feature.filter(function (feature) {
            return feature.getType() == type;
        });
    },

    /** 
     * @param {String} id
     * @throws will throw an error if id is null or undefined
     * @throws will throw an error if feature could not be found
     * @return the feature with the given id
     */
    findFeatureById:function (id) {
        $assert($defined(id), 'id can not be null');
        var result = this._feature.filter(function (feature) {
            return feature.getId() == id;
        });
        $assert(result.length == 1, "Feature could not be found:" + id);
        return result[0];
    },

    /** */
    getPropertiesKeys:function () {
        return Object.keys(this._properties);
    },

    /** 
     * @param key
     * @param value
     * @throws will throw an error if key is null or undefined
     */
    putProperty:function (key, value) {
        $defined(key, 'key can not be null');
        this._properties[key] = value;
    },

    /** */
    getProperties:function () {
        return this._properties;
    },

    /** */
    getProperty:function (key) {
        $defined(key, 'key can not be null');
        var result = this._properties[key];
        return !$defined(result) ? null : result;
    },

    /** 
     * @return {mindplot.model.NodeModel} an identical clone of the NodeModel
     */
    clone:function () {
        var result = new mindplot.model.NodeModel(this.getType(), this._mindmap);
        result._children = this._children.map(function (node) {
            var cnode = node.clone();
            cnode._parent = result;
            return cnode;
        });

        result._properties = Object.clone(this._properties);
        result._feature = this._feature.clone();
        return result;
    },

    /**
     * Similar to clone, assign new id to the elements ...
     * @return {mindplot.model.NodeModel}
     */
    deepCopy:function () {
        var result = new mindplot.model.NodeModel(this.getType(), this._mindmap);
        result._children = this._children.map(function (node) {
            var cnode = node.deepCopy();
            cnode._parent = result;
            return cnode;
        });

        var id = result.getId();
        result._properties = Object.clone(this._properties);
        result.setId(id);

        result._feature = this._feature.clone();
        return result;
    },

    /** 
     * @param {mindplot.model.NodeModel} child 
     * @throws will throw an error if child is null, undefined or not a NodeModel object
     */
    append:function (child) {
        $assert(child && child.isNodeModel(), 'Only NodeModel can be appended to Mindmap object');
        this._children.push(child);
        child._parent = this;
    },

    /** 
     * @param {mindplot.model.NodeModel} child
     * @throws will throw an error if child is null, undefined or not a NodeModel object
     */
    removeChild:function (child) {
        $assert(child && child.isNodeModel(), 'Only NodeModel can be appended to Mindmap object.');
        this._children.erase(child);
        child._parent = null;
    },

    /** */
    getChildren:function () {
        return this._children;
    },

    /** */
    getParent:function () {
        return this._parent;
    },

    /** */
    setParent:function (parent) {
        $assert(parent != this, 'The same node can not be parent and child if itself.');
        this._parent = parent;
    },

    _isChildNode:function (node) {
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

    /** 
     * @id
     * @return {mindplot.model.NodeModel} the node with the respective id
     */
    findNodeById:function (id) {
        var result = null;
        if (this.getId() == id) {
            result = this;
        } else {
            var children = this.getChildren();
            for (var i = 0; i < children.length; i++) {
                var child = children[i];
                result = child.findNodeById(id);
                if (result) {
                    break;
                }
            }
        }
        return result;
    }
});