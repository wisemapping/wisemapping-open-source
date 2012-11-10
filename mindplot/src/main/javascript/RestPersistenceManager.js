/*
 *    Copyright [2012] [wisemapping]
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

mindplot.RESTPersistenceManager = new Class({
        Extends:mindplot.PersistenceManager,
        initialize:function (options) {
            this.parent();
            $assert(options.saveUrl, "saveUrl can not be null");
            $assert(options.revertUrl, "revertUrl can not be null");
            $assert(options.lockUrl, "lockUrl can not be null");
            $assert(options.session, "session can not be null");
            $assert(options.timestamp, "timestamp can not be null");

            this.saveUrl = options.saveUrl;
            this.revertUrl = options.revertUrl;
            this.lockUrl = options.lockUrl;
            this.timestamp = options.timestamp;
            this.session = options.session;
        },

        saveMapXml:function (mapId, mapXml, pref, saveHistory, events, sync) {

            var data = {
                id:mapId,
                xml:mapXml,
                properties:pref
            };

            var persistence = this;
            var query = "minor=" + !saveHistory;
            query = query + (this.timestamp ? "&timestamp=" + this.timestamp : "");
            query = query + (this.session ? "&session=" + this.session : "");

            var request = new Request({
                url:this.saveUrl.replace("{id}", mapId) + "?" + query,
                method:'put',
                async:!sync,

                onSuccess:function (responseText, responseXML) {
                    events.onSuccess();
                    persistence.timestamp = responseText;
                },

                onException:function (headerName, value) {
                    console.log("onException....");

                    events.onError(persistence._buildError());
                },

                onFailure:function (xhr) {
                    console.log("onFailure....");
                    var responseText = xhr.responseText;
                    var error = null;

                    var contentType = this.getHeader("Content-Type");
                    if (contentType != null && contentType.indexOf("application/json") != -1) {
                        try {
                            error = JSON.decode(responseText);
                            events.onError(persistence._buildError(error));
                        } catch (e) {
                            events.onError(persistence._buildError());
                            throw new Error("Unexpected error saving. Error response is not json object:" + responseText);
                        }
                    } else {
                        var msg = {severity:"ERROR", message:$msg('SAVE_COULD_NOT_BE_COMPLETED')};
                        if (this.status == 405) {
                            msg = {severity:"ERROR", message:$msg('SESSION_EXPIRED')};
                        }
                        events.onError(msg);
                    }

                },

                headers:{"Content-Type":"application/json", "Accept":"application/json"},
                emulation:false,
                urlEncoded:false
            });
            request.put(JSON.encode(data));
        },

        discardChanges:function (mapId) {
            var request = new Request({
                url:this.revertUrl.replace("{id}", mapId),
                async:false,
                method:'post',
                onSuccess:function () {
                },
                onException:function () {
                },
                onFailure:function () {
                },
                headers:{"Content-Type":"application/json", "Accept":"application/json"},
                emulation:false,
                urlEncoded:false
            });
            request.post();
        },

        unlockMap:function (mindmap) {
            var mapId = mindmap.getId();
            var request = new Request({
                url:this.lockUrl.replace("{id}", mapId),
                async:false,
                method:'put',
                onSuccess:function () {

                },
                onException:function () {
                },
                onFailure:function () {
                },
                headers:{"Content-Type":"text/plain"},
                emulation:false,
                urlEncoded:false
            });
            request.put("false");
        },

        _buildError:function (jsonSeverResponse) {
            var message = jsonSeverResponse ? jsonSeverResponse.globalErrors[0] : null;
            var severity = jsonSeverResponse ? jsonSeverResponse.globalSeverity : null;

            if (!message) {
                message = $msg('SAVE_COULD_NOT_BE_COMPLETED');
            }

            if (!severity) {
                severity = "INFO";
            }
            return {severity:severity, message:message};
        }
    }
);


