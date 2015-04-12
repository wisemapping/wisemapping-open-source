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

mindplot.widget.FontSizePanel = new Class({
    Extends : mindplot.widget.ListToolbarPanel,
    initialize : function(buttonId, model) {
        this.parent(buttonId, model);
    },

    buildPanel: function() {

        var content = $("<div class='toolbarPanel' id='fontSizePanel'></div>");
        content[0].innerHTML = '' +
            '<div id="small" model="6" style="font-size:8px">Small</div>' +
            '<div id="normal" model="8" style="font-size:12px">Normal</div>' +
            '<div id="large" model="10" style="font-size:15px">Large</div>' +
            '<div id="huge"  model="15" style="font-size:24px">Huge</div>';

        return content;

    }
});