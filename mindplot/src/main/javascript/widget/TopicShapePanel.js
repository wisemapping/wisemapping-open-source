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

mindplot.widget.TopicShapePanel = new Class({
    Extends : mindplot.widget.ToolbarPanel,
    initialize : function(buttonId, model) {
        this.parent(buttonId, model);
    },

    buildPanel: function() {

        var content = new Element("div", {'class':'toolbarPanel','id':'topicShapePanel'});
        content.innerHTML = '' +
            '<div id="rectagle" model="rectagle"><img src="../images/shape-rectangle.png" alt="Rectangle" width="40" height="25"></div>' +
            '<div id="rounded_rectagle" model="rounded rectagle" ><img src="../images/shape-rectangle-rounded.png"alt="Rounded Rectangle" width="40" height="25"></div>' +
            '<div id="line" model="line"><img src="../images/shape-line.png" alt="Line" width="40" height="7"></div>' +
            '<div id="elipse" model="elipse" class="toolbarPanelLink"><img src="../images/shape-elipse.png" alt="Elipse" width="40" height="25"></div>';

        return content;

    }
});