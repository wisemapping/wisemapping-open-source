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

    initialize : function(designer, containerId, mapId, readOnly) {
        this.parent(designer, containerId, mapId);

        var baseUrl = "../css/widget";

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

        this._addButton('export', false, false, function() {
            var reqDialog = new MooDialog.Request('../c/export.htm?mapId=' + mapId, null,
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

        this._addButton('print', false, false, function() {
            printMap();
        });

        this._addButton('zoomIn', false, false, function() {
            designer.zoomIn();
        });

        this._addButton('zoomOut', false, false, function() {
            designer.zoomOut();
        });

        this._addButton('undoEdition', false, false, function() {
            designer.undo();
        });

        this._addButton('redoEdition', false, false, function() {
            designer.redo();
        });

        this._addButton('addTopic', true, false, function() {
            designer.createChildForSelectedNode();
        });

        this._addButton('deleteTopic', true, true, function() {
            designer.deleteCurrentNode();
        });

        this._addButton('topicLink', true, false, function() {
            designer.addLink();
        });

        this._addButton('topicRelation', true, false, function(event) {
            designer.showRelPivot(event);
        });

        this._addButton('topicNote', true, false, function() {
            designer.addNote();
        });

        this._addButton('fontBold', true, false, function() {
            designer.changeFontWeight();
        });

        this._addButton('fontItalic', true, false, function() {
            designer.changeFontStyle();
        });

        var saveElem = $('save');
        if (saveElem) {
            this._addButton('save', false, false, function() {
                this.save(saveElem, designer, true);
            }.bind(this));

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

                if (!readOnly) {
                    displayLoading();
                    window.document.location = "mymaps.htm";
                } else {
                    displayLoading();
                    window.document.location = "home.htm";
                }
            });
        }

        var tagElem = $('tagIt');
        if (tagElem) {
            this._addButton('tagIt', false, false, function() {
                var reqDialog = new MooDialog.Request('../c/tags.htm?mapId=' + mapId, null,
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
                var reqDialog = new MooDialog.Request('../c/mymaps.htm?action=collaborator&userEmail=paulo%40pveiga.com.ar&mapId=' + mapId, null,
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
                var reqDialog = new MooDialog.Request('../c/publish.htm?mapId=' + mapId, null,
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
                var reqDialog = new MooDialog.Request('../c/history.htm?action=list&goToMindmapList&mapId=' + mapId, null,
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
                    console.log(disable);
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
        var button = new mindplot.widget.ToolbarItem(buttonId, function(event) {
            fn(event);
            this.clear();
        }.bind(this), {topicAction:topic,relAction:rel});
        this._toolbarElems.push(button);
    }
});