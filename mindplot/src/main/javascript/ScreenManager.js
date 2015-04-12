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

mindplot.ScreenManager = new Class({
    initialize: function (divElement) {
        $assert(divElement, "can not be null");
        this._divContainer = divElement;
        this._padding = {x: 0, y: 0};

        // Ignore default click event propagation. Prevent 'click' event on drag.
        this._clickEvents = [];
        this._divContainer.bind('click', function (event) {
            event.stopPropagation()
        });

        this._divContainer.bind('dblclick', function (event) {
            event.stopPropagation();
            event.preventDefault();
        });
    },

    setScale: function (scale) {
        $assert(scale, 'Screen scale can not be null');
        this._scale = scale;
    },

    addEvent: function (event, listener) {
        if (event == 'click')
            this._clickEvents.push(listener);
        else
            this._divContainer.bind(event, listener);
    },

    removeEvent: function (event, listener) {
        if (event == 'click') {
            this._clickEvents.remove(listener);
        }
        else {
            this._divContainer.unbind(event, listener);
        }
    },

    fireEvent: function (type, event) {
        if (type == 'click') {
            _.each(this._clickEvents, function (listener) {
                listener(type, event);
            });
        }
        else {
            this._divContainer.trigger(type, event);
        }
    },

    _getElementPosition: function (elem) {
        // Retrieve current element position.
        var elementPosition = elem.getPosition();
        var x = elementPosition.x;
        var y = elementPosition.y;

        // Add workspace offset.
        x = x - this._padding.x;
        y = y - this._padding.y;

        // Scale coordinate in order to be relative to the workspace. That's coord/size;
        x = x / this._scale;
        y = y / this._scale;

        // Remove decimal part..
        return {x: x, y: y};
    },

    getWorkspaceIconPosition: function (e) {
        // Retrieve current icon position.
        var image = e.getImage();
        var elementPosition = image.getPosition();
        var imageSize = e.getSize();

        //Add group offset
        var iconGroup = e.getGroup();
        var group = iconGroup.getNativeElement();
        var coordOrigin = group.getCoordOrigin();
        var groupSize = group.getSize();
        var coordSize = group.getCoordSize();

        var scale = {x: coordSize.width / parseInt(groupSize.width), y: coordSize.height / parseInt(groupSize.height)};

        var x = (elementPosition.x - coordOrigin.x - (parseInt(imageSize.width) / 2)) / scale.x;
        var y = (elementPosition.y - coordOrigin.y - (parseInt(imageSize.height) / 2)) / scale.y;

        //Retrieve iconGroup Position
        var groupPosition = iconGroup.getPosition();
        x = x + groupPosition.x;
        y = y + groupPosition.y;

        //Retrieve topic Position
        var topic = iconGroup.getTopic();
        var topicPosition = this._getElementPosition(topic);
        topicPosition.x = topicPosition.x - (parseInt(topic.getSize().width) / 2);

        // Remove decimal part..
        return {x: x + topicPosition.x, y: y + topicPosition.y};
    },

    getWorkspaceMousePosition: function (event) {
        // Retrieve current mouse position.
        var x = event.clientX;
        var y = event.clientY;

        //FIXME: paulo: why? Subtract div position.
        /*var containerPosition = this.getContainer().position();
         x = x - containerPosition.x;
         y = y - containerPosition.y;*/

        // Scale coordinate in order to be relative to the workspace. That's coordSize/size;
        x = x * this._scale;
        y = y * this._scale;

        // Add workspace offset.
        x = x + this._padding.x;
        y = y + this._padding.y;

        // Remove decimal part..
        return new core.Point(x, y);
    },

    getContainer: function () {
        return this._divContainer;
    },

    setOffset: function (x, y) {
        this._padding.x = x;
        this._padding.y = y;
    }
});
