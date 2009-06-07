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

web2d.Text = function(attributes)
{
    var peer = web2d.peer.Toolkit.createText();
    web2d.Element.call(this, peer, attributes);
};
objects.extend(web2d.Text, web2d.Element);

web2d.Text.prototype.getType = function()
{
    return "Text";
};

web2d.Text.prototype.setText = function(text)
{
    this._peer.setText(text);
};

web2d.Text.prototype.setTextSize = function(width, height)
{
    this._peer.setContentSize(width, height);
};

web2d.Text.prototype.getText = function()
{
    return this._peer.getText();
};

web2d.Text.prototype.setFont = function(font, size, style, weight)
{
    this._peer.setFont(font, size, style, weight);
};

web2d.Text.prototype.setColor = function(color)
{
    this._peer.setColor(color);
};

web2d.Text.prototype.getColor = function()
{
    return this._peer.getColor();
};

web2d.Text.prototype.setStyle = function(style)
{
    this._peer.setStyle(style);
};

web2d.Text.prototype.setWeight = function(weight)
{
    this._peer.setWeight(weight);
};

web2d.Text.prototype.setFontFamily = function(family)
{
    this._peer.setFontFamily(family);
};

web2d.Text.prototype.getFont = function()
{
    return this._peer.getFont();
};

web2d.Text.prototype.setSize = function(size)
{
    this._peer.setSize(size);
};

web2d.Text.prototype.getHtmlFontSize = function()
{
    return this._peer.getHtmlFontSize();
};

web2d.Text.prototype.getWidth = function()
{
    return this._peer.getWidth();
};

web2d.Text.prototype.getHeight = function()
{
    return parseInt(this._peer.getHeight());
};