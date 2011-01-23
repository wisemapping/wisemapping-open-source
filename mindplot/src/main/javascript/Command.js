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

mindplot.Command = new Class(
{
    initialize: function()
    {
        this._id = mindplot.Command._nextUUID();
    },
    execute: function(commandContext)
    {
        throw "execute must be implemented.";
    },
    undoExecute: function(commandContext)
    {
        throw "undo must be implemented.";
    },
    getId:function()
    {
        return this._id;
    }
});

mindplot.Command._nextUUID = function()
{
    if (!mindplot.Command._uuid)
    {
        mindplot.Command._uuid = 1;
    }

    mindplot.Command._uuid = mindplot.Command._uuid + 1;
    return mindplot.Command._uuid;
};