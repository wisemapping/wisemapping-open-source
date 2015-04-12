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

mindplot.widget.TopicShapePanel = new Class({
    Extends: mindplot.widget.ListToolbarPanel,
    initialize: function (buttonId, model) {
        this.parent(buttonId, model);
    },

    buildPanel: function () {

        var content = $("<div class='toolbarPanel' id='topicShapePanel'></div>");
        content[0].innerHTML = '' +
            '<div id="rectagle" model="rectagle"><img src="images/shape-rectangle.png" alt="Rectangle"></div>' +
            '<div id="rounded_rectagle" model="rounded rectagle" ><img src="images/shape-rectangle-round.png" alt="Rounded Rectangle"></div>' +
            '<div id="line" model="line"><img src="images/shape-line.png" alt="Line"></div>' +
            '<div id="elipse" model="elipse"><img src="images/shape-circle.png"></div>';

        return content;

    }
});