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
mindplot.commands.freeMind.ReconnectTopicCommand = mindplot.Command.extend(
{
    initialize: function()
    {
        this._modifiedTopics=null;
        this._id = mindplot.Command._nextUUID();
        this._node = null;
        this._targetNode = null;
        this._pivot = null;
        this._oldParent = null;
    },
    execute: function(commandContext)
    {
        var node = commandContext.findTopics(parseInt(this._node))[0];
        var targetNode = commandContext.findTopics(parseInt(this._targetNode))[0];
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
            if(id = this._node){
                node._originalPosition = modTopic.originalPos;
            }
        }
        var oldParent = this._oldParent!=null?commandContext.findTopics(parseInt(this._oldParent))[0]:null;
        node.relationship = this._pivot;
        node._relationship_oldParent = oldParent;
        node._relationship_index = this._index;
        commandContext.disconnect(node);
        var parentNode = targetNode;
        if(this._pivot != "Child"){
            parentNode = targetNode.getParent();
            node._relationship_sibling_node = targetNode;
        }
        commandContext.connect(node, parentNode);
        delete node.relationship;
        delete node._relationship_oldParent;
        delete node._relationship_sibling_node;
        delete node._relationship_index;
        delete node._originalPosition;
    },
    undoExecute: function(commandContext)
    {
        var node = commandContext.findTopics(parseInt(this._node))[0];
        var targetNode = this._oldParent!=null?commandContext.findTopics(parseInt(this._oldParent))[0]:null;

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
            if(id = this._node){
                node._originalPosition = modTopic.newPos;
            }
        }
        var oldParent = commandContext.findTopics(parseInt(this._targetNode))[0];
        if(this._pivot != "Child"){
            oldParent = oldParent.getParent();
        }
        if(targetNode!=null){
            node.relationship = "undo";
            node._relationship_oldParent = oldParent;
            node._relationship_index = this._index;
        }
        commandContext.disconnect(node);
        if(targetNode!=null){
            commandContext.connect(node, targetNode);
            delete node.relationship;
            delete node._relationship_oldParent;
            delete node._relationship_index;
        }
        delete node._originalPosition;
    },
    setModifiedTopics:function(modifiedTopics){
        this._modifiedTopics = modifiedTopics;
    },
    setDraggedTopic:function(node, index){
        this._node = node.getId();
        var outgoingConnectedTopic = node.getOutgoingConnectedTopic();
        this._oldParent = outgoingConnectedTopic!=null?outgoingConnectedTopic.getId():null;
        this._index = index;
    },
    setTargetNode:function(node){
        this._targetNode = node.getId();
    },
    setAs:function(relationship){
        this._pivot = relationship;
    }
});