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

mindplot.commands.AddTopicCommand = new Class(
{
    Extends:mindplot.Command,
    initialize: function(model, parentTopicId, animated)
    {
        core.assert(model, 'Model can not be null');
        this._model = model;
        this._parentId = parentTopicId;
        this._id = mindplot.Command._nextUUID();
        this._animated = $defined(animated)?animated:false;
    },
    execute: function(commandContext)
    {
        // Add a new topic ...

        var topic = commandContext.createTopic(this._model, !this._animated);

        // Connect to topic ...
        if ($defined(this._parentId))
        {
            var parentTopic = commandContext.findTopics(this._parentId)[0];
            commandContext.connect(topic, parentTopic, !this._animated);
        }

        var doneFn = function(){
            // Finally, focus ...
            var designer = commandContext._designer;
            designer.onObjectFocusEvent.attempt(topic, designer);
            topic.setOnFocus(true);
        };
        
        if(this._animated){
            core.Utils.setVisibilityAnimated([topic,topic.getOutgoingLine()],true,doneFn);
        } else
            doneFn.attempt();
    },
    undoExecute: function(commandContext)
    {
        // Finally, delete the topic from the workspace ...
        var topicId = this._model.getId();
        var topic = commandContext.findTopics(topicId)[0];
        var doneFn = function(){
            commandContext.deleteTopic(topic);
        };
        if(this._animated){
            core.Utils.setVisibilityAnimated([topic,topic.getOutgoingLine()],false, doneFn);
        }
        else
            doneFn.attempt();
    }
});