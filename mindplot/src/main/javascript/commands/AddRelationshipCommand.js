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
        // Finally, focus ...
        var designer = commandContext._designer;
        designer.onObjectFocusEvent.attempt(relationship, designer);
        relationship.setOnFocus(true);
    },
    undoExecute: function(commandContext)
    {
        var relationship = commandContext.removeRelationship(this._model);
        this._mindmap.removeRelationship(this._model);
    }
});