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

web2d.Font = function(fontFamily, textPeer)
{
    var font = "web2d.peer.Toolkit.create" + fontFamily + "Font();";
    this._peer = eval(font);
    this._textPeer = textPeer;
};

web2d.Font.prototype.getHtmlSize = function ()
{
    var scale = web2d.peer.utils.TransformUtil.workoutScale(this._textPeer);
    return this._peer.getHtmlSize(scale);
};

web2d.Font.prototype.getGraphSize = function ()
{
    var scale = web2d.peer.utils.TransformUtil.workoutScale(this._textPeer);
    return this._peer.getGraphSize(scale);
};

web2d.Font.prototype.getFontScale = function ()
{
    return web2d.peer.utils.TransformUtil.workoutScale(this._textPeer).height;
};

web2d.Font.prototype.getSize = function ()
{
    return this._peer.getSize();
};

web2d.Font.prototype.getStyle = function ()
{
    return this._peer.getStyle();
};

web2d.Font.prototype.getWeight = function ()
{
    return this._peer.getWeight();
};

web2d.Font.prototype.getFontFamily = function ()
{
    return this._peer.getFontFamily();
};

web2d.Font.prototype.setSize = function (size)
{
    return this._peer.setSize(size);
};

web2d.Font.prototype.setStyle = function (style)
{
    return this._peer.setStyle(style);
};

web2d.Font.prototype.setWeight = function (weight)
{
    return this._peer.setWeight(weight);
};

web2d.Font.prototype.getFont = function ()
{
    return this._peer.getFont();
};

web2d.Font.prototype.getWidthMargin = function ()
{
    return this._peer.getWidthMargin();
};


web2d.Font.ARIAL = "Arial";
web2d.Font.TIMES = "Times";
web2d.Font.TAHOMA = "Tahoma";
web2d.Font.VERDANA = "Verdana";

