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

web2d.peer.svg.ImagePeer = function()
{
    var svgElement = window.document.createElementNS(this.svgNamespace, 'image');
    web2d.peer.svg.ElementPeer.call(this, svgElement);
    this._position = {x:0,y:0};
    this._href="";
};

objects.extend(web2d.peer.svg.ImagePeer, web2d.peer.svg.ElementPeer);

web2d.peer.svg.ImagePeer.prototype.setPosition = function(x, y)
{
    this._position = {x:x, y:y};
    this._native.setAttribute('y', y);
    this._native.setAttribute('x', x);
};


web2d.peer.svg.ImagePeer.prototype.getPosition = function()
{
    return this._position;
};

web2d.peer.svg.ImagePeer.prototype.setHref = function(url)
{
    this._native.setAttributeNS(this.linkNamespace, "href", url);
    this._href = url;
};

web2d.peer.svg.ImagePeer.prototype.getHref = function()
{
    return this._href;
};