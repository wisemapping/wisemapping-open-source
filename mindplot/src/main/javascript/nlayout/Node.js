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
mindplot.nlayout.Node = new Class({
    initialize:function(id, size, position, sorter) {
        $assert(typeof id === 'number' && isFinite(id), "id can not be null");
        $assert(size, "size can not be null");
        $assert(position, "position can not be null");
        $assert(sorter, "sorter can not be null");

        this._id = id;
        this._sorter = sorter;
        this._properties = {};

        this.setSize(size);
        this.setPosition(position);
        this.setShrunken(false);
    },

    getId:function() {
        return this._id;
    },

    setShrunken: function(value) {
        this._setProperty('shrink', value);
    },

    areChildrenShrunken: function() {
        return this._getProperty('shrink');
    },

    setOrder: function(order) {
        $assert(typeof order === 'number' && isFinite(order), "Order can not be null. Value:" + order);
        this._setProperty('order', order);
    },

    resetPositionState : function() {
        var prop = this._properties['position'];
        if (prop) {
            prop.hasChanded = false;
        }
    },

    resetOrderState : function() {
        var prop = this._properties['order'];
        if (prop) {
            prop.hasChanded = false;
        }
    },

    getOrder: function() {
        return this._getProperty('order');
    },

    hasOrderChanged: function() {
        return this._isPropertyChanged('order');
    },

    hasPositionChanged: function() {
        return this._isPropertyChanged('position');

    },

    getPosition: function() {
        return this._getProperty('position');
    },

    setSize : function(size) {
        $assert($defined(size), "Size can not be null");
        this._setProperty('size', Object.clone(size));
    },

    getSize: function() {
        return this._getProperty('size');
    },

    setPosition : function(position) {
        $assert($defined(position), "Position can not be null");
        $assert($defined(position.x), "x can not be null");
        $assert($defined(position.y), "y can not be null");

        this._setProperty('position', Object.clone(position));
    },

    _setProperty: function(key, value) {
        var prop = this._properties[key];
        if (!prop) {
            prop = {
                hasChanded:false,
                value: null,
                oldValue : null
            };
        }

        prop.oldValue = prop.value;
        prop.value = value;
        prop.hasChanded = true;

        this._properties[key] = prop;
    },

    _getProperty: function(key) {
        var prop = this._properties[key];
        return $defined(prop) ? prop.value : null;
    },

    _isPropertyChanged: function(key) {
        var prop = this._properties[key];
        return prop ? prop.hasChanded : false;
    },

    _setPropertyUpdated : function(key) {
        var prop = this._properties[key];
        if (prop) {
            this._properties[key] = true;
        }
    },

    getSorter: function() {
        return this._sorter;
    },


    toString: function() {
        return "[id:" + this.getId() + ", order:" + this.getOrder() + ", position: {" + this.getPosition().x + "," + this.getPosition().y + "}, size: {" + this.getSize().width + "," + this.getSize().height + "}";
    }

});

