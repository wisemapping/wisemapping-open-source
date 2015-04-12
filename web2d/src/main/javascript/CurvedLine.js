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

web2d.CurvedLine = new Class({
    Extends: web2d.Element,
    initialize: function(attributes) {
        var peer = web2d.peer.Toolkit.createCurvedLine();
        var defaultAttributes = {strokeColor:'blue',strokeWidth:1,strokeStyle:'solid',strokeOpacity:1};
        for (var key in attributes) {
            defaultAttributes[key] = attributes[key];
        }
        this.parent(peer, defaultAttributes);
    },

    getType : function() {
        return "CurvedLine";
    },

    setFrom : function(x, y) {
        $assert(!isNaN(x), "x must be defined");
        $assert(!isNaN(y), "y must be defined");

        this._peer.setFrom(x, y);
    },

    setTo : function(x, y) {
        $assert(!isNaN(x), "x must be defined");
        $assert(!isNaN(y), "y must be defined");

        this._peer.setTo(x, y);
    },

    getFrom : function() {
        return this._peer.getFrom();
    },

    getTo : function() {
        return this._peer.getTo();
    },

    setShowEndArrow : function(visible) {
        this._peer.setShowEndArrow(visible);
    },

    isShowEndArrow : function() {
        return this._peer.isShowEndArrow();
    },

    setShowStartArrow : function(visible) {
        this._peer.setShowStartArrow(visible);
    },

    isShowStartArrow : function() {
        return this._peer.isShowStartArrow();
    },

    setSrcControlPoint : function(control) {
        this._peer.setSrcControlPoint(control);
    },

    setDestControlPoint : function(control) {
        this._peer.setDestControlPoint(control);
    },

    getControlPoints : function() {
        return this._peer.getControlPoints();
    },

    isSrcControlPointCustom : function() {
        return this._peer.isSrcControlPointCustom();
    },

    isDestControlPointCustom : function() {
        return this._peer.isDestControlPointCustom();
    },

    setIsSrcControlPointCustom : function(isCustom) {
        this._peer.setIsSrcControlPointCustom(isCustom);
    },

    setIsDestControlPointCustom : function(isCustom) {
        this._peer.setIsDestControlPointCustom(isCustom);
    },

    updateLine : function(avoidControlPointFix) {
        return this._peer.updateLine(avoidControlPointFix);
    },

    setStyle : function(style) {
        this._peer.setLineStyle(style);

    },

    getStyle : function() {
        return this._peer.getLineStyle();
    },

    setDashed : function(length, spacing) {
        this._peer.setDashed(length, spacing);
    }
});

web2d.CurvedLine.SIMPLE_LINE = false;
web2d.CurvedLine.NICE_LINE = true;

