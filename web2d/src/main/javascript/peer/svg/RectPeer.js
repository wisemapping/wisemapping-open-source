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

/**
 * http://www.w3.org/TR/SVG/shapes.html#RectElement
 */
web2d.peer.svg.RectPeer = function(arc)
{
    var svgElement = window.document.createElementNS(this.svgNamespace, 'rect');
    web2d.peer.svg.ElementPeer.call(this, svgElement);
    this._arc = arc;
    this.attachChangeEventListener("strokeStyle", web2d.peer.svg.ElementPeer.prototype.updateStrokeStyle);
};

objects.extend(web2d.peer.svg.RectPeer, web2d.peer.svg.ElementPeer);

web2d.peer.svg.RectPeer.prototype.setPosition = function(x, y)
{
    if ($defined(x))
    {
        this._native.setAttribute('x', parseInt(x));
    }
    if ($defined(y))
    {
        this._native.setAttribute('y', parseInt(y));
    }
};

web2d.peer.svg.RectPeer.prototype.getPosition = function()
{
    var x = this._native.getAttribute('x');
    var y = this._native.getAttribute('y');
    return {x:parseInt(x),y:parseInt(y)};
};

web2d.peer.svg.RectPeer.prototype.setSize = function(width, height)
{
    web2d.peer.svg.RectPeer.superClass.setSize.call(this, width, height);

    var min = width < height?width:height;
    if ($defined(this._arc))
    {
        // Transform percentages to SVG format.
        var arc = (min / 2) * this._arc;
        this._native.setAttribute('rx', arc);
        this._native.setAttribute('ry', arc);
    }
};