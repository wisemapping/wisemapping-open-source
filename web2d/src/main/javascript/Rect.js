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

/**
 * Create a rectangle and variations of a rectangle shape.
 * arc must be specified to create rounded rectangles.
 * arc = "<length>"
 *     For rounded rectangles, radius of the ellipse used to round off the corners of the rectangle.
 */
web2d.Rect = new Class({
    Extends: web2d.Element,
    initialize : function(arc, attributes) {
        if (arc && arc > 1) {
            throw "Arc must be 0<=arc<=1";
        }
        if (arguments.length <= 0) {
            var rx = 0;
            var ry = 0;
        }

        var peer = web2d.peer.Toolkit.createRect(arc);
        var defaultAttributes = {width:40, height:40, x:5, y:5,stroke:'1 solid black',fillColor:'green'};
        for (var key in attributes) {
            defaultAttributes[key] = attributes[key];
        }
        this.parent(peer, defaultAttributes);
    },

    getType : function() {
        return "Rect";
    },

    getSize : function() {
        return this._peer.getSize();
    }
});