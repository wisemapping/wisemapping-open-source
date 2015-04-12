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

mindplot.ShirinkConnector = new Class({
    initialize: function (topic) {

        var ellipse = new web2d.Elipse(mindplot.Topic.prototype.INNER_RECT_ATTRIBUTES);
        this._ellipse = ellipse;
        ellipse.setFill('rgb(62,118,179)');

        ellipse.setSize(mindplot.Topic.CONNECTOR_WIDTH, mindplot.Topic.CONNECTOR_WIDTH);
        ellipse.addEvent('click', function (event) {
            var model = topic.getModel();
            var collapse = !model.areChildrenShrunken();

            var topicId = topic.getId();
            var actionDispatcher = mindplot.ActionDispatcher.getInstance();
            actionDispatcher.shrinkBranch([topicId], collapse);

            event.stopPropagation();

        });

        ellipse.addEvent('mousedown', function (event) {
            // Avoid node creation ...
            event.stopPropagation();
        });

        ellipse.addEvent('dblclick', function (event) {
            // Avoid node creation ...
            event.stopPropagation();
        });

        ellipse.addEvent('mouseover', function (event) {

            ellipse.setFill('rgb(153, 0, 255)');
        });
        var me = this;
        ellipse.addEvent('mouseout', function (event) {
            var color = topic.getBackgroundColor();
            me.setFill(color);
        });

        ellipse.setCursor('default');
        this._fillColor = '#f7f7f7';
        var model = topic.getModel();
        this.changeRender(model.areChildrenShrunken());

    },

    changeRender: function (isShrink) {
        var elipse = this._ellipse;
        if (isShrink) {
            elipse.setStroke('2', 'solid');
        } else {
            elipse.setStroke('1', 'solid');
        }
    },

    setVisibility: function (value) {
        this._ellipse.setVisibility(value);
    },

    setOpacity: function (opacity) {
        this._ellipse.setOpacity(opacity);
    },

    setFill: function (color) {
        this._fillColor = color;
        this._ellipse.setFill(color);
    },

    setAttribute: function (name, value) {
        this._ellipse.setAttribute(name, value);
    },

    addToWorkspace: function (group) {
        group.append(this._ellipse);
    },

    setPosition: function (x, y) {
        this._ellipse.setPosition(x, y);
    },

    moveToBack: function () {
        this._ellipse.moveToBack();
    },

    moveToFront: function () {
        this._ellipse.moveToFront();
    }
});
