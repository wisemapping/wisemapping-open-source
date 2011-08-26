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
    initialize : function(designer, containerId) {
        $assert(designer, "designer can not be null");
        $assert(containerId, "containerId can not be null");
        // @Todo: Remove hardcode ...
        var baseUrl = "/mindplot/src/main/javascript/widget";

        // Init variables ...
        this._designer = designer;
        this._toolbarElems = [];
        this._containerId = containerId;

        // Stop event propagation ...
        $(this._containerId).addEvent('click', function(event) {
            event.stopPropagation();
            return false;
        });

        $(this._containerId).addEvent('dblclick', function(event) {
            event.stopPropagation();
            return false;
        });

        // Create panels ...
        var designerModel = designer.getModel();
        var fontFamilyModel = {
            getValue: function() {
                var nodes = designerModel.filterSelectedTopics();
                var result = null;
                for (var i = 0; i < nodes.length; i++) {
                    var fontFamily = nodes[i].getFontFamily();
                    if (result != null && result != fontFamily) {
                        result = null;
                        break;
                    }
                    result = fontFamily;
                }
                return result;
            },

            setValue: function(value) {
                designer.changeFontFamily(value);

            }
        };
        this._toolbarElems.push(new mindplot.widget.FontFamilyPanel("fontFamily", fontFamilyModel));

        var fontSizeModel = {
            getValue: function() {
                var nodes = designerModel.filterSelectedTopics();
                var result = null;
                for (var i = 0; i < nodes.length; i++) {
                    var fontSize = nodes[i].getFontSize();
                    if (result != null && result != fontSize) {
                        result = null;
                        break;
                    }
                    result = fontSize;
                }
                return result;
            },
            setValue: function(value) {
                designer.changeFontSize(value);
            }
        };
        this._toolbarElems.push(new mindplot.widget.FontSizePanel("fontSize", fontSizeModel));

        var topicShapeModel = {
            getValue: function() {
                var nodes = designerModel.filterSelectedTopics();
                var result = null;
                for (var i = 0; i < nodes.length; i++) {
                    var shapeType = nodes[i].getShapeType();
                    if (result != null && result != shapeType) {
                        result = null;
                        break;
                    }
                    result = shapeType;
                }
                return result;
            },
            setValue: function(value) {
                designer.changeTopicShape(value);
            }
        };
        this._toolbarElems.push(new mindplot.widget.TopicShapePanel("topicShape", topicShapeModel));

        // Create icon panel dialog ...
        var topicIconModel = {
            getValue: function() {
                return null;
            },
            setValue: function(value) {
                designer.addIconType(value);
            }
        };
        this._toolbarElems.push(new mindplot.widget.IconPanel('topicIcon', topicIconModel));

        // Topic color item ...
        var topicColorModel =
        {
            getValue : function() {
                var nodes = designerModel.filterSelectedTopics();
                var result = null;
                for (var i = 0; i < nodes.length; i++) {
                    var color = nodes[i].getBackgroundColor();
                    if (result != null && result != color) {
                        result = null;
                        break;
                    }
                    result = color;
                }
                return result;
            },
            setValue : function (hex) {
                designer.changeBackgroundColor(hex);
            }
        };
        this._toolbarElems.push(new mindplot.widget.ColorPalettePanel('topicColor', topicColorModel, baseUrl));

        // Border color item ...
        var borderColorModel =
        {
            getValue : function() {
                var nodes = designerModel.filterSelectedTopics();
                var result = null;
                for (var i = 0; i < nodes.length; i++) {
                    var color = nodes[i].getBorderColor();
                    if (result != null && result != color) {
                        result = null;
                        break;
                    }
                    result = color;
                }
                return result;
            },
            setValue : function (hex) {
                designer.changeBorderColor(hex);
            }
        };
        this._toolbarElems.push(new mindplot.widget.ColorPalettePanel('topicBorder', borderColorModel, baseUrl));

        // Font color item ...
        var fontColorModel =
        {
            getValue : function() {
                var result = null;
                var nodes = designerModel.filterSelectedTopics();
                for (var i = 0; i < nodes.length; i++) {
                    var color = nodes[i].getFontColor();
                    if (result != null && result != color) {
                        result = null;
                        break;
                    }
                    result = color;
                }
                return result;
            },
            setValue : function (hex) {
                designer.changeFontColor(hex);
            }
        };
        this._toolbarElems.push(new mindplot.widget.ColorPalettePanel('fontColor', fontColorModel, baseUrl));

        // Register on close events ...
        this._toolbarElems.forEach(function(elem) {
            elem.addEvent('show', function() {
                this.clear()
            }.bind(this));
        }.bind(this));


        // Register Events ...
        $('zoomIn').addEvent('click', function() {
            this.clear();
            designer.zoomIn();
        }.bind(this));

        $('zoomOut').addEvent('click', function() {
            this.clear();
            designer.zoomOut();
        }.bind(this));

        $('undoEdition').addEvent('click', function() {
            this.clear();
            designer.undo();
        }.bind(this));

        $('redoEdition').addEvent('click', function() {
            this.clear();
            designer.redo();
        }.bind(this));

        $('addTopic').addEvent('click', function() {
            this.clear();
            designer.createSiblingForSelectedNode();
        }.bind(this));

        $('deleteTopic').addEvent('click', function() {
            this.clear();
            designer.deleteCurrentNode();
        }.bind(this));


        $('topicLink').addEvent('click', function() {
            this.clear();
            designer.addLink();
        }.bind(this));

        $('topicRelation').addEvent('click', function(event) {
            designer.addRelationShip(event);
        });

        $('topicNote').addEvent('click', function() {
            this.clear();
            designer.addNote();
        }.bind(this));

        $('fontBold').addEvent('click', function() {
            designer.changeFontWeight();
        });

        $('fontItalic').addEvent('click', function() {
            designer.changeFontStyle();
        });

        designer.addEvent("modelUpdate", function(event) {
            if (event.undoSteps > 0) {
                $("undoEdition").setStyle("background-image", "url(../images/file_undo.png)");
            } else {
                $("undoEdition").setStyle("background-image", "url(../images/file_undo_dis.png)");
            }

            if (event.redoSteps > 0) {
                $("redoEdition").setStyle("background-image", "url(../images/file_redo.png)");
            } else {
                $("redoEdition").setStyle("background-image", "url(../images/file_redo_dis.png)");
            }

        });
    },

    clear : function() {
        this._toolbarElems.forEach(function(elem) {
            elem.hide();
        });
    }
});