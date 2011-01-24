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

web2d.peer.vml.TextPeer = function()
{
    var container = window.document.createElement('v:shape');
    this._textElement = window.document.createElement('v:textpath');
    web2d.peer.vml.ElementPeer.call(this, container);
    this._native.appendChild(this._textElement);
    container.style.position = "absolute";
    container.style.top = "1";
    container.style.left = "1";
    container.style.width = "2";
    container.style.height = "2";
    container.style.antialias = "true";
    container.style.zIndex = 10;
    // Create the path object
    var myPath = document.createElement("v:path");
    myPath.textpathok = "True";
    myPath.v = "m 0,0 l 10,0 m 0,20 l 10,20";

    // Add it to the DOM hierarchy
    container.appendChild(myPath);

     // Create the fill object
    this._fontColor = document.createElement("v:fill");
    this._fontColor.on = "true";
    this._fontColor.color = "red";

    // Add it to the DOM hierarchy
    container.appendChild(this._fontColor);

    // The border is not going to be shown. To show it change .on to true
    this._fontBorderColor = document.createElement("v:stroke");
    this._fontBorderColor.on = "false";
    this._fontBorderColor.color="red";

    // Add it to the DOM hierarchy
    container.appendChild(this._fontBorderColor);

    this._textElement.on = "true";
    this._textElement.fitpath = "false";
    this._textElement.style.setAttribute("V-Text-Align","left");
    this._size=12;
};

objects.extend(web2d.peer.vml.TextPeer, web2d.peer.vml.ElementPeer);

web2d.peer.vml.TextPeer.prototype.setPosition = function(x, y)
{
    // VML position is positioned the coorner of the oval.
    // That's why, I have to move the circle to the center by hand.
    if (core.Utils.isDefined(x))
    {
        this._position.x = parseInt(x);
        this._native.style.left = x;
    }

    if (core.Utils.isDefined(y))
    {
        this._position.y = parseInt(y);
        this._native.style.top = y - parseInt(parseInt(this._size)/8);
    }
};

web2d.peer.vml.TextPeer.prototype.getPosition = function()
{
    return this._position;
};

web2d.peer.vml.TextPeer.prototype.appendChild = function(element)
{
    this._textElement.appendChild(element._native);
};

web2d.peer.vml.TextPeer.prototype.setText = function(text)
{
    this._text = text;
    this._textElement.string=text;
};

web2d.peer.vml.TextPeer.prototype.getText = function()
{
    return this._text;
};

web2d.peer.vml.TextPeer.prototype.setFont = function(font, size, style, weight)
{
    var scale=web2d.peer.utils.TransformUtil.workoutScale(this);
    this._size=parseInt(size)*scale.height*43/32;
    this._textElement.style.font = style + " " + weight + " " + this._size + " " + font;
};

web2d.peer.vml.TextPeer.prototype.setColor = function(color)
{
    this._fontColor.color=color;
    this._fontBorderColor.color=color;
};

web2d.peer.vml.TextPeer.prototype.getHtmlFontSize = function ()
{
    return this._font.getHtmlSize();
};