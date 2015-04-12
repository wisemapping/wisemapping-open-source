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

mindplot.Topic = new Class(/** @lends Topic */{
    Extends: mindplot.NodeGraph,
    /**
     * @extends mindplot.NodeGraph
     * @constructs
     * @param model
     * @param options
     */
    initialize: function (model, options) {
        this.parent(model, options);
        this._children = [];
        this._parent = null;
        this._relationships = [];
        this._isInWorkspace = false;
        this._buildTopicShape();

        // Position a topic ....
        var pos = model.getPosition();
        if (pos != null && this.isCentralTopic()) {
            this.setPosition(pos);
        }

        // Register events for the topic ...
        if (!this.isReadOnly()) {
            this._registerEvents();
        }
    },

    _registerEvents: function () {

        this.setMouseEventsEnabled(true);

        // Prevent click on the topics being propagated ...
        this.addEvent('click', function (event) {
            event.stopPropagation();
        });
        var me = this;
        this.addEvent('dblclick', function (event) {
            me._getTopicEventDispatcher().show(me);
            event.stopPropagation();
        });
    },

    /**
     * @param {String} type the topic shape type
     * @see {@link mindplot.model.INodeModel}
     */
    setShapeType: function (type) {
        this._setShapeType(type, true);
    },

    /** @return {mindplot.Topic} parent topic */
    getParent: function () {
        return this._parent;
    },

    _setShapeType: function (type, updateModel) {
        // Remove inner shape figure ...
        var model = this.getModel();
        if ($defined(updateModel) && updateModel) {
            model.setShapeType(type);
        }

        var oldInnerShape = this.getInnerShape();
        if (oldInnerShape != null) {

            this._removeInnerShape();

            // Create a new one ...
            var innerShape = this.getInnerShape();

            // Update figure size ...
            var size = this.getSize();
            this.setSize(size, true);

            var group = this.get2DElement();
            group.append(innerShape);

            // Move text to the front ...
            var text = this.getTextShape();
            text.moveToFront();

            //Move iconGroup to front ...
            var iconGroup = this.getIconGroup();
            if ($defined(iconGroup)) {
                iconGroup.moveToFront();
            }

            //Move connector to front
            var connector = this.getShrinkConnector();
            if ($defined(connector)) {
                connector.moveToFront();
            }
        }

    },

    /** @return {String} topic shape type */
    getShapeType: function () {
        var model = this.getModel();
        var result = model.getShapeType();
        if (!$defined(result)) {
            result = mindplot.TopicStyle.defaultShapeType(this);
        }
        return result;
    },

    _removeInnerShape: function () {
        var group = this.get2DElement();
        var innerShape = this.getInnerShape();
        group.removeChild(innerShape);
        this._innerShape = null;
        return innerShape;
    },

    /** @return {web2d.Line|web2d.Rect|web2d.Image} inner shape of the topic */
    getInnerShape: function () {
        if (!$defined(this._innerShape)) {
            // Create inner box.
            this._innerShape = this._buildShape(mindplot.Topic.INNER_RECT_ATTRIBUTES, this.getShapeType());

            // Update bgcolor ...
            var bgColor = this.getBackgroundColor();
            this._setBackgroundColor(bgColor, false);

            // Update border color ...
            var brColor = this.getBorderColor();
            this._setBorderColor(brColor, false);

            // Define the pointer ...
            if (!this.isCentralTopic() && !this.isReadOnly()) {
                this._innerShape.setCursor('move');
            } else {
                this._innerShape.setCursor('default');
            }

        }
        return this._innerShape;
    },

    _buildShape: function (attributes, shapeType) {
        $assert(attributes, "attributes can not be null");
        $assert(shapeType, "shapeType can not be null");

        var result;
        if (shapeType == mindplot.model.TopicShape.RECTANGLE) {
            result = new web2d.Rect(0, attributes);
        } else if (shapeType == mindplot.model.TopicShape.IMAGE) {
            var model = this.getModel();
            var url = model.getImageUrl();
            var size = model.getImageSize();

            result = new web2d.Image();
            result.setHref(url);
            result.setSize(size.width, size.height);

            result.getSize = function () {
                return model.getImageSize();
            };

            result.setPosition = function () {
            };
        }
        else if (shapeType == mindplot.model.TopicShape.ELLIPSE) {
            result = new web2d.Rect(0.9, attributes);
        }
        else if (shapeType == mindplot.model.TopicShape.ROUNDED_RECT) {
            result = new web2d.Rect(0.3, attributes);
        }
        else if (shapeType == mindplot.model.TopicShape.LINE) {
            result = new web2d.Line({strokeColor: "#495879", strokeWidth: 1});
            result.setSize = function (width, height) {
                this.size = {width: width, height: height};
                result.setFrom(0, height);
                result.setTo(width, height);

                // Lines will have the same color of the default connection lines...
                var stokeColor = mindplot.ConnectionLine.getStrokeColor();
                result.setStroke(1, 'solid', stokeColor);
            };

            result.getSize = function () {
                return this.size;
            };

            result.setPosition = function () {
            };

            result.setFill = function () {

            };

            result.setStroke = function () {

            };
        }
        else {
            $assert(false, "Unsupported figure shapeType:" + shapeType);
        }
        result.setPosition(0, 0);
        return result;
    },

    /** @param {String} type the cursor type, either 'pointer', 'default' or 'move' */
    setCursor: function (type) {
        var innerShape = this.getInnerShape();
        innerShape.setCursor(type);

        var outerShape = this.getOuterShape();
        outerShape.setCursor(type);

        var textShape = this.getTextShape();
        textShape.setCursor(type);
    },

    /** @return outer shape */
    getOuterShape: function () {
        if (!$defined(this._outerShape)) {
            var rect = this._buildShape(mindplot.Topic.OUTER_SHAPE_ATTRIBUTES, mindplot.model.TopicShape.ROUNDED_RECT);
            rect.setPosition(-2, -3);
            rect.setOpacity(0);
            this._outerShape = rect;
        }

        return this._outerShape;
    },

    /** @return text shape */
    getTextShape: function () {
        if (!$defined(this._text)) {
            this._text = this._buildTextShape(false);

            // Set Text ...
            var text = this.getText();
            this._setText(text, false);
        }

        return this._text;
    },

    /** @return icon group */
    getOrBuildIconGroup: function () {
        if (!$defined(this._iconsGroup)) {
            this._iconsGroup = this._buildIconGroup();
            var group = this.get2DElement();
            group.append(this._iconsGroup.getNativeElement());
            this._iconsGroup.moveToFront();
        }
        return this._iconsGroup;
    },

    /** */
    getIconGroup: function () {
        return this._iconsGroup;
    },

    _buildIconGroup: function () {
        var textHeight = this.getTextShape().getFontHeight();
        var result = new mindplot.IconGroup(this.getId(), textHeight);
        var padding = mindplot.TopicStyle.getInnerPadding(this);
        result.setPosition(padding, padding);

        // Load topic features ...
        var model = this.getModel();
        var featuresModel = model.getFeatures();
        for (var i = 0; i < featuresModel.length; i++) {
            var featureModel = featuresModel[i];
            var icon = mindplot.TopicFeature.createIcon(this, featureModel, this.isReadOnly());
            result.addIcon(icon, featureModel.getType() == mindplot.TopicFeature.Icon.id && !this.isReadOnly());
        }

        return result;
    },

    /**
     * assigns the new feature model to the topic's node model and adds the respective icon
     * @param {mindplot.model.FeatureModel} featureModel
     * @return {mindplot.Icon} the icon corresponding to the feature model
     */
    addFeature: function (featureModel) {
        var iconGroup = this.getOrBuildIconGroup();
        this.closeEditors();

        // Update model ...
        var model = this.getModel();
        model.addFeature(featureModel);

        var result = mindplot.TopicFeature.createIcon(this, featureModel, this.isReadOnly());
        iconGroup.addIcon(result, featureModel.getType() == mindplot.TopicFeature.Icon.id && !this.isReadOnly());

        this._adjustShapes();
        return result;
    },

    /** */
    findFeatureById: function (id) {
        var model = this.getModel();
        return model.findFeatureById(id);
    },

    /** */
    removeFeature: function (featureModel) {
        $assert(featureModel, "featureModel could not be null");

        //Removing the icon from MODEL
        var model = this.getModel();
        model.removeFeature(featureModel);

        //Removing the icon from UI
        var iconGroup = this.getIconGroup();
        if ($defined(iconGroup)) {
            iconGroup.removeIconByModel(featureModel);
        }
        this._adjustShapes();
    },

    /** */
    addRelationship: function (relationship) {
        this._relationships.push(relationship);
    },

    /** */
    deleteRelationship: function (relationship) {
        this._relationships.erase(relationship);
    },

    /** */
    getRelationships: function () {
        return this._relationships;
    },

    _buildTextShape: function (readOnly) {
        var result = new web2d.Text();
        var family = this.getFontFamily();
        var size = this.getFontSize();
        var weight = this.getFontWeight();
        var style = this.getFontStyle();
        result.setFont(family, size, style, weight);

        var color = this.getFontColor();
        result.setColor(color);

        if (!readOnly) {
            // Propagate mouse events ...
            if (!this.isCentralTopic()) {
                result.setCursor('move');
            } else {
                result.setCursor('default');
            }
        }

        return result;
    },

    /** */
    setFontFamily: function (value, updateModel) {
        var textShape = this.getTextShape();
        textShape.setFontFamily(value);
        if ($defined(updateModel) && updateModel) {
            var model = this.getModel();
            model.setFontFamily(value);
        }
        this._adjustShapes(updateModel);
    },

    /** */
    setFontSize: function (value, updateModel) {

        var textShape = this.getTextShape();
        textShape.setSize(value);

        if ($defined(updateModel) && updateModel) {
            var model = this.getModel();
            model.setFontSize(value);
        }
        this._adjustShapes(updateModel);

    },

    /** */
    setFontStyle: function (value, updateModel) {
        var textShape = this.getTextShape();
        textShape.setStyle(value);
        if ($defined(updateModel) && updateModel) {
            var model = this.getModel();
            model.setFontStyle(value);
        }
        this._adjustShapes(updateModel);
    },

    /** */
    setFontWeight: function (value, updateModel) {
        var textShape = this.getTextShape();
        textShape.setWeight(value);
        if ($defined(updateModel) && updateModel) {
            var model = this.getModel();
            model.setFontWeight(value);
        }
        this._adjustShapes();
    },

    /** */
    getFontWeight: function () {
        var model = this.getModel();
        var result = model.getFontWeight();
        if (!$defined(result)) {
            var font = mindplot.TopicStyle.defaultFontStyle(this);
            result = font.weight;
        }
        return result;
    },

    /** */
    getFontFamily: function () {
        var model = this.getModel();
        var result = model.getFontFamily();
        if (!$defined(result)) {
            var font = mindplot.TopicStyle.defaultFontStyle(this);
            result = font.font;
        }
        return result;
    },

    /** */
    getFontColor: function () {
        var model = this.getModel();
        var result = model.getFontColor();
        if (!$defined(result)) {
            var font = mindplot.TopicStyle.defaultFontStyle(this);
            result = font.color;
        }
        return result;
    },

    /** */
    getFontStyle: function () {
        var model = this.getModel();
        var result = model.getFontStyle();
        if (!$defined(result)) {
            var font = mindplot.TopicStyle.defaultFontStyle(this);
            result = font.style;
        }
        return result;
    },

    /** */
    getFontSize: function () {
        var model = this.getModel();
        var result = model.getFontSize();
        if (!$defined(result)) {
            var font = mindplot.TopicStyle.defaultFontStyle(this);
            result = font.size;
        }
        return result;
    },

    /** */
    setFontColor: function (value, updateModel) {
        var textShape = this.getTextShape();
        textShape.setColor(value);
        if ($defined(updateModel) && updateModel) {
            var model = this.getModel();
            model.setFontColor(value);
        }
    },

    _setText: function (text, updateModel) {
        var textShape = this.getTextShape();
        textShape.setText(text == null ? mindplot.TopicStyle.defaultText(this) : text);

        if ($defined(updateModel) && updateModel) {
            var model = this.getModel();
            model.setText(text);
        }
    },

    /** */
    setText: function (text) {
        // Avoid empty nodes ...
        if (!text || $.trim(text).length == 0) {
            text = null;
        }

        this._setText(text, true);
        this._adjustShapes();
    },

    /** */
    getText: function () {
        var model = this.getModel();
        var result = model.getText();
        if (!$defined(result)) {
            result = mindplot.TopicStyle.defaultText(this);
        }
        return result;
    },

    /** */
    setBackgroundColor: function (color) {
        this._setBackgroundColor(color, true);
    },

    _setBackgroundColor: function (color, updateModel) {
        var innerShape = this.getInnerShape();
        innerShape.setFill(color);

        var connector = this.getShrinkConnector();
        if (connector) {
            connector.setFill(color);
        }

        if ($defined(updateModel) && updateModel) {
            var model = this.getModel();
            model.setBackgroundColor(color);
        }
    },

    /** */
    getBackgroundColor: function () {
        var model = this.getModel();
        var result = model.getBackgroundColor();
        if (!$defined(result)) {
            result = mindplot.TopicStyle.defaultBackgroundColor(this);
        }
        return result;
    },

    /** */
    setBorderColor: function (color) {
        this._setBorderColor(color, true);
    },

    _setBorderColor: function (color, updateModel) {
        var innerShape = this.getInnerShape();
        innerShape.setAttribute('strokeColor', color);

        var connector = this.getShrinkConnector();
        if (connector) {
            connector.setAttribute('strokeColor', color);
        }

        if ($defined(updateModel) && updateModel) {
            var model = this.getModel();
            model.setBorderColor(color);
        }
    },

    /** */
    getBorderColor: function () {
        var model = this.getModel();
        var result = model.getBorderColor();
        if (!$defined(result)) {
            result = mindplot.TopicStyle.defaultBorderColor(this);
        }
        return result;
    },

    _buildTopicShape: function () {
        var groupAttributes = {width: 100, height: 100, coordSizeWidth: 100, coordSizeHeight: 100};
        var group = new web2d.Group(groupAttributes);
        this._set2DElement(group);

        // Shape must be build based on the model width ...
        var outerShape = this.getOuterShape();
        var innerShape = this.getInnerShape();
        var textShape = this.getTextShape();

        // Add to the group ...
        group.append(outerShape);
        group.append(innerShape);
        group.append(textShape);

        // Update figure size ...
        var model = this.getModel();
        if (model.getFeatures().length != 0) {
            this.getOrBuildIconGroup();
        }

        var shrinkConnector = this.getShrinkConnector();
        if ($defined(shrinkConnector)) {
            shrinkConnector.addToWorkspace(group);
        }

        // Register listeners ...
        this._registerDefaultListenersToElement(group, this);
    },

    _registerDefaultListenersToElement: function (elem, topic) {
        var mouseOver = function (event) {
            if (topic.isMouseEventsEnabled()) {
                topic.handleMouseOver(event);
            }
        };
        elem.addEvent('mouseover', mouseOver);

        var outout = function (event) {
            if (topic.isMouseEventsEnabled()) {
                topic.handleMouseOut(event);
            }
        };
        elem.addEvent('mouseout', outout);

        var me = this;
        // Focus events ...
        elem.addEvent('mousedown', function (event) {
            if (!me.isReadOnly()) {
                // Disable topic selection of readOnly mode ...
                var value = true;
                if ((event.metaKey && Browser.Platform.mac) || (event.ctrlKey && !Browser.Platform.mac)) {
                    value = !me.isOnFocus();
                    event.stopPropagation();
                    event.preventDefault();
                }
                topic.setOnFocus(value);
            }

            var eventDispatcher = me._getTopicEventDispatcher();
            eventDispatcher.process(mindplot.TopicEvent.CLICK, me);
            event.stopPropagation();

        });
    },

    /** */
    areChildrenShrunken: function () {
        var model = this.getModel();
        return model.areChildrenShrunken() && !this.isCentralTopic();
    },

    /** */
    isCollapsed: function () {
        var result = false;

        var current = this.getParent();
        while (current && !result) {
            result = current.areChildrenShrunken();
            current = current.getParent();
        }
        return result;
    },

    /** */
    setChildrenShrunken: function (value) {
        // Update Model ...
        var model = this.getModel();
        model.setChildrenShrunken(value);

        // Change render base on the state.
        var shrinkConnector = this.getShrinkConnector();
        if ($defined(shrinkConnector)) {
            shrinkConnector.changeRender(value);
        }

        // Do some fancy animation ....
        var elements = this._flatten2DElements(this);
        var fade = new mindplot.util.FadeEffect(elements, !value);
        var me = this;
        fade.addEvent('complete', function () {
            // Set focus on the parent node ...
            if (value) {
                me.setOnFocus(true);
            }

            // Set focus in false for all the children ...
            elements.forEach(function (elem) {
                if (elem.setOnFocus) {
                    elem.setOnFocus(false);
                }
            });
        });
        fade.start();

        mindplot.EventBus.instance.fireEvent(mindplot.EventBus.events.NodeShrinkEvent, model);

    },

    /** */
    getShrinkConnector: function () {
        var result = this._connector;
        if (this._connector == null) {
            this._connector = new mindplot.ShirinkConnector(this);
            this._connector.setVisibility(false);
            result = this._connector;

        }
        return result;
    },

    /** */
    handleMouseOver: function () {
        var outerShape = this.getOuterShape();
        outerShape.setOpacity(1);
    },

    /** */
    handleMouseOut: function () {
        var outerShape = this.getOuterShape();
        if (!this.isOnFocus()) {
            outerShape.setOpacity(0);
        }
    },

    /** */
    showTextEditor: function (text) {
        this._getTopicEventDispatcher().show(this, {text: text});
    },

    /** */
    showNoteEditor: function () {

        var topicId = this.getId();
        var model = this.getModel();
        var editorModel = {
            getValue: function () {
                var notes = model.findFeatureByType(mindplot.TopicFeature.Note.id);
                var result;
                if (notes.length > 0)
                    result = notes[0].getText();

                return result;
            },

            setValue: function (value) {
                var dispatcher = mindplot.ActionDispatcher.getInstance();
                var notes = model.findFeatureByType(mindplot.TopicFeature.Note.id);
                if (!$defined(value)) {
                    var featureId = notes[0].getId();
                    dispatcher.removeFeatureFromTopic(topicId, featureId);
                }
                else {
                    if (notes.length > 0) {
                        dispatcher.changeFeatureToTopic(topicId, notes[0].getId(), {text: value});
                    }
                    else {
                        dispatcher.addFeatureToTopic(topicId, mindplot.TopicFeature.Note.id, {text: value});
                    }
                }
            }
        };
        var editor = new mindplot.widget.NoteEditor(editorModel);
        this.closeEditors();
        editor.show();
    },

    /** opens a dialog where the user can enter or edit an existing link associated with this topic */
    showLinkEditor: function () {

        var topicId = this.getId();
        var model = this.getModel();
        var editorModel = {
            getValue: function () {
                //@param {mindplot.model.LinkModel[]} links
                var links = model.findFeatureByType(mindplot.TopicFeature.Link.id);
                var result;
                if (links.length > 0)
                    result = links[0].getUrl();

                return result;
            },

            setValue: function (value) {
                var dispatcher = mindplot.ActionDispatcher.getInstance();
                var links = model.findFeatureByType(mindplot.TopicFeature.Link.id);
                if (!$defined(value)) {
                    var featureId = links[0].getId();
                    dispatcher.removeFeatureFromTopic(topicId, featureId);
                }
                else {
                    if (links.length > 0) {
                        dispatcher.changeFeatureToTopic(topicId, links[0].getId(), {url: value});
                    }
                    else {
                        dispatcher.addFeatureToTopic(topicId, mindplot.TopicFeature.Link.id, {url: value});
                    }
                }
            }
        };

        this.closeEditors();
        var editor = new mindplot.widget.LinkEditor(editorModel);
        editor.show();
    },

    /** */
    closeEditors: function () {
        this._getTopicEventDispatcher().close(true);
    },

    _getTopicEventDispatcher: function () {
        return mindplot.TopicEventDispatcher.getInstance();
    },

    /**
     * Point: references the center of the rect shape.!!!
     */
    setPosition: function (point) {
        $assert(point, "position can not be null");
        point.x = Math.ceil(point.x);
        point.y = Math.ceil(point.y);

        // Update model's position ...
        var model = this.getModel();
        model.setPosition(point.x, point.y);

        // Elements are positioned in the center.
        // All topic element must be positioned based on the innerShape.
        var size = this.getSize();

        var cx = point.x - (size.width / 2);
        var cy = point.y - (size.height / 2);

        // Update visual position.
        this._elem2d.setPosition(cx, cy);

        // Update connection lines ...
        this._updateConnectionLines();

        // Check object state.
        this.invariant();
    },

    /** */
    getOutgoingLine: function () {
        return this._outgoingLine;
    },

    /** */
    getIncomingLines: function () {
        var result = [];
        var children = this.getChildren();
        for (var i = 0; i < children.length; i++) {
            var node = children[i];
            var line = node.getOutgoingLine();
            if ($defined(line)) {
                result.push(line);
            }
        }
        return result;
    },

    /** */
    getOutgoingConnectedTopic: function () {
        var result = null;
        var line = this.getOutgoingLine();
        if ($defined(line)) {
            result = line.getTargetTopic();
        }
        return result;
    },

    _updateConnectionLines: function () {
        // Update this to parent line ...
        var outgoingLine = this.getOutgoingLine();
        if ($defined(outgoingLine)) {
            outgoingLine.redraw();
        }

        // Update all the incoming lines ...
        var incomingLines = this.getIncomingLines();
        for (var i = 0; i < incomingLines.length; i++) {
            incomingLines[i].redraw();
        }

        // Update relationship lines
        for (var j = 0; j < this._relationships.length; j++) {
            this._relationships[j].redraw();
        }
    },

    /** */
    setBranchVisibility: function (value) {
        var current = this;
        var parent = this;
        while (parent != null && !parent.isCentralTopic()) {
            current = parent;
            parent = current.getParent();
        }
        current.setVisibility(value);
    },

    /** */
    setVisibility: function (value) {
        this._setTopicVisibility(value);

        // Hide all children...
        this._setChildrenVisibility(value);

        // If there there are connection to the node, topic must be hidden.
        this._setRelationshipLinesVisibility(value);

        // If it's connected, the connection must be rendered.
        var outgoingLine = this.getOutgoingLine();
        if (outgoingLine) {
            outgoingLine.setVisibility(value);
        }
    },

    /** */
    moveToBack: function () {

        // Update relationship lines
        for (var j = 0; j < this._relationships.length; j++) {
            this._relationships[j].moveToBack();
        }
        var connector = this.getShrinkConnector();
        if ($defined(connector)) {
            connector.moveToBack();
        }

        this.get2DElement().moveToBack();
    },

    /** */
    moveToFront: function () {

        this.get2DElement().moveToFront();
        var connector = this.getShrinkConnector();
        if ($defined(connector)) {
            connector.moveToFront();
        }
        // Update relationship lines
        for (var j = 0; j < this._relationships.length; j++) {
            this._relationships[j].moveToFront();
        }
    },

    /** */
    isVisible: function () {
        var elem = this.get2DElement();
        return elem.isVisible();
    },

    _setRelationshipLinesVisibility: function (value) {
        _.each(this._relationships, function (relationship) {
            var sourceTopic = relationship.getSourceTopic();
            var targetTopic = relationship.getTargetTopic();

            var targetParent = targetTopic.getModel().getParent();
            var sourceParent = sourceTopic.getModel().getParent();
            relationship.setVisibility(value && (targetParent == null || !targetParent.areChildrenShrunken()) && (sourceParent == null || !sourceParent.areChildrenShrunken()));
        });
    },

    _setTopicVisibility: function (value) {
        var elem = this.get2DElement();
        elem.setVisibility(value);

        if (this.getIncomingLines().length > 0) {
            var connector = this.getShrinkConnector();
            if ($defined(connector)) {
                connector.setVisibility(value);
            }
        }

        var textShape = this.getTextShape();
        textShape.setVisibility(this.getShapeType() != mindplot.model.TopicShape.IMAGE ? value : false);
    },

    /** */
    setOpacity: function (opacity) {
        var elem = this.get2DElement();
        elem.setOpacity(opacity);

        var connector = this.getShrinkConnector();
        if ($defined(connector)) {
            connector.setOpacity(opacity);
        }
        var textShape = this.getTextShape();
        textShape.setOpacity(opacity);
    },

    _setChildrenVisibility: function (isVisible) {

        // Hide all children.
        var children = this.getChildren();
        var model = this.getModel();

        isVisible = isVisible ? !model.areChildrenShrunken() : isVisible;
        for (var i = 0; i < children.length; i++) {
            var child = children[i];
            child.setVisibility(isVisible);

            var outgoingLine = child.getOutgoingLine();
            outgoingLine.setVisibility(isVisible);
        }

    },

    /** */
    invariant: function () {
        var line = this._outgoingLine;
        var model = this.getModel();
        var isConnected = model.isConnected();

        // Check consistency...
        if ((isConnected && !line) || (!isConnected && line)) {
            // $assert(false,'Illegal state exception.');
        }
    },

    /** */
    setSize: function (size, force) {
        $assert(size, "size can not be null");
        $assert($defined(size.width), "size seem not to be a valid element");
        size = {width: Math.ceil(size.width), height: Math.ceil(size.height)};

        var oldSize = this.getSize();
        var hasSizeChanged = oldSize.width != size.width || oldSize.height != size.height;
        if (hasSizeChanged || force) {
            mindplot.NodeGraph.prototype.setSize.call(this, size);

            var outerShape = this.getOuterShape();
            var innerShape = this.getInnerShape();

            outerShape.setSize(size.width + 4, size.height + 6);
            innerShape.setSize(size.width, size.height);

            // Update the figure position(ej: central topic must be centered) and children position.
            this._updatePositionOnChangeSize(oldSize, size);

            if (hasSizeChanged) {
                mindplot.EventBus.instance.fireEvent(mindplot.EventBus.events.NodeResizeEvent, {
                    node: this.getModel(),
                    size: size
                });
            }
        }
    },

    _updatePositionOnChangeSize: function () {
        $assert(false, "this method must be overwrited.");
    },

    /** */
    disconnect: function (workspace) {
        var outgoingLine = this.getOutgoingLine();
        if ($defined(outgoingLine)) {
            $assert(workspace, 'workspace can not be null');

            this._outgoingLine = null;

            // Disconnect nodes ...
            var targetTopic = outgoingLine.getTargetTopic();
            targetTopic.removeChild(this);

            // Update model ...
            var childModel = this.getModel();
            childModel.disconnect();

            this._parent = null;

            // Remove graphical element from the workspace...
            outgoingLine.removeFromWorkspace(workspace);

            // Remove from workspace.
            mindplot.EventBus.instance.fireEvent(mindplot.EventBus.events.NodeDisconnectEvent, this.getModel());

            // Change text based on the current connection ...
            var model = this.getModel();
            if (!model.getText()) {
                var text = this.getText();
                this._setText(text, false);
            }
            if (!model.getFontSize()) {
                var size = this.getFontSize();
                this.setFontSize(size, false);
            }

            // Hide connection line?.
            if (targetTopic.getChildren().length == 0) {
                var connector = targetTopic.getShrinkConnector();
                if ($defined(connector)) {
                    connector.setVisibility(false);
                }
            }
        }
    },

    /** */
    getOrder: function () {
        var model = this.getModel();
        return model.getOrder();
    },

    /** */
    setOrder: function (value) {
        var model = this.getModel();
        model.setOrder(value);
    },

    /** */
    connectTo: function (targetTopic, workspace) {
        $assert(!this._outgoingLine, 'Could not connect an already connected node');
        $assert(targetTopic != this, 'Circular connection are not allowed');
        $assert(targetTopic, 'Parent Graph can not be null');
        $assert(workspace, 'Workspace can not be null');

        // Connect Graphical Nodes ...
        targetTopic.append(this);
        this._parent = targetTopic;

        // Update model ...
        var targetModel = targetTopic.getModel();
        var childModel = this.getModel();
        childModel.connectTo(targetModel);

        // Create a connection line ...
        var outgoingLine = new mindplot.ConnectionLine(this, targetTopic);
        outgoingLine.setVisibility(false);

        this._outgoingLine = outgoingLine;
        workspace.append(outgoingLine);

        // Update figure is necessary.
        this.updateTopicShape(targetTopic);

        // Change text based on the current connection ...
        var model = this.getModel();
        if (!model.getText()) {
            var text = this.getText();
            this._setText(text, false);
        }
        if (!model.getFontSize()) {
            var size = this.getFontSize();
            this.setFontSize(size, false);
        }
        this.getTextShape();

        // Display connection node...
        var connector = targetTopic.getShrinkConnector();
        if ($defined(connector)) {
            connector.setVisibility(true);
        }

        // Redraw line ...
        outgoingLine.redraw();

        // Fire connection event ...
        if (this.isInWorkspace()) {
            mindplot.EventBus.instance.fireEvent(mindplot.EventBus.events.NodeConnectEvent, {
                parentNode: targetTopic.getModel(),
                childNode: this.getModel()
            });
        }
    },

    /** */
    append: function (child) {
        var children = this.getChildren();
        children.push(child);
    },

    /** */
    removeChild: function (child) {
        var children = this.getChildren();
        children.erase(child);
    },

    /** */
    getChildren: function () {
        var result = this._children;
        if (!$defined(result)) {
            this._children = [];
            result = this._children;
        }
        return result;
    },

    /** */
    removeFromWorkspace: function (workspace) {
        var elem2d = this.get2DElement();
        workspace.removeChild(elem2d);
        var line = this.getOutgoingLine();
        if ($defined(line)) {
            workspace.removeChild(line);
        }
        this._isInWorkspace = false;
        mindplot.EventBus.instance.fireEvent(mindplot.EventBus.events.NodeRemoved, this.getModel());
    },

    /** */
    addToWorkspace: function (workspace) {
        var elem = this.get2DElement();
        workspace.append(elem);
        if (!this.isInWorkspace()) {
            if (!this.isCentralTopic()) {
                mindplot.EventBus.instance.fireEvent(mindplot.EventBus.events.NodeAdded, this.getModel());
            }

            if (this.getModel().isConnected())
                mindplot.EventBus.instance.fireEvent(mindplot.EventBus.events.NodeConnectEvent, {
                    parentNode: this.getOutgoingConnectedTopic().getModel(),
                    childNode: this.getModel()
                });

        }
        this._isInWorkspace = true;
        this._adjustShapes();
    },

    /** */
    isInWorkspace: function () {
        return this._isInWorkspace;
    },

    /** */
    createDragNode: function (layoutManager) {
        var result = this.parent(layoutManager);

        // Is the node already connected ?
        var targetTopic = this.getOutgoingConnectedTopic();
        if ($defined(targetTopic)) {
            result.connectTo(targetTopic);
            result.setVisibility(false);
        }

        // If a drag node is create for it, let's hide the editor.
        this._getTopicEventDispatcher().close();

        return result;
    },

    _adjustShapes: function () {
        if (this._isInWorkspace) {

            var textShape = this.getTextShape();
            if (this.getShapeType() != mindplot.model.TopicShape.IMAGE) {

                var textWidth = textShape.getWidth();

                var textHeight = textShape.getHeight();
                textHeight = textHeight != 0 ? textHeight : 20;

                var topicPadding = mindplot.TopicStyle.getInnerPadding(this);

                // Adjust the icon size to the size of the text ...
                var iconGroup = this.getOrBuildIconGroup();
                var fontHeight = this.getTextShape().getFontHeight();
                iconGroup.setPosition(topicPadding, topicPadding);
                iconGroup.seIconSize(fontHeight, fontHeight);

                // Add a extra padding between the text and the icons
                var iconsWidth = iconGroup.getSize().width;
                if (iconsWidth != 0) {

                    iconsWidth = iconsWidth + (textHeight / 4);
                }

                var height = textHeight + (topicPadding * 2);
                var width = textWidth + iconsWidth + (topicPadding * 2);

                this.setSize({width: width, height: height});

                // Position node ...
                textShape.setPosition(topicPadding + iconsWidth, topicPadding);
            } else {
                // In case of images, the size if fixed ...
                var size = this.getModel().getImageSize();
                this.setSize(size);
            }
        }
    },

    _flatten2DElements: function (topic) {
        var result = [];

        var children = topic.getChildren();
        for (var i = 0; i < children.length; i++) {

            var child = children[i];
            result.push(child);
            result.push(child.getOutgoingLine());

            var relationships = child.getRelationships();
            result = result.concat(relationships);

            if (!child.areChildrenShrunken()) {
                var innerChilds = this._flatten2DElements(child);
                result = result.concat(innerChilds);
            }
        }
        return result;
    },

    /**
     * @param childTopic
     * @return {Boolean} true if childtopic is a child topic of this topic or the topic itself
     */
    isChildTopic: function (childTopic) {
        var result = (this.getId() == childTopic.getId());
        if (!result) {
            var children = this.getChildren();
            for (var i = 0; i < children.length; i++) {
                var parent = children[i];
                result = parent.isChildTopic(childTopic);
                if (result) {
                    break;
                }
            }
        }
        return result;
    },

    /** @return {Boolean} true if the topic is the central topic of the map */
    isCentralTopic: function () {
        return this.getModel().getType() == mindplot.model.INodeModel.CENTRAL_TOPIC_TYPE;
    }


});

/**
 * @constant
 * @type {Number}
 * @default
 */
mindplot.Topic.CONNECTOR_WIDTH = 6;
/**
 * @constant
 * @type {Object<String, Number>}
 * @default
 */
mindplot.Topic.OUTER_SHAPE_ATTRIBUTES = {fillColor: 'rgb(252,235,192)', stroke: '1 dot rgb(241,163,39)', x: 0, y: 0};
/**
 * @constant
 * @type {Object<String, Number>}
 * @default
 */
mindplot.Topic.OUTER_SHAPE_ATTRIBUTES_FOCUS = {fillColor: 'rgb(244,184,45)', x: 0, y: 0};
/**
 * @constant
 * @type {Object<String>}
 * @default
 * */
mindplot.Topic.INNER_RECT_ATTRIBUTES = {stroke: '2 solid'};



