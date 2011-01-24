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

web2d.peer.vml.TextBoxPeer = function()
{
    var container = window.document.createElement('v:shape');
    this._textElement = window.document.createElement('v:textbox');
    web2d.peer.vml.ElementPeer.call(this, container);
    this._native.appendChild(this._textElement);
    this._container = window.document.createElement('span');
    this._container.style.width = "100%";
    this._container.style.height = "100%";
    this._textContainer = window.document.createElement('span');
    this._container.appendChild(this._textContainer);
    this._textElement.appendChild(this._container);
    container.style.position = "absolute";
    container.style.top = "0";
    container.style.left = "0";
    container.style.width = "1";
    container.style.height = "1";
    container.style.antialias = "true";
    container.style.zIndex = 10;
    this._textElement.style.position = "absolute";
    this._textElement.style.overflow = "visible";
    this._textElement.inset = "0 0 0 0";
    this.attachChangeEventListener("onChangeCoordSize", web2d.peer.vml.TextBoxPeer.prototype._updateTextSize);
    this._font = new web2d.Font("Arial", this);
};

objects.extend(web2d.peer.vml.TextBoxPeer, web2d.peer.vml.ElementPeer);

web2d.peer.vml.TextBoxPeer.prototype._updateTextSize = function()
{
    if (core.Utils.isDefined(this._font.getSize()))
    {
        this._updateFontStyle();
    }
};

//todo: use ths method to specify the maximum size of the text box
/*web2d.web2d.peer.vml.TextBoxPeer.prototype.setSize = function(width, height)
{
    web2d.web2d.peer.vml.TextBoxPeer.superClass.setSize.call(this,width,height);
    this._native.setAttribute('rx', width / 2);
    this._native.setAttribute('ry', height /ose 2);
};
*/

web2d.peer.vml.TextBoxPeer.prototype.appendChild = function(element)
{
    this._native.appendChild(element._native);
};

web2d.peer.vml.TextBoxPeer.prototype.setText = function(text)
{
    text = core.Utils.escapeInvalidTags(text);
    //remove previous text
    //var child = this._textContainer.firstChild;
    var child = this._textContainer.firstChild;
    if (child)
    {
        this._textContainer.removeChild(child);
    }

    this._text = text;
    var textNode = window.document.createTextNode(text);
    this._textContainer.appendChild(textNode);
    this._updateSize();
};

web2d.peer.vml.TextBoxPeer.prototype.getText = function()
{
    //todo(discutir): deberiamos buscar recursivamente por todos los hijos (textspan)?
    return this._text;
};

web2d.peer.vml.TextBoxPeer.prototype._updateSize = function()
{
    var size = 0;
    if (this._font.getSize())
    {
        size = this._font.getSize();
    }
    var length = 0;
    if (this._text)
    {
        length = this._text.length;
    }
    this._native.style.width = length * size;
    //this._textElement.style.marginleft="0px";
};

web2d.peer.vml.TextBoxPeer.prototype.setPosition = function(x, y)
{
    if (core.Utils.isDefined(x))
    {
        this._position.x = parseInt(x);
        var leftmargin = 0;
        //0.375;
        this._native.style.left = x - leftmargin;
    }

    if (core.Utils.isDefined(y))
    {
        this._position.y = parseInt(y);
        var topmargin = 0;
        //2.375;
        this._native.style.top = y - topmargin;
    }
};

web2d.peer.vml.TextBoxPeer.prototype.setFont = function(font, size, style, weight)
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

web2d.peer.vml.TextBoxPeer.prototype.setColor = function(color)
{
    this._textElement.style.color = color;
};

web2d.peer.vml.TextBoxPeer.prototype.getColor = function()
{
    return this._textElement.style.color;
};

web2d.peer.vml.TextBoxPeer.prototype.setTextSize = function (size)
{
    this._font.setSize(size);
    this._updateFontStyle();
};

web2d.peer.vml.TextBoxPeer.prototype.setStyle = function (style)
{
    this._font.setStyle(style);
    this._updateFontStyle();
};

web2d.peer.vml.TextBoxPeer.prototype.setWeight = function (weight)
{
    this._font.setWeight(weight);
    this._updateFontStyle();
};

web2d.peer.vml.TextBoxPeer.prototype.setSize = function (size)
{
    this._font.setSize(size);
    this._updateFontStyle();
};

web2d.peer.vml.TextBoxPeer.prototype.setFontFamily = function (family)
{
    var oldFont = this._font;
    this._font = new web2d.Font(family, this);
    this._font.setSize(oldFont.getSize());
    this._font.setStyle(oldFont.getStyle());
    this._font.setWeight(oldFont.getWeight());
    this._updateFontStyle();
};

web2d.peer.vml.TextBoxPeer.prototype._updateFontStyle = function ()
{
    this._textElement.style.font = this._font.getStyle() + " " + this._font.getWeight() + " " + this._font.getGraphSize() + " " + this._font.getFontFamily();
    this._updateSize();

    var scale = this._font.getFontScale();
    this._textElement.xFontScale = scale.toFixed(1);
};


web2d.peer.vml.TextBoxPeer.prototype.setContentSize = function(width, height)
{
    this._textElement.xTextSize = width.toFixed(1) + "," + height.toFixed(1);
};

web2d.peer.vml.TextBoxPeer.prototype.getFont = function()
{
    return {
        font:this._font.getFont(),
        size:parseInt(this._font.getSize()),
        style:this._font.getStyle(),
        weight:this._font.getWeight()
    };
};

web2d.peer.vml.TextBoxPeer.prototype.getWidth = function ()
{
    var scale = web2d.peer.utils.TransformUtil.workoutScale(this);
    var size = (this._textContainer.offsetWidth / scale.width);
    return size;
};

web2d.peer.vml.TextBoxPeer.prototype.getHeight = function ()
{
    //return this._font.getGraphSize();
    var scale = web2d.peer.utils.TransformUtil.workoutScale(this);
    var size = (this._textContainer.offsetHeight / scale.height);
    return size;
};

web2d.peer.vml.TextBoxPeer.prototype.getHtmlFontSize = function ()
{
    return this._font.getHtmlSize();
};