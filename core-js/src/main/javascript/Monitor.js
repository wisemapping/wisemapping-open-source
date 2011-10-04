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

core.Monitor = new Class({
    initialize : function(fadeElement, logContentElem) {
        $assert(fadeElement, "fadeElement can not be null");
        $assert(logContentElem, "logContentElem can not be null");

        this.pendingMessages = [];
        this.inProgress = false;
        this._container = fadeElement;
        this._currentMessage = null;
        this._logContentElem = logContentElem;

        this._fxOpacity = fadeElement.effect('opacity', { duration: 6000 });

    },

    _logMessage : function(msg, msgKind) {
        this._fxOpacity.clearTimer();
        if (msgKind == core.Monitor.MsgKind.ERROR) {
            msg = "<div id='small_error_icon'>" + msg + "</div>";
        }
        this._currentMessage = msg;
        this._fxOpacity.start(1, 0);
        this._logContentElem.innerHTML = msg;

    },

    logError : function(userMsg) {
        this.logMessage(userMsg, core.Monitor.MsgKind.ERROR);
        console.log(userMsg);
    },

    logMessage : function(msg, msgKind) {
        if (!msgKind) {
            msgKind = core.Monitor.MsgKind.INFO;
        }

        if (msgKind == core.Monitor.MsgKind.FATAL) {
            // In this case, a modal dialog must be shown... No recovery is possible.
            new Windoo.Alert(msg,
                {
                    'window': {  theme:Windoo.Themes.aero,
                        title:"Outch!!. An unexpected error.",
                        'onClose':function() {
                        }
                    }
                });
        } else {
            var messages = this.pendingMessages;
            var monitor = this;

            if (!this.executer) {
                // Log current message ...
                monitor._logMessage(msg, msgKind);

                // Start worker thread ...
                var disptacher = function() {
                    if (messages.length > 0) {
                        var msgToDisplay = messages.shift();
                        monitor._logMessage(msgToDisplay);
                    }

                    // Stop thread?
                    if (messages.length == 0) {
                        $clear(monitor.executer);
                        monitor.executer = null;
                        monitor._fxOpacity.hide();
                        this._currentMessage = null;
                    }
                };
                this.executer = disptacher.periodical(600);
            } else {
                if (this._currentMessage != msg) {
                    messages.push(msg);
                }
            }
        }
    }
});

core.Monitor.setInstance = function(monitor) {
    this.monitor = monitor;
};

core.Monitor.getInstance = function() {
    var result = this.monitor;
    if (result == null) {
        result = {
            logError: function(msg) {
                console.log(msg)
            },

            logMessage: function(msg) {
                console.log(msg)
            }
        };
    }
    return result;
};


core.Monitor.MsgKind =
{
    INFO:1,
    WARNING:2,
    ERROR:3,
    FATAL:4
};