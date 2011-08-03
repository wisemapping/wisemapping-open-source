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

mindplot.TextEditor = new Class({
    initialize:function(designer) {
        this._designer = designer;
        this._screenManager = designer.getWorkSpace().getScreenManager();
        this._container = this._screenManager.getContainer();
        this._isVisible = false;

        //Create editor ui
        this._createUI();

        this._addListeners();

    },

    _createUI:function() {
        this._size = {width:500, height:100};
        this._myOverlay = new Element('div').setStyles({position:"absolute", display: "none", zIndex: "8", top: 0, left:0, width:"500px", height:"100px"});
        var inputContainer = new Element('div').setStyles({border:"none", overflow:"auto"}).inject(this._myOverlay);
        this.inputText = new Element('input').setProperties({type:"text", tabindex:'-1', id:"inputText", value:""}).setStyles({border:"none", background:"transparent"}).inject(inputContainer);
        var spanContainer = new Element('div').setStyle('visibility', "hidden").inject(this._myOverlay);
        this._spanText = new Element('span').setProperties({id: "spanText", tabindex:"-1"}).setStyle('white-space', "nowrap").setStyle('nowrap', 'nowrap').inject(spanContainer);
        this._myOverlay.inject(this._container);
    },

    _addListeners:function() {
        var elem = this;
        this.applyChanges = true;
        this.inputText.onkeyup = function (evt) {
            var event = new Event(evt);
            var key = event.key;
            switch (key) {
                case 'esc':
                    elem.applyChanges = false;
                case 'enter':
                    var executor = function(editor) {
                        return function() {
                            elem.lostFocus(true);
                            $(document.documentElement).fireEvent('focus');
                        };
                    };
                    setTimeout(executor(this), 3);

                    break;
                default:
                    var span = $('spanText');
                    var input = $('inputText');
                    span.innerHTML = input.value;
                    var size = input.value.length + 1;
                    input.size = size;
                    if (span.offsetWidth > (parseInt(elem._myOverlay.style.width) - 100)) {
                        elem._myOverlay.style.width = (span.offsetWidth + 100) + "px";
                    }
                    break;
            }
        };
        //Register onLostFocus/onBlur event
        $(this.inputText).addEvent('blur', this.lostFocusEvent.bind(this));
        $(this._myOverlay).addEvent('click', this.clickEvent.bindWithEvent(this));
        $(this._myOverlay).addEvent('dblclick', this.clickEvent.bindWithEvent(this));
        $(this._myOverlay).addEvent('mousedown', this.mouseDownEvent.bindWithEvent(this));

        var elem = this;
        var onComplete = function() {
            this._myOverlay.setStyle('display', "none");
            this._isVisible = false;
            this.inputText.setStyle('opacity', 1);

            this.setPosition(0, 0);
            if (elem._currentNode != null) {
                this._currentNode.getTextShape().setVisibility(true);
                if (this.applyChanges) {
                    this._updateNode();
                }
                this.applyChanges = true;
                this._currentNode = null;
            }

            setTimeout("$('ffoxWorkarroundInput').focus();", 0);
        };
        this.fx = new Fx.Tween(this.inputText, {property: 'opacity', duration: 10});
        this.fx.addEvent('onComplete', onComplete.bind(this));
    },

    lostFocusEvent : function () {
        this.fx.options.duration = 10;
        this.fx.start(1, 0);
        //myAnim.animate();
    },

    isVisible : function () {
        return this._isVisible;
    },

    getFocusEvent: function (node) {
        //console.log('focus event');
        if (this.isVisible()) {
            this.getFocusEvent.delay(10, this);
        }
        else {
            //console.log('calling init');
            this.init(node);
        }
        //console.log('focus event done');
    },

    setInitialText : function (text) {
        this.initialText = text;
    },

    _updateNode : function () {

        if ($defined(this._currentNode) && this._currentNode.getText() != this.getText()) {
            var text = this.getText();
            var topicId = this._currentNode.getId();

            var commandFunc = function(topic, value) {
                var result = topic.getText();
                topic.setText(value);
                return result;
            };
            var command = new mindplot.commands.GenericFunctionCommand(commandFunc, text, [topicId]);
            this._actionRunner.execute(command);
        }
    },

    listenEventOnNode : function(topic, eventName, stopPropagation) {
        var elem = this;
        topic.addEventListener(eventName, function (event) {
            if (elem._designer.getWorkSpace().isWorkspaceEventsEnabled()) {
                mindplot.EventBus.instance.fireEvent(mindplot.EventBus.events.NodeMouseOutEvent, [topic ]);
                elem.lostFocus();
                elem.getFocusEvent.attempt(topic, elem);

                if (stopPropagation) {
                    if ($defined(event.stopPropagation)) {
                        event.stopPropagation(true);
                    } else {
                        event.cancelBubble = true;
                    }
                }
            }
        });
    },

    init : function (nodeGraph) {
        //console.log('init method');
        nodeGraph.getTextShape().setVisibility(false);
        this._currentNode = nodeGraph;

        //set Editor Style
        var nodeText = nodeGraph.getTextShape();
        var text;
        var selectText = true;
        if (this.initialText && this.initialText != "") {
            text = this.initialText;
            this.initialText = null;
            selectText = false;
        }
        else
            text = nodeText.getText();

        var font = nodeText.getFont();
        font.size = nodeText.getHtmlFontSize();
        font.color = nodeText.getColor();

        this.setStyle(font);

        //set editor's initial text
        this.setText(text);

        //set editor's initial size
        var editor = this;
        var executor = function(editor) {
            return function() {
                //console.log('setting editor in init thread');
                var scale = web2d.peer.utils.TransformUtil.workoutScale(editor._currentNode.getTextShape()._peer);
                var elemSize = editor._currentNode.getSize();
                //var textSize = editor.getSize();
                var pos = editor._screenManager.getWorkspaceElementPosition(editor._currentNode);

                var textWidth = editor._currentNode.getTextShape().getWidth();
                var textHeight = editor._currentNode.getTextShape().getHeight();
                var iconGroup = editor._currentNode.getIconGroup();
                var iconGroupSize;
                if ($defined(iconGroup)) {
                    iconGroupSize = editor._currentNode.getIconGroup().getSize();
                }
                else {
                    iconGroupSize = {width:0, height:0};
                }
                var position = {x:0,y:0};
                position.x = pos.x - ((textWidth * scale.width) / 2) + (((iconGroupSize.width) * scale.width) / 2);
                var fixError = 1;
                position.y = pos.y - ((textHeight * scale.height) / 2) - fixError;

                editor.setEditorSize(elemSize.width, elemSize.height, scale);
                //console.log('setting position:'+pos.x+';'+pos.y);
                editor.setPosition(position.x, position.y, scale);
                editor.showTextEditor(selectText);
                //console.log('setting editor done');
            };
        };

        setTimeout(executor(this), 10);
    },

    setStyle : function (fontStyle) {
        var inputField = $("inputText");
        var spanField = $("spanText");
        if (!$defined(fontStyle.font)) {
            fontStyle.font = "Arial";
        }
        if (!$defined(fontStyle.style)) {
            fontStyle.style = "normal";
        }
        if (!$defined(fontStyle.weight)) {
            fontStyle.weight = "normal";
        }
        if (!$defined(fontStyle.size)) {
            fontStyle.size = 12;
        }
        inputField.style.fontSize = fontStyle.size + "px";
        inputField.style.fontFamily = fontStyle.font;
        inputField.style.fontStyle = fontStyle.style;
        inputField.style.fontWeight = fontStyle.weight;
        inputField.style.color = fontStyle.color;
        spanField.style.fontFamily = fontStyle.font;
        spanField.style.fontStyle = fontStyle.style;
        spanField.style.fontWeight = fontStyle.weight;
        spanField.style.fontSize = fontStyle.size + "px";
    },

    setText : function(text) {
        var inputField = $("inputText");
        inputField.size = text.length + 1;
        //this._myOverlay.cfg.setProperty("width", (inputField.size * parseInt(inputField.style.fontSize) + 100) + "px");
        this._myOverlay.style.width = (inputField.size * parseInt(inputField.style.fontSize) + 100) + "px";
        var spanField = $("spanText");
        spanField.innerHTML = text;
        inputField.value = text;
    },

    getText : function() {
        return $('inputText').value;
    },

    setEditorSize : function (width, height, scale) {
        //var scale = web2d.peer.utils.TransformUtil.workoutScale(this._currentNode.getTextShape()._peer);
        this._size = {width:width * scale.width, height:height * scale.height};
        //this._myOverlay.cfg.setProperty("width",this._size.width*2+"px");
        this._myOverlay.style.width = this._size.width * 2 + "px";
        //this._myOverlay.cfg.setProperty("height",this._size.height+"px");
        this._myOverlay.style.height = this._size.height + "px";
    },

    getSize : function () {
        return {width:$("spanText").offsetWidth,height:$("spanText").offsetHeight};
    },

    setPosition : function (x, y, scale) {
        $(this._myOverlay).setStyles({top : y + "px", left: x + "px"});
        //this._myOverlay.style.left = x + "px";
    },

    showTextEditor : function(selectText) {
        //this._myOverlay.show();
        //var myAnim = new YAHOO.util.Anim('inputText',{opacity: {to:1}}, 0.10, YAHOO.util.Easing.easeOut);
        //$('inputText').style.opacity='1';
        var elem = this;
        //myAnim.onComplete.subscribe(function(){
        //elem._myOverlay.show();
        elem._myOverlay.setStyle('display', "block");
        this._isVisible = true;
        //elem.cfg.setProperty("visible", false);
        //elem._myOverlay.cfg.setProperty("xy", [0, 0]);
        //elem._myOverlay.cfg.setProperty("visible", true);
        //select the text in the input
        $('inputText').disabled = false;

        if ($('inputText').createTextRange) //ie
        {
            var range = $('inputText').createTextRange();
            var pos = $('inputText').value.length;
            if (selectText) {
                range.select();
                range.move("character", pos);
            }
            else {
                range.move("character", pos);
                range.select();
            }
        }
        else if (selectText) {
            $('inputText').setSelectionRange(0, $('inputText').value.length);
        }

        var executor = function(editor) {
            return function() {
                try {
                    $('inputText').focus();
                }
                catch (e) {

                }
            };
        };
        setTimeout(executor(this), 0);
        //});
        //myAnim.animate();

    },

    lostFocus : function(bothBrowsers) {
        if (this.isVisible()) {
            //the editor is opened in another node. lets Finish it.
            var fireOnThis = $('inputText');
            fireOnThis.fireEvent('blur');
        }
    },
    clickEvent : function(event) {
        if (this.isVisible()) {
            if ($defined(event.stopPropagation)) {
                event.stopPropagation(true);
            } else {
                event.cancelBubble = true;
            }
            event.preventDefault();
        }

    },
    mouseDownEvent : function(event) {
        if (this.isVisible()) {
            if ($defined(event.stopPropagation)) {
                event.stopPropagation(true);
            } else {
                event.cancelBubble = true;
            }
        }
    }

});

