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


web2d.peer.svg.TextPeer = new Class({
    Extends: web2d.peer.svg.ElementPeer,
    initialize: function () {
        var svgElement = window.document.createElementNS(this.svgNamespace, 'text');
        this.parent(svgElement);
        this._position = {x: 0, y: 0};
        this._font = new web2d.Font("Arial", this);
    },

    append: function (element) {
        this._native.appendChild(element._native);
    },

    setTextAlignment: function (align) {
        this._textAlign = align;
    },


    getTextAlignment: function () {
        return $defined(this._textAlign) ? this._textAlign : 'left';
    },

    setText: function (text) {
        // Remove all previous nodes ...
        while (this._native.firstChild) {
            this._native.removeChild(this._native.firstChild);
        }

        this._text = text;
        if (text) {
            var lines = text.split('\n');
            var me = this;
            //FIXME: we could use underscorejs here
            lines.forEach(function (line) {
                var tspan = window.document.createElementNS(me.svgNamespace, 'tspan');
                tspan.setAttribute('dy', '1em');
                tspan.setAttribute('x', me.getPosition().x);

                tspan.textContent = line.length == 0 ? " " : line;
                me._native.appendChild(tspan);
            });
        }
    },

    getText: function () {
        return this._text;
    },

    setPosition: function (x, y) {
        this._position = {x: x, y: y};
        this._native.setAttribute('y', y);
        this._native.setAttribute('x', x);

        // tspan must be positioned manually.
        $(this._native).children('tspan').attr('x', x);
    },

    getPosition: function () {
        return this._position;
    },

    getNativePosition: function() {
        return $(this._native).position();
    },

    setFont: function (font, size, style, weight) {
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

    _updateFontStyle: function () {
        this._native.setAttribute('font-family', this._font.getFontFamily());
        this._native.setAttribute('font-size', this._font.getGraphSize());
        this._native.setAttribute('font-style', this._font.getStyle());
        this._native.setAttribute('font-weight', this._font.getWeight());
    },

    setColor: function (color) {
        this._native.setAttribute('fill', color);
    },

    getColor: function () {
        return this._native.getAttribute('fill');
    },

    setTextSize: function (size) {
        this._font.setSize(size);
        this._updateFontStyle();
    },

    setContentSize: function (width, height) {
        this._native.xTextSize = width.toFixed(1) + "," + height.toFixed(1);
    },

    setStyle: function (style) {
        this._font.setStyle(style);
        this._updateFontStyle();
    },

    setWeight: function (weight) {
        this._font.setWeight(weight);
        this._updateFontStyle();
    },

    setFontFamily: function (family) {
        var oldFont = this._font;
        this._font = new web2d.Font(family, this);
        this._font.setSize(oldFont.getSize());
        this._font.setStyle(oldFont.getStyle());
        this._font.setWeight(oldFont.getWeight());
        this._updateFontStyle();
    },

    getFont: function () {
        return {
            font: this._font.getFont(),
            size: parseInt(this._font.getSize()),
            style: this._font.getStyle(),
            weight: this._font.getWeight()
        };
    },

    setSize: function (size) {
        this._font.setSize(size);
        this._updateFontStyle();
    },

    getWidth: function () {
        var computedWidth;
        // Firefox hack for this issue:http://stackoverflow.com/questions/6390065/doing-ajax-updates-in-svg-breaks-getbbox-is-there-a-workaround
        try {

            computedWidth = this._native.getBBox().width;
            // Chrome bug is producing this error, oly during page loading. Remove the hack if it works. The issue seems to be
            // caused when the element is hidden. I don't know why, but it works ...
            if (computedWidth == 0) {
                var bbox = this._native.getBBox();
                computedWidth = bbox.width;
            }

        } catch (e) {
            computedWidth = 10;

        }

        var width = parseInt(computedWidth);
        width = width + this._font.getWidthMargin();
        return width;
    },

    getHeight: function () {
        // Firefox hack for this issue:http://stackoverflow.com/questions/6390065/doing-ajax-updates-in-svg-breaks-getbbox-is-there-a-workaround
        try {
            var computedHeight = this._native.getBBox().height;
        } catch (e) {
            computedHeight = 10;
        }
        return parseInt(computedHeight);
    },

    getHtmlFontSize: function () {
        return this._font.getHtmlSize();
    }
});

