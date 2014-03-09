/*
 *    Copyright [2012] [wisemapping]
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
    //Extends: mindplot.Keyboard,
    Static:{
        register:function (designer) {
            this._instance = new mindplot.DesignerKeyboard(designer);
            //this._instance.activate();
        },

        getInstance:function () {
            return this._instance;
        }
    },

    initialize:function (designer) {
        //console.error("Re-impl required ....");
        $assert(designer, "designer can not be null");
        this._registerEvents(designer);
    },

    //FIXME: mover al parent
    addShortcut: function(shortcut, callback) {
        $(document).bind('keydown', shortcut, callback);
    },

    _registerEvents:function (designer) {

        // Try with the keyboard ..
        var model = designer.getModel();
        this.addShortcut(
            'backspace', function (event) {
                event.preventDefault();
                event.stopPropagation();
                designer.deleteSelectedEntities();
            }
        );
        this.addShortcut(
            'space', function() {
                designer.shrinkSelectedBranch();
            }
        );
        this.addShortcut(
            'f2',function() {
                var node = model.selectedTopic();
                if (node) {
                    node.showTextEditor();
                }
            }
        );
        this.addShortcut(
            'del', function(event) {
                designer.deleteSelectedEntities();
                event.preventDefault();
                event.stopPropagation();
            }
        );
        this.addShortcut(
            'enter', function() {
                designer.createSiblingForSelectedNode();
            }
        );
        this.addShortcut(
            'insert', function(event) {
                designer.createChildForSelectedNode();
                event.preventDefault();
                event.stopPropagation();
            }
        );
        this.addShortcut(
            'tab', function(event) {
                designer.createChildForSelectedNode();
                event.preventDefault();
                event.stopPropagation();
            }
        );
        this.addShortcut(
            '-', function() { // "-" is a insert on several Browsers. Don't ask why ...
                designer.createChildForSelectedNode();
            }
        );
        this.addShortcut(
            'meta+enter', function(event) {
                event.preventDefault();
                event.stopPropagation();
                designer.createChildForSelectedNode();
            }
        );
        this.addShortcut(
            'ctrl+z', function(event) {
                event.preventDefault(event);
                event.stopPropagation();
                designer.undo();
            }
        );
        this.addShortcut(
            'meta+z', function(event) {
                event.preventDefault();
                event.stopPropagation();
                designer.undo();
            }
        );
        this.addShortcut(
            'ctrl+c', function (event) {
                event.preventDefault(event);
                event.stopPropagation();
                designer.copyToClipboard();
            }
        );
        this.addShortcut(
            'meta+c', function (event) {
                event.preventDefault();
                event.stopPropagation();
                designer.copyToClipboard();
            }
        );
        this.addShortcut(
            'ctrl+v', function (event) {
                event.preventDefault(event);
                event.stopPropagation();
                designer.pasteClipboard();
            }
        );
        this.addShortcut(
            'meta+v', function (event) {
                event.preventDefault();
                event.stopPropagation();
                designer.pasteClipboard();
            }
        );
        this.addShortcut(
            'ctrl+z+shift', function (event) {
                event.preventDefault();
                event.stopPropagation();
                designer.redo();
            }
        );
        this.addShortcut(
            'meta+z+shift', function (event) {
                event.preventDefault();
                event.stopPropagation();
                designer.redo();
            }
        );
        this.addShortcut(
            'ctrl+y', function (event) {
                event.preventDefault();
                event.stopPropagation();
                designer.redo();
            }
        );
        this.addShortcut(
            'meta+y', function (event) {
                event.preventDefault();
                event.stopPropagation();
                designer.redo();
            }
        );
        this.addShortcut(
            'ctrl+a', function (event) {
                event.preventDefault();
                event.stopPropagation();
                designer.selectAll();
            }
        );
        this.addShortcut(
            'ctrl+b', function (event) {
                event.preventDefault();
                event.stopPropagation();

                designer.changeFontWeight();
            }
        );
        this.addShortcut(
            'meta+b', function (event) {
                event.preventDefault();
                event.stopPropagation();

                designer.changeFontWeight();
            }
        );
        this.addShortcut(
            'ctrl+s', function (event) {
                event.preventDefault();
                event.stopPropagation();
                document.id('save').fireEvent('click');
            }
        );
        this.addShortcut(
            'meta+s', function (event) {
                event.preventDefault();
                event.stopPropagation();

                document.id('save').fireEvent('click');
            }
        );
        this.addShortcut(
            'ctrl+i', function (event) {
                event.preventDefault();
                event.stopPropagation();

                designer.changeFontStyle();
            }
        );
        this.addShortcut(
            'meta+i', function (event) {
                event.preventDefault();
                event.stopPropagation();

                designer.changeFontStyle();
            }
        );
        this.addShortcut(
            'meta+shift+a', function (event) {
                event.preventDefault();
                event.stopPropagation();

                designer.deselectAll();
            }
        );
        this.addShortcut(
            'ctrl+shift+a', function (event) {
                event.preventDefault();
                event.stopPropagation();

                designer.deselectAll();
            }
        );
        this.addShortcut(
            'meta+a', function (event) {
                event.preventDefault();
                event.stopPropagation();

                designer.selectAll();
            }
        );
        this.addShortcut(
            'meta+=', function (event) {
                event.preventDefault();
                event.stopPropagation();

                designer.zoomIn();
            }
        );
        this.addShortcut(
            'meta+-', function (event) {
                event.preventDefault();
                event.stopPropagation();

                designer.zoomOut();
            }
        );
        this.addShortcut(
            'ctrl+=', function (event) {
                event.preventDefault();
                event.stopPropagation();

                designer.zoomIn();
            }
        );
        this.addShortcut(
            'ctrl+-', function (event) {
                event.preventDefault();
                event.stopPropagation();

                designer.zoomOut();
            }
        );
        this.addShortcut(
            'right', function (event) {
                var node = model.selectedTopic();
                if (node) {
                    if (node.isCentralTopic()) {
                        this._goToSideChild(designer, node, 'RIGHT');
                    }
                    else {
                        if (node.getPosition().x < 0) {
                            this._goToParent(designer, node);
                        }
                        else if (!node.areChildrenShrunken()) {
                            this._goToChild(designer, node);
                        }
                    }
                } else {
                    var centralTopic = model.getCentralTopic();
                    this._goToNode(designer, centralTopic);
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
                        this._goToSideChild(designer, node, 'LEFT');
                    }
                    else {
                        if (node.getPosition().x > 0) {
                            this._goToParent(designer, node);
                        }
                        else if (!node.areChildrenShrunken()) {
                            this._goToChild(designer, node);
                        }
                    }
                } else {
                    var centralTopic = model.getCentralTopic();
                    this._goToNode(designer, centralTopic);
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
                        this._goToBrother(designer, node, 'UP');
                    }
                } else {
                    var centralTopic = model.getCentralTopic();
                    this._goToNode(designer, centralTopic);
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
                        this._goToBrother(designer, node, 'DOWN');
                    }
                } else {
                    var centralTopic = model.getCentralTopic();
                    this._goToNode(designer, centralTopic);
                }
                event.preventDefault();
                event.stopPropagation();
            }
        );

        var regex = /^(?:shift|control|ctrl|alt|meta)$/;
        var modifiers = ['shift', 'control', 'alt', 'meta'];

        var excludes = ['esc', 'capslock', 'tab', 'f1', 'f3', 'f4', 'f5', 'f6', 'f7', 'f8', 'f9', 'f10', 'f11', 'f12', 'backspace', 'down', 'up', 'left', 'right', 'control'];
        if (!Browser.Platform.mac) {
            // This is to avoid enter on edition mode in the node when alt+tab is pressed.
            excludes.push("alt");
        }

/*
        document.id(document).addEvent('keydown', function (event) {

            // Convert key to mootools keyboard event format...
            var keys = [];
            modifiers.each(function (mod) {
                if (event[mod]) keys.push(mod);
            });
            if (!regex.test(event.key))
                keys.push(event.key);
            var key = keys.join('+');

            // Is the pressed key one of the already registered in the keyboard  ?
            var isRegistered = false;
            for (var eKey in  keyboardEvents) {
                if (eKey == key) {
                    isRegistered = true;
                    break;
                }
            }

            // If it's not registered, let's
            if (!isRegistered && !excludes.contains(key) && !excludes.contains(event.key) && !event.meta && !event.control) {
                var nodes = designer.getModel().filterSelectedTopics();
                if (nodes.length > 0) {

                    // If a modifier is press, the key selected must be ignored.
                    var pressKey = event.key;
                    if (modifiers.contains(event.key)) {
                        pressKey = "";
                    }

                    nodes[0].showTextEditor(pressKey);
                    event.stopPropagation();
                }
            }
        });
*/

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
