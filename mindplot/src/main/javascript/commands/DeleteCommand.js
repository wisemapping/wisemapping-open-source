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

        // If a parent has been selected for deletion, the children must be excluded from the delete ...
        var topics = this._filterChildren(this._topicIds, commandContext);


        if (topics.length > 0) {
            topics.each(function (topic) {
                var model = topic.getModel();

                // Delete relationships
                var relationships = this._collectInDepthRelationships(topic);
                this._deletedRelModel.append(relationships.map(function (rel) {
                    return rel.getModel().clone();
                }));

                relationships.each(function (relationship) {
                    commandContext.deleteRelationship(relationship);
                });

                // Store information for undo ...
                var clonedModel = model.clone();
                this._deletedTopicModels.push(clonedModel);
                var outTopic = topic.getOutgoingConnectedTopic();
                var outTopicId = null;
                if (outTopic != null) {
                    outTopicId = outTopic.getId();
                }
                this._parentTopicIds.push(outTopicId);

                // Finally, delete the topic from the workspace...
                commandContext.deleteTopic(topic);

            }, this);
        }

        var rels = commandContext.findRelationships(this._relIds);
        if (rels.length > 0) {
            rels.each(function (rel) {
                this._deletedRelModel.push(rel.getModel().clone());
                commandContext.deleteRelationship(rel);
            }, this);
        }
    },

    undoExecute:function (commandContext) {

        var parent = commandContext.findTopics(this._parentTopicIds);
        this._deletedTopicModels.each(function (model, index) {
            var topic = commandContext.createTopic(model);

            // Was the topic connected?
            var parentTopic = parent[index];
            if (parentTopic != null) {
                commandContext.connect(topic, parentTopic);
                topic.setOnFocus(true);
            }

        }, this);


        this._deletedRelModel.each(function (model) {
            commandContext.addRelationship(model);
        }.bind(this));

        this._deletedTopicModels = [];
        this._parentTopicIds = [];
        this._deletedRelModel = [];
    },

    _filterChildren:function (topicIds, commandContext) {
        var topics = commandContext.findTopics(topicIds);

        var result = [];
        topics.each(function (topic) {
            var parent = topic.getParent();
            var found = false;
            while (parent != null && !found) {
                found = topicIds.contains(parent.getId());
                if (found) {
                    break;
                }
                parent = parent.getParent();
            }

            if (!found) {
                result.push(topic);
            }
        });

        return result;
    },

    _collectInDepthRelationships:function (topic) {
        var result = [];
        var children = topic.getChildren();
        if (children.length > 0) {
            var rels = children.map(function (topic) {
                return this._collectInDepthRelationships(topic);
            }, this);
            result.append(rels.flatten());
        } else {
            result.append(topic.getRelationships());
        }
        return result;
    }

});