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

mindplot.NodeGraph = new Class(/** @lends NodeGraph */{
    /**
     * @constructs
     * @param {mindplot.model.NodeModel} nodeModel
     * @param {Object<Number, String, Boolean>} options
     * @throws will throw an error if nodeModel is null or undefined
     */
    initialize: function (nodeModel, options) {
        $assert(nodeModel, "model can not be null");

        this._options = options;
        this._mouseEvents = true;
        this.setModel(nodeModel);
        this._onFocus = false;
        this._size = {width: 50, height: 20};
    },

    /** @return true if option is set to read-only */
    isReadOnly: function () {
        return this._options.readOnly;
    },

    /** @return model type */
    getType: function () {
        var model = this.getModel();
        return model.getType();
    },

    /**
     * @param {String} id
     * @throws will throw an error if the topic id is not a number
     */
    setId: function (id) {
        $assert(typeof  topic.getId() == "number", "id is not a number:" + id);
        this.getModel().setId(id);
    },

    _set2DElement: function (elem2d) {
        this._elem2d = elem2d;
    },

    /**
     * @return 2D element
     * @throws will throw an error if the element is null or undefined within node graph
     */
    get2DElement: function () {
        $assert(this._elem2d, 'NodeGraph has not been initialized properly');
        return this._elem2d;
    },

    /** @abstract */
    setPosition: function (point, fireEvent) {
        throw "Unsupported operation";
    },

    /** */
    addEvent: function (type, listener) {
        var elem = this.get2DElement();
        elem.addEvent(type, listener);
    },

    /** */
    removeEvent: function (type, listener) {
        var elem = this.get2DElement();
        elem.removeEvent(type, listener);
    },

    /** */
    fireEvent: function (type, event) {
        var elem = this.get2DElement();
        elem.trigger(type, event);
    },

    /** */
    setMouseEventsEnabled: function (isEnabled) {
        this._mouseEvents = isEnabled;
    },

    /** */
    isMouseEventsEnabled: function () {
        return this._mouseEvents;
    },

    /** @return {Object<Number>} size*/
    getSize: function () {
        return this._size;
    },

    /** @param {Object<Number>} size*/
    setSize: function (size) {
        this._size.width = parseInt(size.width);
        this._size.height = parseInt(size.height);
    },

    /**
     * @return {mindplot.model.NodeModel} the node model
     */
    getModel: function () {
        $assert(this._model, 'Model has not been initialized yet');
        return this._model;
    },

    /**
     * @param {mindplot.NodeModel} model the node model
     * @throws will throw an error if model is null or undefined
     */
    setModel: function (model) {
        $assert(model, 'Model can not be null');
        this._model = model;
    },

    /** */
    getId: function () {
        return this._model.getId();
    },

    /** */
    setOnFocus: function (focus) {
        if (this._onFocus != focus) {

            this._onFocus = focus;
            var outerShape = this.getOuterShape();
            if (focus) {
                outerShape.setFill(mindplot.Topic.OUTER_SHAPE_ATTRIBUTES_FOCUS.fillColor);
                outerShape.setOpacity(1);

            } else {
                outerShape.setFill(mindplot.Topic.OUTER_SHAPE_ATTRIBUTES.fillColor);
                outerShape.setOpacity(0);
            }
            this.setCursor('move');

            // In any case, always try to hide the editor ...
            this.closeEditors();

            // Fire event ...
            this.fireEvent(focus ? 'ontfocus' : 'ontblur', this);

        }
    },

    /** @return {Boolean} true if the node graph is on focus */
    isOnFocus: function () {
        return this._onFocus;
    },

    /** */
    dispose: function (workspace) {
        this.setOnFocus(false);
        workspace.removeChild(this);
    },

    /** */
    createDragNode: function (layoutManager) {
        var dragShape = this._buildDragShape();
        return new mindplot.DragTopic(dragShape, this, layoutManager);
    },

    _buildDragShape: function () {
        $assert(false, '_buildDragShape must be implemented by all nodes.');
    },

    /** */
    getPosition: function () {
        var model = this.getModel();
        return model.getPosition();
    }
});

/**
 * creates a new topic from the given node model
 * @memberof mindplot.Nodegraph
 * @param {mindplot.model.NodeModel} nodeModel
 * @param {Object} options
 * @throws will throw an error if nodeModel is null or undefined
 * @throws will throw an error if the nodeModel's type is null or undefined
 * @throws will throw an error if the node type cannot be recognized as either central or main
 * topic type
 * @return {mindplot.CentralTopic|mindplot.MainTopic} the new topic
 */
mindplot.NodeGraph.create = function (nodeModel, options) {
    $assert(nodeModel, 'Model can not be null');

    var type = nodeModel.getType();
    $assert(type, 'Node model type can not be null');

    var result;
    if (type == mindplot.model.INodeModel.CENTRAL_TOPIC_TYPE) {
        result = new mindplot.CentralTopic(nodeModel, options);
    } else if (type == mindplot.model.INodeModel.MAIN_TOPIC_TYPE) {
        result = new mindplot.MainTopic(nodeModel, options);
    } else {
        $assert(false, "unsupported node type:" + type);
    }

    return result;
};