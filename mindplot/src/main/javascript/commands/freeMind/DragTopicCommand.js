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
mindplot.commands.freeMind={};

mindplot.commands.freeMind.DragTopicCommand = mindplot.Command.extend(
{
    initialize: function()
    {
        this._modifiedTopics=null;
        this._id = mindplot.Command._nextUUID();
    },
    execute: function(commandContext)
    {
        var keys = this._modifiedTopics.keys();
        for(var i=0; i<keys.length; i++){
            var id = keys[i];
            var modTopic = this._modifiedTopics.get(id);
            var topic = commandContext.findTopics(parseInt(id))[0];

            var position = topic.getPosition();
            var pos = modTopic.newPos;
            if(position.x != pos.x || position.y  != pos.y){
                topic.setPosition(pos.clone(), true);
            }
        }
    },
    undoExecute: function(commandContext)
    {
        var keys = this._modifiedTopics.keys();
        for(var i=0; i<keys.length; i++){
            var id = keys[i];
            var modTopic = this._modifiedTopics.get(id);
            var topic = commandContext.findTopics(parseInt(id))[0];

            var position = topic.getPosition();
            var pos = modTopic.originalPos;
            if(position.x != pos.x || position.y  != pos.y){
                topic.setPosition(pos.clone(), true);
            }
        }
    },
    setModifiedTopics:function(modifiedTopics){
        this._modifiedTopics = modifiedTopics;
    }
});