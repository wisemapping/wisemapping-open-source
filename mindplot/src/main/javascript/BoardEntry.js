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

mindplot.BoardEntry = new Class({
    initialize:function(lowerLimit, upperLimit, order) {
        if ($defined(lowerLimit) && $defined(upperLimit)) {
            $assert(lowerLimit < upperLimit, 'lowerLimit can not be greater that upperLimit');
        }
        this._upperLimit = upperLimit;
        this._lowerLimit = lowerLimit;
        this._order = order;
        this._topic = null;
        this._xPos = null;
    },


    getUpperLimit : function() {
        return this._upperLimit;
    },

    setXPosition : function(xPosition) {
        this._xPos = xPosition;
    },

    workoutEntryYCenter : function() {
        return this._lowerLimit + ((this._upperLimit - this._lowerLimit) / 2);
    },

    setUpperLimit : function(value) {
        $assert($defined(value), "upper limit can not be null");
        $assert(!isNaN(value), "illegal value");
        this._upperLimit = value;
    },

    isCoordinateIn : function(coord) {
        return this._lowerLimit <= coord && coord < this._upperLimit;
    },

    getLowerLimit : function() {
        return this._lowerLimit;
    },

    setLowerLimit : function(value) {
        $assert($defined(value), "upper limit can not be null");
        $assert(!isNaN(value), "illegal value");
        this._lowerLimit = value;
    },

    setOrder : function(value) {
        this._order = value;
    },

    getWidth : function() {
        return Math.abs(this._upperLimit - this._lowerLimit);
    },


    getTopic : function() {
        return this._topic;
    },


    removeTopic : function() {
        $assert(!this.isAvailable(), "Entry doesn't have a topic.");
        var topic = this.getTopic();
        this.setTopic(null);
        topic.setOrder(null);
    },


    update : function() {
        var topic = this.getTopic();
        this.setTopic(topic);
    },

    setTopic : function(topic, updatePosition) {
        if (!$defined(updatePosition) || ($defined(updatePosition) && !updatePosition)) {
            updatePosition = true;
        }

        this._topic = topic;
        if ($defined(topic)) {
            // Fixed positioning. Only for main topic ...
            var position = null;
            var topicPosition = topic.getPosition();

            // Must update position base on the border limits?
            if ($defined(this._xPos)) {
                position = new core.Point();

                // Update x position ...
                var topicSize = topic.getSize();
                var halfTopicWidh = parseInt(topicSize.width / 2);
                halfTopicWidh = (this._xPos > 0) ? halfTopicWidh : -halfTopicWidh;
                position.x = this._xPos + halfTopicWidh;
                position.y = this.workoutEntryYCenter();
            } else {

                // Central topic
                this._height = topic.getSize().height;
                var xPos = topicPosition.x;
                var yPos = this.workoutEntryYCenter();
                position = new core.Point(xPos, yPos);
            }

            // @todo: No esta de mas...
            topic.setPosition(position);
            topic.setOrder(this._order);
        }
        else {
            this._height = this._defaultWidth;
        }
    },

    isAvailable : function() {
        return !$defined(this._topic);
    },

    getOrder : function() {
        return this._order;
    },

    inspect : function() {
        return '(order: ' + this._order + ', lowerLimit:' + this._lowerLimit + ', upperLimit: ' + this._upperLimit + ', available:' + this.isAvailable() + ')';
    }
});