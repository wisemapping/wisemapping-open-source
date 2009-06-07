/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License.  You may obtain a copy of the License at
*
*       http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*
* $Id: file 64488 2006-03-10 17:32:09Z paulo $
*/

mindplot.BubbleTip = function(divContainer){
    this.initialize(divContainer);
    };
mindplot.BubbleTip.prototype.initialize=function(divContainer){
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
        if($chk(this.options.form))
            this.scanElements(this.options.form);
        this.buildBubble();
        this._isMouseOver=false;
        this._open=false;
    };
mindplot.BubbleTip.prototype.scanElements=function(form){
        $$($(form).getElements('a')).each(function(el) {
            if (el.href && el.hasClass('bubble') && !el.onclick) {
                el.addEvent('mouseover',this.click.bindWithEvent(this,el));
            }
        }, this);
    };
mindplot.BubbleTip.prototype.buildBubble=function(invert){
        var opts = this.options;

        var panel = new Element('div').addClass('bubbleContainer');
        if($chk(opts.height))
            panel.setStyle('height', opts.height);
        if($chk(opts.width))
            panel.setStyle('width', opts.width);

        var topClass="";
        var bottomClass="Hint";
        if($chk(invert)){
            var tmpClass = topClass;
            topClass=bottomClass;
            bottomClass=tmpClass;
        }

        //build top part of bubble
        this.topContainer = new Element('div').addClass('bublePartContainer');
        this.topLeft = new Element('div').addClass('bubblePart').addClass('bubble'+topClass+'TopLeftBlue');
        this.top = new Element('div').addClass('bubblePart').addClass('bubble'+topClass+'TopBlue');
        this.topHint =new Element('div').addClass('bubblePart').addClass('bubbleTop'+topClass+'Blue').setStyle('width',58);
        this.top2 = new Element('div').addClass('bubblePart').addClass('bubble'+topClass+'TopBlue');
        this.topRight = new Element('div').addClass('bubblePart').addClass('bubble'+topClass+'TopRightBlue');
        this.topLeft.inject(this.topContainer);
        this.top.inject(this.topContainer);
        this.topHint.inject(this.topContainer);
        this.top2.inject(this.topContainer);
        this.topRight.inject(this.topContainer);

        //build middle part of bubble
        this.middleContainer = new Element('div').addClass('bublePartContainer');
        this.left = new Element('div').addClass('bubblePart').addClass('bubbleLeftBlue');
        this.center = new Element('div').addClass('bubblePart').addClass('bubbleCenterBlue');
        this.right = new Element('div').addClass('bubblePart').addClass('bubbleRightBlue');
        this.left.inject(this.middleContainer);
        this.center.inject(this.middleContainer);
        this.right.inject(this.middleContainer);

        //build bottom part of bubble
        this.bottomContainer = new Element('div').addClass('bublePartContainer');
        this.bottomLeft = new Element('div').addClass('bubblePart').addClass('bubble'+bottomClass+'BottomLeftBlue');
        this.bottom = new Element('div').addClass('bubblePart').addClass('bubble'+bottomClass+'BottomBlue');
        this.bottomHint =new Element('div').addClass('bubblePart').addClass('bubbleBottom'+bottomClass+'Blue').setStyle('width',58);
        this.bottom2 = new Element('div').addClass('bubblePart').addClass('bubble'+bottomClass+'BottomBlue');
        this.bottomRight = new Element('div').addClass('bubblePart').addClass('bubble'+bottomClass+'BottomRightBlue');
        this.bottomLeft.inject(this.bottomContainer);
        this.bottom.inject(this.bottomContainer);
        this.bottomHint.inject(this.bottomContainer);
        this.bottom2.inject(this.bottomContainer);
        this.bottomRight.inject(this.bottomContainer);

        this.topContainer.inject(panel);
        this.middleContainer.inject(panel);
        this.bottomContainer.inject(panel);

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
mindplot.BubbleTip.prototype.click= function(event, el) {
        return this.open(event, el);
    };
mindplot.BubbleTip.prototype.open= function(event, content, source){
        this._isMouseOver=true;
        this._evt = new Event(event);
        this.doOpen.delay(500, this,[content,source]);
    };
mindplot.BubbleTip.prototype.doOpen= function(content, source){
        if($chk(this._isMouseOver) &&!$chk(this._open) && !$chk(this._opening))
        {
            this._opening=true;
            var container = new Element('div');
            $(content).inject(container);
            this.options.content=content;
            this.options.container=container;
            $(this.options.container).inject(this.center);
            this.init(this._evt,source);
            $(this.options.panel).effect('opacity',{duration:500, onComplete:function(){this._open=true; this._opening = false;}.bind(this)}).start(0,100);
        }
    };
mindplot.BubbleTip.prototype.updatePosition=function(event){
        this._evt = new Event(event);
    };
mindplot.BubbleTip.prototype.close=function(event){
        this._isMouseOver=false;
        this.doClose.delay(50,this,new Event(event));
    };
mindplot.BubbleTip.prototype.doClose=function(event){

        if(!$chk(this._isMouseOver) && $chk(this._opening))
            this.doClose.delay(500,this,this._evt);
        
        if(!$chk(this._isMouseOver) && $chk(this._open))
        {
            this.forceClose();
        }
    };
mindplot.BubbleTip.prototype.forceClose=function(){
        this.options.panel.effect('opacity',{duration:100, onComplete:function(){
            this._open=false;
            $(this.options.panel).setStyles({left:0,top:0});
            $(this.top2).setStyle('width', 3);
            $(this.bottom2).setStyle('width', 3);
            $(this.top).setStyle('width', 3);
            $(this.bottom).setStyle('width', 3);
            $(this.left).setStyle('height', 4);
            $(this.right).setStyle('height', 4);
            $(this.options.container).remove();
        }.bind(this)}).start(100,0);
    };
mindplot.BubbleTip.prototype.init=function(event,source){
        var opts = this.options;
        var coordinates = $(opts.panel).getCoordinates();
        var panelHeight = coordinates.height; //not total height, but close enough

        var offset = designer.getWorkSpace().getScreenManager().getWorkspaceIconPosition(source);

        var containerCoords = $(opts.divContainer).getCoordinates();
        var screenWidth = containerCoords.width;
        var screenHeight = containerCoords.height;

        var invert = false;
        var picoFix=20;

        var centerWidth = $(this.center).getCoordinates().width;

        if(offset.y > panelHeight){ //hint goes on the bottom
            if(!$(this.topLeft).hasClass('bubbleTopLeftBlue')){
                $(this.options.panel).remove();
                this.buildBubble(false);
                $(this.options.container).inject(this.center);
            }
        }
        else{
            invert=true;
            picoFix=0;
            if($(this.topLeft).hasClass('bubbleTopLeftBlue')){
                $(this.options.panel).remove();
                this.buildBubble(invert);
                $(this.options.container).inject(this.center);
            }
            centerWidth = centerWidth-1;
        }

        offset.y = offset.y + picoFix;
        var width = centerWidth - $(this.topHint).getCoordinates().width;

        if((screenWidth -offset.x)>coordinates.width){
            width= width-$(this.top).getCoordinates().width;
            $(this.top2).setStyle('width', width);
            $(this.bottom2).setStyle('width', width);
        }
        else{
            width= width-$(this.top2).getCoordinates().width;
            $(this.top).setStyle('width', width);
            $(this.bottom).setStyle('width', width);
        }

        width = centerWidth + $(this.topLeft).getCoordinates().width;
        //width = width + $(this.top).getCoordinates().width;
        //width = width + $(this.topHint).getCoordinates().width;
        width = width + $(this.topRight).getCoordinates().width;

        var height = $(this.center).getCoordinates().height;
        $(this.left).setStyle('height', height);
        $(this.right).setStyle('height', height);

        height = height+ $(this.topLeft).getCoordinates().height;
        height = height+ $(this.bottomLeft).getCoordinates().height;
        $(opts.panel).setStyles({width:width,height:height});

        this.moveTopic(offset, $(opts.panel).getCoordinates().height, invert);
    };
mindplot.BubbleTip.prototype.moveTopic=function(offset, panelHeight, invert){
        var f = 1;
        if($chk(invert))
            f=0;
        var opts = this.options;
        var width = $(this.bottomLeft).getCoordinates().width+$(this.bottom).getCoordinates().width-(2*(f-1));
        $(opts.panel).setStyles({left:offset.x - width, top:offset.y - (panelHeight*f)});
    };

mindplot.BubbleTip.getInstance = function(divContainer)
{
    var result = mindplot.BubbleTip.instance;
    if(!result)
    {
        mindplot.BubbleTip.instance = new mindplot.BubbleTip(divContainer);
        result = mindplot.BubbleTip.instance;
    }
    return result;
};


/*
buildAnchorContent:function(el, title){
        var imgContainer= new Element('div');
        var img = new Element('img');
        img.src='http://open.thumbshots.org/image.pxf?url='+el.href;
        img.inject(imgContainer);

        var attribution = new Element('div');
        attribution.innerHTML="<a href='http://www.thumbshots.org' target='_blank' title='About Thumbshots thumbnails'>About Thumbshots thumbnails</a>";

        var element = new Element('div');
        imgContainer.inject(element);
        attribution.inject(element);
        return element;
    }*/
