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

mindplot.LinkIcon = new Class({

    Extends: mindplot.Icon,
    initialize : function(topic, linkModel) {
        $assert(topic, 'topic can not be null');
        $assert(linkModel, 'linkModel can not be null');


        this.parent(mindplot.LinkIcon.IMAGE_URL);
        this._noteModel = linkModel;
        this._topic = topic;

        this._registerEvents();
    },

    _registerEvents : function() {
        this._image.setCursor('pointer');

        // Add on click event to open the editor ...
        this.addEvent('click', function(event) {
            this._topic.showLinkEditor();
            event.stopPropagation();
        }.bind(this));

        this._tip = new mindplot.widget.LinkIconTooltip(this);
    },

    getModel : function() {
        return this._noteModel;
    },

    remove : function() {
        var actionDispatcher = mindplot.ActionDispatcher.getInstance();
        actionDispatcher.removeLinkFromTopic(this._topic.getId());
    }
});
mindplot.LinkIcon.IMAGE_URL = "../images/world_link.png";

 