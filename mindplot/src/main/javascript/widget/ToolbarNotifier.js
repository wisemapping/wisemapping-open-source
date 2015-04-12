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

mindplot.widget.ToolbarNotifier = new Class({

    initialize:function () {
        this.container = $('#headerNotifier');
    },

    hide:function () {
        this.container.hide();
    },

    logMessage:function (msg, fade) {
        $assert(msg, 'msg can not be null');
        // In case of print,embedded no message is displayed ....
        if (this.container && !this.container.data('transitioning')) {
            this.container.data('transitioning', true);
            this.container.text(msg);
            this.container.css({top: "5px", left: ($(window).width() - this.container.width()) / 2 - 9});
            this.container.show().fadeOut(5000);
        }
        this.container.data('transitioning', false);
    }

});

var toolbarNotifier = new mindplot.widget.ToolbarNotifier();
$notify = function(msg) {
    toolbarNotifier.logMessage(msg);
};