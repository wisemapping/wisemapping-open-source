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

mindplot.Tip = function(divContainer){
    this.initialize(divContainer);
};

mindplot.Tip.prototype.initialize=function(divContainer){
    this.options={
        panel:null,
        container:null,
        divContainer:divContainer,
        content:null,
        onShowComplete:Class.empty,
        onHideComplete:Class.empty,
        width:null,
        height:null,
        form:null
    };
        this.buildTip();
        this._isMouseOver=false;
        this._open=false;
};

mindplot.Tip.prototype.buildTip=function(){
        var opts = this.options;
        var panel = new Element('div').addClass('bubbleContainer');
        if($chk(opts.height))
            panel.setStyle('height', opts.height);
        if($chk(opts.width))
            panel.setStyle('width', opts.width);
        if(!$chk(opts.divContainer))
        {
            opts.divContainer=document.body;
        }
        panel.injectTop(opts.divContainer);
        opts.panel = $(panel);
        opts.panel.setStyle('opacity',0);
        opts.panel.addEvent('mouseover',function(){this._isMouseOver=true;}.bind(this));
        opts.panel.addEvent('mouseleave',function(event){this.close(event);}.bindWithEvent(this));//this.close.bindWithEvent(this)

};

mindplot.Tip.prototype.click= function(event, el) {
        return this.open(event, el);
};

mindplot.Tip.prototype.open= function(event, content, source){
        this._isMouseOver=true;
        this._evt = new Event(event);
        this.doOpen.delay(500, this,[content,source]);
};

mindplot.Tip.prototype.doOpen= function(content, source){
    if($chk(this._isMouseOver) &&!$chk(this._open) && !$chk(this._opening))
        {
            this._opening=true;
            var container = new Element('div');
            $(content).inject(container);
            this.options.content=content;
            this.options.container=container;
            $(this.options.container).inject(this.options.panel);
            this.init(this._evt,source);
            $(this.options.panel).effect('opacity',{duration:500, onComplete:function(){this._open=true; this._opening = false;}.bind(this)}).start(0,100);
        }
};

mindplot.Tip.prototype.updatePosition=function(event){
        this._evt = new Event(event);
};

mindplot.Tip.prototype.close=function(event){
        this._isMouseOver=false;
        this.doClose.delay(50,this,new Event(event));
};

mindplot.Tip.prototype.doClose=function(event){

        if(!$chk(this._isMouseOver) && $chk(this._opening))
            this.doClose.delay(500,this,this._evt);

        if(!$chk(this._isMouseOver) && $chk(this._open))
        {
            this.forceClose();
        }
};

mindplot.Tip.prototype.forceClose=function(){
        this.options.panel.effect('opacity',{duration:100, onComplete:function(){
            this._open=false;
            $(this.options.panel).setStyles({left:0,top:0});
            $(this.options.container).dispose();
        }.bind(this)}).start(100,0);
};

mindplot.Tip.prototype.init=function(event,source){
        var opts = this.options;
        var coordinates = $(opts.panel).getCoordinates();
        var width = coordinates.width;   //not total width, but close enough
        var height = coordinates.height; //not total height, but close enough

        var offset = designer.getWorkSpace().getScreenManager().getWorkspaceIconPosition(source);

        var containerCoords = $(opts.divContainer).getCoordinates();
        var screenWidth = containerCoords.width;
        var screenHeight = containerCoords.height;

        $(this.options.panel).dispose();
        this.buildTip();
        $(this.options.container).inject(this.options.panel);
        this.moveTopic(offset, $(opts.panel).getCoordinates().height);
};

mindplot.Tip.prototype.moveTopic=function(offset, panelHeight){
        var opts = this.options;
        var width = $(opts.panel).getCoordinates().width;
        $(opts.panel).setStyles({left:offset.x - (width/2), top:offset.y - (panelHeight*2) + 35});
};

mindplot.Tip.getInstance = function(divContainer)
{
    var result = mindplot.Tip.instance;
    if(!$defined(result))
    {
        mindplot.Tip.instance = new mindplot.Tip(divContainer);
        result = mindplot.Tip.instance;
    }
    return result;
};