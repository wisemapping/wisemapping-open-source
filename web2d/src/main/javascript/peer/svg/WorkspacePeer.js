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

web2d.peer.svg.WorkspacePeer = function(element)
{
    this._element = element;
    var svgElement = window.document.createElementNS(this.svgNamespace, 'svg');
    web2d.peer.svg.ElementPeer.call(this, svgElement);
    this._native.setAttribute("focusable", "true");
    this._native.setAttribute("id", "workspace");
    this._native.setAttribute("preserveAspectRatio", "true");

};

objects.extend(web2d.peer.svg.WorkspacePeer, web2d.peer.svg.ElementPeer);


/**
 * http://www.w3.org/TR/SVG/coords.html 7.7 The viewBox  attribute
 * It is often desirable to specify that a given set of graphics stretch to fit a particular container element. The viewBox attribute provides this capability.
 *
 * All elements that establish a new viewport (see elements that establish viewports), plus the 'marker', 'pattern' and 'view' elements have attribute viewBox. The value of the viewBox attribute is a list of four numbers <min-x>, <min-y>, <width> and <height>, separated by whitespace and/or a comma, which specify a rectangle in user space which should be mapped to the bounds of the viewport established by the given element, taking into account attribute preserveAspectRatio. If specified, an additional transformation is applied to all descendants of the given element to achieve the specified effect.
 *
 * A negative value for <width> or <height> is an error (see Error processing). A value of zero disables rendering of the element.
 *
 */

web2d.peer.svg.WorkspacePeer.prototype.setCoordSize = function(width, height)
{
    var viewBox = this._native.getAttribute('viewBox');
    var coords = [0,0,0,0];
    if (viewBox != null)
    {
        coords = viewBox.split(/ /);
    }
    if ($defined(width))
    {
        coords[2] = width;
    }

    if ($defined(height))
    {
        coords[3] = height;
    }

    this._native.setAttribute('viewBox', coords.join(" "));
    this._native.setAttribute("preserveAspectRatio", "none");
    web2d.peer.utils.EventUtils.broadcastChangeEvent(this, "strokeStyle");
};

web2d.peer.svg.WorkspacePeer.prototype.getCoordSize = function()
{
    var viewBox = this._native.getAttribute('viewBox');
    var coords = [1,1,1,1];
    if (viewBox != null)
    {
        coords = viewBox.split(/ /);
    }
    return {width:coords[2],height:coords[3]};
};

web2d.peer.svg.WorkspacePeer.prototype.setCoordOrigin = function(x, y)
{
    var viewBox = this._native.getAttribute('viewBox');

    // ViewBox min-x ,min-y by default initializated with 0 and 0.
    var coords = [0,0,0,0];
    if (viewBox != null)
    {
        coords = viewBox.split(/ /);
    }

    if ($defined(x))
    {
        coords[0] = x;
    }

    if ($defined(y))
    {
        coords[1] = y;
    }

    this._native.setAttribute('viewBox', coords.join(" "));
};

web2d.peer.svg.WorkspacePeer.prototype.appendChild = function(child)
{
    web2d.peer.svg.WorkspacePeer.superClass.appendChild.call(this, child);
    web2d.peer.utils.EventUtils.broadcastChangeEvent(child, "onChangeCoordSize");
};

web2d.peer.svg.WorkspacePeer.prototype.getCoordOrigin = function(child)
{
    var viewBox = this._native.getAttribute('viewBox');
    var coords = [1,1,1,1];
    if (viewBox != null)
    {
        coords = viewBox.split(/ /);
    }
    var x = parseFloat(coords[0]);
    var y = parseFloat(coords[1]);
    return {x:x,y:y};
};

web2d.peer.svg.WorkspacePeer.prototype.getPosition = function()
{
    return {x:0,y:0};
};