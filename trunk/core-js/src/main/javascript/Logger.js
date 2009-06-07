/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License.  You may obtain a copy of the License at
*
*       http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*
* $Id: file 64488 2006-03-10 17:32:09Z paulo $
*/

core.Logger =
{
    _enabled: false,
    setEnabled: function (enabled) {
        this._enabled = enabled;
    },init: function(serverLogger)
{
    this._serverLogger = serverLogger;
    if (window.onerror) {
        // Save any previously defined handler to call
        core.Logger._origOnWindowError = window.onerror;
    }
    window.onerror = core.Logger._onWindowError;
},
    log: function(message, severity, src)
    {
        if (!severity)
        {
            severity = core.LoggerSeverity.DEBUG;
        }

        // Error messages must be loggued in the server ...
        if (severity >= core.LoggerSeverity.ERROR)
        {
            if (this._serverLogger)
            {
                try
                {
                    this._serverLogger.logError(core.LoggerSeverity.ERROR, message);
                } catch(e)
                {
                }
            }
        }

        //        // Finally, log the error in debug console if it's enabled.
        if (this._enabled)
        {
            this._browserLogger(message);
        }
    },
    _browserLogger: function(message) {
        // Firebug is not enabled.

        if (core.Logger._origOnWindowError) {
            core.Logger._origOnWindowError();
        }

        if (!console)
        {
            if (!this._isInitialized)
            {
                this._console = window.document.createElement("div");
                this._console.style.position = "absolute";
                this._console.style.width = "300px";
                this._console.style.height = "200px";
                this._console.style.bottom = 0;
                this._console.style.right = 0;
                this._console.style.border = '1px solid black';
                this._console.style.background = 'yellow';
                this._console.style.zIndex = 60000;

                this._textArea = window.document.createElement("textarea");
                this._textArea.cols = "40";
                this._textArea.rows = "10";
                this._console.appendChild(this._textArea);

                window.document.body.appendChild(this._console);
                this._isInitialized = true;
            }
            this._textArea.value = this._textArea.value + "\n" + msg;

        } else
        {
            // Firebug console...
            console.log(message);
        }
    },
/**
 * Handles logging of messages due to window error events.
 *
 * @method _onWindowError
 * @param sMsg {String} The error message.
 * @param sUrl {String} URL of the error.
 * @param sLine {String} Line number of the error.
 * @private
 */
    _onWindowError: function(sMsg, sUrl, sLine) {
        // Logger is not in scope of this event handler
        // http://cfis.savagexi.com/articles/2007/05/08/what-went-wrong-with-my-javascript
        try {
            core.Logger.log(sMsg + ' (' + sUrl + ', line ' + sLine + ')', core.LoggerSeverity.ERROR, "window");
        }
        catch(e) {
            return false;
        }
        return true;
    },
    logError: function(msg) {
        core.Logger.log(msg, core.LoggerSeverity.ERROR, "code");
    }
};

core.LoggerSeverity =
{
    DEBUG: 1,
    WARNING: 2,
    ERROR: 3,
    WINDOW: 4
};