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
    Extends: mindplot.widget.IMenu,

    initialize : function(designer, containerId, mapId, readOnly, baseUrl) {
        this.parent(designer, containerId, mapId);

        baseUrl = !$defined(baseUrl) ? "" : baseUrl;
        var widgetsBaseUrl = baseUrl + "css/widget";

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
        this._registerTooltip('fontFamily', "Text font");


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
        this._registerTooltip('fontSize', "Text size");


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
        this._registerTooltip('topicShape', "Topic shape");


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
        this._registerTooltip('topicIcon', "Icon");


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
        this._toolbarElems.push(new mindplot.widget.ColorPalettePanel('topicColor', topicColorModel, widgetsBaseUrl));
        this._registerTooltip('topicColor', "Topic color");


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
        this._toolbarElems.push(new mindplot.widget.ColorPalettePanel('topicBorder', borderColorModel, widgetsBaseUrl));
        this._registerTooltip('topicBorder', "Border color");


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
        this._registerTooltip('fontColor', "Text color");


        this._addButton('export', false, false, "Export", function() {
            var reqDialog = new MooDialog.Request('c/export.htm?mapId=' + mapId, null,
                {'class': 'exportModalDialog',
                    closeButton:true,
                    destroyOnClose:true,
                    title:'Export'
                });
            reqDialog.setRequestOptions({
                onRequest: function() {
                    reqDialog.setContent('loading...');
                }
            });
            MooDialog.Request.active = reqDialog;
        });
        this._registerTooltip('export', "Export");


        this._addButton('print', false, false, function() {
            printMap();
        });
        this._registerTooltip('print', "Print");

        this._addButton('zoomIn', false, false, function() {
            designer.zoomIn();
        });
        this._registerTooltip('zoomIn', "Zoom in");

        this._addButton('zoomOut', false, false, function() {
            designer.zoomOut();
        });
        this._registerTooltip('zoomOut', "Zoom out");


        this._addButton('undoEdition', false, false, function() {
            designer.undo();
        });
        this._registerTooltip('undoEdition', "Undo", "meta+Z");


        this._addButton('redoEdition', false, false, function() {
            designer.redo();
        });
        this._registerTooltip('redoEdition', "Redo", "meta+Y");


        this._addButton('addTopic', true, false, function() {
            designer.createChildForSelectedNode();
        });
        this._registerTooltip('addTopic', "Add topic", "Enter");


        this._addButton('deleteTopic', true, true, function() {
            designer.deleteCurrentNode();
        });
        this._registerTooltip('deleteTopic', "Delete topic", "Backspace");


        this._addButton('topicLink', true, false, function() {
            designer.addLink();
        });
        this._registerTooltip('topicLink', "Add link");


        this._addButton('topicRelation', true, false, function(event) {
            designer.showRelPivot(event);
        });
        this._registerTooltip('topicRelation', "Relationship");


        this._addButton('topicNote', true, false, function() {
            designer.addNote();
        });
        this._registerTooltip('topicNote', "Add note");


        this._addButton('fontBold', true, false, function() {
            designer.changeFontWeight();
        });
        this._registerTooltip('fontBold', "Text bold", "meta+B");


        this._addButton('fontItalic', true, false, function() {
            designer.changeFontStyle();
        });
        this._registerTooltip('fontItalic', "Text italic", "meta+I");


        var saveElem = $('save');
        if (saveElem) {
            this._addButton('save', false, false, function() {
                this.save(saveElem, designer, true);
            }.bind(this));
            this._registerTooltip('save', "Save", "meta+S");


            if (!readOnly) {
                // To prevent the user from leaving the page with changes ...
                $(window).addEvent('beforeunload', function () {
                    if (designer.needsSave()) {
                        this.save(saveElem, designer, false);
                    }
                }.bind(this));

                // Autosave on a fixed period of time ...
                (function() {
                    if (designer.needsSave()) {
                        this.save(saveElem, designer, false);
                    }
                }.bind(this)).periodical(30000);
            }
        }

        var discardElem = $('discard');
        if (discardElem) {
            this._addButton('discard', false, false, function() {
                this.discard();
                window.location.reload();
            }.bind(this));
            this._registerTooltip('discard', "Discard");
        }

        var tagElem = $('tagIt');
        if (tagElem) {
            this._addButton('tagIt', false, false, function() {
                var reqDialog = new MooDialog.Request('c/tags.htm?mapId=' + mapId, null,
                    {'class': 'tagItModalDialog',
                        closeButton:true,
                        destroyOnClose:true,
                        title:'Tags'
                    });
                reqDialog.setRequestOptions({
                    onRequest: function() {
                        reqDialog.setContent('loading...');
                    }
                });
            });
        }

        var shareElem = $('shareIt');
        if (shareElem) {
            this._addButton('shareIt', false, false, function() {
                var reqDialog = new MooDialog.Request('c/mymaps.htm?action=collaborator&mapId=' + mapId, null,
                    {'class': 'shareItModalDialog',
                        closeButton:true,
                        destroyOnClose:true,
                        title:'Share It'
                    });
                reqDialog.setRequestOptions({
                    onRequest: function() {
                        reqDialog.setContent('loading...');
                    }
                });

            });
        }

        var publishElem = $('publishIt');
        if (publishElem) {
            this._addButton('publishIt', false, false, function() {
                var reqDialog = new MooDialog.Request('c/publish.htm?mapId=' + mapId, null,
                    {'class': 'publishModalDialog',
                        closeButton:true,
                        destroyOnClose:true,
                        title:'Publish'
                    });
                reqDialog.setRequestOptions({
                    onRequest: function() {
                        reqDialog.setContent('loading...');
                    }
                });

            });
        }

        var historyElem = $('history');
        if (historyElem) {

            this._addButton('history', false, false, function() {
                var reqDialog = new MooDialog.Request('c/history.htm?action=list&goToMindmapList&mapId=' + mapId, null,
                    {'class': 'historyModalDialog',
                        closeButton:true,
                        destroyOnClose:true,
                        title:'History'
                    });
                reqDialog.setRequestOptions({
                    onRequest: function() {
                        reqDialog.setContent('loading...');
                    }
                });
            });
        }

        this._registerEvents(designer);
    },

    _registerEvents : function(designer) {

        // Register on close events ...
        this._toolbarElems.forEach(function(elem) {
            elem.addEvent('show', function() {
                this.clear()
            }.bind(this));
        }.bind(this));

        designer.addEvent('onblur', function() {
            var topics = designer.getModel().filterSelectedTopics();
            var rels = designer.getModel().filterSelectedRelations();

            this._toolbarElems.forEach(function(button) {
                var disable = false;
                if (button.isTopicAction() && button.isRelAction()) {
                    disable = rels.length == 0 && topics.length == 0;
                } else if (!button.isTopicAction() && !button.isRelAction()) {
                    disable = false;
                }
                else if (button.isTopicAction() && topics.length == 0) {
                    disable = true;
                } else if (button.isRelAction() && rels.length == 0) {
                    disable = true;
                }

                if (disable) {
                    button.disable();
                } else {
                    button.enable();
                }

            })
        }.bind(this));

        designer.addEvent('onfocus', function() {
            var topics = designer.getModel().filterSelectedTopics();
            var rels = designer.getModel().filterSelectedRelations();

            this._toolbarElems.forEach(function(button) {
                if (button.isTopicAction() && topics.length > 0) {
                    button.enable();
                }

                if (button.isRelAction() && rels.length > 0) {
                    button.enable();
                }
            })
        }.bind(this));
    },

    _addButton:function (buttonId, topic, rel, fn) {
        // Register Events ...
        if ($(buttonId)) {

            var button = new mindplot.widget.ToolbarItem(buttonId, function(event) {
                fn(event);
                this.clear();
            }.bind(this), {topicAction:topic,relAction:rel});

            this._toolbarElems.push(button);
        }
    },

    _registerTooltip: function(buttonId, text, shortcut) {
        if ($(buttonId)) {
            var tooltip = text;
            if (shortcut) {
                shortcut = Browser.Platform.mac ? shortcut.replace("meta+", "⌘") : shortcut.replace("meta+", "ctrl");
                tooltip = tooltip + " (" + shortcut + ")";
            }
            new mindplot.widget.KeyboardShortcutTooltip($(buttonId), tooltip);
        }
    }
});