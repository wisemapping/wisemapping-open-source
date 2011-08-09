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
    Extends: mindplot.widget.ToolbarItem,
    initialize : function(buttonId, model) {
        this.parent(buttonId, model);
        this._panelId = this.initPanel();
    },

    buildPanel : function() {
        throw "Method must be implemented";
    }.protect(),

    initPanel: function () {
        var panelElem = this.buildPanel();
        var buttonElem = this.getButtonElem();

        // Add panel content ..
        panelElem.setStyle('display', 'none');
        panelElem.inject(buttonElem);

        // Register on toolbar elements ...
        var menuElems = panelElem.getElements('div');
        menuElems.forEach(function(elem) {
            elem.addEvent('click', function(event) {
                var value = $defined(elem.getAttribute('model')) ? elem.getAttribute('model') : elem.id;
                this.getModel().setValue(value);
                event.stopPropagation();
                this.hide();
            }.bind(this));
        }.bind(this));

        // Font family event handling ....
        buttonElem.addEvent('click', function(event) {

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
        if (!this.isVisible()) {
            this.parent();
            var menuElems = $(this._panelId).getElements('div');
            var value = this.getModel().getValue();
            menuElems.forEach(function(elem) {
                var elemValue = $defined(elem.getAttribute('model')) ? elem.getAttribute('model') : elem.id;
                if (elemValue == value)
                    elem.className = "toolbarPanelLinkSelectedLink";
                else
                    elem.className = "toolbarPanelLink";
            });
            $(this._panelId).setStyle('display', 'block');
        }
    },

    hide : function() {
        if (this.isVisible()) {
            this.parent();
            $(this._panelId).setStyle('display', 'none');
        }
    },

    isVisible : function() {
        return $(this._panelId).getStyle('display') == 'block';
    }
});