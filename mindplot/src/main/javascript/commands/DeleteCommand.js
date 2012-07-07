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

mindplot.commands.DeleteCommand = new Class({
    Extends:mindplot.Command,
    initialize:function (topicIds, relIds) {
        $assert($defined(relIds), 'topicIds can not be null');

        this.parent();
        this._relIds = relIds;
        this._topicIds = topicIds;
        this._deletedTopicModels = [];
        this._deletedRelModel = [];
        this._parentTopicIds = [];
    },

    execute:function (commandContext) {
        var topics = commandContext.findTopics(this._topicIds);
        if (topics.length > 0) {
            topics.forEach(
                function (topic, index) {
                    var model = topic.getModel();

                    // Delete relationships
                    var relationships = topic.getRelationships();
                    while (relationships.length > 0) {
                        var relationship = relationships[0];

                        this._deletedRelModel.push(relationship.getModel().clone());
                        commandContext.deleteRelationship(relationship);
                    }

                    this._deletedTopicModels.push(model);

                    // Is connected?.
                    var outTopic = topic.getOutgoingConnectedTopic();
                    var outTopicId = null;
                    if (outTopic != null) {
                        outTopicId = outTopic.getId();
                    }
                    this._parentTopicIds.push(outTopicId);

                    // Finally, delete the topic from the workspace...
                    commandContext.deleteTopic(topic);

                }.bind(this)
            );
        }
        var rels = commandContext.findRelationships(this._relIds);
        if (rels.length > 0) {
            rels.forEach(function (rel) {
                this._deletedRelModel.push(rel.getModel().clone());
                commandContext.deleteRelationship(rel);
            }.bind(this));
        }
    },

    undoExecute:function (commandContext) {

        var topics = commandContext.findTopics(this._topicIds);
        var parent = commandContext.findTopics(this._parentTopicIds);

        this._deletedTopicModels.forEach(
            function (model, index) {
                var topic = commandContext.createTopic(model);

                // Was the topic connected?
                var parentTopic = parent[index];
                if (parentTopic != null) {
                    commandContext.connect(topic, parentTopic);
                }

            }.bind(this)
        );

        this._deletedRelModel.forEach(function (model) {
            commandContext.addRelationship(model);
        }.bind(this));

        this._deletedTopicModels = [];
        this._parentTopicIds = [];
        this._deletedRelModel = [];
    }
});