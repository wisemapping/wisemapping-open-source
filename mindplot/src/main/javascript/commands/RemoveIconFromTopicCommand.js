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

mindplot.commands.RemoveIconFromTopicCommand = new Class({
    Extends:mindplot.Command,
    initialize: function(topicIds, iconModel) {
        $assert(topicIds, 'topicIds can not be null');
        $assert(iconModel, 'iconModel can not be null');
        this._objectsIds = topicIds;
        this._iconModel = iconModel;
    },

    execute: function(commandContext) {
        var topic = commandContext.findTopics(this._objectsIds)[0];
        topic.removeIcon(this._iconModel);
    },

    undoExecute: function(commandContext) {
        var topic = commandContext.findTopics(this._objectsIds)[0];
        var iconType = this._iconModel.getIconType();
        var iconImg = topic.addIcon(iconType, commandContext._designer);
        this._iconModel = iconImg.getModel();
    }
});