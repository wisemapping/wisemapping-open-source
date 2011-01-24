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

web2d.peer.vml.GroupPeer = function()
{
    var vmlElement = window.document.createElement('v:group');
    web2d.peer.vml.ElementPeer.call(this, vmlElement);
    this._native.style.cursor = 'move';
};

objects.extend(web2d.peer.vml.GroupPeer, web2d.peer.vml.ElementPeer);

web2d.peer.vml.ElementPeer.prototype.setCoordOrigin = function(x, y)
{
    this._native.coordorigin = x + "," + y;
};

web2d.peer.vml.GroupPeer.prototype.setCoordSize = function(width, height)
{
    this._native.coordsize = width + "," + height;
    web2d.peer.utils.EventUtils.broadcastChangeEvent(this, "onChangeCoordSize");
};

web2d.peer.vml.GroupPeer.prototype.getCoordSize = function()
{
    var coordSize = this._native.coordsize + "";
    var coord;
    if (coordSize)
    {
        coord = coordSize.split(",");
    } else
    {
        coord = [1,1];
    }
    return { width:coord[0], height:coord[1] };
};

web2d.peer.vml.GroupPeer.prototype.setSize = function(width, height)
{
    web2d.peer.vml.ElipsePeer.superClass.setSize.call(this, width, height);
    web2d.peer.utils.EventUtils.broadcastChangeEvent(this, "onChangeCoordSize");
};
web2d.peer.vml.GroupPeer.prototype.appendChild = function(child)
{
    web2d.peer.vml.GroupPeer.superClass.appendChild.call(this, child);
    web2d.peer.utils.EventUtils.broadcastChangeEvent(child, "onChangeCoordSize");
};

web2d.peer.vml.GroupPeer.prototype.getCoordOrigin = function()
{
    var coordOrigin = this._native.coordorigin + "";
    var coord;
    if (coordOrigin)
    {
        coord = coordOrigin.split(",");
    } else
    {
        coord = [1,1];
    }
    return { x:coord[0], y:coord[1] };
};
