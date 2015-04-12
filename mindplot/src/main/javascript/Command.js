/*
*    Copyright [2015] [wisemapping]
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

mindplot.Command = new Class(/** @lends mindplot.Command */{
    /**
     * @classdesc The command base class for handling do/undo mindmap operations
     * @constructs
     */
    initialize: function()
    {
        this._id = mindplot.Command._nextUUID();
    },

    /** 
     * @abstract
     */
    execute: function(commandContext)
    {
        throw "execute must be implemented.";
    },

    /** 
     * Triggered by the undo button - reverses the executed command 
     * @abstract
     */
    undoExecute: function(commandContext)
    {
        throw "undo must be implemented.";
    },

    /** 
     * Returns the unique id of this command
     * @returns {Number} command id
     */
    getId:function()
    {
        return this._id;
    }
});

mindplot.Command._nextUUID = function()
{
    if (!$defined(mindplot.Command._uuid))
    {
        mindplot.Command._uuid = 1;
    }

    mindplot.Command._uuid = mindplot.Command._uuid + 1;
    return mindplot.Command._uuid;
};