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
 * A group object can be used to collect shapes.
 */
web2d.Group = new Class({
    Extends: web2d.Element,
    initialize: function (attributes) {
        var peer = web2d.peer.Toolkit.createGroup();
        var defaultAttributes = {width: 50, height: 50, x: 50, y: 50, coordOrigin: '0 0', coordSize: '50 50'};
        for (var key in attributes) {
            defaultAttributes[key] = attributes[key];
        }
        this.parent(peer, defaultAttributes);
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

    /**
     * Appends an element as a child to the object.
     */
    append: function (element) {
        if (!$defined(element)) {
            throw "Child element can not be null";
        }

        if (element == this) {
            throw "It's not posible to add the group as a child of itself";
        }

        var elementType = element.getType();
        if (elementType == null) {
            throw "It seems not to be an element ->" + element;
        }

        if (elementType == "Workspace") {
            throw "A group can not have a workspace as a child";
        }

        this._peer.append(element._peer);
    },


    getType: function () {
        return "Group";
    },

    /**
     * The group element is a containing blocks for this content - they define a CSS2 "block level box".
     * Inside the containing block a local coordinate system is defined for any sub-elements using the coordsize and coordorigin attributes.
     * All CSS2 positioning information is expressed in terms of this local coordinate space.
     * Consequently CSS2 position attributes (left, top, width, height and so on) have no unit specifier -
     * they are simple numbers, not CSS length quantities.
     */
    setCoordSize: function (width, height) {
        this._peer.setCoordSize(width, height);
    },

    setCoordOrigin: function (x, y) {
        this._peer.setCoordOrigin(x, y);
    },

    getCoordOrigin: function () {
        return this._peer.getCoordOrigin();
    },
    getSize: function () {
        return this._peer.getSize();
    },

    setFill: function (color, opacity) {
        throw "Unsupported operation. Fill can not be set to a group";
    },

    setStroke: function (width, style, color, opacity) {
        throw "Unsupported operation. Stroke can not be set to a group";
    },

    getCoordSize: function () {
        return this._peer.getCoordSize();
    },

    appendDomChild: function (DomElement) {
        if (!$defined(DomElement)) {
            throw "Child element can not be null";
        }

        if (DomElement == this) {
            throw "It's not possible to add the group as a child of itself";
        }

        this._peer._native.append(DomElement);
    },

    setOpacity: function (value) {
        this._peer.setOpacity(value);
    }

});