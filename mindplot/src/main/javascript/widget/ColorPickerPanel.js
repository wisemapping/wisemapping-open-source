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

mindplot.widget.ColorPickerPanel = new Class({
    Extends: mindplot.widget.ToolbarPaneItem,
    initialize : function(buttonId, model) {
        this.parent(buttonId, model);
        this._mooRainbow = new MooRainbow(buttonId, {
            id: buttonId,
            imgPath: '../images/',
            startColor: [255, 255, 255],
            onInit: function() {
                this.fireEvent("show");
            }.bind(this),
            onChange: function(color) {
                this.getModel().setValue(color.hex);
            }.bind(this),
            onComplete: function() {
                this.hide();
            }.bind(this)
        });
    },

    show : function() {
        this.parent();
        this._mooRainbow.show();
    },

    hide : function() {
        this.parent();
        this._mooRainbow.hide();

    }
});