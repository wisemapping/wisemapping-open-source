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

web2d.Workspace = new Class({
    Extends: web2d.Element,
    initialize: function (attributes) {
        this._htmlContainer = this._createDivContainer();

        var peer = web2d.peer.Toolkit.createWorkspace(this._htmlContainer);
        var defaultAttributes = {
            width: '200px', height: '200px', stroke: '1px solid #edf1be',
            fillColor: "white", coordOrigin: '0 0', coordSize: '200 200'
        };
        for (var key in attributes) {
            defaultAttributes[key] = attributes[key];
        }
        this.parent(peer, defaultAttributes);
        this._htmlContainer.append(this._peer._native);
    },

    getType: function () {
        return "Workspace";
    },

    /**
     * Appends an element as a child to the object.
     */
    append: function (element) {
        if (!$defined(element)) {
            throw "Child element can not be null";
        }
        var elementType = element.getType();
        if (elementType == null) {
            throw "It seems not to be an element ->" + element;
        }

        if (elementType == "Workspace") {
            throw "A workspace can not have a workspace as a child";
        }

        this._peer.append(element._peer);
    },

    addItAsChildTo: function (element) {
        if (!$defined(element)) {
            throw "Workspace div container can not be null";
        }
        element.append(this._htmlContainer);
    },

    /**
     * Create a new div element that will be responsible for containing the workspace elements.
     */
    _createDivContainer: function () {
        var container = window.document.createElement("div");
        container.id = "workspaceContainer";
//        container.style.overflow = "hidden";
        container.style.position = "relative";
        container.style.top = "0px";
        container.style.left = "0px";
        container.style.height = "688px";
        container.style.border = '1px solid red';

        return $(container);
    },

    /**
     *  Set the workspace area size. It can be defined using different units:
     * in (inches; 1in=2.54cm)
     * cm (centimeters; 1cm=10mm)
     * mm (millimeters)
     * pt (points; 1pt=1/72in)
     * pc (picas; 1pc=12pt)
     */
    setSize: function (width, height) {
        // HTML container must have the size of the group element.
        if ($defined(width)) {
            this._htmlContainer.css('width', width);

        }

        if ($defined(height)) {
            this._htmlContainer.css('height', height);
        }
        this._peer.setSize(width, height);
    },

    /**
     * The workspace element is a containing blocks for this content - they define a CSS2 "block level box".
     * Inside the containing block a local coordinate system is defined for any sub-elements using the coordsize and coordorigin attributes.
     * All CSS2 positioning information is expressed in terms of this local coordinate space.
     * Consequently CSS2 position attributes (left, top, width, height and so on) have no unit specifier -
     * they are simple numbers, not CSS length quantities.
     */
    setCoordSize: function (width, height) {
        this._peer.setCoordSize(width, height);
    },

    /**
     * @Todo: Complete Doc
     */
    setCoordOrigin: function (x, y) {
        this._peer.setCoordOrigin(x, y);
    },

    /**
     * @Todo: Complete Doc
     */
    getCoordOrigin: function () {
        return this._peer.getCoordOrigin();
    },


// Private method declaration area
    /**
     * All the SVG elements will be children of this HTML element.
     */
    _getHtmlContainer: function () {
        return this._htmlContainer;
    },

    setFill: function (color, opacity) {
        this._htmlContainer.css('background-color', color);
        if (opacity || opacity === 0) {
            throw "Unsupported operation. Opacity not supported.";
        }
    },

    getFill: function () {
        var color = this._htmlContainer.css('background-color');
        return {color: color};
    },


    getSize: function () {
        var width = this._htmlContainer.css('width');
        var height = this._htmlContainer.css('height');
        return {width: width, height: height};
    },

    setStroke: function (width, style, color, opacity) {
        if (style != 'solid') {
            throw 'Not supported style stroke style:' + style;
        }
        this._htmlContainer.css('border', width + ' ' + style + ' ' + color);

        if (opacity || opacity === 0) {
            throw "Unsupported operation. Opacity not supported.";
        }
    },


    getCoordSize: function () {
        return this._peer.getCoordSize();
    },

    /**
     * Remove an element as a child to the object.
     */
    removeChild: function (element) {
        if (!$defined(element)) {
            throw "Child element can not be null";
        }

        if (element == this) {
            throw "It's not possible to add the group as a child of itself";
        }

        var elementType = element.getType();
        if (elementType == null) {
            throw "It seems not to be an element ->" + element;
        }

        this._peer.removeChild(element._peer);
    },

    dumpNativeChart: function () {
        var elem = this._htmlContainer
        return elem.innerHTML;
    }
});