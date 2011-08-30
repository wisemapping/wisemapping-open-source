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

web2d.peer.svg.TextPeer = new Class({
    Extends: web2d.peer.svg.ElementPeer,
    initialize : function() {
        var svgElement = window.document.createElementNS(this.svgNamespace, 'text');
        this.parent(svgElement);
        this._position = {x:0,y:0};
        this._font = new web2d.Font("Arial", this);
    },

    appendChild  : function(element) {
        this._native.appendChild(element._native);
    },

    setText  : function(text) {
        text = core.Utils.escapeInvalidTags(text);
        var child = this._native.firstChild;
        if ($defined(child)) {
            this._native.removeChild(child);
        }
        this._text = text;
        var textNode = window.document.createTextNode(text);
        this._native.appendChild(textNode);
    },

    getText  : function() {
        return this._text;
    },

    setPosition  : function(x, y) {
        this._position = {x:x, y:y};
        var height = this._font.getSize();
        if ($defined(this._parent) && $defined(this._native.getBBox))
            height = this.getHeight();
        var size = parseInt(height);
        this._native.setAttribute('y', y + size * 3 / 4);
        //y+size/2
        this._native.setAttribute('x', x);
    },

    getPosition  : function() {
        return this._position;
    },

    setFont  : function(font, size, style, weight) {
        if ($defined(font)) {
            this._font = new web2d.Font(font, this);
        }
        if ($defined(style)) {
            this._font.setStyle(style);
        }
        if ($defined(weight)) {
            this._font.setWeight(weight);
        }
        if ($defined(size)) {
            this._font.setSize(size);
        }
        this._updateFontStyle();
    },

    _updateFontStyle  : function() {
        this._native.setAttribute('font-family', this._font.getFontFamily());
        this._native.setAttribute('font-size', this._font.getGraphSize());
        this._native.setAttribute('font-style', this._font.getStyle());
        this._native.setAttribute('font-weight', this._font.getWeight());

        var scale = this._font.getFontScale();
        this._native.xFontScale = scale.toFixed(1);

    },
    setColor  : function(color) {
        this._native.setAttribute('fill', color);
    },

    getColor  : function() {
        return this._native.getAttribute('fill');
    },

    setTextSize  : function (size) {
        this._font.setSize(size);
        this._updateFontStyle();
    },

    setContentSize  : function(width, height) {
        this._native.xTextSize = width.toFixed(1) + "," + height.toFixed(1);
    },

    setStyle  : function (style) {
        this._font.setStyle(style);
        this._updateFontStyle();
    },

    setWeight  : function (weight) {
        this._font.setWeight(weight);
        this._updateFontStyle();
    },

    setFontFamily  : function (family) {
        var oldFont = this._font;
        this._font = new web2d.Font(family, this);
        this._font.setSize(oldFont.getSize());
        this._font.setStyle(oldFont.getStyle());
        this._font.setWeight(oldFont.getWeight());
        this._updateFontStyle();
    },

    getFont  : function () {
        return {
            font:this._font.getFont(),
            size:parseInt(this._font.getSize()),
            style:this._font.getStyle(),
            weight:this._font.getWeight()
        };
    },

    setSize  : function (size) {
        this._font.setSize(size);
        this._updateFontStyle();
    },

    getWidth  : function () {
        var computedWidth = this._native.getBBox().width;
        var width = parseInt(computedWidth);
        width = width + this._font.getWidthMargin();
        return width;
    },

    getHeight  : function () {
        var computedHeight = this._native.getBBox().height;
        return parseInt(computedHeight);
    },

    getHtmlFontSize  : function () {
        return this._font.getHtmlSize();
    }
});

