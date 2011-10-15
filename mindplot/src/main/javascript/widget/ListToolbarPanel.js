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

mindplot.widget.ListToolbarPanel = new Class({
    Extends: mindplot.widget.ToolbarPaneItem,
    initialize : function(buttonId, model) {
        this.parent(buttonId, model);
        this._initPanel();
    },

    _initPanel: function () {
        // Register on toolbar elements ...
        var menuElems = this.getPanelElem().getElements('div');
        menuElems.forEach(function(elem) {
            elem.addEvent('click', function(event) {
                event.stopPropagation();
                this.hide();

                var value = $defined(elem.getAttribute('model')) ? elem.getAttribute('model') : elem.id;
                this.getModel().setValue(value);
            }.bind(this));
        }.bind(this));
    },

    _updateSelectedItem : function() {
        var panelElem = this.getPanelElem();
        var menuElems = panelElem.getElements('div');
        var value = this.getModel().getValue();
        menuElems.forEach(function(elem) {
            var elemValue = $defined(elem.getAttribute('model')) ? elem.getAttribute('model') : elem.id;
            $assert(elemValue,"elemValue can not be null");
            if (elemValue == value)
                elem.className = "toolbarPanelLinkSelectedLink";
            else
                elem.className = "toolbarPanelLink";
        });
        return panelElem;
    }
});