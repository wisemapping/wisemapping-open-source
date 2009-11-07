/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License.  You may obtain a copy of the License at
*
*       http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*
* $Id: file 64488 2006-03-10 17:32:09Z paulo $
*/

web2d.peer.vml.LinePeer = function()
{
    var vmlElement = window.document.createElement('v:line');
    web2d.peer.vml.ElementPeer.call(this, vmlElement);
    this._native.setAttribute('fillcolor', 'none');
};

objects.extend(web2d.peer.vml.LinePeer, web2d.peer.vml.ElementPeer);

web2d.peer.vml.LinePeer.prototype.setFrom = function(x1, y1)
{
    this._native.setAttribute('from', x1 + "," + y1);
};

web2d.peer.vml.LinePeer.prototype.setTo = function(x2, y2)
{
    this._native.setAttribute('to', x2 + "," + y2);
};


web2d.peer.vml.LinePeer.prototype.setStroke = function(width, style, color, opacity)
{
    web2d.peer.vml.LinePeer.superClass.setStroke.call(this, width, style, color, opacity);

    if (core.Utils.isDefined(width))
    {
        this._vmlStroke.setAttribute('weight', width + "px");
    }
};

web2d.peer.vml.LinePeer.prototype.setArrowStyle = function(startStyle, endStyle)
{
    var vmlStroke = this.getVMLStroke();
    if (core.Utils.isDefined(startStyle))
    {
        vmlStroke.setAttribute('startarrow', startStyle);
        vmlStroke.setAttribute('startarrowwidth', 'narrow');
        vmlStroke.setAttribute('startarrowlength', 'short');
    }

    if (core.Utils.isDefined(endStyle))
    {
        vmlStroke.setAttribute('endarrow', endStyle);
        vmlStroke.setAttribute('endarrowwidth', 'narrow');
        vmlStroke.setAttribute('endarrowlength', 'short');

    }
};


