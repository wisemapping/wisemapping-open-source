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

mindplot.LinkIcon = new Class({

    Extends: mindplot.Icon,
    initialize: function (topic, linkModel, readOnly) {
        $assert(topic, 'topic can not be null');
        $assert(linkModel, 'linkModel can not be null');

        this.parent(mindplot.LinkIcon.IMAGE_URL);
        this._linksModel = linkModel;
        this._topic = topic;
        this._readOnly = readOnly;

        this._registerEvents();
    },

    _registerEvents: function () {
        this._image.setCursor('pointer');
        this._tip = new mindplot.widget.LinkIconTooltip(this);

        var me = this;
        if (!this._readOnly) {
            // Add on click event to open the editor ...
            this.addEvent('click', function (event) {
                me._tip.hide();
                me._topic.showLinkEditor();
                event.stopPropagation();
            });
            //FIXME: we shouldn't have timeout of that..
            this.addEvent("mouseleave", function (event) {
                window.setTimeout(function () {
                    if (!$("#linkPopover:hover").length) {
                        me._tip.hide();
                    }
                    event.stopPropagation();
                }, 100)
            });
        }

        $(this.getImage()._peer._native).mouseenter(function () {
            me._tip.show();
        })
    },

    getModel: function () {
        return this._linksModel;
    }
});
mindplot.LinkIcon.IMAGE_URL = "images/links.png";

 