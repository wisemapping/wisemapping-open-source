/*
*    Copyright [2011] [wisemapping]
*
*   Licensed under the Apache License, Version 2.0 (the "License") plus the
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

web2d.peer.vml.PolyLinePeer = function()
{
    var vmlElement = window.document.createElement('v:polyline');
    web2d.peer.vml.ElementPeer.call(this, vmlElement);
    this._native.setAttribute('filled', 'false');
    this.breakDistance = 10;
};

objects.extend(web2d.peer.vml.PolyLinePeer, web2d.peer.vml.ElementPeer);

web2d.peer.vml.PolyLinePeer.prototype.setFrom = function(x1, y1)
{
    this._x1 = x1;
    this._y1 = y1;
    this._updatePath();
};

web2d.peer.vml.PolyLinePeer.prototype.setTo = function(x2, y2)
{
    this._x2 = x2;
    this._y2 = y2;
    this._updatePath();

};


web2d.peer.vml.PolyLinePeer.prototype.setStyle = function(style)
{
    this._style = style;
    this._updatePath();
};

web2d.peer.vml.PolyLinePeer.prototype.getStyle = function()
{
    return this._style;
};

web2d.peer.vml.PolyLinePeer.prototype._updatePath = function()
{
    if (this._style == "Curved")
    {
        this._updateMiddleCurvePath();
    }
    else if (this._style == "Straight")
    {
        this._updateStraightPath();
    }
    else
    {
        this._updateCurvePath();
    }
};

web2d.peer.vml.PolyLinePeer.prototype._updateStraightPath = function()
{
    if (core.Utils.isDefined(this._x1) && core.Utils.isDefined(this._x2) && core.Utils.isDefined(this._y1) && core.Utils.isDefined(this._y2))
    {
        this.buildStraightPath(this.breakDistance, this._x1, this._y1, this._x2, this._y2);

        // Hack: I could not solve why this is happening...
        if (!this._native.points)
        {
            this._native.setAttribute('points', path);
        } else
        {
            this._native.points.value = path;
        }
    }
};

web2d.peer.vml.PolyLinePeer.prototype._updateMiddleCurvePath = function()
{
    var x1 = this._x1;
    var y1 = this._y1;
    var x2 = this._x2;
    var y2 = this._y2;
    if (core.Utils.isDefined(x1) && core.Utils.isDefined(x2) && core.Utils.isDefined(y1) && core.Utils.isDefined(y2))
    {
        var diff = x2 - x1;
        var middlex = (diff / 2) + x1;
        var signx = 1;
        var signy = 1;
        if (diff < 0)
        {
            signx = -1;
        }
        if (y2 < y1)
        {
            signy = -1;
        }


        var path = x1 + ", " + y1 + " " + (middlex - 10 * signx) + ", " + y1 + " " + middlex + ", " + (y1 + 10 * signy) + " " + middlex + ", " + (y2 - 10 * signy) + " " + (middlex + 10 * signx) + ", " + y2 + " " + x2 + ", " + y2;

        // Hack: I could not solve why this is happening...
        if (!this._native.points)
        {
            this._native.setAttribute('points', path);
        } else
        {
            this._native.points.value = path;
        }
    }
};

web2d.peer.vml.PolyLinePeer.prototype._updateCurvePath = function()
{
    if (core.Utils.isDefined(this._x1) && core.Utils.isDefined(this._x2) && core.Utils.isDefined(this._y1) && core.Utils.isDefined(this._y2))
    {
        var path = web2d.PolyLine.buildCurvedPath(this.breakDistance, this._x1, this._y1, this._x2, this._y2);

        // Hack: I could not solve why this is happening...
        if (!this._native.points)
        {
            this._native.setAttribute('points', path);
        } else
        {
            this._native.points.value = path;
            this._native.xPoints = path;
        }
    }
};

web2d.peer.vml.PolyLinePeer.prototype.setStroke = function(width, style, color, opacity)
{
    web2d.peer.vml.LinePeer.superClass.setStroke.call(this, null, style, color, opacity);

    if (core.Utils.isDefined(width))
    {
        this._vmlStroke.setAttribute('weight', width + "px");
    }
};
