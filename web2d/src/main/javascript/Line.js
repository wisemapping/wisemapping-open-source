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

web2d.Line = new Class({
    Extends: web2d.Element,
    initialize: function(attributes) {
        var peer = web2d.peer.Toolkit.createLine();
        var defaultAttributes = {strokeColor:'#495879',strokeWidth:1, strokeOpacity:1};
        for (var key in attributes) {
            defaultAttributes[key] = attributes[key];
        }
        this.parent(peer, defaultAttributes);
    },

    getType : function() {
        return "Line";
    },

    setFrom : function(x, y) {
        this._peer.setFrom(x, y);
    },

    setTo : function(x, y) {
        this._peer.setTo(x, y);
    },

    getFrom : function() {
        return this._peer.getFrom();
    },

    getTo : function() {
        return this._peer.getTo();
    },

    /**
     * Defines the start and the end line arrow style.
     * Can have values "none | block | classic | diamond | oval | open | chevron | doublechevron"
     **/
    setArrowStyle : function(startStyle, endStyle) {
        this._peer.setArrowStyle(startStyle, endStyle);
    },

    setPosition : function(cx, cy) {
        throw "Unsupported operation";
    },

    setSize : function(width, height) {
        throw "Unsupported operation";
    },

    setFill : function(color, opacity) {
        throw "Unsupported operation";
    }
});
