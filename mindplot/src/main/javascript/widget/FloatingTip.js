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

mindplot.widget.FloatingTip = new Class({
    Implements: [Options, mindplot.Events],

    options: {
        animation: true,
        html: false,
        placement: 'right',
        selector: false,
        trigger: 'hover',
        title: '',
        content: '',
        delay: 0,
        container: false,
        destroyOnExit: false
    },

    initialize: function (element, options) {
        this.setOptions(options);
        this.element = element;
        this._createPopover();
    },

    //FIXME: find a better way to do that...
    _createPopover: function() {
        this.element.popover(this.options);
        var me = this;
        if (this.options.destroyOnExit) {
            this.element.one('hidden.bs.popover', function() {
                me.element.popover('destroy');
                me._createPopover();
            });
        }
    },

    show: function () {
        this.element.popover('show');
        this.fireEvent('show');
        return this;
    },

    hide: function () {
        this.element.popover('hide');
        this.fireEvent('hide');
        return this;
    }
});
