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

mindplot.DesignerKeyboard = new Class({
    Extends: mindplot.Keyboard,
    Static:{
        register:function (designer) {
            this._instance = new mindplot.DesignerKeyboard(designer);
        },

        getInstance:function () {
            return this._instance;
        }
    },

    initialize:function (designer) {
        $assert(designer, "designer can not be null");
        this._registerEvents(designer);
    },

    _registerEvents:function (designer) {

        // Try with the keyboard ..
        var model = designer.getModel();
        this.addShortcut(
            ['backspace'], function (event) {
                event.preventDefault();
                event.stopPropagation();
                designer.deleteSelectedEntities();
            }
        );
        this.addShortcut(
            ['space'], function() {
                designer.shrinkSelectedBranch();
            }
        );
        this.addShortcut(
            ['f2'],function(event) {
                event.stopPropagation();
                event.preventDefault();
                var node = model.selectedTopic();
                if (node) {
                    node.showTextEditor();
                }
            }
        );
        this.addShortcut(
            ['del'], function(event) {
                designer.deleteSelectedEntities();
                event.preventDefault();
                event.stopPropagation();
            }
        );
        this.addShortcut(
            ['enter'], function() {
                designer.createSiblingForSelectedNode();
            }
        );
        this.addShortcut(
            ['insert'], function(event) {
                designer.createChildForSelectedNode();
                event.preventDefault();
                event.stopPropagation();
            }
        );
        this.addShortcut(
            ['tab'], function(event) {
                designer.createChildForSelectedNode();
                event.preventDefault();
                event.stopPropagation();
            }
        );
        this.addShortcut(
            ['meta+enter'], function(event) {
                event.preventDefault();
                event.stopPropagation();
                designer.createChildForSelectedNode();
            }
        );
        this.addShortcut(
            ['ctrl+z', 'meta+z'], function(event) {
                event.preventDefault(event);
                event.stopPropagation();
                designer.undo();
            }
        );
        this.addShortcut(
            ['ctrl+c', 'meta+c'], function (event) {
                event.preventDefault(event);
                event.stopPropagation();
                designer.copyToClipboard();
            }
        );
        this.addShortcut(
            ['ctrl+v', 'meta+v'], function (event) {
                event.preventDefault(event);
                event.stopPropagation();
                designer.pasteClipboard();
            }
        );
        this.addShortcut(
            ['ctrl+shift+z', 'meta+shift+z', 'ctrl+y', 'meta+y'], function (event) {
                event.preventDefault();
                event.stopPropagation();
                designer.redo();
            }
        );
        this.addShortcut(
            ['ctrl+a', 'meta+a'], function (event) {
                event.preventDefault();
                event.stopPropagation();
                designer.selectAll();
            }
        );
        this.addShortcut(
            ['ctrl+b', 'meta+b'], function (event) {
                event.preventDefault();
                event.stopPropagation();

                designer.changeFontWeight();
            }
        );
        this.addShortcut(
            ['ctrl+s', 'meta+s'], function (event) {
                event.preventDefault();
                event.stopPropagation();
                $(document).find('#save').trigger('click');
            }
        );
        this.addShortcut(
            ['ctrl+i', 'meta+i'], function (event) {
                event.preventDefault();
                event.stopPropagation();

                designer.changeFontStyle();
            }
        );
        this.addShortcut(
            ['ctrl+shift+a', 'meta+shift+a'], function (event) {
                event.preventDefault();
                event.stopPropagation();

                designer.deselectAll();
            }
        );
        this.addShortcut(
            ['meta+=', 'ctrl+='], function (event) {
                event.preventDefault();
                event.stopPropagation();

                designer.zoomIn();
            }
        );
        this.addShortcut(
            ['meta+-', 'ctrl+-'], function (event) {
                event.preventDefault();
                event.stopPropagation();

                designer.zoomOut();
            }
        );
        var me = this;
        this.addShortcut(
            'right', function (event) {
                var node = model.selectedTopic();
                if (node) {
                    if (node.isCentralTopic()) {
                        me._goToSideChild(designer, node, 'RIGHT');
                    }
                    else {
                        if (node.getPosition().x < 0) {
                            me._goToParent(designer, node);
                        }
                        else if (!node.areChildrenShrunken()) {
                            me._goToChild(designer, node);
                        }
                    }
                } else {
                    var centralTopic = model.getCentralTopic();
                    me._goToNode(designer, centralTopic);
                }
                event.preventDefault();
                event.stopPropagation();
            }
        );
        this.addShortcut(
            'left', function (event) {
                var node = model.selectedTopic();
                if (node) {
                    if (node.isCentralTopic()) {
                        me._goToSideChild(designer, node, 'LEFT');
                    }
                    else {
                        if (node.getPosition().x > 0) {
                            me._goToParent(designer, node);
                        }
                        else if (!node.areChildrenShrunken()) {
                            me._goToChild(designer, node);
                        }
                    }
                } else {
                    var centralTopic = model.getCentralTopic();
                    me._goToNode(designer, centralTopic);
                }
                event.preventDefault();
                event.stopPropagation();
            }
        );
        this.addShortcut(
            'up', function (event) {
                var node = model.selectedTopic();
                if (node) {
                    if (!node.isCentralTopic()) {
                        me._goToBrother(designer, node, 'UP');
                    }
                } else {
                    var centralTopic = model.getCentralTopic();
                    me._goToNode(designer, centralTopic);
                }
                event.preventDefault();
                event.stopPropagation();
            }
        );
        this.addShortcut(
            'down', function (event) {
                var node = model.selectedTopic();
                if (node) {
                    if (!node.isCentralTopic()) {
                        me._goToBrother(designer, node, 'DOWN');
                    }
                } else {
                    var centralTopic = model.getCentralTopic();
                    me._goToNode(designer, centralTopic);
                }
                event.preventDefault();
                event.stopPropagation();
            }
        );
        var excludes = ['esc', 'escape', 'f1', 'f3', 'f4', 'f5', 'f6', 'f7', 'f8', 'f9', 'f10', 'f11', 'f12'];
        
        $(document).on('keypress', function (event) {
            var keyCode;
            // Firefox doesn't skip special keys for keypress event...
            if (event.key && excludes.contains(event.key.toLowerCase())) {
                return;
            }
            // Sometimes Firefox doesn't contain keyCode value
            if (event.key && event.keyCode == 0) {
                keyCode = event.charCode;
            } else {
                keyCode = event.keyCode;
            }

            var specialKey = jQuery.hotkeys.specialKeys[keyCode];
            if (["enter", "capslock"].indexOf(specialKey) == -1 && !jQuery.hotkeys.shiftNums[keyCode]) {
                var nodes = designer.getModel().filterSelectedTopics();
                if (nodes.length > 0) {

                    // If a modifier is press, the key selected must be ignored.
                    var pressKey = String.fromCharCode(keyCode);
                    if (event.ctrlKey || event.altKey || event.metaKey) {
                        return;
                    }
                    nodes[0].showTextEditor(pressKey);
                    event.stopPropagation();
                }
            }
            
        });

    },

    _goToBrother:function (designer, node, direction) {
        var parent = node.getParent();
        if (parent) {
            var brothers = parent.getChildren();

            var target = node;
            var y = node.getPosition().y;
            var x = node.getPosition().x;
            var dist = null;
            for (var i = 0; i < brothers.length; i++) {
                var sameSide = (x * brothers[i].getPosition().x) >= 0;
                if (brothers[i] != node && sameSide) {
                    var brother = brothers[i];
                    var brotherY = brother.getPosition().y;
                    if (direction == "DOWN" && brotherY > y) {
                        var distancia = y - brotherY;
                        if (distancia < 0) {
                            distancia = distancia * (-1);
                        }
                        if (dist == null || dist > distancia) {
                            dist = distancia;
                            target = brothers[i];
                        }
                    }
                    else if (direction == "UP" && brotherY < y) {
                        var distance = y - brotherY;
                        if (distance < 0) {
                            distance = distance * (-1);
                        }
                        if (dist == null || dist > distance) {
                            dist = distance;
                            target = brothers[i];
                        }
                    }
                }
            }
            this._goToNode(designer, target);
        }
    },


    _goToSideChild:function (designer, node, side) {
        var children = node.getChildren();
        if (children.length > 0) {
            var target = children[0];
            var top = null;
            for (var i = 0; i < children.length; i++) {
                var child = children[i];
                var childY = child.getPosition().y;
                if (side == 'LEFT' && child.getPosition().x < 0) {
                    if (top == null || childY < top) {
                        target = child;
                        top = childY;
                    }
                }
                if (side == 'RIGHT' && child.getPosition().x > 0) {
                    if (top == null || childY < top) {
                        target = child;
                        top = childY;
                    }
                }
            }

            this._goToNode(designer, target);
        }
    },

    _goToParent:function (designer, node) {
        var parent = node.getParent();
        if (parent) {
            this._goToNode(designer, parent);
        }
    },

    _goToChild:function (designer, node) {
        var children = node.getChildren();
        if (children.length > 0) {
            var target = children[0];
            var top = target.getPosition().y;
            for (var i = 0; i < children.length; i++) {
                var child = children[i];
                if (child.getPosition().y < top) {
                    top = child.getPosition().y;
                    target = child;
                }
            }
            this._goToNode(designer, target);
        }
    },

    _goToNode:function (designer, node) {
        // First deselect all the nodes ...
        designer.deselectAll();

        // Give focus to the selected node....
        node.setOnFocus(true);
    }

});

mindplot.DesignerKeyboard.specialKeys = {
    8: "backspace", 9: "tab", 10: "return", 13: "enter", 16: "shift", 17: "ctrl", 18: "alt", 19: "pause",
    20: "capslock", 27: "esc", 32: "space", 33: "pageup", 34: "pagedown", 35: "end", 36: "home",
    37: "left", 38: "up", 39: "right", 40: "down", 45: "insert", 46: "del",
    96: "0", 97: "1", 98: "2", 99: "3", 100: "4", 101: "5", 102: "6", 103: "7",
    104: "8", 105: "9", 106: "*", 107: "+", 109: "-", 110: ".", 111 : "/",
    112: "f1", 113: "f2", 114: "f3", 115: "f4", 116: "f5", 117: "f6", 118: "f7", 119: "f8",
    120: "f9", 121: "f10", 122: "f11", 123: "f12", 144: "numlock", 145: "scroll", 186: ";", 191: "/",
    220: "\\", 222: "'", 224: "meta"
};