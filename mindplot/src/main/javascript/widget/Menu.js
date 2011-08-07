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

mindplot.widget.Menu = new Class({
    initialize : function(designer) {
        this._designer = designer;
        this._toolbarElems = [];
        this._colorPickers = [];

        var fontFamilyModel = {
            getValue: function() {
                var nodes = designer.getSelectedNodes();
                var length = nodes.length;
                if (length == 1) {
                    return nodes[0].getFontFamily();
                }
            },

            setValue: function(value) {
                designer.setFont2SelectedNode(value);

            }
        };
        var fontFamilyPanel = new mindplot.widget.FontFamilyPanel("fontFamily", fontFamilyModel);
        fontFamilyPanel.addEvent('show',function(){this.clear()}.bind(this));
        this._toolbarElems.push(fontFamilyPanel);

        var fontSizeModel = {
            getValue: function() {
                var nodes = designer.getSelectedNodes();
                var length = nodes.length;
                if (length == 1) {
                    return nodes[0].getFontSize();
                }
            },
            setValue: function(value) {
                designer.setFontSize2SelectedNode(value);
            }
        };
        var fontSizePanel = new mindplot.widget.FontSizePanel("fontSize", fontSizeModel);
        fontSizePanel.addEvent('show',function(){this.clear()}.bind(this));
        this._toolbarElems.push(fontSizePanel);

        var topicShapeModel = {
            getValue: function() {
                var nodes = designer.getSelectedNodes();
                var length = nodes.length;
                if (length == 1) {
                    return nodes[0].getShapeType();
                }
            },
            setValue: function(value) {
                designer.setShape2SelectedNode(value);
            }
        };
        var topicShapePanel = new mindplot.widget.TopicShapePanel("topicShape", topicShapeModel);
        topicShapePanel.addEvent('show',function(){this.clear()}.bind(this));
        this._toolbarElems.push(topicShapePanel);

        // Create icon panel dialog ...
        var topicIconModel = {
            getValue: function() {
                return null;
            },
            setValue: function(value) {
                designer.addIconType2SelectedNode(value);
            }
        };
        var iconPanel = new mindplot.widget.IconPanel('topicIcon', topicIconModel);
        iconPanel.addEvent('show',function(){this.clear()}.bind(this));
        this._toolbarElems.push(iconPanel);


        var topicColorPicker = new MooRainbow('topicColor', {
            id: 'topicColor',
            imgPath: '../images/',
            startColor: [255, 255, 255],
            onInit: function() {
                this.clear();
            }.bind(this),

            onChange: function(color) {
                designer.setBackColor2SelectedNode(color.hex);
            },
            onComplete: function() {
                this.clear();
            }.bind(this)
        });
        this._colorPickers.push(topicColorPicker);

        var borderColorPicker = new MooRainbow('topicBorder', {
            id: 'topicBorder',
            imgPath: '../images/',
            startColor: [255, 255, 255],
            onInit: function() {
                this.clear();
            }.bind(this),
            onChange: function(color) {
                designer.setBorderColor2SelectedNode(color.hex);
            },
            onComplete: function() {
                this.clear();
            }.bind(this)

        });
        this._colorPickers.push(borderColorPicker);

        var fontColorPicker = new MooRainbow('fontColor', {
            id: 'fontColor',
            imgPath: '../images/',
            startColor: [255, 255, 255],
            onInit: function() {
                this.clear();
            }.bind(this),
            onChange: function(color) {
                designer.setFontColor2SelectedNode(color.hex);
            },
            onComplete: function() {
                this.clear();
            }.bind(this)
        });
        this._colorPickers.push(fontColorPicker);
    },

    clear : function() {
        this._toolbarElems.forEach(function(elem) {
            elem.hide();
        });

        this._colorPickers.forEach(function(elem) {
            $clear(elem);
            elem.hide();
        });
    }
});