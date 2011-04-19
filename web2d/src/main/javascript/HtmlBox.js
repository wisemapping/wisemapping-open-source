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

web2d.HtmlBox = function(attributes)
{
    var peer = web2d.peer.Toolkit.createHtmlBox();
    web2d.Element.call(this, peer, attributes);
};
objects.extend(web2d.HtmlBox, web2d.Element);

web2d.HtmlBox.prototype.getType = function()
{
    return "HtmlBox";
};

web2d.HtmlBox.prototype.setText = function(text)
{
    this._peer.setText(text);
};

web2d.HtmlBox.prototype.getText = function()
{
    return this._peer.getText();
};

web2d.HtmlBox.prototype.setFont = function(font, size, style, weight)
{
    this._peer.setFont(font, size, style, weight);
};

web2d.HtmlBox.prototype.setColor = function(color)
{
    this._peer.setColor(color);
};

web2d.HtmlBox.prototype.getColor = function()
{
    return this._peer.getColor();
};

web2d.HtmlBox.prototype.setStyle = function(style)
{
    this._peer.setStyle(style);
};

web2d.HtmlBox.prototype.setWeight = function(weight)
{
    this._peer.setWeight(weight);
};

web2d.HtmlBox.prototype.setFontFamily = function(family)
{
    this._peer.setFontFamily(family);
};

web2d.HtmlBox.prototype.getFont = function()
{
    return this._peer.getFont();
};

web2d.HtmlBox.prototype.setSize = function(size)
{
    this._peer.setSize(size);
};

web2d.HtmlBox.prototype.getWidth = function()
{
    return parseInt(this._peer.getWidth());
};

web2d.HtmlBox.prototype.getHeight = function()
{
    return parseInt(this._peer.getHeight());
};

web2d.HtmlBox.prototype.setWidth = function(width)
{
    return this._peer.setWidth(width);
};

web2d.HtmlBox.prototype.setHeight = function(height)
{
    return this._peer.setHeight(height);
};

web2d.HtmlBox.prototype.setTextSize = function()
{
    return this._peer.updateSize();
};