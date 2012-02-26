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


mindplot.TextTopic = new Class({
    Extends:mindplot.Topic,
    initialize : function(model, options) {
        this._textEditor = new mindplot.MultilineTextEditor(this);
        this.parent(model, options);
    },

    _registerEvents : function() {

        this.parent();
        this.addEvent('dblclick', function (event) {
            this._textEditor.show();
            event.stopPropagation(true);
        }.bind(this));

        this._textEditor.addEvent('input', function(event, text) {
            var textShape = this.getTextShape();
//            var oldText = textShape.getText();

//            this._setText(text, false);
            // @Todo: I must resize, no change the position ...
//            textShape.setText(oldText);
        }.bind(this));

    },

    setShapeType : function(type) {
        this._setShapeType(type, true);

    },

    _setShapeType : function(type, updateModel) {
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
            var size = model.getSize();
            this.setSize(size, true);

            var group = this.get2DElement();
            group.appendChild(innerShape);

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

    getShapeType : function() {
        var model = this.getModel();
        var result = model.getShapeType();
        if (!$defined(result)) {
            result = this._defaultShapeType();
        }
        return result;
    },

    getInnerShape : function() {
        if (!$defined(this._innerShape)) {
            // Create inner box.
            this._innerShape = this.buildShape(mindplot.TextTopic.INNER_RECT_ATTRIBUTES);

            // Update bgcolor ...
            var bgColor = this.getBackgroundColor();
            this._setBackgroundColor(bgColor, false);

            // Update border color ...
            var brColor = this.getBorderColor();
            this._setBorderColor(brColor, false);

            // Define the pointer ...
            if (this.getType() != mindplot.model.INodeModel.CENTRAL_TOPIC_TYPE) {
                this._innerShape.setCursor('move');
            } else {
                this._innerShape.setCursor('default');
            }

        }
        return this._innerShape;
    },

    buildShape : function(attributes, type) {
        var result;
        if (!$defined(type)) {
            type = this.getShapeType();
        }

        if (type == mindplot.model.INodeModel.SHAPE_TYPE_RECT) {
            result = new web2d.Rect(0, attributes);
        }
        else if (type == mindplot.model.INodeModel.SHAPE_TYPE_ELIPSE) {
            result = new web2d.Rect(0.9, attributes);
        }
        else if (type == mindplot.model.INodeModel.SHAPE_TYPE_ROUNDED_RECT) {
            result = new web2d.Rect(0.3, attributes);
        }
        else if (type == mindplot.model.INodeModel.SHAPE_TYPE_LINE) {
            result = new web2d.Line({strokeColor:"#495879",strokeWidth:1});
            result.setSize = function(width, height) {
                this.size = {width:width, height:height};
                result.setFrom(0, height);
                result.setTo(width, height);

                // Lines will have the same color of the default connection lines...
                var stokeColor = mindplot.ConnectionLine.getStrokeColor();
                result.setStroke(1, 'solid', stokeColor);
            };

            result.getSize = function() {
                return this.size;
            };

            result.setPosition = function() {
            };

            var setStrokeFunction = result.setStroke;
            result.setFill = function() {

            };

            result.setStroke = function() {

            };
        }
        else {
            $assert(false, "Unsupported figure type:" + type);
        }
        result.setPosition(0, 0);
        return result;
    },


    getOuterShape : function() {
        if (!$defined(this._outerShape)) {
            var rect = this.buildShape(mindplot.Topic.OUTER_SHAPE_ATTRIBUTES, mindplot.model.INodeModel.SHAPE_TYPE_ROUNDED_RECT);
            rect.setPosition(-2, -3);
            rect.setOpacity(0);
            this._outerShape = rect;
        }

        return this._outerShape;
    },

    getTextShape : function() {
        if (!$defined(this._text)) {
            this._text = this._buildTextShape(false);

            // Set Text ...
            var text = this.getText();
            this._setText(text, false);
        }
        return this._text;
    },


    _buildTextShape : function(readOnly) {
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
            if (this.getType() != mindplot.model.INodeModel.CENTRAL_TOPIC_TYPE) {
                result.setCursor('move');
            } else {
                result.setCursor('default');
            }
        }

        return result;
    },

    _getInnerPadding : function() {
        throw "this must be implemented";
    },

    setFontFamily : function(value, updateModel) {
        var textShape = this.getTextShape();
        textShape.setFontFamily(value);
        if ($defined(updateModel) && updateModel) {
            var model = this.getModel();
            model.setFontFamily(value);
        }
        this._adjustShapes(updateModel);
    },

    setFontSize : function(value, updateModel) {

        var textShape = this.getTextShape();
        textShape.setSize(value);

        if ($defined(updateModel) && updateModel) {
            var model = this.getModel();
            model.setFontSize(value);
        }
        this._adjustShapes(updateModel);

    },

    setFontStyle : function(value, updateModel) {
        var textShape = this.getTextShape();
        textShape.setStyle(value);
        if ($defined(updateModel) && updateModel) {
            var model = this.getModel();
            model.setFontStyle(value);
        }
        this._adjustShapes(updateModel);
    },

    setFontWeight : function(value, updateModel) {
        var textShape = this.getTextShape();
        textShape.setWeight(value);
        if ($defined(updateModel) && updateModel) {
            var model = this.getModel();
            model.setFontWeight(value);
        }
        this._adjustShapes();
    },

    getFontWeight : function() {
        var model = this.getModel();
        var result = model.getFontWeight();
        if (!$defined(result)) {
            var font = this._defaultFontStyle();
            result = font.weight;
        }
        return result;
    },

    getFontFamily : function() {
        var model = this.getModel();
        var result = model.getFontFamily();
        if (!$defined(result)) {
            var font = this._defaultFontStyle();
            result = font.font;
        }
        return result;
    },

    getFontColor : function() {
        var model = this.getModel();
        var result = model.getFontColor();
        if (!$defined(result)) {
            var font = this._defaultFontStyle();
            result = font.color;
        }
        return result;
    },

    getFontStyle : function() {
        var model = this.getModel();
        var result = model.getFontStyle();
        if (!$defined(result)) {
            var font = this._defaultFontStyle();
            result = font.style;
        }
        return result;
    },

    getFontSize : function() {
        var model = this.getModel();
        var result = model.getFontSize();
        if (!$defined(result)) {
            var font = this._defaultFontStyle();
            result = font.size;
        }
        return result;
    },

    setFontColor : function(value, updateModel) {
        var textShape = this.getTextShape();
        textShape.setColor(value);
        if ($defined(updateModel) && updateModel) {
            var model = this.getModel();
            model.setFontColor(value);
        }
    },

    _setText : function(text, updateModel) {
        var textShape = this.getTextShape();
        textShape.setText(text == null ? this._defaultText() : text);

        if ($defined(updateModel) && updateModel) {
            var model = this.getModel();
            model.setText(text);
        }
    },

    setText : function(text) {
        // Avoid empty nodes ...
        if (text.trim().length == 0) {
            text = null;
        }

        this._setText(text, true);
        this._adjustShapes();
    },

    getText : function() {
        var model = this.getModel();
        var result = model.getText();
        if (!$defined(result)) {
            result = this._defaultText();
        }
        return result;
    },

    setBackgroundColor : function(color) {
        this._setBackgroundColor(color, true);
    },

    _setBackgroundColor : function(color, updateModel) {
        var innerShape = this.getInnerShape();
        innerShape.setFill(color);

        var connector = this.getShrinkConnector();
        connector.setFill(color);

        if ($defined(updateModel) && updateModel) {
            var model = this.getModel();
            model.setBackgroundColor(color);
        }
    },

    getBackgroundColor : function() {
        var model = this.getModel();
        var result = model.getBackgroundColor();
        if (!$defined(result)) {
            result = this._defaultBackgroundColor();
        }
        return result;
    },

    setBorderColor : function(color) {
        this._setBorderColor(color, true);
    },

    _setBorderColor : function(color, updateModel) {
        var innerShape = this.getInnerShape();
        innerShape.setAttribute('strokeColor', color);

        var connector = this.getShrinkConnector();
        connector.setAttribute('strokeColor', color);


        if ($defined(updateModel) && updateModel) {
            var model = this.getModel();
            model.setBorderColor(color);
        }
    },

    getBorderColor : function() {
        var model = this.getModel();
        var result = model.getBorderColor();
        if (!$defined(result)) {
            result = this._defaultBorderColor();
        }
        return result;
    },

    _buildShape : function() {
        var groupAttributes = {width: 100, height:100,coordSizeWidth:100,coordSizeHeight:100};
        var group = new web2d.Group(groupAttributes);
        this._set2DElement(group);

        // Shape must be build based on the model width ...
        var outerShape = this.getOuterShape();
        var innerShape = this.getInnerShape();
        var textShape = this.getTextShape();
        var shrinkConnector = this.getShrinkConnector();

        // Add to the group ...
        group.appendChild(outerShape);
        group.appendChild(innerShape);
        group.appendChild(textShape);

        // Update figure size ...
        var model = this.getModel();
        if (model.getLinks().length != 0 || model.getNotes().length != 0 || model.getIcons().length != 0) {
            this.getOrBuildIconGroup();
        }

        if (this.getType() != mindplot.model.INodeModel.CENTRAL_TOPIC_TYPE) {
            shrinkConnector.addToWorkspace(group);
        }

        // Register listeners ...
        this._registerDefaultListenersToElement(group, this);
    },


    showTextEditor : function(text) {
        this._textEditor.show(text);
    },

    showLinkEditor : function() {

        var topicId = this.getId();
        var model = this.getModel();
        var editorModel = {
            getValue : function() {
                var links = model.getLinks();
                var result;
                if (links.length > 0)
                    result = links[0].getUrl();

                return result;
            },

            setValue : function(value) {
                var dispatcher = mindplot.ActionDispatcher.getInstance();
                if (!$defined(value)) {
                    dispatcher.removeLinkFromTopic(topicId);
                }
                else {
                    dispatcher.changeLinkToTopic(topicId, value);
                }
            }
        };

        this.closeEditors();
        var editor = new mindplot.widget.LinkEditor(editorModel);
        editor.show();
    },


    closeEditors : function() {
        this._textEditor.close(true);
    },

    /**
     * Point: references the center of the rect shape.!!!
     */
    setPosition : function(point) {
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

    getOutgoingConnectedTopic : function() {
        var result = null;
        var line = this.getOutgoingLine();
        if ($defined(line)) {
            result = line.getTargetTopic();
        }
        return result;
    },

    _setTopicVisibility : function(value) {
        this.parent(value);

        var textShape = this.getTextShape();
        textShape.setVisibility(value);

    },

    setOpacity : function(opacity) {
        this.parent();

        var textShape = this.getTextShape();
        textShape.setOpacity(opacity);
    },

    _updatePositionOnChangeSize : function(oldSize, newSize) {
        $assert(false, "this method must be overwrited.");
    },

    createDragNode : function(layoutManager) {
        var result = this.parent(layoutManager);

        // Is the node already connected ?
        var targetTopic = this.getOutgoingConnectedTopic();
        if ($defined(targetTopic)) {
            result.connectTo(targetTopic);
            result.setVisibility(false);
        }

        // If a drag node is create for it, let's hide the editor.
        this._textEditor.close();

        return result;
    },

    _adjustShapes : function() {
        if (this._isInWorkspace) {
            var textShape = this.getTextShape();
            var textWidth = textShape.getWidth();

            var textHeight = textShape.getHeight();
            textHeight = textHeight != 0 ? textHeight : 20;

            var topicPadding = this._getInnerPadding();

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

            this.setSize({width:width,height:height});

            // Position node ...
            textShape.setPosition(topicPadding + iconsWidth, topicPadding);
        }
    }

});

mindplot.TextTopic.INNER_RECT_ATTRIBUTES = {stroke:'2 solid'};



