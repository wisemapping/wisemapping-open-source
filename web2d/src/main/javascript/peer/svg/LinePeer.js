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

web2d.peer.svg.LinePeer = function()
{
    var svgElement = window.document.createElementNS(this.svgNamespace, 'line');
    web2d.peer.svg.ElementPeer.call(this, svgElement);
    this.attachChangeEventListener("strokeStyle", web2d.peer.svg.ElementPeer.prototype.updateStrokeStyle);
};

objects.extend(web2d.peer.svg.LinePeer, web2d.peer.svg.ElementPeer);

web2d.peer.svg.LinePeer.prototype.setFrom = function(x1, y1)
{
    this._x1=x1;
    this._y1=y1;
    this._native.setAttribute('x1', x1);
    this._native.setAttribute('y1', y1);
};

web2d.peer.svg.LinePeer.prototype.setTo = function(x2, y2)
{
    this._x2=x2;
    this._y2=y2;
    this._native.setAttribute('x2', x2);
    this._native.setAttribute('y2', y2);
};

web2d.peer.svg.LinePeer.prototype.getFrom = function(){
    return new core.Point(this._x1,this._y1);
};

web2d.peer.svg.LinePeer.prototype.getTo = function(){
    return new core.Point(this._x2,this._y2);
};


/*
* http://www.zvon.org/HowTo/Output/howto_jj_svg_27.html?at=marker-end
*/
web2d.peer.svg.LinePeer.prototype.setArrowStyle = function(startStyle, endStyle)
{
    if (core.Utils.isDefined(startStyle))
    {
        // Todo: This must be implemented ...
    }

    if (core.Utils.isDefined(endStyle))
    {
        // Todo: This must be implemented ...
    }
};