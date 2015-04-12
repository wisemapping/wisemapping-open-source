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

web2d.PolyLine = new Class({
    Extends: web2d.Element,
    initialize:function(attributes) {
        var peer = web2d.peer.Toolkit.createPolyLine();
        var defaultAttributes = {strokeColor:'blue',strokeWidth:1,strokeStyle:'solid',strokeOpacity:1};
        for (var key in attributes) {
            defaultAttributes[key] = attributes[key];
        }
        this.parent(peer, defaultAttributes);
    },

    getType : function() {
        return "PolyLine";
    },

    setFrom : function(x, y) {
        this._peer.setFrom(x, y);
    },

    setTo : function(x, y) {
        this._peer.setTo(x, y);
    },

    setStyle : function(style) {
        this._peer.setStyle(style);
    },

    getStyle : function() {
        return this._peer.getStyle();
    },

    buildCurvedPath : function(dist, x1, y1, x2, y2) {
        var signx = 1;
        var signy = 1;
        if (x2 < x1) {
            signx = -1;
        }
        if (y2 < y1) {
            signy = -1;
        }

        var path;
        if (Math.abs(y1 - y2) > 2) {
            var middlex = x1 + ((x2 - x1 > 0) ? dist : -dist);
            path = x1.toFixed(1) + ", " + y1.toFixed(1) + " " + middlex.toFixed(1) + ", " + y1.toFixed(1) + " " + middlex.toFixed(1) + ", " + (y2 - 5 * signy).toFixed(1) + " " + (middlex + 5 * signx).toFixed(1) + ", " + y2.toFixed(1) + " " + x2.toFixed(1) + ", " + y2.toFixed(1);
        } else {
            path = x1.toFixed(1) + ", " + y1.toFixed(1) + " " + x2.toFixed(1) + ", " + y2.toFixed(1);
        }

        return path;
    },

    buildStraightPath : function(dist, x1, y1, x2, y2) {
        var middlex = x1 + ((x2 - x1 > 0) ? dist : -dist);
        return  x1 + ", " + y1 + " " + middlex + ", " + y1 + " " + middlex + ", " + y2 + " " + x2 + ", " + y2;
    }
});


