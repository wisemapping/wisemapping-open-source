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
    initialize: function(objectIds) {
        $assert(objectIds, "objectIds must be defined");
        this._objectsIds = objectIds;
        this._deletedTopicModels = [];
        this._parentTopicIds = [];
        this._deletedRelationships = [];
        this._id = mindplot.Command._nextUUID();
    },

    execute: function(commandContext) {
        var topics = commandContext.findTopics(this._objectsIds.nodes);
        if (topics.length > 0) {
            topics.forEach(
                function(topic, index) {
                    var model = topic.getModel().clone();

                    //delete relationships
                    var relationships = topic.getRelationships();
                    while (relationships.length > 0) {
                        var relationship = relationships[0];
                        this._deletedRelationships.push(relationship.getModel().clone());
                        commandContext.removeRelationship(relationship.getModel());
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
        var lines = commandContext.findRelationships(this._objectsIds.relationship);
        if (lines.length > 0) {
            lines.forEach(function(line, index) {
                if (line.isInWorkspace()) {
                    this._deletedRelationships.push(line.getModel().clone());
                    commandContext.removeRelationship(line.getModel());
                }
            }.bind(this));
        }
    },
    undoExecute: function(commandContext) {

        var topics = commandContext.findTopics(this._objectsIds);
        var parent = commandContext.findTopics(this._parentTopicIds);

        this._deletedTopicModels.forEach(
            function(model, index) {
                var topic = commandContext.createTopic(model);

                // Was the topic connected?
                var parentTopic = parent[index];
                if (parentTopic != null) {
                    commandContext.connect(topic, parentTopic);
                }

            }.bind(this)
        );
        this._deletedRelationships.forEach(
            function(relationship, index) {
                commandContext.createRelationship(relationship);
            }.bind(this));

        this._deletedTopicModels = [];
        this._parentTopicIds = [];
        this._deletedRelationships = [];
    }
});