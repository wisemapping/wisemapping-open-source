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

web2d.peer.vml.ElipsePeer = function()
{
    var vmlElement = window.document.createElement('v:oval');
    web2d.peer.vml.ElementPeer.call(this, vmlElement);
    this.attachChangeEventListener("onChangeCoordSize", web2d.peer.vml.ElementPeer.prototype._updateStrokeWidth);
};

objects.extend(web2d.peer.vml.ElipsePeer, web2d.peer.vml.ElementPeer);

web2d.peer.vml.ElipsePeer.prototype.setPosition = function(cx, cy)
{
    // VML position is positioned the coorner of the oval.
    // That's why, I have to move the circle to the center by hand.
    if (core.Utils.isDefined(cx))
    {
        this._position.x = parseInt(cx);
        this._native.style.left = cx;// + parseInt(this.getSize().width / 2);
    }

    if (core.Utils.isDefined(cy))
    {
        this._position.y = parseInt(cy);
        this._native.style.top = cy;// + parseInt(this.getSize().height / 2);
    }
};


web2d.peer.vml.ElementPeer.prototype.getPosition = function()
{
    return {x:this._position.x, y:this._position.y};
};

web2d.peer.vml.ElipsePeer.prototype.setSize = function(width, height)
{
    web2d.peer.vml.ElipsePeer.superClass.setSize.call(this, width, height);

    var coord = this.getPosition();
    this.setPosition(coord.x, coord.y);
};
