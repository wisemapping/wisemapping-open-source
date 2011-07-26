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

mindplot.ShirinkConnector = new Class({
    initialize: function(topic) {

        var elipse = new web2d.Elipse(mindplot.Topic.prototype.INNER_RECT_ATTRIBUTES);
        this._elipse = elipse;
        elipse.setFill('#f7f7f7');

        elipse.setSize(mindplot.Topic.CONNECTOR_WIDTH, mindplot.Topic.CONNECTOR_WIDTH);
        elipse.addEventListener('click', function(event) {
            var model = topic.getModel();
            var isShrink = !model.areChildrenShrinked();

            var actionRunner = mindplot.DesignerActionRunner.getInstance();
            var topicId = topic.getId();

            var commandFunc = function(topic, isShrink) {
                topic.setChildrenShrinked(isShrink);
                return !isShrink;
            };

            var command = new mindplot.commands.GenericFunctionCommand(commandFunc, isShrink, [topicId]);
            actionRunner.execute(command);

            var e = new Event(event).stop();
            e.preventDefault();

        });

        elipse.addEventListener('mousedown', function(event) {
            // Avoid node creation ...
            var e = new Event(event).stop();
            e.preventDefault();
        });

        elipse.addEventListener('dblclick', function(event) {
            // Avoid node creation ...
            event = new Event(event).stop();
            event.preventDefault();

        });

        elipse.addEventListener('mouseover', function(event) {
            this.setFill('#009900');
        });

        elipse.addEventListener('mouseout', function(event) {
            var color = topic.getBackgroundColor();
            this.setFill(color);
        });

        elipse.setCursor('default');
        this._fillColor = '#f7f7f7';
        var model = topic.getModel();
        this.changeRender(model.areChildrenShrinked());

    },
    changeRender: function(isShrink) {
        var elipse = this._elipse;
        if (isShrink) {
            elipse.setStroke('2', 'solid');
        } else {
            elipse.setStroke('1', 'solid');
        }
    },

    setVisibility: function(value) {
        this._elipse.setVisibility(value);
    },

    setOpacity: function(opacity) {
        this._elipse.setOpacity(opacity);
    },

    setFill: function(color) {
        this._fillColor = color;
        this._elipse.setFill(color);
    },

    setAttribute: function(name, value) {
        this._elipse.setAttribute(name, value);
    },

    addToWorkspace: function(group) {
        group.appendChild(this._elipse);
    },


    setPosition: function(x, y) {
        this._elipse.setPosition(x, y);
    },

    moveToBack: function() {
        this._elipse.moveToBack();
    },

    moveToFront: function() {
        this._elipse.moveToFront();
    }
});