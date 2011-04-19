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

mindplot.ActionIcon = function(topic, url) {
    mindplot.Icon.call(this, url);
    this._node = topic;
};

objects.extend(mindplot.ActionIcon, mindplot.Icon);

mindplot.ActionIcon.prototype.initialize = function() {
    var pos = this.getImage.getPosition();
    var size = this.getSize();
    this._position = new core.Point(pos.x - size.width/2, pos.y - size.height/2);
};

mindplot.ActionIcon.prototype.getNode = function(){
    return this._node;
};

mindplot.ActionIcon.prototype.setPosition = function(x,y){
    var size = this.getSize();
    this._position = new core.Point(x,y);
    this.getImage().setPosition(x-size.width/2, y-size.height/2);
};

mindplot.ActionIcon.prototype.getPosition = function(){
    return this._position;
};

mindplot.ActionIcon.prototype.addEventListener = function(event, fn){
    this.getImage().addEventListener(event, fn);
};

mindplot.ActionIcon.prototype.addToGroup = function(group){
    group.appendChild(this.getImage());
};

mindplot.ActionIcon.prototype.setVisibility = function(visible){
    this.getImage().setVisibility(visible);
};

mindplot.ActionIcon.prototype.isVisible = function(){
    return this.getImage().isVisible();
};

mindplot.ActionIcon.prototype.setCursor = function(cursor){
    return this.getImage().setCursor(cursor);
};

mindplot.ActionIcon.prototype.moveToBack = function(cursor){
    return this.getImage().moveToBack(cursor);
};

mindplot.ActionIcon.prototype.moveToFront = function(cursor){
    return this.getImage().moveToFront(cursor);
};

