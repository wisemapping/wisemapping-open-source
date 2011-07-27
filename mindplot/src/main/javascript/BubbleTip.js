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

mindplot.BubbleTip = new Class({
    initialize : function(divContainer) {
        this.options = {
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
        if ($chk(this.options.form))
            this.scanElements(this.options.form);
        this.buildBubble();
        this._isMouseOver = false;
        this._open = false;
    },
    scanElements : function(form) {
        $$($(form).getElements('a')).each(function(el) {
            if (el.href && el.hasClass('bubble') && !el.onclick) {
                el.addEvent('mouseover', this.click.bindWithEvent(this, el));
            }
        }, this);
    },
    buildBubble : function() {
        var opts = this.options;

        var panel = new Element('div').addClass('bubbleContainer');
        if ($chk(opts.height))
            panel.setStyle('height', opts.height);
        if ($chk(opts.width))
            panel.setStyle('width', opts.width);

        this.center = new Element('div').addClass('bubblePart').addClass('bubbleCenterBlue');
        this.center.inject(panel);
        if (!$chk(opts.divContainer)) {
            opts.divContainer = document.body;
        }
        panel.injectTop(opts.divContainer);
        opts.panel = $(panel);
        opts.panel.setStyle('opacity', 0);
        opts.panel.addEvent('mouseover', function() {
            this._isMouseOver = true;
        }.bind(this));
        opts.panel.addEvent('mouseleave', function(event) {
            this.close(event);
        }.bindWithEvent(this));//this.close.bindWithEvent(this)

    },
    click : function(event, el) {
        return this.open(event, el);
    },
    open : function(event, content, source) {
        this._isMouseOver = true;
        this._evt = new Event(event);
        this.doOpen.delay(500, this, [content,source]);
    },
    doOpen : function(content, source) {
        if ($chk(this._isMouseOver) && !$chk(this._open) && !$chk(this._opening)) {
            this._opening = true;
            var container = new Element('div');
            $(content).inject(container);
            this.options.content = content;
            this.options.container = container;
            $(this.options.container).inject(this.center);
            this.init(this._evt, source);
            $(this.options.panel).effect('opacity', {duration:500, onComplete:function() {
                this._open = true;
                this._opening = false;
            }.bind(this)}).start(0, 100);
        }
    },
    updatePosition : function(event) {
        this._evt = new Event(event);
    },
    close : function(event) {
        this._isMouseOver = false;
        this.doClose.delay(50, this, new Event(event));
    },
    doClose : function(event) {

        if (!$chk(this._isMouseOver) && $chk(this._opening))
            this.doClose.delay(500, this, this._evt);

        if (!$chk(this._isMouseOver) && $chk(this._open)) {
            this.forceClose();
        }
    },
    forceClose : function() {
        this.options.panel.effect('opacity', {duration:100, onComplete:function() {
            this._open = false;
            $(this.options.panel).setStyles({left:0,top:0});
            $(this.options.container).remove();
        }.bind(this)}).start(100, 0);
    },
    init : function(event, source) {
        var opts = this.options;
        var coordinates = $(opts.panel).getCoordinates();
        var panelHeight = coordinates.height; //not total height, but close enough
        var panelWidth = coordinates.width; //not total height, but close enough

        var offset = designer.getWorkSpace().getScreenManager().getWorkspaceIconPosition(source);

        var containerCoords = $(opts.divContainer).getCoordinates();
        var screenWidth = containerCoords.width;
        var screenHeight = containerCoords.height;

        var width = $(this.center).getCoordinates().width;

        var invert = !(offset.y > panelHeight); //hint goes on the bottom
        var invertX = (screenWidth - offset.x > panelWidth); // hint goes on the right
        $(this.options.panel).remove();
        this.buildBubble();
        $(this.options.container).inject(this.center);

        var height = $(this.center).getCoordinates().height;
        $(opts.panel).setStyles({width:width,height:height});
        this.moveTopic(offset, $(opts.panel).getCoordinates().height, $(opts.panel).getCoordinates().width, invert, invertX);
    },
    moveTopic : function(offset, panelHeight, panelWidth, invert, invertX) {
        var f = 1, fX = 1;
        if ($chk(invert))
            f = 0;
        if ($chk(invertX))
            fX = 0;
        var opts = this.options;
        $(opts.panel).setStyles({left:offset.x - (panelWidth * fX), top:offset.y - (panelHeight * f)});
    }

});

mindplot.BubbleTip.getInstance = function(divContainer) {
    var result = mindplot.BubbleTip.instance;
    if (!core.Utils.isDefined(result)) {
        mindplot.BubbleTip.instance = new mindplot.BubbleTip(divContainer);
        result = mindplot.BubbleTip.instance;
    }
    return result;
}
