/*
*    Copyright [2011] [wisemapping]
*
*   Licensed under the Apache License, Version 2.0 (the "License") plus the
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

mindplot.DesignerActionRunner = new Class({
    execute:function(command)
    {
        core.assert(command, "command can not be null");
        // Execute action ...
        command.execute(this._context);

        // Enqueue it ...
        this._undoManager.enqueue(command);

        // Fire event
        var event = this._undoManager._buildEvent();
        this._designer._fireEvent("change", event);
    },
    initialize: function(designer)
    {
        this._designer = designer;
        this._undoManager = new mindplot.DesignerUndoManager();
        this._context = new mindplot.CommandContext(this._designer);
    },
    undo: function()
    {
        this._undoManager.execUndo(this._context);

        // Fire event
        var event = this._undoManager._buildEvent();
        this._designer._fireEvent("change", event);
    },
    redo: function()
    {
        this._undoManager.execRedo(this._context);

        // Fire event
        var event = this._undoManager._buildEvent();
        this._designer._fireEvent("change", event);

    },
    markAsChangeBase: function()
    {
        return this._undoManager.markAsChangeBase();
    },
    hasBeenChanged: function()
    {
        return this._undoManager.hasBeenChanged();
    }
});

mindplot.CommandContext = new Class({
    initialize: function(designer)
    {
        this._designer = designer;
    },
    findTopics:function(topicsIds)
    {
        var designerTopics = this._designer._topics;
        if (!(topicsIds instanceof Array))
        {
            topicsIds = [topicsIds];
        }

        var result = designerTopics.filter(function(topic) {
            var found = false;
            if (topic != null)
            {
                var topicId = topic.getId();
                found = topicsIds.contains(topicId);
            }
            return found;

        });
        return result;
    },
    deleteTopic:function(topic)
    {
        this._designer._removeNode(topic);
    },
    createTopic:function(model)
    {
        core.assert(model, "model can not be null");
        var topic = this._designer._nodeModelToNodeGraph(model);

        // @todo: Is this required ?
        var designer = this._designer;
        designer.onObjectFocusEvent.attempt(topic, designer);

        return topic;
    },
    createModel:function()
    {
        var mindmap = this._designer.getMindmap();
        var model = mindmap.createNode(mindplot.NodeModel.MAIN_TOPIC_TYPE);
        return model;
    },
    connect:function(childTopic, parentTopic)
    {
        childTopic.connectTo(parentTopic, this._designer._workspace);
    } ,
    disconnect:function(topic)
    {
        topic.disconnect(this._designer._workspace);
    },
    createRelationship:function(model){
        core.assert(model, "model cannot be null");
        var relationship = this._designer.createRelationship(model);
        return relationship;
    },
    removeRelationship:function(model) {
        this._designer.removeRelationship(model);
    },
    findRelationships:function(lineIds){
        var result = [];
        lineIds.forEach(function(lineId, index){
            var line = this._designer._relationships[lineId];
            if(core.Utils.isDefined(line)){
                result.push(line);
            }
        }.bind(this));
        return result;
    }
});

mindplot.DesignerActionRunner.setInstance = function(actionRunner)
{
    mindplot.DesignerActionRunner._instance = actionRunner;
};

mindplot.DesignerActionRunner.getInstance = function()
{
    return mindplot.DesignerActionRunner._instance;
};
