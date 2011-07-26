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

var IconPanel = new Class({
    Extends:Options,
    options:{
        width:253,
        initialWidth:0,
        height:200,
        content:null,
        panel:null,
        button:null,
        onStart:Class.empty,
        state:'close'
    },
    initialize:function(options){
        this.setOptions(options);
        if($chk(this.options.button))
        {
            this.init();
        }
    },
    setButton:function(button){
        this.options.button=button;
    },
    init:function(){
        var panel = new Element('div');
        var coord = this.options.button.getCoordinates();
        var top = this.options.button.getTop() + coord.height+2;
        var left = this.options.button.getLeft();
        panel.setStyles({width:this.options.initialWidth, height:0,position:'absolute', top:top, left:left, background:'#e5e5e5', border:'1px solid #BBB4D6', zIndex:20, overflow:'hidden'});
        this.options.panel=panel;
        this.options.content.inject(panel);

        this.options.content.addEvent('click', function(event) {
            this.close();
        }.bind(this));
        panel.setStyle('opacity',0);
        panel.inject($(document.body));
        this.registerOpenPanel();
    },
    open:function(){
        if(this.options.state=='close')
        {
            if(!$chk(this.options.panel))
            {
                this.init();
            }
            var panel = this.options.panel;
            var options = this.options;
            panel.setStyles({border: '1px solid #636163', opacity:100});
            this.fireEvent('onStart');
            var fx = panel.effects({duration:500, onComplete:function(){
                this.registerClosePanel();
            }.bind(this)});
            fx.start({'height':[0,this.options.height], 'width':[this.options.initialWidth, this.options.width]});
            this.options.state='open';

        }
    },
    close:function(){
        if(this.options.state=='open')
        {
            var fx = this.options.panel.effects({duration:500, onComplete:function(){
                this.options.panel.setStyles({border: '1px solid transparent', opacity:0});
                this.registerOpenPanel();
            }.bind(this)});
            fx.start({'height':[this.options.height,0], 'width':[this.options.width, this.options.initialWidth]});
            this.options.state = 'close';
        }
    },
    registerOpenPanel:function(){
        this.options.button.removeEvents('click');
        this.options.button.addEvent('click',function(event){
            this.open();
        }.bindWithEvent(this));
    },
    registerClosePanel:function(){
        this.options.button.removeEvents('click');
        this.options.button.addEvent('click', function(event){
            this.close();
        }.bindWithEvent(this));
    }
});