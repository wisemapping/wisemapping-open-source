/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License.  You may obtain a copy of the License at
*
*       http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*
* $Id: file 64488 2006-03-10 17:32:09Z paulo $
*/

mindplot.commands.DeleteTopicCommand = mindplot.Command.extend(
{
    initialize: function(topicsIds)
    {
        core.assert(topicsIds, "topicsIds must be defined");
        this._topicId = topicsIds;
        this._deletedTopicModels = [];
        this._parentTopicIds = [];
        this._id = mindplot.Command._nextUUID();
    },
    execute: function(commandContext)
    {
        var topics = commandContext.findTopics(this._topicId);
        topics.forEach(
                function(topic, index)
                {
                    var model = topic.getModel().clone();
                    this._deletedTopicModels.push(model);

                    // Is connected?.
                    var outTopic = topic.getOutgoingConnectedTopic();
                    var outTopicId = null;
                    if (outTopic != null)
                    {
                        outTopicId = outTopic.getId();
                    }
                    this._parentTopicIds.push(outTopicId);

                    // Finally, delete the topic from the workspace...
                    commandContext.deleteTopic(topic);

                }.bind(this)
                )
    },
    undoExecute: function(commandContext)
    {

        var topics = commandContext.findTopics(this._topicId);
        var parent = commandContext.findTopics(this._parentTopicIds);

        this._deletedTopicModels.forEach(
                function(model, index)
                {
                    var topic = commandContext.createTopic(model);

                    // Was the topic connected?
                    var parentTopic = parent[index];
                    if (parentTopic != null)
                    {
                        commandContext.connect(topic, parentTopic);
                    }

                }.bind(this)
                )

        this._deletedTopicModels = [];
        this._parentTopicIds = [];
    }
});