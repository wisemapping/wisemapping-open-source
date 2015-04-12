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

mindplot.RESTPersistenceManager = new Class({
        Extends: mindplot.PersistenceManager,
        initialize: function (options) {
            this.parent();
            $assert(options.documentUrl, "documentUrl can not be null");
            $assert(options.revertUrl, "revertUrl can not be null");
            $assert(options.lockUrl, "lockUrl can not be null");
            $assert(options.session, "session can not be null");
            $assert(options.timestamp, "timestamp can not be null");

            this.documentUrl = options.documentUrl;
            this.revertUrl = options.revertUrl;
            this.lockUrl = options.lockUrl;
            this.timestamp = options.timestamp;
            this.session = options.session;
        },

        saveMapXml: function (mapId, mapXml, pref, saveHistory, events, sync) {

            var data = {
                id: mapId,
                xml: mapXml,
                properties: pref
            };

            var persistence = this;
            var query = "minor=" + !saveHistory;
            query = query + "&timestamp=" + this.timestamp;
            query = query + "&session=" + this.session;

            if (!persistence.onSave) {

                // Mark save in process and fire a event unlocking the save ...
                persistence.onSave = true;
                persistence.clearTimeout = setTimeout(function () {
                    persistence.clearTimeout = null;
                    persistence.onSave = false;
                }, 10000);

                $.ajax({
                    url: this.documentUrl.replace("{id}", mapId) + "?" + query,
                    type: 'put',
                    dataType: "json",
                    data: JSON.stringify(data),
                    contentType: "application/json; charset=utf-8",
                    async: !sync,

                    success: function (data, textStatus, jqXHRresponseText) {
                        persistence.timestamp = data;
                        events.onSuccess();
                    },
                    error: function (jqXHR, textStatus, errorThrown) {
                        events.onError(persistence._buildError());
                    },
                    complete: function () {
                        // Clear event timeout ...
                        if (persistence.clearTimeout) {
                            clearTimeout(persistence.clearTimeout);
                        }
                        persistence.onSave = false;
                    },
                    fail: function (xhr, textStatus) {

                        var responseText = xhr.responseText;
                        var userMsg = {severity: "SEVERE", message: $msg('SAVE_COULD_NOT_BE_COMPLETED')};

                        var contentType = xhr.getResponseHeader("Content-Type");
                        if (contentType != null && contentType.indexOf("application/json") != -1) {
                            var serverMsg = null;
                            try {
                                serverMsg = $.parseJSON(responseText);
                                serverMsg = serverMsg.globalSeverity ? serverMsg : null;
                            } catch (e) {
                                // Message could not be decoded ...
                            }
                            userMsg = persistence._buildError(serverMsg);

                        } else {
                            if (this.status == 405) {
                                userMsg = {severity: "SEVERE", message: $msg('SESSION_EXPIRED')};
                            }
                        }
                        events.onError(userMsg);
                        persistence.onSave = false;
                    }
                });
            }
        },

        discardChanges: function (mapId) {
            $.ajax({
                url: this.revertUrl.replace("{id}", mapId),
                async: false,
                method: 'post',
                headers: {"Content-Type": "application/json; charset=utf-8", "Accept": "application/json"}
            });
        },

        unlockMap: function (mindmap) {
            var mapId = mindmap.getId();
            $.ajax({
                url: this.lockUrl.replace("{id}", mapId),
                async: false,
                method: 'put',
                headers: {"Content-Type": "text/plain"},
                data: "false"
            });
        },

        _buildError: function (jsonSeverResponse) {
            var message = jsonSeverResponse ? jsonSeverResponse.globalErrors[0] : null;
            var severity = jsonSeverResponse ? jsonSeverResponse.globalSeverity : null;

            if (!message) {
                message = $msg('SAVE_COULD_NOT_BE_COMPLETED');
            }

            if (!severity) {
                severity = "INFO";
            }
            return {severity: severity, message: message};
        },

        loadMapDom: function (mapId) {
            // Let's try to open one from the local directory ...
            var xml;
            $.ajax({
                url: this.documentUrl.replace("{id}", mapId) + "/xml",
                method: 'get',
                async: false,
                headers: {"Content-Type": "text/plain", "Accept": "application/xml"},
                success: function (responseText) {
                    xml = responseText;
                }
            });

            // If I could not load it from a file, hard code one.
            if (xml == null) {
                throw new Error("Map could not be loaded");
            }

            return xml;
        }
    }
);
