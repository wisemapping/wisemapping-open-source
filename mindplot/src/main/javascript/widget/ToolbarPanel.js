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

mindplot.widget.ToolbarPanel = new Class({
    Implements:[Events],
    initialize : function(buttonId, model) {
        $assert(buttonId, "buttonId can not be null");
        $assert(model, "model can not be null");
        this._model = model;
        this._buttonId = buttonId;
        this._panelId = this.initPanel(buttonId);
    },

    buildPanel : function() {
        throw "Method must be implemented";
    }.protect(),

    initPanel: function (buttonId) {
        $assert(buttonId, "buttonId can not be null");

        var panelElem = this.buildPanel();
        var buttonElem = $(buttonId);

        // Add panel content ..
        panelElem.setStyle('display', 'none');
        panelElem.inject(buttonElem);

        // Register on toolbar elements ...
        var menuElems = panelElem.getElements('div');
        menuElems.forEach(function(elem) {
            elem.addEvent('click', function(event) {
                var value = $defined(elem.getAttribute('model')) ? elem.getAttribute('model') : elem.id;
                this._model.setValue(value);
                this.hide();
                event.stopPropagation();
            }.bind(this));
        }.bind(this));

        // Font family event handling ....
        buttonElem.addEvent('click', function() {

            // Is the panel being displayed ?
            if (this.isVisible()) {
                this.hide();
            } else {
                this.show();
            }

        }.bind(this));
        return panelElem.id;
    },

    show : function() {
        this.fireEvent('show');

        var menuElems = $(this._panelId).getElements('div');
        var value = this._model.getValue();
        menuElems.forEach(function(elem) {
            var elemValue = $defined(elem.getAttribute('model')) ? elem.getAttribute('model') : elem.id;
            if (elemValue == value)
                elem.className = "toolbarPanelLinkSelectedLink";
            else
                elem.className = "toolbarPanelLink";
        });
        $(this._panelId).setStyle('display', 'block');

        // Mark the button as active...
        $(this._buttonId).className = 'buttonActive';
    },

    hide : function() {
        $(this._panelId).setStyle('display', 'none');
        $(this._buttonId).className = 'button';

    },

    isVisible : function() {
        return $(this._panelId).getStyle('display') == 'block';
    }
});