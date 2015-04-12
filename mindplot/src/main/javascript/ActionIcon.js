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

mindplot.ActionIcon = new Class({
    Extends:mindplot.Icon,
    initialize: function(topic, url) {
        this.parent(url);
        this._node = topic;
    },
    getNode:function() {
        return this._node;
    },

    setPosition:function(x, y) {
        var size = this.getSize();
        this.getImage().setPosition(x - size.width / 2, y - size.height / 2);
    },

    addEvent:function(event, fn) {
        this.getImage().addEvent(event, fn);
    },

    addToGroup:function(group) {
        group.append(this.getImage());
    },

    setVisibility:function(visible) {
        this.getImage().setVisibility(visible);
    },

    isVisible:function() {
        return this.getImage().isVisible();
    },

    setCursor:function(cursor) {
        return this.getImage().setCursor(cursor);
    },

    moveToBack:function(cursor) {
        return this.getImage().moveToBack(cursor);
    },

    moveToFront:function(cursor) {
        return this.getImage().moveToFront(cursor);
    }
});

