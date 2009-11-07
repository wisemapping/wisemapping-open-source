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
 * Create a rectangle and variations of a rectangle shape.
 * arc must be specified to create rounded rectangles.
 * arc = "<length>"
 *     For rounded rectangles, radius of the ellipse used to round off the corners of the rectangle.
 */
web2d.Rect = function(arc, attributes)
{
    if (arc && arc > 1)
    {
        throw "Arc must be 0<=arc<=1";
    }
    if (arguments.length <= 0)
    {
        var rx = 0;
        var ry = 0;
    }

    var peer = web2d.peer.Toolkit.createRect(arc);
    var defaultAttributes = {width:40, height:40, x:5, y:5,stroke:'1 solid black',fillColor:'green'};
    for (var key in attributes)
    {
        defaultAttributes[key] = attributes[key];
    }
    web2d.Element.call(this, peer, defaultAttributes);
};
objects.extend(web2d.Rect, web2d.Element);

web2d.Rect.prototype.getType = function()
{
    return "Rect";
};

/**
 * @Todo: Complete Doc
 */
web2d.Rect.prototype.getSize = function()
{
    return this._peer.getSize();
};