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

mindplot.commands.DeleteCommand = new Class(/** @lends mindplot.commands.DeleteCommand */{
    Extends:mindplot.Command,
    /** 
     * @classdesc This command class handles do/undo of deleting a topic.
     * @constructs
     * @param {Array<String>} topicIds ids of the topics to delete
     * @param {Array<String>} relIds ids of the relationships connected to the topics
     * @extends mindplot.Command
     */
    initialize:function (topicIds, relIds) {
        $assert($defined(relIds), 'topicIds can not be null');

        this.parent();
        this._relIds = relIds;
        this._topicIds = topicIds;
        this._deletedTopicModels = [];
        this._deletedRelModel = [];
        this._parentTopicIds = [];
    },

    /** 
     * Overrides abstract parent method 
     */
    execute:function (commandContext) {

        // If a parent has been selected for deletion, the children must be excluded from the delete ...
        var topics = this._filterChildren(this._topicIds, commandContext);

        if (topics.length > 0) {
            _.each(topics, function (topic) {
                // In case that it's editing text node, force close without update ...
                topic.closeEditors();

                var model = topic.getModel();

                // Delete relationships
                var relationships = this._collectInDepthRelationships(topic);
                this._deletedRelModel.append(relationships.map(function (rel) {
                    return rel.getModel().clone();
                }));

                _.each(relationships, function (relationship) {
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
            _.each(rels, function (rel) {
                this._deletedRelModel.push(rel.getModel().clone());
                commandContext.deleteRelationship(rel);
            }, this);
        }
    },

    /** 
     * Overrides abstract parent method
     * @see {@link mindplot.Command.undoExecute} 
     */
    undoExecute:function (commandContext) {

        // Add all the topics ...
        _.each(this._deletedTopicModels, function (model) {
            commandContext.createTopic(model);
        }, this);

        // Do they need to be connected ?
        _.each(this._deletedTopicModels, function (topicModel, index) {
            var topics = commandContext.findTopics(topicModel.getId());

            var parentId = this._parentTopicIds[index];
            if (parentId) {
                var parentTopics = commandContext.findTopics(parentId);
                commandContext.connect(topics[0], parentTopics[0]);
            }
        }, this);

        // Add rebuild relationships ...
        _.each(this._deletedRelModel, function (model) {
            commandContext.addRelationship(model);
        });

        // Finally display the topics ...
        _.each(this._deletedTopicModels, function (topicModel) {
            var topics = commandContext.findTopics(topicModel.getId());
            topics[0].setBranchVisibility(true);
        }, this);

        // Focus on last recovered topic ..
        if (this._deletedTopicModels.length > 0) {
            var firstTopic = this._deletedTopicModels[0];
            var topic = commandContext.findTopics(firstTopic.getId())[0];
            topic.setOnFocus(true);
        }

        this._deletedTopicModels = [];
        this._parentTopicIds = [];
        this._deletedRelModel = [];
    },

    _filterChildren:function (topicIds, commandContext) {
        var topics = commandContext.findTopics(topicIds);

        var result = [];
        _.each(topics, function (topic) {
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
        result.append(topic.getRelationships());

        var children = topic.getChildren();
        var rels = children.map(function (topic) {
            return this._collectInDepthRelationships(topic);
        }, this);
        result.append(rels.flatten());

        if (result.length > 0) {
            // Filter for unique ...
            result = result.sort(function (a, b) {
                return a.getModel().getId() - b.getModel().getId();
            });
            var ret = [result[0]];
            for (var i = 1; i < result.length; i++) { // start loop at 1 as element 0 can never be a duplicate
                if (result[i - 1] !== result[i]) {
                    ret.push(result[i]);
                }
            }
            result = ret;
        }
        return result;
    }

});