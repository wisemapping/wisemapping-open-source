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

mindplot.RichTextEditor = mindplot.TextEditor.extend({
    initialize:function(screenManager,actionRunner){
        this.parent(screenManager, actionRunner);
    },
    _createUI:function(){
        //Create editor ui
        this._size = {width:440, height:200};
        this._myOverlay = new Element('div').setStyles({position:"absolute", display: "none", zIndex: "8", top: "50%", left:"50%", marginLeft:"-200px", marginTop:"-90px", width:"400px", height:"180px"});
        var inputContainer = new Element('div').setStyles({border:"none", overflow:"auto"}).injectInside(this._myOverlay);
        this.inputText = new Element('textarea').setProperties({tabindex:'-1', id:"inputText2", value:""}).setStyles({width:"398px", height:"175px", border:"none", background:"transparent"}).injectInside(inputContainer);
        /*var spanContainer = new Element('div').setStyle('visibility', "hidden").injectInside(this._myOverlay);
        this._spanText = new Element('span').setProperties({id: "spanText2", tabindex:"-1"}).setStyle('white-space', "nowrap").setStyle('nowrap', 'nowrap').injectInside(spanContainer);
        */this._myOverlay.injectInside(this._screenManager.getContainer());
        this._editorNode = new web2d.Rect(0.3,mindplot.Topic.OUTER_SHAPE_ATTRIBUTES);
        this._editorNode.setSize(50,20);
        this._editorNode.setVisibility(false);
        this._designer.getWorkSpace().appendChild(this._editorNode);

//        $(this.inputText).setStyle('display','block');
        this._addListeners();
    },
    _addListeners:function(){

        $(this._myOverlay).addEvent('click', function(event){
            event.preventDefault();
            event.stop();
        }.bindWithEvent(this));
        $(this._myOverlay).addEvent('dblclick', function(event){
            event.preventDefault();
            event.stop();
        }.bindWithEvent(this));
    },
    getFocusEvent:function(node){
        var screenSize = this._designer.getWorkSpace().getSize();
        var coordOrigin = this._designer.getWorkSpace()._workspace.getCoordOrigin();
        var middlePosition = {x:parseInt(screenSize.width)/2 + parseInt(coordOrigin.x), y:parseInt(screenSize.height)/2 + parseInt(coordOrigin.y)};

        this._designer.getWorkSpace().enableWorkspaceEvents(false);
        var position = node.getPosition().clone();
        var size = node.getSize();
        this._editorNode.setPosition(position.x-(size.width/2), position.y-(size.height/2));
        position = this._editorNode.getPosition();
        this._editorNode.setSize(size.width, size.height);
        this._editorNode.moveToFront();
        this._editorNode.setVisibility(true);
        var scale = web2d.peer.utils.TransformUtil.workoutScale(node.getOuterShape());
//        scale.width=1;
//        scale.height = 1;
        var steps = 10;
        this._delta = {width:((this._size.width/scale.width)-size.width)/steps, height:((this._size.height/scale.height)-size.height)/steps};
        var finx = (middlePosition.x-(((this._size.width)/2)/scale.width));
        var finy = (middlePosition.y-((this._size.height/2)/scale.height));
        var step = 10;
        var d = {x:(position.x - finx)/step, y:(position.y - finy)/step};
        var _animEffect = null;
        var effect = function(){
            if(step>=0){
                var xStep= (position.x -finx)/step;
                var yStep= (position.y -finy)/step;
                var pos = {x:position.x - d.x*(10-step), y: position.y -d.y *(10-step)};

                var size = this._editorNode.getSize();
                this._editorNode.setSize(size.width + this._delta.width, size.height + this._delta.height);
                this._editorNode.setPosition(pos.x, pos.y);
                if(step>0)
                    this._editorNode.setOpacity(1-step/10);
                step--;
            }else{
                $clear(_animEffect);
                this._editorNode.setSize((this._size.width/scale.width), (this._size.height/scale.height));
                this.init(node);
            }
        }.bind(this);
        _animEffect = effect.periodical(10);
        $(this.inputText).value = core.Utils.isDefined(this.initialText)&& this.initialText!=""? this.initialText: node.getText();
        this._editor = new nicEditor({iconsPath: '../images/nicEditorIcons.gif', buttonList : ['bold','italic','underline','removeformat','forecolor', 'fontSize', 'fontFamily', 'xhtml']}).panelInstance("inputText2");
    },
    init:function(node){
        this._currentNode = node;
        this.applyChanges = false;
        $(this._myOverlay.setStyle('display','block'));
        inst = this._editor.instanceById("inputText2");
        inst.elm.focus();



        //becarefull this._editor is not mootools!!
        this._editor.addEvent('blur',function(event){
            this._myOverlay.setStyle('display','none');
                var text = this._text;
                this._text = this._editor.instanceById("inputText2").getContent();
                if(text!=this._text){
                    this.applyChanges = true;
                }
                console.log('bye');
                this.lostFocusListener();
                this._editor.removeInstance("inputText2");
                this._editor.destruct();
                this._editor = null;

        }.bind(this));

        this._editor.fireEvent();
        $(this.inputText).focus();
    },
    getText:function(){
        return this._text;
    },
    lostFocusListener:function(){
        this._hideNode();
        if (this._currentNode != null)
            {
                if(this.applyChanges)
                {
                    this._updateNode();
                }
                this.applyChanges=true;
                this._currentNode = null;
            }
    },
    _hideNode:function(){
        var _animEffect = null;
        var step = 10;
        var position = this._editorNode.getPosition();
        var finx = this._currentNode.getPosition().x - this._currentNode.getSize().width/2;
        var finy = this._currentNode.getPosition().y - this._currentNode.getSize().height/2;
        var d = {x:(position.x - finx)/step, y:(position.y - finy)/step};
        var effect = function(){
            if(step>=0){
                var pos = {x:position.x - d.x*(10-step), y: position.y - d.y*(10-step)};

                var size = this._editorNode.getSize();
                this._editorNode.setSize(size.width - this._delta.width, size.height - this._delta.height);
                this._editorNode.setPosition(pos.x, pos.y);
                this._editorNode.setOpacity(step/10);
                step--;
            }else{
                $clear(_animEffect);
                this._designer.getWorkSpace().enableWorkspaceEvents(true);
                this._editorNode.setVisibility(false);            }
        }.bind(this);
        _animEffect = effect.periodical(10);
    }
});
