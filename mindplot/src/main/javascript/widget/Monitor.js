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

mindplot.widget.Monitor = new Class({
    initialize : function(fadeElem, msgElem) {
        $assert(fadeElem, "fadeElem can not be null");
        $assert(msgElem, "msgElem can not be null");
        this.msgQueue = [];

        this._fadeElem = fadeElem;
        this._msgElem = msgElem;
    },

    _showMsg : function(msg, msgKind) {
        if (msgKind == core.Monitor.MsgKind.ERROR) {
            msg = "<div id='small_error_icon'>" + msg + "</div>";
        }
        this._msgElem.innerHTML = msg;
    },

    logError : function(userMsg) {
        this.logMessage(userMsg, core.Monitor.MsgKind.ERROR);
    },

    logMessage : function(msg, msgKind) {
        console.log(msg);
    }
});

core.Monitor.MsgKind = {
    INFO:1,
    WARNING:2,
    ERROR:3,
    FATAL:4
};