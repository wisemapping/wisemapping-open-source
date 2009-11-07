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
 * A group object can be used to collect shapes.
 */
web2d.Group = function(attributes)
{
    var peer = web2d.peer.Toolkit.createGroup();
    var defaultAttributes = {width:50, height:50, x:50, y:50,coordOrigin:'0 0',coordSize:'50 50'};
    for (var key in attributes)
    {
        defaultAttributes[key] = attributes[key];
    }
    web2d.Element.call(this, peer, defaultAttributes);
};

objects.extend(web2d.Group, web2d.Element);

/**
 * Remove an element as a child to the object.
 */
web2d.Group.prototype.removeChild = function(element)
{
    if (!element)
    {
        throw "Child element can not be null";
    }

    if (element == this)
    {
        throw "It's not posible to add the group as a child of itself";
    }

    var elementType = element.getType();
    if (elementType == null)
    {
        throw "It seems not to be an element ->" + element;
    }

    this._peer.removeChild(element._peer);
};

/**
 * Appends an element as a child to the object.
 */
web2d.Group.prototype.appendChild = function(element)
{
    if (!element)
    {
        throw "Child element can not be null";
    }

    if (element == this)
    {
        throw "It's not posible to add the group as a child of itself";
    }

    var elementType = element.getType();
    if (elementType == null)
    {
        throw "It seems not to be an element ->" + element;
    }

    if (elementType == "Workspace")
    {
        throw "A group can not have a workspace as a child";
    }

    this._peer.appendChild(element._peer);
};


web2d.Group.prototype.getType = function()
{
    return "Group";
};

/**
 * The group element is a containing blocks for this content - they define a CSS2 "block level box".
 * Inside the containing block a local coordinate system is defined for any sub-elements using the coordsize and coordorigin attributes.
 * All CSS2 positioning information is expressed in terms of this local coordinate space.
 * Consequently CSS2 position attributes (left, top, width, height and so on) have no unit specifier -
 * they are simple numbers, not CSS length quantities.
 */
web2d.Group.prototype.setCoordSize = function(width, height)
{
    this._peer.setCoordSize(width, height);
};

web2d.Group.prototype.getCoordSize = function(){
    return this.peer.getCoordSize();
};

/**
 * @Todo: Complete Doc
 */
web2d.Group.prototype.setCoordOrigin = function(x, y)
{
    this._peer.setCoordOrigin(x, y);
};

web2d.Group.prototype.getCoordOrigin = function(){
    return this._peer.getCoordOrigin();
};

/**
 * @Todo: Complete Doc
 */
web2d.Group.prototype.getSize = function()
{
    return this._peer.getSize();
};

web2d.Group.prototype.setFill = function(color, opacity)
{
    throw "Unsupported operation. Fill can not be set to a group";
};

web2d.Group.prototype.setStroke = function(width, style, color, opacity)
{
    throw "Unsupported operation. Stroke can not be set to a group";
};

web2d.Group.prototype.getCoordSize = function()
{
  return this._peer.getCoordSize();
};

web2d.Group.prototype.appendDomChild = function(DomElement)
{
        if (!DomElement)
    {
        throw "Child element can not be null";
    }

    if (DomElement == this)
    {
        throw "It's not posible to add the group as a child of itself";
    }

    this._peer._native.appendChild(DomElement);
};