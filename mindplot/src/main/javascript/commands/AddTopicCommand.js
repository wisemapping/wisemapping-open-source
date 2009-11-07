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

mindplot.commands.AddTopicCommand = mindplot.Command.extend(
{
    initialize: function(model, parentTopicId)
    {
        core.assert(model, 'Model can not be null');
        this._model = model;
        this._parentId = parentTopicId;
        this._id = mindplot.Command._nextUUID();
    },
    execute: function(commandContext)
    {
        // Add a new topic ...
        var topic = commandContext.createTopic(this._model);

        // Connect to topic ...
        if (this._parentId)
        {
            var parentTopic = commandContext.findTopics(this._parentId)[0];
            commandContext.connect(topic, parentTopic);
        }

        // Finally, focus ...
        topic.setOnFocus(true);
    },
    undoExecute: function(commandContext)
    {
        // Finally, delete the topic from the workspace ...
        var topicId = this._model.getId();
        var topic = commandContext.findTopics(topicId)[0];
        commandContext.deleteTopic(topic);
    }
});