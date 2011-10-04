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

mindplot.widget.ToolbarPaneItem = new Class({
    Extends:mindplot.widget.ToolbarItem,
    initialize : function(buttonId, model) {
        $assert(buttonId, "buttonId can not be null");
        $assert(model, "model can not be null");
        var fn = function() {
            // Is the panel being displayed ?
            if (this.isVisible()) {
                this.hide();
            } else {
                this.show();
            }

        }.bind(this);
        this.parent(buttonId, fn, {topicAction:true,relAction:true});
        this._model = model;
        this._panelId = this._init().id;
    },

    _init:function () {
        // Load the context of the panel ...
        var panelElem = this.buildPanel();
        var buttonElem = this.getButtonElem();

        // Add panel content ..
        panelElem.setStyle('display', 'none');
        panelElem.inject(buttonElem);

        return panelElem;
    },

    getModel : function() {
        return this._model;
    },

    getButtonElem : function() {
        var elem = $(this._buttonId);
        $assert(elem, "Could not find element for " + this._buttonId);
        return elem;
    }.protect(),

    getPanelElem : function() {
        return $(this._panelId);
    }.protect(),

    show : function() {
        if (!this.isVisible()) {
            this.parent();
            this.getButtonElem().className = 'buttonActive';
            this.getPanelElem().setStyle('display', 'block');
        }
    },

    hide : function() {
        if (this.isVisible()) {
            this.getButtonElem().className = 'buttonOn';
            this.getPanelElem().setStyle('display', 'none');
            this.parent();
        }
    },

    isVisible : function() {
        return this.getPanelElem().getStyle('display') == 'block';
    },

    disable: function() {
        this.hide();
        this.parent();
    },

    buildPanel : function() {
        throw "Method must be implemented";
    }.protect()

});