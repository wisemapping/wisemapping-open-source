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

mindplot.layout.ChangeEvent = new Class(/** @lends ChangeEvent */{
    /** 
     * @constructs
     * @param {} id
     * @throws will throw an error if the given id is not/cannot be converted to a numerical value
     */
    initialize:function(id) {
        $assert(!isNaN(id), "id can not be null");
        this._id = id;
        this._position = null;
        this._order = null;
    },

    /** @return id */
    getId:function() {
        return this._id;
    },

    /** @return order */
    getOrder: function() {
        return this._order;
    },

    /** @return position */
    getPosition: function() {
        return this._position;
    },

    /** 
     * @param {} value the order to set 
     * @throws will throw an error if the given parameter is not/cannot be converted to a numerical
     * value
     */
    setOrder: function(value) {
        $assert(!isNaN(value), "value can not be null");
        this._order = value;
    },

    /** @param {} value
     *  @throws will throw an error if the value is null or undefined*/
    setPosition: function(value) {
        $assert(value, "value can not be null");
        this._position = value;
    },

    /** @return {String} order and position */
    toString: function() {
        return "[order:" + this.getOrder() + ", position: {" + this.getPosition().x + "," + this.getPosition().y + "}]";
    }
});


