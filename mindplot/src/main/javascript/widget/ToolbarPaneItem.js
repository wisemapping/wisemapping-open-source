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

mindplot.widget.ToolbarPaneItem = new Class({
    Extends:mindplot.widget.ToolbarItem,
    initialize : function(buttonId, model) {
        $assert(buttonId, "buttonId can not be null");
        $assert(model, "model can not be null");
        this._model = model;
        var me = this;
        var fn = function() {
            // Is the panel being displayed ?
            me.isVisible() ? me.hide() : me.show();
        };
        this.parent(buttonId, fn, {topicAction:true,relAction:false});
        this._panelElem = this._init();
        this._visible = false;
    },

    _init:function () {
        // Load the context of the panel ...
        var panelElem = this.buildPanel();
        panelElem.css('cursor', 'default');
        var buttonElem = this.getButtonElem();

        var me = this;
        this._tip = new mindplot.widget.FloatingTip(buttonElem, {
            html: true,
            placement: 'bottom',
            content: function() {
                return me._updateSelectedItem();
            },
            className: 'toolbarPaneTip',
            trigger: 'manual',
            template: '<div class="popover popoverGray" role="tooltip"><div class="arrow arrowGray"></div><h3 class="popover-title"></h3><div class="popover-content"></div></div>'
        });

        this._tip.addEvent('hide', function() {
            me._visible = false
        });

        this._tip.addEvent('show', function() {
            me._visible = true
        });

        return panelElem;
    },

    getModel : function() {
        return this._model;
    },

    getPanelElem : function() {
        return this._panelElem;
    }.protect(),

    show : function() {
        if (!this.isVisible()) {
            this.parent();
            this._tip.show();
            this.getButtonElem().className = 'buttonExtActive';
        }
    },

    hide : function() {
        if (this.isVisible()) {
            this.parent();
            this._tip.hide();
            this.getButtonElem().className = 'buttonExtOn';
        }
    },

    isVisible : function() {
        return this._visible;
    },

    disable : function() {
        this.hide();
        var elem = this.getButtonElem();
        if (this._enable) {
            elem.unbind('click', this._fn);
            elem.removeClass('buttonExtOn');

            // Todo: Hack...
            elem.removeClass('buttonOn');

            elem.addClass('buttonExtOff');
            this._enable = false;
        }
    },

    enable : function() {
        var elem = this.getButtonElem();
        if (!this._enable) {
            elem.bind('click', this._fn);
            elem.removeClass('buttonExtOff');
            elem.addClass('buttonExtOn');
            this._enable = true;
        }
    },


    buildPanel : function() {
        throw "Method must be implemented";
    }.protect()

});