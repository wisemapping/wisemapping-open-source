/*
*    Copyright [2011] [wisemapping]
*
*   Licensed under the Apache License, Version 2.0 (the "License") plus the
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

var Panel = new Class({
    options:{
        width:'140px',
        height:250,
        opacity:0.9,
        panelButton:null,
        content:null,
        frame:null,
        onStart:Class.empty,
        onComplete:Class.empty
    },
    initialize:function(options){
        this.setOptions(options);

        this.buildBody();

        //atach listener to button
        this.options.panelButton.addEvent('click',this.openPanel.bindWithEvent(this));
    },
    buildBody:function(){
        var opts = this.options;
        opts.frame = new Element('div');
        if($chk(opts.content))
        {
            this.setContent(opts.content);
        }

    },
    setContent:function(content){
        this.options.content=content;
        this.options.content.inject(this.options.frame);
        this.setInitialStyles();
        this.options.frame.injectBefore(this.options.panelButton);
    },
    setInitialStyles:function(){
        var opts = this.options;
        var buttonTop = opts.panelButton.getTop();
        var buttonLeft = opts.panelButton.getLeft();
        var bodyHeight = parseInt(opts.panelButton.getParent().getStyle('height'));
        var coordinates = opts.panelButton.getCoordinates();
        var width = opts.width || coordinates.width;
        var elemCoords = {
                            top:'0px',
                            left:coordinates.left,
                            width:width,
                            height:'0px',
                            zIndex:'10',
                            overflow:'hidden'
                         };
        var elemStyles = {
                            backgroundColor: opts.backgroundColor||'blue',
                            opacity: opts.opacity,
                            position:'absolute'
                         }
        opts.frame.setStyles(elemCoords).setStyles(elemStyles);
    },
    openPanel:function(){
        this.fireEvent('onStart');

        var button = this.options.panelButton;
        button.removeEvents('click');
        button.addEvent('click',this.hidePanel.bindWithEvent(this));
        var top = parseInt(this.options.frame.getStyle('top'));
        var fx = this.options.frame.effects({duration:500, onComplete:function(){this.options.content.fireEvent('show');}.bind(this)});
        fx.start({'height':[0,this.options.height],'top':[top, top-this.options.height]});

        this.fireEvent('onComplete');
    },
    hidePanel:function(){
        this.fireEvent('onStart');

        var button = this.options.panelButton;
        button.removeEvents('click');
        button.addEvent('click',this.openPanel.bindWithEvent(this));
        var top = parseInt(this.options.frame.getStyle('top'));
        this.options.content.fireEvent('hide');
        var fx = this.options.frame.effects({duration:500});
        fx.start({'height':[this.options.height,0],'top':[top, top+this.options.height]});

        this.fireEvent('onComplete');
    }
});

Panel.implement(new Events, new Options);