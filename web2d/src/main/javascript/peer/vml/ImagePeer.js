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

web2d.peer.vml.ImagePeer = function()
{
    var vmlElement = window.document.createElement('v:image');
    web2d.peer.vml.ElementPeer.call(this, vmlElement);
};

objects.extend(web2d.peer.vml.ImagePeer, web2d.peer.vml.ElementPeer);

web2d.peer.vml.ImagePeer.prototype.setHref = function(url)
{
    this._native.setAttribute("src", url);
    this._href = url;
};

web2d.peer.vml.ImagePeer.prototype.getHref = function()
{
    return this._href;
};

