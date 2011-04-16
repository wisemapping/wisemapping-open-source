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

web2d.peer.svg.TextPeer = function()
{
    var svgElement = window.document.createElementNS(this.svgNamespace, 'text');
    web2d.peer.svg.ElementPeer.call(this, svgElement);
    this._native.setAttribute("focusable", "true");
    this._position = {x:0,y:0};
    this._font = new web2d.Font("Arial", this);
};

objects.extend(web2d.peer.svg.TextPeer, web2d.peer.svg.ElementPeer);

//todo: use ths method to specify the maximum size of the text box
/*web2d.web2d.peer.svg.TextPeer.prototype.setSize = function(width, height)
{
    web2d.web2d.peer.svg.TextPeer.superClass.setSize.call(this,width,height);
    this._native.setAttribute('rx', width / 2);
    this._native.setAttribute('ry', height /ose 2);
};
*/

web2d.peer.svg.TextPeer.prototype.appendChild = function(element)
{
    this._native.appendChild(element._native);
};

web2d.peer.svg.TextPeer.prototype.setText = function(text)
{
    text = core.Utils.escapeInvalidTags(text);
    var child = this._native.firstChild;
    if (core.Utils.isDefined(child))
    {
        this._native.removeChild(child);
    }
    this._text = text;
    var textNode = window.document.createTextNode(text);
    this._native.appendChild(textNode);
};

web2d.peer.svg.TextPeer.prototype.getText = function()
{
    return this._text;
};

web2d.peer.svg.TextPeer.prototype.setPosition = function(x, y)
{
    this._position = {x:x, y:y};
    var height = this._font.getSize();
    if(core.Utils.isDefined(this._parent) && core.Utils.isDefined(this._native.getBBox))
        height = this.getHeight();
    var size = parseInt(height);
    this._native.setAttribute('y', y+size*3/4);
    //y+size/2
    this._native.setAttribute('x', x);
};

web2d.peer.svg.TextPeer.prototype.getPosition = function()
{
    return this._position;
};

web2d.peer.svg.TextPeer.prototype.setFont = function(font, size, style, weight)
{
    if (core.Utils.isDefined(font))
    {
        this._font = new web2d.Font(font, this);
    }
    if (core.Utils.isDefined(style))
    {
        this._font.setStyle(style);
    }
    if (core.Utils.isDefined(weight))
    {
        this._font.setWeight(weight);
    }
    if (core.Utils.isDefined(size))
    {
        this._font.setSize(size);
    }
    this._updateFontStyle();
};

web2d.peer.svg.TextPeer.prototype._updateFontStyle = function()
{
    this._native.setAttribute('font-family', this._font.getFontFamily());
    this._native.setAttribute('font-size', this._font.getGraphSize());
    this._native.setAttribute('font-style', this._font.getStyle());
    this._native.setAttribute('font-weight', this._font.getWeight());

    var scale = this._font.getFontScale();
    this._native.xFontScale = scale.toFixed(1);

};
web2d.peer.svg.TextPeer.prototype.setColor = function(color)
{
    this._native.setAttribute('fill', color);
};

web2d.peer.svg.TextPeer.prototype.getColor = function()
{
    return this._native.getAttribute('fill');
};

web2d.peer.svg.TextPeer.prototype.setTextSize = function (size)
{
    this._font.setSize(size);
    this._updateFontStyle();
};

web2d.peer.svg.TextPeer.prototype.setContentSize = function(width, height)
{
    this._native.xTextSize = width.toFixed(1) + "," + height.toFixed(1);
};

web2d.peer.svg.TextPeer.prototype.setStyle = function (style)
{
    this._font.setStyle(style);
    this._updateFontStyle();
};

web2d.peer.svg.TextPeer.prototype.setWeight = function (weight)
{
    this._font.setWeight(weight);
    this._updateFontStyle();
};

web2d.peer.svg.TextPeer.prototype.setFontFamily = function (family)
{
    var oldFont = this._font;
    this._font = new web2d.Font(family, this);
    this._font.setSize(oldFont.getSize());
    this._font.setStyle(oldFont.getStyle());
    this._font.setWeight(oldFont.getWeight());
    this._updateFontStyle();
};

web2d.peer.svg.TextPeer.prototype.getFont = function ()
{
    return {
        font:this._font.getFont(),
        size:parseInt(this._font.getSize()),
        style:this._font.getStyle(),
        weight:this._font.getWeight()
    };
};

web2d.peer.svg.TextPeer.prototype.setSize = function (size)
{
    this._font.setSize(size);
    this._updateFontStyle();
};

web2d.peer.svg.TextPeer.prototype.getWidth = function ()
{
    var computedWidth = this._native.getBBox().width;
    var width = parseInt(computedWidth);
    width = width + this._font.getWidthMargin();
    return width;
};

web2d.peer.svg.TextPeer.prototype.getHeight = function ()
{
    var computedHeight = this._native.getBBox().height;
    return parseInt(computedHeight);
};

web2d.peer.svg.TextPeer.prototype.getHtmlFontSize = function ()
{
    return this._font.getHtmlSize();
};

