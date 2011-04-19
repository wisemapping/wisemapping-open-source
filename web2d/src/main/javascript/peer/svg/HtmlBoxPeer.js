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

web2d.peer.svg.HtmlBoxPeer = function()
{
    var svgElement = window.document.createElementNS(this.svgNamespace, 'foreignObject');
    web2d.peer.svg.ElementPeer.call(this, svgElement);
    this._native.setAttribute("focusable", "true");
    svgElement.setAttribute('width',1);
    svgElement.setAttribute('height',1);
    $(svgElement).setAttribute("x",0);
    $(svgElement).setAttribute("y",0);

    this._position = {x:0,y:0};
    this._font = new web2d.Font("Arial", this);
    var body = window.document.createElementNS("http://www.w3.org/1999/xhtml",'body');
    this._body = window.document.createElementNS("http://www.w3.org/1999/xhtml",'div');
    $(body).setStyle("backgroundColor","transparent");
    $(this._body).setStyles({position:"absolute", left:0, top:0, whiteSpace:"nowrap"});
    body.appendChild(this._body);
    this._native.appendChild(body);
};

objects.extend(web2d.peer.svg.HtmlBoxPeer, web2d.peer.svg.ElementPeer);

//todo: use ths method to specify the maximum size of the text box
/*web2d.web2d.peer.svg.HtmlBoxPeer.prototype.setSize = function(width, height)
{
    web2d.web2d.peer.svg.HtmlBoxPeer.superClass.setSize.call(this,width,height);
    this._native.setAttribute('rx', width / 2);
    this._native.setAttribute('ry', height /ose 2);
};
*/

web2d.peer.svg.HtmlBoxPeer.prototype.appendChild = function(element)
{
    this._native.appendChild(element._native);
};

web2d.peer.svg.HtmlBoxPeer.prototype.setText = function(text)
{
    var child = this._body.firstChild;
    if (core.Utils.isDefined(child))
    {
        this._body.removeChild(child);
    }
    this._text = text;
    this._body.innerHTML=text;
    this.updateSize.delay(1, this);
};

web2d.peer.svg.HtmlBoxPeer.prototype.getText = function()
{
    return this._text;
};

web2d.peer.svg.HtmlBoxPeer.prototype.setPosition = function(x, y)
{
    this._position = {x:x, y:y};
    $(this._native).setAttribute('y', y);
    $(this._native).setAttribute('x', x);
};

web2d.peer.svg.HtmlBoxPeer.prototype.getPosition = function()
{
    return this._position;
};

web2d.peer.svg.HtmlBoxPeer.prototype.setFont = function(font, size, style, weight)
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

web2d.peer.svg.HtmlBoxPeer.prototype._updateFontStyle = function()
{
    $(this._body).setStyles({fontFamily: this._font.getFontFamily(), fontSize: this._font.getGraphSize(), fontStyle: this._font.getStyle(), fontWeight: this._font.getWeight()});

};
web2d.peer.svg.HtmlBoxPeer.prototype.setColor = function(color)
{
    $(this._body).setStyle('color', color);
};

web2d.peer.svg.HtmlBoxPeer.prototype.getColor = function()
{
    return $(this._body).getStyle('color');
};

web2d.peer.svg.HtmlBoxPeer.prototype.setStyle = function (style)
{
    this._font.setStyle(style);
    this._updateFontStyle();
};

web2d.peer.svg.HtmlBoxPeer.prototype.setWeight = function (weight)
{
    this._font.setWeight(weight);
    this._updateFontStyle();
};

web2d.peer.svg.HtmlBoxPeer.prototype.setFontFamily = function (family)
{
    var oldFont = this._font;
    this._font = new web2d.Font(family, this);
    this._font.setSize(oldFont.getSize());
    this._font.setStyle(oldFont.getStyle());
    this._font.setWeight(oldFont.getWeight());
    this._updateFontStyle();
};

web2d.peer.svg.HtmlBoxPeer.prototype.getFont = function ()
{
    return {
        font:this._font.getFont(),
        size:parseInt(this._font.getSize()),
        style:this._font.getStyle(),
        weight:this._font.getWeight()
    };
};

web2d.peer.svg.HtmlBoxPeer.prototype.setSize = function (size)
{
    this._font.setSize(size);
    this._updateFontStyle();
};

web2d.peer.svg.HtmlBoxPeer.prototype.getWidth = function ()
{
    var scale = web2d.peer.utils.TransformUtil.workoutScale(this);
    var width = $(this._body).getSize().size.x;
    return width*scale.width*3/4;
};

web2d.peer.svg.HtmlBoxPeer.prototype.getHeight = function ()
{
    var scale = web2d.peer.utils.TransformUtil.workoutScale(this);
    var height = $(this._body).getSize().size.y;
    return height*scale.height*3/4;
};

web2d.peer.svg.HtmlBoxPeer.prototype.updateSize = function()
{
    var width = this.getWidth();
    this._native.setAttribute("width", width>1?width:1);
    var height = this.getHeight();
    this._native.setAttribute("height", height>1?height:1);
};