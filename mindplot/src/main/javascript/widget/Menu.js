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
        var fontFamilyModel = {
            getValue: function() {
                var nodes = designer.getSelectedNodes();
                if (nodes.length == 1) {
                    return nodes[0].getFontFamily();
                }
            },

            setValue: function(value) {
                designer.setFont2SelectedNode(value);

            }
        };
        this._toolbarElems.push(new mindplot.widget.FontFamilyPanel("fontFamily", fontFamilyModel));

        var fontSizeModel = {
            getValue: function() {
                var nodes = designer.getSelectedNodes();
                if (nodes.length == 1) {
                    return nodes[0].getFontSize();
                }
            },
            setValue: function(value) {
                designer.setFontSize2SelectedNode(value);
            }
        };
        this._toolbarElems.push(new mindplot.widget.FontSizePanel("fontSize", fontSizeModel));

        var topicShapeModel = {
            getValue: function() {
                var nodes = designer.getSelectedNodes();
                if (nodes.length == 1) {
                    return nodes[0].getShapeType();
                }
            },
            setValue: function(value) {
                designer.setShape2SelectedNode(value);
            }
        };
        this._toolbarElems.push(new mindplot.widget.TopicShapePanel("topicShape", topicShapeModel));

        // Create icon panel dialog ...
        var topicIconModel = {
            getValue: function() {
                return null;
            },
            setValue: function(value) {
                designer.addIconType2SelectedNode(value);
            }
        };
        this._toolbarElems.push(new mindplot.widget.IconPanel('topicIcon', topicIconModel));

        // Topic color item ...
        var topicColorModel =
        {
            getValue : function() {
                var nodes = designer.getSelectedNodes();
                if (nodes.length == 1) {
                    return nodes[0].getBackgroundColor();
                }
                return null;
            },
            setValue : function (hex) {
                designer.setBackColor2SelectedNode(hex);
            }
        };
        this._toolbarElems.push(new mindplot.widget.ColorPalettePanel('topicColor', topicColorModel, baseUrl));

        // Border color item ...
        var borderColorModel =
        {
            getValue : function() {
                var nodes = designer.getSelectedNodes();
                if (nodes.length == 1) {
                    return nodes[0].getBorderColor();
                }
            },
            setValue : function (hex) {
                designer.setBorderColor2SelectedNode(hex);
            }
        };
        this._toolbarElems.push(new mindplot.widget.ColorPalettePanel('topicBorder', borderColorModel, baseUrl));

        // Font color item ...
        var fontColorModel =
        {
            getValue : function() {
                var nodes = designer.getSelectedNodes();
                if (nodes.length == 1) {
                    return nodes[0].getFontColor();
                }
            },
            setValue : function (hex) {
                designer.setFontColor2SelectedNode(hex);
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
        $('zoomIn').addEvent('click', function(event) {
            designer.zoomIn();
        });

        $('zoomOut').addEvent('click', function(event) {
            designer.zoomOut();
        });

        $('undoEdition').addEvent('click', function(event) {
            designer.undo();
        });

        $('redoEdition').addEvent('click', function(event) {
            designer.redo();
        });

        $('addTopic').addEvent('click', function(event) {
            designer.createSiblingForSelectedNode();
        });

        $('deleteTopic').addEvent('click', function(event) {
            designer.deleteCurrentNode();
        });


        $('topicLink').addEvent('click', function(event) {
            designer.addLink2SelectedNode();

        });

        $('topicRelation').addEvent('click', function(event) {
            designer.addRelationShip2SelectedNode(event);
        });

        $('topicNote').addEvent('click', function(event) {
            designer.addNote2SelectedNode();

        });

        $('fontBold').addEvent('click', function(event) {
            designer.setWeight2SelectedNode();
        });

        $('fontItalic').addEvent('click', function(event) {
            designer.setStyle2SelectedNode();
        });

        designer.addEventListener("modelUpdate", function(event) {
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