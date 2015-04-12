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

mindplot.DesignerActionRunner = new Class({
    initialize: function (commandContext, notifier) {
        $assert(commandContext, "commandContext can not be null");

        this._undoManager = new mindplot.DesignerUndoManager();
        this._context = commandContext;
        this._notifier = notifier;
    },

    execute: function (command) {
        $assert(command, "command can not be null");
        command.execute(this._context);
        this._undoManager.enqueue(command);
        this.fireChangeEvent();
        mindplot.EventBus.instance.fireEvent(mindplot.EventBus.events.DoLayout);

    },

    undo: function () {
        this._undoManager.execUndo(this._context);
        this.fireChangeEvent();
        mindplot.EventBus.instance.fireEvent(mindplot.EventBus.events.DoLayout);
    },

    redo: function () {
        this._undoManager.execRedo(this._context);
        this.fireChangeEvent();
        mindplot.EventBus.instance.fireEvent(mindplot.EventBus.events.DoLayout);

    },

    fireChangeEvent: function () {
        var event = this._undoManager.buildEvent();
        this._notifier.fireEvent("modelUpdate", event);
    }
});
