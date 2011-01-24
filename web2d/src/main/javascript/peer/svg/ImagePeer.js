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

web2d.peer.svg.ImagePeer = function()
{
    var svgElement = window.document.createElementNS(this.svgNamespace, 'image');
    web2d.peer.svg.ElementPeer.call(this, svgElement);
    this._position = {x:0,y:0};
    this._href="";
};

objects.extend(web2d.peer.svg.ImagePeer, web2d.peer.svg.ElementPeer);

web2d.peer.svg.ImagePeer.prototype.setPosition = function(x, y)
{
    this._position = {x:x, y:y};
    this._native.setAttribute('y', y);
    this._native.setAttribute('x', x);
};


web2d.peer.svg.ImagePeer.prototype.getPosition = function()
{
    return this._position;
};

web2d.peer.svg.ImagePeer.prototype.setHref = function(url)
{
    this._native.setAttributeNS(this.linkNamespace, "href", url);
    this._href = url;
};

web2d.peer.svg.ImagePeer.prototype.getHref = function()
{
    return this._href;
};