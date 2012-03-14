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
    initialize: function(model, parentTopicId) {
        $assert(model, 'Model can not be null');

        this.parent();
        this._model = model;
        this._parentId = parentTopicId;
    },

    execute: function(commandContext) {

        // Add a new topic ...
        var topic = commandContext.createTopic(this._model, false);

        // Connect to topic ...
        if ($defined(this._parentId)) {
            var parentTopic = commandContext.findTopics(this._parentId)[0];
            commandContext.connect(topic, parentTopic);
        }

        // Finally, focus ...
        var designer = commandContext._designer;
        var fade = new mindplot.util.FadeEffect([topic,topic.getOutgoingLine()], true);
        fade.addEvent('complete', function() {
            designer.onObjectFocusEvent(topic);
            topic.setOnFocus(true);
        });
        fade.start();
    },

    undoExecute: function(commandContext) {
        // Finally, delete the topic from the workspace ...
        var topicId = this._model.getId();
        var topic = commandContext.findTopics(topicId)[0];
        commandContext.deleteTopic(topic);
    }
});