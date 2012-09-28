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

mindplot.commands.AddTopicCommand = new Class({
    Extends:mindplot.Command,
    initialize:function (models, parentTopicsId) {
        $assert(models, 'models can not be null');
        $assert(parentTopicsId == null || parentTopicsId.length == models.length, 'parents and models must have the same size');

        this.parent();
        this._models = models;
        this._parentsIds = parentTopicsId;
    },

    execute:function (commandContext) {

        this._models.each(function (model, index) {

            // Add a new topic ...
            var topic = commandContext.createTopic(model);

            // Connect to topic ...
            if (this._parentsIds) {
                var parentId = this._parentsIds[index];
                if ($defined(parentId)) {
                    var parentTopic = commandContext.findTopics(parentId)[0];
                    commandContext.connect(topic, parentTopic);
                }
            }

            // Select just created node ...
            var designer = commandContext._designer;
            designer.onObjectFocusEvent(topic);
            topic.setOnFocus(true);

            // Render node ...
            topic.setVisibility(true);

        }.bind(this));
    },

    undoExecute:function (commandContext) {
        // Finally, delete the topic from the workspace ...
        this._models.each(function (model) {

            var topicId = model.getId();
            var topic = commandContext.findTopics(topicId)[0];
            commandContext.deleteTopic(topic);
        }.bind(this));
    }
});