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
    if (core.Utils.isDefined(x))
    {
        this._native.setAttribute('x', parseInt(x));
    }
    if (core.Utils.isDefined(y))
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
    if (this._arc)
    {
        // Transform percentages to SVG format.
        var arc = (min / 2) * this._arc;
        this._native.setAttribute('rx', arc);
        this._native.setAttribute('ry', arc);
    }
};