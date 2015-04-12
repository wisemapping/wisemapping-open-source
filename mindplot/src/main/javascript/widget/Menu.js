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

mindplot.widget.Menu = new Class({
    Extends: mindplot.widget.IMenu,

    initialize: function (designer, containerId, mapId, readOnly, baseUrl) {
        this.parent(designer, containerId, mapId);

        baseUrl = !$defined(baseUrl) ? "" : baseUrl;
        var widgetsBaseUrl = baseUrl + "css/widget";

        // Stop event propagation ...
        $('#'+this._containerId).bind('click', function (event) {
            event.stopPropagation();
            return false;
        });

        $("#" + this._containerId).bind('dblclick', function (event) {
            event.stopPropagation();
            return false;
        });

        // Create panels ...
        var designerModel = designer.getModel();

        var fontFamilyBtn = $('#fontFamily');
        if (fontFamilyBtn) {
            var fontFamilyModel = {
                getValue: function () {
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

                setValue: function (value) {
                    designer.changeFontFamily(value);

                }
            };
            this._toolbarElems.push(new mindplot.widget.FontFamilyPanel("fontFamily", fontFamilyModel));
            this._registerTooltip('fontFamily', $msg('FONT_FAMILY'));
        }

        var fontSizeBtn = $('#fontSize');
        if (fontSizeBtn) {
            var fontSizeModel = {
                getValue: function () {
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
                setValue: function (value) {
                    designer.changeFontSize(value);
                }
            };
            this._toolbarElems.push(new mindplot.widget.FontSizePanel("fontSize", fontSizeModel));
            this._registerTooltip('fontSize', $msg('FONT_SIZE'));
        }

        var topicShapeBtn = $('#topicShape');
        if (topicShapeBtn) {
            var topicShapeModel = {
                getValue: function () {
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
                setValue: function (value) {
                    designer.changeTopicShape(value);
                }
            };
            this._toolbarElems.push(new mindplot.widget.TopicShapePanel("topicShape", topicShapeModel));
            this._registerTooltip('topicShape', $msg('TOPIC_SHAPE'));
        }

        var topicIconBtn = $('#topicIcon');
        if (topicIconBtn) {
            // Create icon panel dialog ...
            var topicIconModel = {
                getValue: function () {
                    return null;
                },
                setValue: function (value) {
                    designer.addIconType(value);
                }
            };
            this._toolbarElems.push(new mindplot.widget.IconPanel('topicIcon', topicIconModel));
            this._registerTooltip('topicIcon', $msg('TOPIC_ICON'));
        }

        // Topic color item ...
        var topicColorBtn = $('#topicColor');
        if (topicColorBtn) {

            var topicColorModel = {
                getValue: function () {
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
                setValue: function (hex) {
                    designer.changeBackgroundColor(hex);
                }
            };
            this._toolbarElems.push(new mindplot.widget.ColorPalettePanel('topicColor', topicColorModel, widgetsBaseUrl));
            this._registerTooltip('topicColor', $msg('TOPIC_COLOR'));
        }

        // Border color item ...
        var topicBorderBtn = $('#topicBorder');
        if (topicBorderBtn) {
            var borderColorModel =
            {
                getValue: function () {
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
                setValue: function (hex) {
                    designer.changeBorderColor(hex);
                }
            };
            this._toolbarElems.push(new mindplot.widget.ColorPalettePanel('topicBorder', borderColorModel, widgetsBaseUrl));
            this._registerTooltip('topicBorder', $msg('TOPIC_BORDER_COLOR'));
        }

        // Font color item ...
        var fontColorBtn = $('#fontColor');
        if (fontColorBtn) {
            var fontColorModel =
            {
                getValue: function () {
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
                setValue: function (hex) {
                    designer.changeFontColor(hex);
                }
            };
            this._toolbarElems.push(new mindplot.widget.ColorPalettePanel('fontColor', fontColorModel, baseUrl));
            this._registerTooltip('fontColor', $msg('FONT_COLOR'));
        }

        this._addButton('export', false, false, function () {
            BootstrapDialog.Request.active = new BootstrapDialog.Request('c/maps/' + mapId + "/exportf", $msg('EXPORT'), {
                cancelButton: true,
                closeButton: true
            });
        });
        this._registerTooltip('export', $msg('EXPORT'));

        var me = this;

        this._addButton('print', false, false, function () {
            me.save(saveElem, designer, false);
            var baseUrl = window.location.href.substring(0, window.location.href.lastIndexOf("c/maps/"));
            window.open(baseUrl + 'c/maps/' + mapId + '/print');
        });

        this._registerTooltip('print', $msg('PRINT'));

        this._addButton('zoomIn', false, false, function () {
            designer.zoomIn();
        });
        this._registerTooltip('zoomIn', $msg('ZOOM_IN'));

        this._addButton('zoomOut', false, false, function () {
            designer.zoomOut();
        });
        this._registerTooltip('zoomOut', $msg('ZOOM_OUT'));


        var undoButton = this._addButton('undoEdition', false, false, function () {
            designer.undo();
        });
        if (undoButton) {
            undoButton.disable();
        }
        this._registerTooltip('undoEdition', $msg('UNDO'), "meta+Z");


        var redoButton = this._addButton('redoEdition', false, false, function () {
            designer.redo();
        });
        if (redoButton) {
            redoButton.disable();
        }
        this._registerTooltip('redoEdition', $msg('REDO'), "meta+shift+Z");

        if (redoButton && undoButton) {
            designer.addEvent('modelUpdate', function (event) {
                if (event.undoSteps > 0) {
                    undoButton.enable();
                } else {
                    undoButton.disable();
                }
                if (event.redoSteps > 0) {
                    redoButton.enable();
                } else {
                    redoButton.disable();
                }

            });
        }

        this._addButton('addTopic', true, false, function () {
            designer.createSiblingForSelectedNode();
        });
        this._registerTooltip('addTopic', $msg('ADD_TOPIC'), "Enter");


        this._addButton('deleteTopic', true, true, function () {
            designer.deleteSelectedEntities();
        });
        this._registerTooltip('deleteTopic', $msg('TOPIC_DELETE'), "Delete");


        this._addButton('topicLink', true, false, function () {
            designer.addLink();
        });
        this._registerTooltip('topicLink', $msg('TOPIC_LINK'));


        this._addButton('topicRelation', true, false, function (event) {
            designer.showRelPivot(event);
        });
        this._registerTooltip('topicRelation', $msg('TOPIC_RELATIONSHIP'));


        this._addButton('topicNote', true, false, function () {
            designer.addNote();
        });
        this._registerTooltip('topicNote', $msg('TOPIC_NOTE'));


        this._addButton('fontBold', true, false, function () {
            designer.changeFontWeight();
        });
        this._registerTooltip('fontBold', $msg('FONT_BOLD'), "meta+B");


        this._addButton('fontItalic', true, false, function () {
            designer.changeFontStyle();
        });
        this._registerTooltip('fontItalic', $msg('FONT_ITALIC'), "meta+I");

        var saveElem = $('#save');
        if (saveElem) {
            this._addButton('save', false, false,
                function () {
                    me.save(saveElem, designer, true);
                });
            this._registerTooltip('save', $msg('SAVE'), "meta+S");


            if (!readOnly) {
                // To prevent the user from leaving the page with changes ...
//                Element.NativeEvents.unload = 1;
                $(window).bind('unload', function () {
                    if (me.isSaveRequired()) {
                        me.save(saveElem, designer, false, true);
                    }
                    me.unlockMap(designer);
                });

                // Autosave on a fixed period of time ...
                setInterval(
                    function() {
                        if (me.isSaveRequired()) {
                            me.save(saveElem, designer, false);
                        }
                    }, 30000);
            }
        }

        var discardElem = $('#discard');
        if (discardElem) {
            this._addButton('discard', false, false, function () {
                me.discardChanges(designer);
            });
            this._registerTooltip('discard', $msg('DISCARD_CHANGES'));
        }

        var shareElem = $('#shareIt');
        if (shareElem) {
            this._addButton('shareIt', false, false, function () {
                BootstrapDialog.Request.active = new BootstrapDialog.Request('c/maps/' + mapId + "/sharef", $msg('COLLABORATE'), {
                        closeButton: true,
                        cancelButton: true
                });
                designer.onObjectFocusEvent();
            });
            this._registerTooltip('shareIt', $msg('COLLABORATE'));

        }

        var publishElem = $('#publishIt');
        if (publishElem) {
            this._addButton('publishIt', false, false, function () {
                BootstrapDialog.Request.active = new BootstrapDialog.Request('c/maps/' + mapId + "/publishf", $msg('PUBLISH'), {
                        closeButton: true,
                        cancelButton: true
                });
                designer.onObjectFocusEvent();
            });
            this._registerTooltip('publishIt', $msg('PUBLISH'));
        }

        var historyElem = $('#history');
        if (historyElem) {

            this._addButton('history', false, false, function () {
                BootstrapDialog.Request.active = new BootstrapDialog.Request('c/maps/' + mapId + "/historyf", $msg('HISTORY'), {
                    closeButton: true,
                    cancelButton: true
                });
                designer.onObjectFocusEvent();
            });
            this._registerTooltip('history', $msg('HISTORY'));
        }

        this._registerEvents(designer);

        // Keyboard Shortcuts Action ...
        var keyboardShortcut = $('#keyboardShortcuts');
        if (keyboardShortcut) {

            keyboardShortcut.bind('click', function (event) {
                BootstrapDialog.Request.active = new BootstrapDialog.Request('c/keyboard', $msg('SHORTCUTS'), {
                    closeButton: true,
                    cancelButton: true
                });
                designer.onObjectFocusEvent();
                event.preventDefault();
            });
        }


        var videoElem = $('#tutorialVideo');
        if (videoElem) {
            var width = 900;
            var height = 500;
            var left = (screen.width / 2) - (width / 2);
            var top = (screen.height / 2) - (height / 2);

            videoElem.bind('click', function (event) {
                window.open("https://www.youtube.com/tv?vq=medium#/watch?v=rKxZwNKs9cE", "_blank", 'toolbar=no, location=no, directories=no, status=no, menubar=no, scrollbars=no, resizable=yes, copyhistory=no, width=' + width + ', height=' + height + ', top=' + top + ', left=' + left);
                event.preventDefault();
            });
        }

    },

    _registerEvents: function (designer) {
        var me = this;
        // Register on close events ...
        _.each(this._toolbarElems, function (elem) {
            elem.addEvent('show', function () {
                me.clear()
            });
        });

        designer.addEvent('onblur', function () {
            var topics = designer.getModel().filterSelectedTopics();
            var rels = designer.getModel().filterSelectedRelationships();

            _.each(me._toolbarElems, function (button) {
                var isTopicAction = button.isTopicAction();
                var isRelAction = button.isRelAction();

                if (isTopicAction || isRelAction) {
                    if ((isTopicAction && topics.length != 0) || (isRelAction && rels.length != 0)) {
                        button.enable();
                    } else {
                        button.disable();
                    }
                }
            })
        });

        designer.addEvent('onfocus', function () {
            var topics = designer.getModel().filterSelectedTopics();
            var rels = designer.getModel().filterSelectedRelationships();

            _.each(me._toolbarElems, function (button) {
                var isTopicAction = button.isTopicAction();
                var isRelAction = button.isRelAction();

                if (isTopicAction || isRelAction) {

                    if (isTopicAction && topics.length > 0) {
                        button.enable();
                    }

                    if (isRelAction && rels.length > 0) {
                        button.enable();
                    }
                }
            })
        });
    },

    _addButton: function (buttonId, topic, rel, fn) {
        var me = this;
        // Register Events ...
        var result = null;
        if ($('#'+buttonId)) {

            var button = new mindplot.widget.ToolbarItem(buttonId, function (event) {
                fn(event);
                me.clear();
            }, {topicAction: topic, relAction: rel});

            this._toolbarElems.push(button);
            result = button;
        }
        return result;
    },

    _registerTooltip: function (buttonId, text, shortcut) {
        if ($('#'+buttonId)) {
            var tooltip = text;
            if (shortcut) {
                shortcut = Browser.Platform.mac ? shortcut.replace("meta+", "⌘") : shortcut.replace("meta+", "ctrl+");
                tooltip = tooltip + " (" + shortcut + ")";
            }
            new mindplot.widget.KeyboardShortcutTooltip($('#'+buttonId), tooltip);
        }
    }
});