mindplot.commands.AddRelationshipCommand = mindplot.Command.extend(
{
    initialize: function(model, mindmap)
    {
        core.assert(model, 'Relationship model can not be null');
        this._model = model;
        this._mindmap = mindmap;
        this._id = mindplot.Command._nextUUID();
    },
    execute: function(commandContext)
    {
        var relationship = commandContext.createRelationship(this._model);
    },
    undoExecute: function(commandContext)
    {
        var relationship = commandContext.removeRelationship(this._model);
        this._mindmap.removeRelationship(this._model);
    }
});