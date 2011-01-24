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
*
*/

web2d.Arrow = function(attributes)
{
    var peer = web2d.peer.Toolkit.createArrow();
    var defaultAttributes = {strokeColor:'black',strokeWidth:1,strokeStyle:'solid',strokeOpacity:1};
    for (var key in attributes)
    {
        defaultAttributes[key] = attributes[key];
    }
    web2d.Element.call(this, peer, defaultAttributes);
};
objects.extend(web2d.Arrow, web2d.Element);

web2d.Arrow.prototype.getType = function()
{
    return "Arrow";
};

web2d.Arrow.prototype.setFrom = function(x,y)
{
    this._peer.setFrom(x,y);
};

web2d.Arrow.prototype.setControlPoint = function (point)
{
    this._peer.setControlPoint(point);
};

web2d.Arrow.prototype.setStrokeColor = function (color)
{
    this._peer.setStrokeColor(color);
};

web2d.Arrow.prototype.setStrokeWidth = function(width)
{
    this._peer.setStrokeWidth(width);
};

web2d.Arrow.prototype.setDashed = function(isDashed, length, spacing){
    this._peer.setDashed(isDashed, length,  spacing);
};

web2d.Arrow.prototype.reDraw = function()
{
    this._peer._redraw();
};
