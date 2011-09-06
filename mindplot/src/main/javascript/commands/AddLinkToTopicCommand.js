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

mindplot.commands.AddLinkToTopicCommand = new Class({
    Extends:mindplot.Command,
    initialize: function(topicId, url) {
        $assert(topicId, 'topicId can not be null');
        this._objectsIds = topicId;
        this._url = url;
        this._id = mindplot.Command._nextUUID();
    },
    execute: function(commandContext) {
        var topic = commandContext.findTopics(this._objectsIds)[0];
        var updated = function() {
            topic.addLink(this._url, commandContext._designer);
            topic._adjustShapes();
        }.bind(this);
        updated.delay(0);
    },
    undoExecute: function(commandContext) {
        var topic = commandContext.findTopics(this._objectsIds)[0];
        var updated = function() {
            topic.removeLink();
        }.bind(this);
        updated.delay(0);
    }
});