/*
*    Copyright [2015] [wisemapping]
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

web2d.peer.ToolkitSVG =
{
    init: function()
    {
    },
    createWorkspace: function(element)
    {
        return new web2d.peer.svg.WorkspacePeer(element);
    },
    createGroup: function(element)
    {
        return new web2d.peer.svg.GroupPeer();
    },
    createElipse: function()
    {
        return new web2d.peer.svg.ElipsePeer();
    },
    createLine: function()
    {
        return new web2d.peer.svg.LinePeer();
    },
    createPolyLine: function()
    {
        return new web2d.peer.svg.PolyLinePeer();
    },
    createCurvedLine: function()
    {
        return new web2d.peer.svg.CurvedLinePeer();
    },
    createArrow: function()
    {
        return new web2d.peer.svg.ArrowPeer();
    },
    createText: function ()
    {
        return new web2d.peer.svg.TextPeer();
    },
    createImage: function ()
    {
        return new web2d.peer.svg.ImagePeer();
    },
    createRect: function(arc)
    {
        return new web2d.peer.svg.RectPeer(arc);
    },
    createArialFont: function()
    {
        return new web2d.peer.svg.ArialFont();
    },
    createTimesFont: function()
    {
        return new web2d.peer.svg.TimesFont();
    },
    createVerdanaFont: function()
    {
        return new web2d.peer.svg.VerdanaFont();
    },
    createTahomaFont: function()
    {
        return new web2d.peer.svg.TahomaFont();
    }
};

web2d.peer.Toolkit = web2d.peer.ToolkitSVG;