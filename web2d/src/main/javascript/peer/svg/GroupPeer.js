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

web2d.peer.svg.GroupPeer = function()
{
    var svgElement = window.document.createElementNS(this.svgNamespace, 'g');
    web2d.peer.svg.ElementPeer.call(this, svgElement);
    this._native.setAttribute("preserveAspectRatio", "none");
    this._coordSize = {width:1,height:1};
    this._native.setAttribute("focusable","true");
    this._position = {x:0,y:0};
    this._coordOrigin = {x:0,y:0};
};

objects.extend(web2d.peer.svg.GroupPeer, web2d.peer.svg.ElementPeer);

/*web2d.peer.svg.GroupPeer.prototype.setPosition = function(cx, cy)
{
    this._native.setAttribute("transform", "translate(" + parseInt(cx) + " " + parseInt(cy) + ")");
};*/

web2d.peer.svg.GroupPeer.prototype.setCoordSize = function(width, height)
{
    this._coordSize.width = width;
    this._coordSize.height = height;
    this.updateTransform();
    web2d.peer.utils.EventUtils.broadcastChangeEvent(this, "strokeStyle");
};

web2d.peer.svg.GroupPeer.prototype.getCoordSize = function()
{
    return {width:this._coordSize.width,height:this._coordSize.height};
};

/**
 * http://www.w3.org/TR/SVG/coords.html#TransformAttribute
 * 7.6 The transform  attribute
 *
 * The value of the transform attribute is a <transform-list>, which is defined as a list of transform definitions, which are applied in the order provided. The individual transform definitions are separated by whitespace and/or a comma. The available types of transform definitions include:
 *
 *    * matrix(<a> <b> <c> <d> <e> <f>), which specifies a transformation in the form of a transformation matrix of six values. matrix(a,b,c,d,e,f) is equivalent to applying the transformation matrix [a b c d e f].
 *
 *    * translate(<tx> [<ty>]), which specifies a translation by tx and ty. If <ty> is not provided, it is assumed to be zero.
 *
 *    * scale(<sx> [<sy>]), which specifies a scale operation by sx and sy. If <sy> is not provided, it is assumed to be equal to <sx>.
 *
 *    * rotate(<rotate-angle> [<cx> <cy>]), which specifies a rotation by <rotate-angle> degrees about a given point.
 *      If optional parameters <cx> and <cy> are not supplied, the rotate is about the origin of the current user coordinate system. The operation corresponds to the matrix [cos(a) sin(a) -sin(a) cos(a) 0 0].
 *      If optional parameters <cx> and <cy> are supplied, the rotate is about the point (<cx>, <cy>). The operation represents the equivalent of the following specification: translate(<cx>, <cy>) rotate(<rotate-angle>) translate(-<cx>, -<cy>).
 *
 *    * skewX(<skew-angle>), which specifies a skew transformation along the x-axis.
 *
 *    * skewY(<skew-angle>), which specifies a skew transformation along the y-axis.
 **/

web2d.peer.svg.GroupPeer.prototype.updateTransform = function()
{
    var sx = this._size.width / this._coordSize.width;
    var sy = this._size.height / this._coordSize.height;

    var cx = this._position.x - this._coordOrigin.x * sx;
    var cy = this._position.y - this._coordOrigin.y * sy;

    this._native.setAttribute("transform", "translate(" + cx + "," + cy + ") scale(" + sx + "," + sy + ")");
};

web2d.peer.svg.GroupPeer.prototype.setCoordOrigin = function(x, y)
{
    if (core.Utils.isDefined(x))
    {
        this._coordOrigin.x = x;
    }

    if (core.Utils.isDefined(y))
    {
        this._coordOrigin.y = y;
    }
    this.updateTransform();
};

web2d.peer.svg.GroupPeer.prototype.setSize = function(width, height)
{
    web2d.peer.svg.GroupPeer.superClass.setSize.call(this, width, height);
    this.updateTransform();
};

web2d.peer.svg.GroupPeer.prototype.setPosition = function(x, y)
{
    if (core.Utils.isDefined(x))
    {
        this._position.x = parseInt(x);
    }

    if (core.Utils.isDefined(y))
    {
        this._position.y = parseInt(y);
    }
    this.updateTransform();
};

web2d.peer.svg.GroupPeer.prototype.getPosition = function()
{
    return {x:this._position.x,y:this._position.y};
};

web2d.peer.svg.GroupPeer.prototype.appendChild = function(child)
{
    web2d.peer.svg.GroupPeer.superClass.appendChild.call(this, child);
    web2d.peer.utils.EventUtils.broadcastChangeEvent(child, "onChangeCoordSize");
};

web2d.peer.svg.GroupPeer.prototype.getCoordOrigin = function ()
{
    return {x:this._coordOrigin.x, y:this._coordOrigin.y};
};