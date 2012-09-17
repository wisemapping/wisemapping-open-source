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

mindplot.RESTPersistenceManager = new Class({
        Extends:mindplot.PersistenceManager,
        initialize:function (saveUrl, revertUrl) {
            this.parent();
            $assert(saveUrl, "saveUrl can not be null");
            $assert(revertUrl, "revertUrl can not be null");
            this.saveUrl = saveUrl;
            this.revertUrl = revertUrl;
        },

        saveMapXml:function (mapId, mapXml, pref, saveHistory, events) {

            var data = {
                id:mapId,
                xml:mapXml,
                properties:pref
            };

            var request = new Request({
                url:this.saveUrl.replace("{id}", mapId) + "?minor=" + !saveHistory,
                method:'put',
                onSuccess:function (responseText, responseXML) {
                    events.onSuccess();

                },
                onException:function (headerName, value) {
                    events.onError();
                },
                onFailure:function (xhr) {
                    var responseText = xhr.responseText;
                    var error = null;
                    try {
                        error = JSON.decode(responseText);
                    } catch (e) {
                        throw "Unexpected error saving. Error response is not json object:" + responseText;
                    }
                    events.onError(error);
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
        }

    }
);


