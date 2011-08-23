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

//// Init default logger level ...
//var wLogger = new Log4js.getLogger("WiseMapping");
//wLogger.setLevel(Log4js.Level.ALL);
////wLogger.addAppender(new Log4js.BrowserConsoleAppender());
//
//// Is logger service available ?
//if ($defined(window.LoggerService))
//{
//    Log4js.WiseServerAppender = function()
//    {
//      this.layout = new Log4js.SimpleLayout();
//    };
//
//    Log4js.WiseServerAppender.prototype = Log4js.extend(new Log4js.Appender(), {
//        /**
//         * @see Log4js.Appender#doAppend
//         */
//        doAppend: function(loggingEvent) {
//            try {
//                var message = this.layout.format(loggingEvent);
//                var level = this.levelCode(loggingEvent);
//
//                window.LoggerService.logError(level, message);
//
//            } catch (e) {
//               alert(e);
//            }
//        },
//
//        /**
//         * toString
//         */
//        toString: function() {
//            return "Log4js.WiseServerAppender";
//        },
//
//        levelCode: function(loggingEvent)
//        {
//            var retval;
//            switch (loggingEvent.level) {
//                case Log4js.Level.FATAL:
//                    retval = 3;
//                    break;
//                case Log4js.Level.ERROR:
//                    retval = 3;
//                    break;
//                case Log4js.Level.WARN:
//                    retval = 2;
//                    break;
//                default:
//                    retval = 1;
//                    break;
//            }
//
//            return retval;
//        }
//    });
//
//    wLogger.addAppender(new Log4js.WiseServerAppender());
//
//}
//

//// Handle error events ...
//window.onerror = function(sMsg, sUrl, sLine)
//{
//    window.hasUnexpectedErrors = true;
//    var msg =  sMsg + ' (' + sUrl + ', line ' + sLine + ')';
//    wLogger.fatal(msg);
//
//   $(window).fireEvent("error",null,0);
//    return false;
//};