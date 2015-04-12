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

mindplot.layout.Node = new Class(/** @lends Node */{
    /**
     * @constructs
     * @param id
     * @param size
     * @param position
     * @param sorter
     * @throws will throw an error if id is not a finite number or is null or undefined
     * @throws will throw an error if size is null or undefined
     * @throws will throw an error if position is null or undefined
     * @throws will throw an error if sorter is null or undefined
     */
    initialize:function (id, size, position, sorter) {
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

    /** */
    getId:function () {
        return this._id;
    },

    /** */
    setFree:function (value) {
        this._setProperty('free', value);
    },

    /** */
    isFree:function () {
        return this._getProperty('free');
    },

    /** */
    hasFreeChanged:function () {
        return this._isPropertyChanged('free');
    },

    /** */
    hasFreeDisplacementChanged:function () {
        return this._isPropertyChanged('freeDisplacement');
    },

    /** */
    setShrunken:function (value) {
        this._setProperty('shrink', value);
    },

    /** */
    areChildrenShrunken:function () {
        return this._getProperty('shrink');
    },

    /** */
    setOrder:function (order) {
        $assert(typeof order === 'number' && isFinite(order), "Order can not be null. Value:" + order);
        this._setProperty('order', order);
    },

    /** */
    resetPositionState:function () {
        var prop = this._properties['position'];
        if (prop) {
            prop.hasChanged = false;
        }
    },

    /** */
    resetOrderState:function () {
        var prop = this._properties['order'];
        if (prop) {
            prop.hasChanged = false;
        }
    },

    /** */
    resetFreeState:function () {
        var prop = this._properties['freeDisplacement'];
        if (prop) {
            prop.hasChanged = false;
        }
    },

    /** */
    getOrder:function () {
        return this._getProperty('order');
    },

    /** */
    hasOrderChanged:function () {
        return this._isPropertyChanged('order');
    },

    /** */
    hasPositionChanged:function () {
        return this._isPropertyChanged('position');
    },

    /** */
    hasSizeChanged:function () {
        return this._isPropertyChanged('size');
    },

    /** */
    getPosition:function () {
        return this._getProperty('position');
    },

    /** */
    setSize:function (size) {
        $assert($defined(size), "Size can not be null");
        this._setProperty('size', Object.clone(size));
    },

    /** */
    getSize:function () {
        return this._getProperty('size');
    },

    /** */
    setFreeDisplacement:function (displacement) {
        $assert($defined(displacement), "Position can not be null");
        $assert($defined(displacement.x), "x can not be null");
        $assert($defined(displacement.y), "y can not be null");
        var oldDisplacement = this.getFreeDisplacement();
        var newDisplacement = {x:oldDisplacement.x + displacement.x, y:oldDisplacement.y + displacement.y};

        this._setProperty('freeDisplacement', Object.clone(newDisplacement));
    },

    /** */
    resetFreeDisplacement:function () {
        this._setProperty('freeDisplacement', {x:0, y:0});
    },

    /** */
    getFreeDisplacement:function () {
        var freeDisplacement = this._getProperty('freeDisplacement');
        return (freeDisplacement || {x:0, y:0});
    },

    /** */
    setPosition:function (position) {
        $assert($defined(position), "Position can not be null");
        $assert($defined(position.x), "x can not be null");
        $assert($defined(position.y), "y can not be null");

        // This is a performance improvement to avoid movements that really could be avoided.
        var currentPos = this.getPosition();
        if (currentPos == null || Math.abs(currentPos.x - position.x) > 2 || Math.abs(currentPos.y - position.y) > 2)
            this._setProperty('position', position);
    },

    _setProperty:function (key, value) {
        var prop = this._properties[key];
        if (!prop) {
            prop = {
                hasChanged:false,
                value:null,
                oldValue:null
            };
        }

        // Only update if the property has changed ...
        if (JSON.stringify(prop.value) != JSON.stringify(value)) {
            prop.oldValue = prop.value;
            prop.value = value;
            prop.hasChanged = true;
        }
        this._properties[key] = prop;
    },

    _getProperty:function (key) {
        var prop = this._properties[key];
        return $defined(prop) ? prop.value : null;
    },

    _isPropertyChanged:function (key) {
        var prop = this._properties[key];
        return prop ? prop.hasChanged : false;
    },

    /** */
    getSorter:function () {
        return this._sorter;
    },

    /** @return {String} returns id, order, position, size and shrink information*/
    toString:function () {
        return "[id:" + this.getId() + ", order:" + this.getOrder() + ", position: {" + this.getPosition().x + "," + this.getPosition().y + "}, size: {" + this.getSize().width + "," + this.getSize().height + "}, shrink:" + this.areChildrenShrunken() + "]";
    }

});

