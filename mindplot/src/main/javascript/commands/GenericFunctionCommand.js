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

mindplot.commands.GenericFunctionCommand = mindplot.Command.extend(
{
   initialize: function(commandFunc,value,topicsIds)
    {
        core.assert(commandFunc, "commandFunc must be defined");
        core.assert(topicsIds, "topicsIds must be defined");
        this._value = value;
        this._selectedObjectsIds = topicsIds;
        this._commandFunc = commandFunc;
        this._oldValues = [];
        this._id = mindplot.Command._nextUUID();
    },
    execute: function(commandContext)
    {
        if (!this.applied)
        {
            var topics = commandContext.findTopics(this._selectedObjectsIds);
            topics.forEach(function(topic)
            {
                var oldValue = this._commandFunc(topic, this._value);
                this._oldValues.push(oldValue);
            }.bind(this));
            this.applied = true;
        } else
        {
            throw "Command can not be applied two times in a row.";
        }

    },
    undoExecute: function(commandContext)
    {
       if (this.applied)
        {
            var topics = commandContext.findTopics(this._selectedObjectsIds);
            topics.forEach(function(topic,index)
            {
                this._commandFunc(topic, this._oldValues[index]);

            }.bind(this));

            this.applied = false;
            this._oldValues = [];
        } else
        {
            throw "undo can not be applied.";
        }
    }
});