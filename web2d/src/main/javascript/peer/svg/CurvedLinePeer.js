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

web2d.peer.svg.CurvedLinePeer = new Class({
    Extends: web2d.peer.svg.ElementPeer,
    initialize :function() {
        var svgElement = window.document.createElementNS(this.svgNamespace, 'path');
        this.parent(svgElement);
        this._style = {fill:'#495879'};
        this._updateStyle();
        this._customControlPoint_1 = false;
        this._customControlPoint_2 = false;
        this._control1 = new core.Point();
        this._control2 = new core.Point();
        this._lineStyle = true;
    },


    setSrcControlPoint : function(control) {
        this._customControlPoint_1 = true;
        var change = this._control1.x != control.x || this._control1.y != control.y;
        if ($defined(control.x)) {
            this._control1 = control;
            this._control1.x = parseInt(this._control1.x);
            this._control1.y = parseInt(this._control1.y)
        }
        if (change)
            this._updatePath();
    },

    setDestControlPoint : function(control) {
        this._customControlPoint_2 = true;
        var change = this._control2.x != control.x || this._control2.y != control.y;
        if ($defined(control.x)) {
            this._control2 = control;
            this._control2.x = parseInt(this._control2.x);
            this._control2.y = parseInt(this._control2.y)
        }
        if (change)
            this._updatePath();
    },

    isSrcControlPointCustom : function() {
        return this._customControlPoint_1;
    },

    isDestControlPointCustom : function() {
        return this._customControlPoint_2;
    },

    setIsSrcControlPointCustom : function(isCustom) {
        this._customControlPoint_1 = isCustom;
    },

    setIsDestControlPointCustom : function(isCustom) {
        this._customControlPoint_2 = isCustom;
    },


    getControlPoints : function() {
        return [this._control1, this._control2];
    },

    setFrom : function(x1, y1) {
        var change = this._x1 != parseInt(x1) || this._y1 != parseInt(y1);
        this._x1 = parseInt(x1);
        this._y1 = parseInt(y1);
        if (change)
            this._updatePath();
    },

    setTo : function(x2, y2) {
        var change = this._x2 != parseInt(x2) || this._y2 != parseInt(y2);
        this._x2 = parseInt(x2);
        this._y2 = parseInt(y2);
        if (change)
            this._updatePath();
    },

    getFrom : function() {
        return new core.Point(this._x1, this._y1);
    },

    getTo : function() {
        return new core.Point(this._x2, this._y2);
    },

    setStrokeWidth : function(width) {
        this._style['stroke-width'] = width;
        this._updateStyle();
    },

    setColor : function(color) {
        this._style['stroke'] = color;
        this._style['fill'] = color;
        this._updateStyle();
    },

    updateLine : function(avoidControlPointFix) {
        this._updatePath(avoidControlPointFix);
    },

    setLineStyle : function (style) {
        this._lineStyle = style;
        if (this._lineStyle) {
            this._style['fill'] = this._fill;
        } else {
            this._fill = this._style['fill'];
            this._style['fill'] = 'none';
        }
        this._updateStyle();
        this.updateLine();
    },

    getLineStyle : function () {
        return this._lineStyle;
    },


    setShowEndArrow : function(visible) {
        this._showEndArrow = visible;
        this.updateLine();
    },

    isShowEndArrow : function() {
        return this._showEndArrow;
    },

    setShowStartArrow : function(visible) {
        this._showStartArrow = visible;
        this.updateLine();
    },

    isShowStartArrow : function() {
        return this._showStartArrow;
    },


    _updatePath : function(avoidControlPointFix) {
        if ($defined(this._x1) && $defined(this._y1) && $defined(this._x2) && $defined(this._y2)) {
            this._calculateAutoControlPoints(avoidControlPointFix);
            var path = "M" + this._x1 + "," + this._y1
                + " C" + (this._control1.x + this._x1) + "," + (this._control1.y + this._y1) + " "
                + (this._control2.x + this._x2) + "," + (this._control2.y + this._y2) + " "
                + this._x2 + "," + this._y2 +
                (this._lineStyle ? " "
                    + (this._control2.x + this._x2) + "," + (this._control2.y + this._y2 + 3) + " "
                    + (this._control1.x + this._x1) + "," + (this._control1.y + this._y1 + 5) + " "
                    + this._x1 + "," + (this._y1 + 7) + " Z"
                    : ""
                    );
            this._native.setAttribute("d", path);
        }
    },

    _updateStyle : function() {
        var style = "";
        for (var key in this._style) {
            style += key + ":" + this._style[key] + " ";
        }
        this._native.setAttribute("style", style);
    },

    _calculateAutoControlPoints : function(avoidControlPointFix) {
        //Both points available, calculate real points
        var defaultpoints = mindplot.util.Shape.calculateDefaultControlPoints(new core.Point(this._x1, this._y1), new core.Point(this._x2, this._y2));
        if (!this._customControlPoint_1 && !($defined(avoidControlPointFix) && avoidControlPointFix == 0)) {
            this._control1.x = defaultpoints[0].x;
            this._control1.y = defaultpoints[0].y;
        }
        if (!this._customControlPoint_2 && !($defined(avoidControlPointFix) && avoidControlPointFix == 1)) {
            this._control2.x = defaultpoints[1].x;
            this._control2.y = defaultpoints[1].y;
        }
    },

    setDashed : function(length, spacing) {
        if ($defined(length) && $defined(spacing)) {
            this._native.setAttribute("stroke-dasharray", length + "," + spacing);
        } else {
            this._native.setAttribute("stroke-dasharray", "");
        }

    }
});
