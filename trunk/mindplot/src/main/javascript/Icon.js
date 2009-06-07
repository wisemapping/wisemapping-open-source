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

mindplot.Icon = function(url){
    this._image = new web2d.Image();
    this._image.setHref(url);
    this._image.setSize(12,12);
};

mindplot.Icon.prototype.getImage= function(){
    return this._image;
};

mindplot.Icon.prototype.setGroup= function(group){
    this._group=group;
};

mindplot.Icon.prototype.getGroup= function() {
    return this._group;
};

mindplot.Icon.prototype.getSize=function(){
    return this._image.getSize();
};


