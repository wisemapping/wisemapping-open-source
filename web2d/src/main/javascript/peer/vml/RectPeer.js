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
 * http://www.w3.org/TR/NOTE-VML#_Toc416858381
 * VML  	arcsize  	number  	"0.2"
 * Defines rounded corners as a percentage of half the smaller dimension of the rectangle. 0.0 (0%)
 * square corners, 1.0 (100%) - smaller dimension forms a semi-circle.
 */
web2d.peer.vml.RectPeer = function(arc)
{
    this.__act = arc;
    var vmlElement;
    if (!arc)
    {
        vmlElement = window.document.createElement('v:rect');
    } else
    {
        vmlElement = window.document.createElement('v:roundrect');

        // In all examples, arc size 1 looks similiar to 0.5 arc size. This helps to solve look and feel incompatibilities with Fire
        arc = arc / 2;
        vmlElement.setAttribute('arcsize', arc);
    }

    web2d.peer.vml.ElementPeer.call(this, vmlElement);
    this.attachChangeEventListener("onChangeCoordSize", web2d.peer.vml.ElementPeer.prototype._updateStrokeWidth);
};


objects.extend(web2d.peer.vml.RectPeer, web2d.peer.vml.ElementPeer);